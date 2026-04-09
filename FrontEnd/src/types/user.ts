// 用户相关类型

export interface LoginPayload {
  account: string  // 邮箱或手机号
  password: string
}

export interface LoginResponse {
  id: string
  role: 0 | 1  // 0=管理员，1=普通用户
  username: string
  email: string
  phone: string
  sex: 0 | 1 | 2
  image: string  // Base64 Data URI
  status: number
  flag: number
  token: string
}

export interface RegisterPayload {
  username?: string
  password: string
  email?: string
  phone?: string
}

export interface RegisterResponse {
  code: number
  message: string
}

export interface UserInfo {
  id: string
  role: 0 | 1
  username: string
  email: string
  phone: string
  sex: 0 | 1 | 2
  image: string
  status: number
  flag: number
}

export interface BindPayload {
  phone?: string
  email?: string
  bindType: 'PHONE' | 'EMAIL' | 'BOTH'
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
