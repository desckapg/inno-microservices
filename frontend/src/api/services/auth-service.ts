import apiClient from '../axios-client.ts'
import {type LoginResponse} from '../../types/LoginResponse.ts'
import {type RefreshResponse} from '../../types/RefreshResponse.ts'
import axios from 'axios'

export async function login(
    credentials: { login: string; password: string },
): Promise<LoginResponse> {
  try {
    const {data} = await apiClient.post<LoginResponse>('/api/v1/auth/login', credentials)
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status

      if (status === 401) {
        throw new Error('Invalid login or password.')
      }

      throw new Error(err.response?.statusText ?? 'Request failed.')
    }

    throw err
  }
}

export async function register(
    userData: {
      name: string
      surname: string
      email: string
      birthDate: string
      login: string
      password: string
    },
): Promise<LoginResponse> {
  try {
    const {data} = await apiClient.post<LoginResponse>('/api/v1/auth/register', userData)
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status

      if (status === 400) {
        throw new Error('Invalid registration data.')
      }

      if (status === 409) {
        throw new Error('User with this login or email already exists.')
      }

      throw new Error(err.response?.statusText ?? 'Request failed.')
    }

    throw err
  }
}

export async function refreshToken(refreshToken: string): Promise<RefreshResponse> {
  try {
    const {data} = await axios.post<RefreshResponse>(
        `${import.meta.env.VITE_BASE_URL}/api/v1/auth/refresh`,
        {refreshToken}
    )
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      throw new Error(err.response?.statusText ?? 'Token refresh failed.')
    }
    throw err
  }
}
