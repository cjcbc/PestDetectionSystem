<template>
  <div v-if="useShellLayout" class="app-shell">
    <header class="topbar">
      <div class="topbar__inner">
        <router-link to="/" class="brand">
          <span class="brand__badge">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
              <path d="M20 4C14 4 8 8 8 16c0 8 8 16 12 20 4-4 12-12 12-20C32 8 26 4 20 4z" fill="rgba(255,255,255,0.25)"/>
              <path d="M20 10c-2 0-6 2-6 7 0 4 4 9 6 11 2-2 6-7 6-11 0-5-4-7-6-7z" fill="#fff"/>
              <path d="M17 18c2-3 5-4 7-3" stroke="rgba(26,122,58,0.7)" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="26" cy="14" r="2" fill="rgba(255,255,255,0.7)"/>
            </svg>
          </span>
          <div class="brand__text">
            <strong>病虫害识别系统</strong>
            <span>顶部导航栏目入口</span>
          </div>
        </router-link>

        <nav class="topbar__nav" aria-label="主导航">
          <router-link
            v-for="item in displayNavItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :class="{ 'nav-link--active': isActive(item.to) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="topbar__actions">
          <el-dropdown v-if="isAdminUser" trigger="click">
            <el-button text class="admin-entry">
              管理后台
              <el-icon class="admin-entry__icon"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in adminItems"
                  :key="item.to"
                  @click="router.push(item.to)"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button text class="notification-btn" @click="openWarningDrawer">
            <el-badge :value="unreadCount" class="notification-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </el-button>

          <!-- 未登录状态 -->
          <router-link v-if="!userIsLoggedIn" :to="{ name: 'Login' }" class="login-link">
            登录 / 注册
          </router-link>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 头像下拉菜单 -->
            <el-dropdown trigger="click" @command="handleDropdownCommand">
              <div class="user-avatar-dropdown">
                <div class="user-avatar">
                  <img v-if="appStore.userInfo?.image" :src="appStore.userInfo.image" :alt="currentUserName" />
                  <span v-else class="user-avatar__empty">
                    <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <circle cx="40" cy="40" r="40" fill="url(#default-avatar-header)"/>
                      <circle cx="40" cy="30" r="12" fill="#fff" opacity="0.9"/>
                      <ellipse cx="40" cy="62" rx="20" ry="14" fill="#fff" opacity="0.9"/>
                      <defs><linearGradient id="default-avatar-header" x1="0" y1="0" x2="80" y2="80"><stop stop-color="#67c23a"/><stop offset="1" stop-color="#409eff"/></linearGradient></defs>
                    </svg>
                  </span>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <div class="dropdown-user-info">
                    <span class="dropdown-username">{{ currentUserName }}</span>
                  </div>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-button class="mobile-menu" text @click="drawerVisible = true">
              <el-icon><Menu /></el-icon>
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="shell-main">
      <router-view />
    </main>

    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="280px"
      class="nav-drawer"
      title="栏目导航"
    >
      <div class="drawer-links">
        <router-link
          v-for="item in allDrawerItems"
          :key="item.to"
          :to="item.to"
          class="drawer-link"
          @click="drawerVisible = false"
        >
          <span>{{ item.label }}</span>
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <el-button v-if="userIsLoggedIn" type="danger" plain class="drawer-logout" @click="handleLogout">
        退出登录
      </el-button>
    </el-drawer>

    <el-drawer
      v-model="warningDrawerVisible"
      direction="rtl"
      size="380px"
      class="warning-drawer"
      title="预警通知"
    >
      <div v-if="warningLoading" class="warning-loading">加载中...</div>
      <div v-else-if="warningList.length === 0" class="warning-empty">暂无已发布预警</div>
      <div v-else class="warning-list">
        <article
          v-for="item in warningList"
          :key="item.id"
          class="warning-item"
          :class="{ 'warning-item--read': item.isRead }"
        >
          <div class="warning-item__head">
            <h4>{{ item.title }}</h4>
            <span class="warning-item__time">{{ formatWarningTime(item.publishTime) }}</span>
          </div>
          <p class="warning-item__content">{{ item.content }}</p>
          <div class="warning-item__foot">
            <span class="warning-item__severity" :class="warningSeverityClass(item.severity)">等级 {{ warningSeverityText(item.severity) }}</span>
            <el-button
              v-if="!item.isRead"
              size="small"
              type="primary"
              text
              @click="markAsRead(item)"
            >
              标记已读
            </el-button>
            <span v-else class="warning-item__read">已读</span>
          </div>
        </article>
      </div>
    </el-drawer>
  </div>

  <router-view v-else />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { getUserRole } from '@/utils/auth'
import { logout as userLogout } from '@/api/user'
import { logout as adminLogout } from '@/api/admin'
import { getUnreadWarningCount, getWarningList, markWarningRead } from '@/api/warning'
import type { WarningItem } from '@/types/warning'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const drawerVisible = ref(false)
const warningDrawerVisible = ref(false)
const warningLoading = ref(false)
const warningList = ref<WarningItem[]>([])
const unreadCount = ref(0)
let warningTimer: ReturnType<typeof setInterval> | null = null

const publicRouteNames = new Set(['Login', 'Register', 'NotFound'])

const navItems = [
  { label: '首页', to: '/' },
  { label: '病虫识别', to: '/detection' },
  { label: 'AI 问答', to: '/chat' },
  { label: '交流论坛', to: '/forum' },
  { label: '个人中心', to: '/user' }
]

const adminNavItems = [
  { label: '首页', to: '/' },
  { label: '交流论坛', to: '/forum' },
  { label: '个人中心', to: '/user' }
]

const adminItems = [
  { label: '用户管理', to: '/admin/users' },
  { label: '内容管理', to: '/admin/content' },
  { label: '告警管理', to: '/admin/alerts' }
]

const useShellLayout = computed(() => !publicRouteNames.has(String(route.name ?? '')))
const isAdminUser = computed(() => appStore.isAdmin)  // 使用 store 的响应式状态
const displayNavItems = computed(() => isAdminUser.value ? adminNavItems : navItems)
const currentUserName = computed(() => appStore.userInfo?.username || '当前用户')
const userIsLoggedIn = computed(() => appStore.isLoggedIn)  // 使用 store 的响应式状态，无需刷新
const allDrawerItems = computed(() =>
  isAdminUser.value ? [...adminNavItems, ...adminItems] : navItems
)

watch(
  () => route.fullPath,
  () => {
    drawerVisible.value = false
  }
)

watch(
  () => userIsLoggedIn.value,
  (loggedIn) => {
    if (loggedIn) {
      refreshWarningSummary()
      startWarningPolling()
    } else {
      unreadCount.value = 0
      warningList.value = []
      stopWarningPolling()
    }
  },
  { immediate: true }
)

// 监听待显示消息，在页面转跳后显示
watch(
  () => appStore.pendingMessage,
  (message) => {
    if (message) {
      setTimeout(() => {
        ElMessage({
          type: message.type,
          message: message.message,
          duration: 3000,
          offset: 20
        })
        appStore.clearPendingMessage()
      }, 100)
    }
  }
)

function isActive(targetPath: string) {
  if (targetPath === '/') {
    return route.path === '/'
  }

  return route.path === targetPath || route.path.startsWith(`${targetPath}/`)
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async () => {
      try {
        // 根据用户角色调用不同的logout接口
        if (isAdminUser.value) {
          await adminLogout()
        } else {
          await userLogout()
        }
        appStore.logout()
        router.push('/')
        ElMessage.success('已退出登录')
        // 无需刷新，store 更新会自动触发 UI 响应
      } catch (error) {
        appStore.logout()
        ElMessage.warning('退出登录处理中出现错误，已清除本地数据')
        router.push('/')
        // 无需刷新，store 更新会自动触发 UI 响应
      }
    })
    .catch(() => {})
}

