import axios, {type AxiosInstance} from 'axios'

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 5000
})

export default apiClient