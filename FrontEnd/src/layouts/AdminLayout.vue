<template>
  <div class="admin-container">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <i class="el-icon-management"></i>
        <span>管理后台</span>
      </div>
      <ul class="sidebar-menu">
        <li class="menu-item">
          <router-link to="/admin/dashboard">
            <i class="el-icon-data-analysis"></i>
            <span>仪表板</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/admin/users">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/admin/content">
            <i class="el-icon-document"></i>
            <span>内容审核</span>
          </router-link>
        </li>
        <li class="menu-item">
          <router-link to="/admin/alerts">
            <i class="el-icon-warning"></i>
            <span>预警管理</span>
          </router-link>
        </li>
      </ul>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <div class="breadcrumb">
            <span class="breadcrumb-item" v-for="(item, index) in breadcrumbs" :key="index">
              <span v-if="index > 0" class="divider">/</span>
              <span class="breadcrumb-item" :class="{ active: index === breadcrumbs.length - 1 }">
                {{ item }}
              </span>
            </span>
          </div>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleUserMenuClick">
            <div class="user-menu">
              <div class="avatar">
                <i class="el-icon-user"></i>
              </div>
              <span class="username">{{ currentUsername }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <ChangePasswordModal 
      v-model="showChangePasswordModal"
      is-admin
      @success="handlePasswordChangeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { logout } from '@/api/admin'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()

// 修改密码对话框
const showChangePasswordModal = ref(false)

// 当前用户名
const currentUsername = computed(() => {
  return appStore.userInfo?.username || '管理员'
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const path = route.path
  const items = ['管理后台']
  
  if (path.includes('/dashboard')) {
    items.push('仪表板')
  } else if (path.includes('/users')) {
    items.push('用户管理')
  } else if (path.includes('/content')) {
    items.push('内容审核')
  } else if (path.includes('/alerts')) {
    items.push('预警管理')
  }

  return items
})

// 用户菜单点击处理
const handleUserMenuClick = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user')
      break
    case 'password':
      showChangePasswordModal.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理密码修改成功
const handlePasswordChangeSuccess = () => {
  ElMessage.success('密码修改成功，请重新登录')
  // 可选：清除登录状态并跳转到登录页面
  // appStore.logout()
  // router.push('/login')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await logout()
    appStore.logout()
    ElMessage.success('已退出登录')
    await router.push('/login')
  } catch {
    // 取消退出或错误
  }
}
</script>

<style scoped>
@import '@/styles/admin.css';

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 10px;
  user-select: none;
}

.user-menu:hover {
  color: #409eff;
}
</style>
