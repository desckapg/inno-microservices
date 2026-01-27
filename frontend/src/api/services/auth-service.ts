import apiClient from '../axios-client.ts'
import {type LoginResponse} from '../../types/LoginResponse.ts'
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
