import axios, {type AxiosInstance, type InternalAxiosRequestConfig} from 'axios'
import {tokenStore} from '../utils/token-store'

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 5000
})

let isRefreshing = false
let failedQueue: Array<{
  resolve: (value: string) => void
  reject: (error: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else if (token) {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = tokenStore.getAccessToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
)

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config

      if (error.response?.status !== 401 || originalRequest._retry) {
        throw error
      }

      if (originalRequest.url?.includes('/auth/login') ||
          originalRequest.url?.includes('/auth/register') ||
          originalRequest.url?.includes('/auth/refresh')) {
        throw error
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({resolve, reject})
        })
            .then((token) => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              return apiClient(originalRequest)
            })
            .catch((err) => {
              throw err
            })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = tokenStore.getRefreshToken()

      if (!refreshToken) {
        isRefreshing = false
        tokenStore.clearTokens()
        globalThis.location.href = '/login'
        throw error
      }

      try {
        const {data} = await axios.post(
            `${import.meta.env.VITE_BASE_URL}/api/v1/auth/refresh`,
            {refreshToken}
        )

        const {accessToken} = data
        tokenStore.setTokens(accessToken, refreshToken)

        originalRequest.headers.Authorization = `Bearer ${accessToken}`

        processQueue(null, accessToken)
        return apiClient(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        tokenStore.clearTokens()
        globalThis.location.href = '/login'
        throw refreshError
      } finally {
        isRefreshing = false
      }
    }
)

export default apiClient