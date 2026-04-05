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
            v-for="item in navItems"
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

          <el-button text class="notification-btn">
            <el-badge :value="unreadCount" class="notification-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </el-button>

          <div class="user-info">
            <div class="user-avatar">
              <img v-if="appStore.userInfo?.image" :src="appStore.userInfo.image" :alt="currentUserName" />
              <span v-else class="user-avatar__empty">?</span>
            </div>
            <span class="user-tag">{{ currentUserName }}</span>
          </div>
          <el-button class="mobile-menu" text @click="drawerVisible = true">
            <el-icon><Menu /></el-icon>
          </el-button>
          <el-button plain @click="handleLogout">退出登录</el-button>
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
      <el-button type="danger" plain class="drawer-logout" @click="handleLogout">
        退出登录
      </el-button>
    </el-drawer>
  </div>

  <router-view v-else />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight, Menu, Bell } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { getUserRole } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const drawerVisible = ref(false)
const unreadCount = ref(0) // 未读消息数，暂时为 0

const publicRouteNames = new Set(['Login', 'Register', 'NotFound'])

const navItems = [
  { label: '首页', to: '/' },
  { label: '病虫识别', to: '/detection' },
  { label: 'AI 问答', to: '/chat' },
  { label: '交流论坛', to: '/forum' },
  { label: '个人中心', to: '/user' }
]

const adminItems = [
  { label: '用户管理', to: '/admin/users' },
  { label: '内容管理', to: '/admin/content' },
  { label: '告警管理', to: '/admin/alerts' }
]

const useShellLayout = computed(() => !publicRouteNames.has(String(route.name ?? '')))
const isAdminUser = computed(() => appStore.userInfo?.role === 0 || getUserRole() === 0)
const currentUserName = computed(() => appStore.userInfo?.username || '当前用户')
const allDrawerItems = computed(() =>
  isAdminUser.value ? [...navItems, ...adminItems] : navItems
)

watch(
  () => route.fullPath,
  () => {
    drawerVisible.value = false
  }
)

function isActive(targetPath: string) {
  if (targetPath === '/') {
    return route.path === '/'
  }

  return route.path === targetPath || route.path.startsWith(`${targetPath}/`)
}

function handleLogout() {
  appStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
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

.user-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: #eef8f1;
  color: #1f7a44;
  font-size: 14px;
  font-weight: 600;
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
