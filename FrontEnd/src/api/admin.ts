/**
 * 管理后台 API 接口
 */
import request from './request'
import type { AdminUser, SystemStats, PendingContent, AlertInfo } from '@/types/admin'
import {
  createWarning,
  deleteWarning,
  getWarningList,
  toggleWarningStatus,
  updateWarning
} from './warning'

// ===== 用户管理相关 =====

/**
 * 获取所有用户列表
 */
export function getAllUsers(): Promise<AdminUser[]> {
  return request.get('/admin/allusers')
}

/**
 * 禁用用户
 */
export function disableUser(userId: string): Promise<{ code: number; message: string }> {
  return request.patch(`/admin/users/${userId}/disable`)
}

/**
 * 启用用户
 */
export function enableUser(userId: string): Promise<{ code: number; message: string }> {
  return request.patch(`/admin/users/${userId}/enable`)
}

/**
 * 删除用户
 */
export function deleteUser(userId: string): Promise<{ code: number; message: string }> {
  return request.delete(`/admin/users/${userId}`)
}

/**
 * 修改用户角色
 */
export function setUserRole(userId: string, role: 0 | 1): Promise<{ code: number; message: string }> {
  return request.patch(`/admin/users/${userId}/role`, { role })
}

// ===== 系统统计相关 =====

/**
 * 获取系统统计数据
 */
export function getSystemStats(): Promise<SystemStats> {
  return request.get('/admin/stats')
}

// ===== 内容审核相关 =====

/**
 * 获取待审核内容
 */
export function getPendingContents(type?: 'post' | 'comment'): Promise<PendingContent[]> {
  const params = type ? { type } : {}
  return request.get('/admin/contents', { params })
}

/**
 * 通过审核
 */
export function approveContent(contentId: string, type: 'post' | 'comment'): Promise<{ code: number; message: string }> {
  return request.patch(`/admin/contents/${contentId}/approve`, { type })
}

/**
 * 拒绝审核
 */
export function rejectContent(contentId: string, type: 'post' | 'comment', reason?: string): Promise<{ code: number; message: string }> {
  return request.patch(`/admin/contents/${contentId}/reject`, { type, reason })
}

// ===== 预警管理相关 =====

/**
 * 获取预警列表
 */
export function getAlerts(): Promise<AlertInfo[]> {
  return getWarningList({ page: 1, pageSize: 100 }).then((res) => res.list as AlertInfo[])
}

/**
 * 创建预警
 */
export function createAlert(payload: Omit<AlertInfo, 'id' | 'createdTime' | 'updatedTime'>): Promise<AlertInfo> {
  return createWarning({
    title: payload.title,
    content: payload.content,
    region: payload.region,
    pestName: payload.pestName,
    severity: payload.severity,
    status: payload.status
  }) as Promise<AlertInfo>
}

/**
 * 更新预警
 */
export function updateAlert(alertId: string, payload: Partial<AlertInfo>): Promise<AlertInfo> {
  return updateWarning(alertId, {
    title: payload.title,
    content: payload.content,
    region: payload.region,
    pestName: payload.pestName,
    severity: payload.severity,
    status: payload.status
  }) as Promise<AlertInfo>
}

/**
 * 删除预警
 */
export function deleteAlert(alertId: string): Promise<string> {
  return deleteWarning(alertId)
}

/**
 * 切换预警状态
 */
export function toggleAlertStatus(alertId: string): Promise<AlertInfo> {
  return toggleWarningStatus(alertId) as Promise<AlertInfo>
}

// ===== 认证相关 =====

/**
 * 管理员退出登录
 */
export function logout(): Promise<{ code: number; message: string }> {
  return request.post('/admin/logout')
}
