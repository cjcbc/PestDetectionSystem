<template>
  <div v-if="useShellLayout" class="app-shell">
    <header class="topbar">
      <div class="topbar__inner">
        <router-link to="/" class="brand">
          <span class="brand__badge">PDS</span>
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
          <el-button v-if="!userIsLoggedIn" type="primary" @click="openAuthModal">
            登 录
          </el-button>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 头像下拉菜单 -->
            <el-dropdown trigger="click" @command="handleDropdownCommand">
              <div class="user-avatar-dropdown">
                <div class="user-avatar">
                  <img v-if="appStore.userInfo?.image" :src="appStore.userInfo.image" :alt="currentUserName" />
                  <span v-else class="user-avatar__empty">?</span>
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

    <!-- 登录/注册弹窗 -->
    <AuthModal v-model="authModalStore.visible" @success="handleAuthSuccess" />
  </div>

  <router-view v-else />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useAuthModalStore } from '@/stores/auth-modal'
import { getUserRole } from '@/utils/auth'
import { logout as userLogout } from '@/api/user'
import { logout as adminLogout } from '@/api/admin'
import { getUnreadWarningCount, getWarningList, markWarningRead } from '@/api/warning'
import type { WarningItem } from '@/types/warning'
import AuthModal from '@/components/AuthModal.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authModalStore = useAuthModalStore()
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

function openAuthModal() {
  authModalStore.open()
}

function handleAuthSuccess() {
  refreshWarningSummary()
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
    authModalStore.open()
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
    radial-gradient(circle at top left, rgba(46, 204, 113, 0.18), transparent 32%),
    linear-gradient(180deg, #f3fbf6 0%, #f7f8f3 48%, #fcfcfa 100%);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(39, 174, 96, 0.12);
  box-shadow: 0 10px 30px rgba(31, 41, 55, 0.05);
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
  border-radius: 14px;
  background: linear-gradient(135deg, #1f7a44 0%, #39b56b 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.brand__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand__text strong {
  font-size: 18px;
  color: var(--color-text-primary);
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
  color: #1f7a44;
  text-decoration: none;
  background: rgba(46, 204, 113, 0.1);
  transform: translateY(-1px);
}

.nav-link--active {
  color: #14532d;
  background: rgba(46, 204, 113, 0.16);
  box-shadow: inset 0 0 0 1px rgba(39, 174, 96, 0.12);
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

.notification-btn:hover {
  color: #1f7a44;
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
  font-size: 18px;
  color: #999;
  font-weight: 600;
}

.dropdown-user-info {
  padding: 16px 20px;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter, #e4e8ed);
  background: linear-gradient(180deg, rgba(46, 204, 113, 0.06) 0%, transparent 100%);
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
  background: rgba(46, 204, 113, 0.1);
  color: #1f7a44;
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
  background: #e7f7ed;
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
  border: 1px solid rgba(39, 174, 96, 0.18);
  border-radius: 12px;
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
  background: #f56c6c;
}

.warning-item__severity.severity-medium {
  color: #fff;
  background: #e6a23c;
}

.warning-item__severity.severity-low {
  color: #fff;
  background: #f7d748;
  color: #333;
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
