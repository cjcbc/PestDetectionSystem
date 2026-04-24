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
        </div>
      </header>

      <!-- 内容区 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

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
  color: var(--color-primary);
}
</style>
