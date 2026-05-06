/**
 * @deprecated 此 store 随 AuthModal 组件一同弃用。
 * 登录/注册请使用 /login 路由页面（views/Login.vue）。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthModalStore = defineStore('authModal', () => {
  const visible = ref(false)

  function open() {
    visible.value = true
  }

  function close() {
    visible.value = false
  }

  function toggle() {
    visible.value = !visible.value
  }

  return {
    visible,
    open,
    close,
    toggle
  }
})
