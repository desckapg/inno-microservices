import axios, {type AxiosInstance, type InternalAxiosRequestConfig} from 'axios'
import {userManager} from '../utils/auth-config.ts'

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 5000
})

apiClient.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {
      const user = await userManager.getUser();
      const token = user?.access_token;

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

      // If not a 401 error or already retried, just throw the error
      if (error.response?.status !== 401 || originalRequest._retry) {
        throw error
      }

      // Mark request as retried to prevent infinite loops
      originalRequest._retry = true

      try {
          // Try to silently renew the token
          const user = await userManager.signinSilent();
          if (user?.access_token) {
              originalRequest.headers.Authorization = `Bearer ${user.access_token}`;
              return apiClient(originalRequest);
          }
      } catch (silentError) {
          // Silent renew failed - clear user and throw error
          // The component will handle redirecting to login via ProtectedRoute
          console.error('Silent token renewal failed:', silentError);
          await userManager.removeUser();
      }

      // Throw the original error - let the component handle it
      throw error;
    }
)

export default apiClient

