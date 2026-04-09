/**
 * 管理后台状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { AdminUser, SystemStats } from '@/types/admin'
import { 
  getAllUsers, 
  disableUser, 
  enableUser, 
  deleteUser, 
  setUserRole,
  getSystemStats 
} from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  // State
  const users = ref<AdminUser[]>([])
  const stats = ref<SystemStats | null>(null)
  const isLoading = ref(false)

  // 获取用户列表
  async function fetchUsers() {
    isLoading.value = true
    try {
      users.value = await getAllUsers()
    } catch (error) {
      ElMessage.error('加载用户列表失败')
      console.error(error)
    } finally {
      isLoading.value = false
    }
  }

  // 获取统计数据
  async function fetchStats() {
    try {
      stats.value = await getSystemStats()
    } catch (error) {
      ElMessage.error('加载统计数据失败')
      console.error(error)
    }
  }

  // 切换用户状态（启用/禁用）
  async function toggleUserStatus(userId: string, currentStatus: 0 | 1) {
    try {
      if (currentStatus === 1) {
        await disableUser(userId)
        ElMessage.success('已禁用用户')
      } else {
        await enableUser(userId)
        ElMessage.success('已启用用户')
      }
      await fetchUsers()
    } catch (error) {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }

  // 修改用户角色
  async function changeUserRole(userId: string, role: 0 | 1) {
    try {
      await setUserRole(userId, role)
      ElMessage.success('角色已修改')
      await fetchUsers()
    } catch (error) {
      ElMessage.error('修改失败')
      console.error(error)
    }
  }

  // 删除用户
  async function removeUser(userId: string) {
    try {
      await deleteUser(userId)
      ElMessage.success('用户已删除')
      await fetchUsers()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }

  return {
    users,
    stats,
    isLoading,
    fetchUsers,
    fetchStats,
    toggleUserStatus,
    changeUserRole,
    removeUser
  }
})
