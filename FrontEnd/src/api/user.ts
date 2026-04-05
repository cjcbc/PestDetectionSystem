import request from './request'
import type { LoginPayload, LoginResponse, RegisterPayload, RegisterResponse, UserInfo, BindPayload } from '@/types/user'

/**
 * 用户登录
 */
export function login(payload: LoginPayload): Promise<LoginResponse> {
  return request.post('/user/login', payload)
}

/**
 * 用户注册
 */
export function register(payload: RegisterPayload): Promise<RegisterResponse> {
  return request.post('/user/register', payload)
}

/**
 * 获取用户信息
 */
export function getUserInfo(): Promise<UserInfo> {
  return request.get('/user/info')
}

/**
 * 更新用户名
 */
export function updateUsername(newUsername: string): Promise<UserInfo> {
  return request.put('/user/username', { username: newUsername })
}

/**
 * 更新用户性别
 */
export function updateSex(sex: 0 | 1 | 2): Promise<UserInfo> {
  return request.put('/user/sex', { sex })
}

/**
 * 上传用户头像
 */
export function updateAvatar(file: File): Promise<{ code: number; message: string }> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 绑定手机号或邮箱
 */
export function bindPhoneEmail(payload: BindPayload): Promise<{ code: number; message: string }> {
  return request.post('/user/bind', payload)
}

/**
 * 用户登出
 */
export function logout(): Promise<{ code: number; message: string }> {
  return request.post('/user/logout')
}
