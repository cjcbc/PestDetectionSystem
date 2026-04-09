<template>
  <div class="admin-dashboard-container">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-label">总用户数</div>
        <div class="stat-value">{{ adminStore.stats?.totalUsers || 0 }}</div>
        <div class="stat-icon">
          <i class="el-icon-user"></i>
        </div>
      </div>

      <div class="stat-card success">
        <div class="stat-label">活跃用户</div>
        <div class="stat-value">{{ adminStore.stats?.activeUsers || 0 }}</div>
        <div class="stat-icon">
          <i class="el-icon-user-solid"></i>
        </div>
      </div>

      <div class="stat-card warning">
        <div class="stat-label">总帖子数</div>
        <div class="stat-value">{{ adminStore.stats?.totalPosts || 0 }}</div>
        <div class="stat-icon">
          <i class="el-icon-document"></i>
        </div>
      </div>

      <div class="stat-card danger">
        <div class="stat-label">待审核内容</div>
        <div class="stat-value">{{ adminStore.stats?.pendingReview || 0 }}</div>
        <div class="stat-icon">
          <i class="el-icon-warning"></i>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="admin-card" style="margin-top: 20px">
      <div class="card-header">
        <h3>快捷操作</h3>
      </div>
      <div class="card-body">
        <div class="quick-actions">
          <el-button type="primary" @click="goToUsers">
            <i class="el-icon-user"></i>
            用户管理
          </el-button>
          <el-button type="info" @click="goToContent">
            <i class="el-icon-document"></i>
            内容审核
          </el-button>
          <el-button type="warning" @click="goToAlerts">
            <i class="el-icon-warning"></i>
            预警管理
          </el-button>
          <el-button @click="handleRefresh">
            <i class="el-icon-refresh"></i>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统交汇概览 -->
    <div class="admin-card" style="margin-top: 20px">
      <div class="card-header">
        <h3>系统概览</h3>
      </div>
      <div class="card-body">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="8">
            <div class="overview-item">
              <span class="label">管理员数量</span>
              <span class="value">{{ adminAdminCount }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="overview-item">
              <span class="label">普通用户数量</span>
              <span class="value">{{ adminUserCount }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="overview-item">
              <span class="label">禁用用户数量</span>
              <span class="value">{{ disabledUserCount }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

// 计算派生数据
const adminAdminCount = computed(() => {
  return adminStore.users.filter(u => u.role === 0).length
})

const adminUserCount = computed(() => {
  return adminStore.users.filter(u => u.role === 1).length
})

const disabledUserCount = computed(() => {
  return adminStore.users.filter(u => u.status === 0).length
})

// 初始化
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  await adminStore.fetchStats()
  await adminStore.fetchUsers()
}

// 快捷导航
const goToUsers = () => {
  router.push('/admin/users')
}

const goToContent = () => {
  router.push('/admin/content')
}

const goToAlerts = () => {
  router.push('/admin/alerts')
}

// 刷新数据
const handleRefresh = async () => {
  await loadData()
  ElMessage.success('已刷新')
}
</script>

<style scoped>
.admin-dashboard-container {
  width: 100%;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;

  .stat-label {
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
  }

  .stat-icon {
    position: absolute;
    top: 20px;
    right: 20px;
    font-size: 40px;
    opacity: 0.08;
  }

  &.primary {
    background-color: #f0f9ff;
    border-left: 4px solid #409eff;

    .stat-value {
      color: #409eff;
    }

    .stat-icon {
      color: #409eff;
    }
  }

  &.success {
    background-color: #f0f9ff;
    border-left: 4px solid #67c23a;

    .stat-value {
      color: #67c23a;
    }

    .stat-icon {
      color: #67c23a;
    }
  }

  &.warning {
    background-color: #fdf6ec;
    border-left: 4px solid #e6a23c;

    .stat-value {
      color: #e6a23c;
    }

    .stat-icon {
      color: #e6a23c;
    }
  }

  &.danger {
    background-color: #fef0f0;
    border-left: 4px solid #f56c6c;

    .stat-value {
      color: #f56c6c;
    }

    .stat-icon {
      color: #f56c6c;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-actions :deep(.el-button) {
  flex: 1;
  min-width: 120px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  text-align: center;

  .label {
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
    display: block;
  }

  .value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }

  .stat-card {
    padding: 16px;

    .stat-value {
      font-size: 24px;
    }

    .stat-icon {
      font-size: 32px;
    }
  }

  .quick-actions {
    flex-direction: column;
  }

  .quick-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
