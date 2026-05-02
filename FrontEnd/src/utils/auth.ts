// Token 和认证管理
import type { UserInfo } from '@/types/user'

const TOKEN_KEY = 'token'
const ROLE_KEY = 'userRole'
const USER_INFO_KEY = 'userInfo'
const REMEMBERED_LOGIN_KEY = 'rememberedLogin'

export interface RememberedLogin {
  account: string
  password: string
}

/**
 * 设置 token
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 获取 token
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 清除 token
 */
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 判断是否已登录
 */
export function isLoggedIn(): boolean {
  return !!localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置用户角色
 */
export function setUserRole(role: 0 | 1): void {
  localStorage.setItem(ROLE_KEY, String(role))
}

/**
 * 获取用户角色
 */
export function getUserRole(): 0 | 1 | null {
  const role = localStorage.getItem(ROLE_KEY)
  if (role === null) return null
  return parseInt(role) as 0 | 1
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo: UserInfo): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 获取用户信息
 */
export function getUserInfo(): UserInfo | null {
  const info = localStorage.getItem(USER_INFO_KEY)
  if (!info) return null
  try {
    return JSON.parse(info) as UserInfo
  } catch {
    return null
  }
}

/**
 * 清除用户信息
 */
export function clearUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 判断是否是管理员
 */
export function isAdmin(): boolean {
  return getUserRole() === 0
}

export function setRememberedLogin(login: RememberedLogin): void {
  localStorage.setItem(REMEMBERED_LOGIN_KEY, JSON.stringify(login))
}

export function getRememberedLogin(): RememberedLogin | null {
  const info = localStorage.getItem(REMEMBERED_LOGIN_KEY)
  if (!info) return null
  try {
    return JSON.parse(info) as RememberedLogin
  } catch {
    localStorage.removeItem(REMEMBERED_LOGIN_KEY)
    return null
  }
}

export function clearRememberedLogin(): void {
  localStorage.removeItem(REMEMBERED_LOGIN_KEY)
}
