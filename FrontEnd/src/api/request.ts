import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { showError, showWarning } from '@/utils/message'
import router from '@/router'

let isHandling401 = false

export interface MessageHandledError extends Error {
  __messageHandled?: boolean
}

export function markMessageHandled<T extends Error>(error: T): T & MessageHandledError {
  return Object.assign(error, { __messageHandled: true })
}

export function isMessageHandled(error: unknown): boolean {
  return Boolean(error && typeof error === 'object' && '__messageHandled' in error && (error as MessageHandledError).__messageHandled)
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api'

// 调试：打印实际的 baseURL
console.log('[API] 当前 baseURL:', API_BASE_URL)

export function handleAuthExpired() {
  if (isHandling401) return

  isHandling401 = true
  const appStore = useAppStore()
  const redirect = router.currentRoute.value.fullPath

  appStore.logout()
  showWarning('登录已过期，请重新登录')
  ElMessageBox.confirm('是否回到登录页面？', '登录状态已过期', {
    confirmButtonText: '去登录',
    cancelButtonText: '暂不登录',
    type: 'warning'
  })
    .then(() => {
      return router.push({
        name: 'Login',
        query: redirect && redirect !== '/login' ? { redirect } : undefined
      })
    })
    .catch(() => {})
    .finally(() => {
      isHandling401 = false
    })
}

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
      showError(message || '请求失败')
      return Promise.reject(markMessageHandled(new Error(message || '请求失败')))
    }
  },
  (error) => {
    console.error('响应拦截器错误:', error)

    if (error.response) {
      const { status, data } = error.response
      const message = typeof data === 'string' ? data : data?.message || ''

      switch (status) {
        case 401:
          handleAuthExpired()
          markMessageHandled(error)
          break
        case 403:
          showError(message || '权限不足，拒绝访问')
          markMessageHandled(error)
          break
        case 404:
          showError(message || '请求地址不存在')
          markMessageHandled(error)
          break
        case 500:
          showError(message || '服务器错误，请稍后重试')
          markMessageHandled(error)
          break
        default:
          showError(message || '请求失败')
          markMessageHandled(error)
      }
    } else if (error.code === 'ECONNABORTED') {
      showError('请求超时，请检查网络连接')
      markMessageHandled(error)
    } else {
      showError('网络错误，请检查网络连接')
      markMessageHandled(error)
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
