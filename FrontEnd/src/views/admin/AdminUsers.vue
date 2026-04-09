<template>
  <div class="admin-users-container">
    <!-- 搜索和操作栏 -->
    <div class="admin-card">
      <div class="card-body">
        <div class="toolbar">
          <div class="search-group">
            <el-input
              v-model="searchText"
              placeholder="搜索用户名、邮箱、手机号..."
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <i class="el-icon-search"></i>
              </template>
            </el-input>
          </div>

          <div class="filter-group">
            <el-select
              v-model="filterStatus"
              placeholder="按状态筛选"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="启用" :value="1"></el-option>
              <el-option label="禁用" :value="0"></el-option>
            </el-select>

            <el-select
              v-model="filterRole"
              placeholder="按角色筛选"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="管理员" :value="0"></el-option>
              <el-option label="普通用户" :value="1"></el-option>
            </el-select>
          </div>

          <div class="action-group">
            <el-button type="primary" @click="handleRefresh">
              <i class="el-icon-refresh"></i>
              刷新
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="admin-card">
      <div class="card-header">
        <h3>用户列表</h3>
        <div class="header-action">
          <span class="user-count">共 {{ filteredUsers.length }} 人</span>
        </div>
      </div>

      <div class="card-body admin-table">
        <el-table
          :data="filteredUsers"
          stripe
          border
          :default-sort="{ prop: 'createdTime', order: 'descending' }"
          v-loading="adminStore.isLoading"
        >
          <el-table-column
            prop="id"
            label="用户ID"
            width="150"
            show-overflow-tooltip
          />

          <el-table-column
            prop="username"
            label="用户名"
            width="120"
          />

          <el-table-column
            prop="email"
            label="邮箱"
            min-width="180"
            show-overflow-tooltip
          />

          <el-table-column
            prop="phone"
            label="手机"
            width="130"
          />

          <el-table-column
            prop="role"
            label="角色"
            width="100"
            align="center"
          >
            <template #default="{ row }">
              <el-select
                :model-value="row.role"
                @change="(val) => handleChangeRole(row, val)"
                size="small"
              >
                <el-option label="管理员" :value="0"></el-option>
                <el-option label="普通用户" :value="1"></el-option>
              </el-select>
            </template>
          </el-table-column>

          <el-table-column
            prop="status"
            label="状态"
            width="100"
            align="center"
          >
            <template #default="{ row }">
              <el-tag
                :type="row.status === 1 ? 'success' : 'danger'"
                class="status-badge"
              >
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column
            prop="createdTime"
            label="注册时间"
            width="180"
            :formatter="(row) => formatTime(row.createdTime)"
            sortable
          />

          <el-table-column
            label="操作"
            width="200"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>

              <el-divider direction="vertical" />

              <el-popconfirm
                title="确定要删除此用户吗？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button link type="danger" size="small">
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>

          <template #empty>
            <div class="empty-state">
              <i class="el-icon-warning"></i>
              <p>没有找到用户</p>
            </div>
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import type { AdminUser } from '@/types/admin'

const adminStore = useAdminStore()

// 搜索和筛选
const searchText = ref('')
const filterStatus = ref<0 | 1 | null>(null)
const filterRole = ref<0 | 1 | null>(null)

// 计算过滤后的用户列表
const filteredUsers = computed(() => {
  return adminStore.users.filter(user => {
    // 搜索过滤
    if (searchText.value) {
      const keyword = searchText.value.toLowerCase()
      const matches = 
        user.username.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword) ||
        (user.phone && user.phone.includes(keyword))
      if (!matches) return false
    }

    // 状态过滤
    if (filterStatus.value !== null && user.status !== filterStatus.value) {
      return false
    }

    // 角色过滤
    if (filterRole.value !== null && user.role !== filterRole.value) {
      return false
    }

    return true
  })
})

// 初始化
onMounted(() => {
  loadUsers()
})

// 加载用户列表
const loadUsers = async () => {
  await adminStore.fetchUsers()
}

// 搜索处理
const handleSearch = () => {
  // 实时搜索
}

// 筛选变化
const handleFilterChange = () => {
  // 筛选器已处理，由computed属性根据值进行过滤
}

// 刷新
const handleRefresh = async () => {
  await loadUsers()
  ElMessage.success('已刷新')
}

// 切换用户状态
const handleToggleStatus = async (user: AdminUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要${user.status === 1 ? '禁用' : '启用'}此用户吗？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminStore.toggleUserStatus(user.id, user.status)
  } catch {
    // 取消操作
  }
}

// 修改用户角色
const handleChangeRole = async (user: AdminUser, newRole: 0 | 1) => {
  try {
    await ElMessageBox.confirm(
      `确定要将此用户角色改为${newRole === 0 ? '管理员' : '普通用户'}吗？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminStore.changeUserRole(user.id, newRole)
  } catch {
    // 取消操作
  }
}

// 删除用户
const handleDelete = async (user: AdminUser) => {
  try {
    await adminStore.removeUser(user.id)
  } catch {
    // 错误已在store中处理
  }
}

// 时间格式化
const formatTime = (timestamp: number) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.admin-users-container {
  width: 100%;
}

.toolbar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-group {
  flex: 1;
  min-width: 250px;
}

.search-group :deep(.el-input) {
  width: 100%;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-group :deep(.el-select) {
  width: 120px;
}

.action-group {
  display: flex;
  gap: 12px;
}

.user-count {
  font-size: 14px;
  color: #606266;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;

  i {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
  }

  .search-group {
    width: 100%;
  }

  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-group :deep(.el-select) {
    width: 100%;
  }

  .action-group {
    width: 100%;
  }

  .action-group :deep(.el-button) {
    width: 100%;
  }
}
</style>
