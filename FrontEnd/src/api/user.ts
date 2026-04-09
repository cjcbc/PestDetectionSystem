import request from './request'
import type { LoginPayload, LoginResponse, RegisterPayload, RegisterResponse, UserInfo, BindPayload, ChangePasswordPayload } from '@/types/user'

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
 * 获取管理员信息
 */
export function getAdminInfo(): Promise<UserInfo> {
  return request.get('/admin/info')
}

/**
 * 更新用户名
 */
export function updateUsername(username: string): Promise<UserInfo> {
  return request.patch('/user/username', null, {
    params: { username }
  })
}

/**
 * 更新用户性别
 */
export function updateSex(sex: 0 | 1 | 2): Promise<UserInfo> {
  return request.patch('/user/sex', null, {
    params: { sex }
  })
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
 * 修改用户密码
 */
export function changeUserPassword(payload: ChangePasswordPayload): Promise<{ code: number; message: string }> {
  return request.patch('/user/password', payload)
}

/**
 * 修改管理员密码
 */
export function changeAdminPassword(payload: ChangePasswordPayload): Promise<{ code: number; message: string }> {
  return request.patch('/admin/password', payload)
}

/**
 * 绑定手机号或邮箱
 */
export function bindPhoneEmail(payload: BindPayload): Promise<{ code: number; message: string }> {
  return request.patch('/user/bind', payload)
}

/**
 * 用户登出
 */
export function logout(): Promise<{ code: number; message: string }> {
  return request.post('/user/logout')
}