function handleDropdownCommand(command: string) {
  if (command === 'profile') {
    router.push('/user')
  } else if (command === 'logout') {
    handleLogout()
  }
}

async function refreshWarningSummary() {
  if (!userIsLoggedIn.value) {
    return
  }

  try {
    const result = await getUnreadWarningCount()
    unreadCount.value = result.unreadCount ?? 0
  } catch (error) {
    unreadCount.value = 0
  }
}

async function loadWarningList() {
  if (!userIsLoggedIn.value) {
    return
  }

  warningLoading.value = true
  try {
    const result = await getWarningList({ page: 1, pageSize: 20, status: 1 })
    warningList.value = result.list ?? []
  } finally {
    warningLoading.value = false
  }
}

async function openWarningDrawer() {
  if (!userIsLoggedIn.value) {
    ElMessage.warning('请先登录后查看预警通知')
    router.push({ name: 'Login' })
    return
  }

  warningDrawerVisible.value = true
  await Promise.all([loadWarningList(), refreshWarningSummary()])
}

async function markAsRead(item: WarningItem) {
  try {
    await markWarningRead(item.id)
    item.isRead = true
    item.readTime = Date.now()
    unreadCount.value = Math.max(unreadCount.value - 1, 0)
  } catch (error) {
    ElMessage.error('标记已读失败，请稍后重试')
  }
}

function formatWarningTime(timestamp: number) {
  if (!timestamp) {
    return '-'
  }
  return new Date(timestamp).toLocaleString('zh-CN', { hour12: false })
}

function warningSeverityText(value: 1 | 2 | 3) {
  if (value === 3) return '高'
  if (value === 2) return '中'
  return '低'
}

function warningSeverityClass(value: 1 | 2 | 3) {
  if (value === 3) return 'severity-high'
  if (value === 2) return 'severity-medium'
  return 'severity-low'
}

function startWarningPolling() {
  stopWarningPolling()
  warningTimer = setInterval(() => {
    refreshWarningSummary()
  }, 30000)
}

