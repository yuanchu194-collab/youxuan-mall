import axios, { AxiosError, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000'

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('youxuan_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as Result<unknown>
    if (typeof body?.code === 'number' && body.code !== 200) {
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return (body?.data ?? body) as unknown as AxiosResponse
  },
  (error: AxiosError<{ message?: string }>) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
