// API 通用类型

export interface Result<T = any> {
  code: number
  message: string
  data?: T
}

export interface ApiError {
  code: number
  message: string
  status?: number
}
