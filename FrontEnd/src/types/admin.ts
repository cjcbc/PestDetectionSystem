/**
 * 管理后台类型定义
 */

/**
 * 管理员用户信息
 */
export interface AdminUser {
  id: string
  username: string
  email: string
  phone: string
  sex: 0 | 1 | 2
  role: 0 | 1           // 0=管理员, 1=普通用户
  status: 0 | 1         // 0=禁用, 1=启用
  createdTime: number
  image?: string
}

/**
 * 系统统计信息
 */
export interface SystemStats {
  totalUsers: number
  activeUsers: number
  totalPosts: number
  pendingReview: number
  activeAlerts: number
}

/**
 * 待审核内容
 */
export interface PendingContent {
  id: string
  type: 'post' | 'comment'
  author: string
  title?: string
  content: string
  status: 'pending' | 'approved' | 'rejected'
  submittedTime: number
}

/**
 * 预警信息
 */
export interface AlertInfo {
  id: string
  title: string
  content: string
  severity: 'low' | 'medium' | 'high'
  province?: string
  isActive: boolean
  createdTime: number
  updatedTime: number
}
