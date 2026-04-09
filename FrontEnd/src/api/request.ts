import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { clearToken } from '@/utils/auth'
import { useAuthModalStore } from '@/stores/auth-modal'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api'

// 调试：打印实际的 baseURL
console.log('[API] 当前 baseURL:', API_BASE_URL)

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data

    if (code === 200 || code === 0) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    console.error('响应拦截器错误:', error)

    if (error.response) {
      const { status, data } = error.response
      const message = typeof data === 'string' ? data : data?.message || ''

      switch (status) {
        case 401:
          clearToken()
          const authModalStore = useAuthModalStore()
          authModalStore.open()
          ElMessage.error(message || '未授权，请登录')
          break
        case 403:
          ElMessage.error(message || '权限不足，拒绝访问')
          break
        case 404:
          ElMessage.error(message || '请求地址不存在')
          break
        case 500:
          ElMessage.error(message || '服务器错误，请稍后重试')
          break
        default:
          ElMessage.error(message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.patch(url, data, config)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}

export { service }
export default request