function stopWarningPolling() {
  if (warningTimer) {
    clearInterval(warningTimer)
    warningTimer = null
  }
}

onMounted(() => {
  if (userIsLoggedIn.value) {
    refreshWarningSummary()
    startWarningPolling()
  }
})

onUnmounted(() => {
  stopWarningPolling()
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(26, 122, 58, 0.14), transparent 32%),
    linear-gradient(180deg, #f0fdf4 0%, #f4f6f1 48%, #fafaf8 100%);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid var(--color-border-brand);
  box-shadow: var(--shadow-sm);
}

.topbar__inner {
  width: min(1200px, calc(100% - 32px));
  min-height: 78px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 220px;
  color: inherit;
  text-decoration: none;
}

.brand:hover {
  text-decoration: none;
}

.brand__badge {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary) 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
  font-family: var(--font-display);
}

.brand__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand__text strong {
  font-size: 18px;
  color: var(--color-text-primary);
  font-family: var(--font-display);
}

.brand__text span {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.topbar__nav {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 8px;
}

.nav-link {
  padding: 10px 16px;
  border-radius: 999px;
  color: var(--color-text-secondary);
  font-weight: 600;
  transition:
    background-color var(--transition-fast),
    color var(--transition-fast),
    transform var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-primary);
  text-decoration: none;
  background: var(--color-bg-brand);
  transform: translateY(-1px);
}

.nav-link--active {
  color: var(--color-primary-dark);
  background: var(--color-bg-brand);
  box-shadow: inset 0 0 0 1px var(--color-border-brand);
}

.topbar__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 250px;
}

.admin-entry {
  font-weight: 600;
}

.admin-entry__icon {
  margin-left: 4px;
}

.notification-btn {
  font-size: 18px;
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
}

.login-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  border-radius: 999px;
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: background var(--transition-fast), transform var(--transition-fast);
}

.login-link:hover {
  background: var(--color-primary-dark);
  color: #fff;
  text-decoration: none;
  transform: translateY(-1px);
}

.notification-btn:hover {
  color: var(--color-primary);
}

.notification-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar-dropdown {
  cursor: pointer;
  border-radius: 50%;
  transition: transform 0.2s ease;
}

.user-avatar-dropdown:hover {
  transform: scale(1.05);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 2px solid var(--el-border-color-lighter, #e4e8ed);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar__empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar__empty svg {
  width: 100%;
  height: 100%;
}

.dropdown-user-info {
  padding: 16px 20px;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter, #e4e8ed);
  background: linear-gradient(180deg, var(--color-bg-brand) 0%, transparent 100%);
}

.dropdown-username {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #303133);
}

:deep(.el-dropdown-menu__item) {
  padding: 12px 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--color-bg-brand);
  color: var(--color-primary);
}

:deep(.el-dropdown-menu) {
  padding: 8px 0;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.mobile-menu {
  display: none;
}

.shell-main {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
  padding: 28px 0 40px;
}

.drawer-links {
  display: grid;
  gap: 10px;
}

.drawer-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-weight: 600;
}

.drawer-link:hover {
  text-decoration: none;
  background: var(--color-bg-brand);
}

.drawer-logout {
  width: 100%;
  margin-top: 24px;
}

.warning-loading,
.warning-empty {
  color: var(--color-text-secondary);
  text-align: center;
  padding: 24px 8px;
}

.warning-list {
  display: grid;
  gap: 12px;
}

.warning-item {
  border: 1px solid var(--color-border-brand);
  border-radius: var(--radius-md);
  padding: 12px;
  background: #ffffff;
}

.warning-item--read {
  opacity: 0.82;
}

.warning-item__head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}

.warning-item__head h4 {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: var(--color-text-primary);
}

.warning-item__time {
  color: var(--color-text-secondary);
  font-size: 12px;
  white-space: nowrap;
}

.warning-item__content {
  margin: 8px 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.warning-item__foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.warning-item__severity {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  font-weight: 500;
}

.warning-item__severity.severity-high {
  color: #fff;
  background: var(--color-error);
}

.warning-item__severity.severity-medium {
  color: #fff;
  background: var(--color-warning);
}

.warning-item__severity.severity-low {
  color: #333;
  background: #fbbf24;
}

.warning-item__read {
  font-size: 12px;
  color: var(--color-text-secondary);
}

@media (max-width: 900px) {
  .topbar__inner {
    width: calc(100% - 24px);
    min-height: 72px;
  }

  .topbar__nav,
  .user-info,
  .admin-entry,
  .topbar__actions .el-button:not(.mobile-menu) {
    display: none;
  }

  .topbar__actions {
    min-width: auto;
    margin-left: auto;
  }

  .mobile-menu {
    display: inline-flex;
    font-size: 20px;
  }

  .shell-main {
    width: calc(100% - 24px);
    padding-top: 20px;
  }

  .brand {
    min-width: auto;
  }
}
</style>
