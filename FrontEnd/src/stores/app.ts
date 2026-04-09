import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { clearToken, getToken, setToken, setUserRole, setUserInfo as saveUserInfo, getUserInfo as getStoredUserInfo } from '@/utils/auth'
import type { UserInfo } from '@/types/user'

export const useAppStore = defineStore('app', () => {
  const userInfo = ref<UserInfo | null>(getStoredUserInfo()) // 直接从 localStorage 初始化
  const token = ref<string | null>(getToken())
  const pendingMessage = ref<{ type: 'success' | 'error' | 'warning' | 'info'; message: string } | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 0)

  function setUser(info: UserInfo, newToken: string) {
    userInfo.value = info
    token.value = newToken
    setToken(newToken)
    saveUserInfo(info)
    setUserRole(info.role)
  }

  function logout() {
    userInfo.value = null
    token.value = null
    clearToken()
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    saveUserInfo(info)
    if (info.role !== undefined) {
      setUserRole(info.role)
    }
  }

  function updateToken(newToken: string) {
    token.value = newToken
    setToken(newToken)
  }

  function setPendingMessage(type: 'success' | 'error' | 'warning' | 'info', message: string) {
    pendingMessage.value = { type, message }
  }

  function clearPendingMessage() {
    pendingMessage.value = null
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    pendingMessage,
    setUser,
    logout,
    setUserInfo,
    updateToken,
    setPendingMessage,
    clearPendingMessage
  }
})
