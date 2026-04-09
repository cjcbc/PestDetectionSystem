# 阶段六：管理后台系统

## 🎯 阶段目标
实现系统管理功能，包括用户管理、内容审核、预警发布、权限控制等管理员专属功能。

**依赖**：阶段一 | **工作量**：2-3 天 | **优先级**：P3

---

## 📋 核心需求

### 1. 管理后台 API 接口

**文件**：`src/api/admin.ts`

```typescript
// ===== 用户管理相关 =====

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  phone: string;
  role: 0 | 1;              // 0=管理员, 1=普通用户
  status: 0 | 1;            // 0=禁用, 1=启用
  createdTime: number;
}

export function getAllUsers(): Promise<AdminUser[]>

export function disableUser(userId: string): Promise<{ code: 200; message: string }>

export function enableUser(userId: string): Promise<{ code: 200; message: string }>

export function deleteUser(userId: string): Promise<{ code: 200; message: string }>

export function setUserRole(userId: string, role: 0 | 1): Promise<{ code: 200; message: string }>

// ===== 内容审核相关 =====

export interface PendingContent {
  id: string;
  type: 'post' | 'comment';
  author: string;
  title?: string;          // 仅帖子有
  content: string;
  status: 'pending' | 'approved' | 'rejected';
  submittedTime: number;
}

export function getPendingContents(type?: 'post' | 'comment'): Promise<PendingContent[]>

export function approveContent(contentId: string, type: 'post' | 'comment'): Promise<{ code: 200; message: string }>

export function rejectContent(contentId: string, type: 'post' | 'comment', reason?: string): Promise<{ code: 200; message: string }>

// ===== 预警管理相关 =====

export interface AlertInfo {
  id: string;
  title: string;
  content: string;
  severity: 'low' | 'medium' | 'high';
  province?: string;
  isActive: boolean;
  createdTime: number;
  updatedTime: number;
}

export interface CreateAlertPayload {
  title: string;
  content: string;
  severity: 'low' | 'medium' | 'high';
  province?: string;
}

export function getAlerts(): Promise<AlertInfo[]>

export function createAlert(payload: CreateAlertPayload): Promise<AlertInfo>

export function updateAlert(alertId: string, payload: Partial<CreateAlertPayload>): Promise<AlertInfo>

export function deleteAlert(alertId: string): Promise<{ code: 200; message: string }>

export function toggleAlertStatus(alertId: string): Promise<{ code: 200; message: string }>

// ===== 系统统计相关 =====

export interface SystemStats {
  totalUsers: number;
  activeUsers: number;
  totalPosts: number;
  pendingReview: number;
  activeAlerts: number;
}

export function getSystemStats(): Promise<SystemStats>
```

**后端 API 规范**（待实现）：
```
GET /api/admin/allusers
PATCH /api/admin/users/:userId/status
DELETE /api/admin/users/:userId
PATCH /api/admin/users/:userId/role

GET /api/admin/contents?type=post|comment
PATCH /api/admin/contents/:contentId/approve
PATCH /api/admin/contents/:contentId/reject

GET /api/admin/alerts
POST /api/admin/alerts
PATCH /api/admin/alerts/:alertId
DELETE /api/admin/alerts/:alertId
PATCH /api/admin/alerts/:alertId/toggle

GET /api/admin/stats
```

---

### 2. 管理后台路由和权限

**修改 router/index.ts**：

```typescript
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理面板' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('@/views/admin/AdminContent.vue'),
        meta: { title: '内容审核' }
      },
      {
        path: 'alerts',
        name: 'AdminAlerts',
        component: () => import('@/views/admin/AdminAlerts.vue'),
        meta: { title: '预警管理' }
      }
    ]
  }
]
```

---

### 3. AdminLayout 布局组件

**文件**：`src/layouts/AdminLayout.vue`

**功能需求**：

#### 左侧导航栏
- Logo / 系统名称
- 菜单项：
  - 仪表板（Dashboard）
  - 用户管理
  - 内容审核
  - 预警管理
  - 系统设置（可选）
- 当前路由高亮
- 可收起（响应式）

#### 顶部操作栏
- 欢迎信息（"欢迎，管理员名称"）
- 用户菜单：
  - 个人设置
  - 修改密码
  - 退出登录
- 通知铃（待审核数量，可选）

#### 内容区
- \`<router-view />\` 渲染子页面
- 面包屑导航（当前位置）

---

### 4. Dashboard 仪表板

**文件**：`src/views/admin/Dashboard.vue`

**功能需求**：

#### 系统统计卡片（上半部分）
- 总用户数
- 活跃用户数（今日/过去 7 天）
- 总帖子数
- 待审核内容数（红色警告）
- 活跃预警数

#### 图表展示（下半部分，可选）
- 用户增长趋势（折线图）
- 内容发布趋势（柱状图）
- 预警分布（饼图）

#### 最近操作日志（表格）
- 操作人、操作类型、操作内容、时间戳
- 分页显示

---

### 5. AdminUsers 用户管理页

**文件**：`src/views/admin/AdminUsers.vue`

**功能需求**：

#### 用户列表表格
- 列：ID、用户名、邮箱、手机、角色、状态、操作
- 排序：按注册时间倒序
- 搜索：用户名、邮箱、手机号

#### 状态切换
- 启用/禁用 toggle 按钮
- 点击确认对话框

#### 角色管理
- 下拉菜单：普通用户 / 管理员
- 修改后立即生效

#### 批量操作（可选）
- 多选框
- 批量禁用、批量启用、批量删除按钮

#### 用户详情抽屉（可选）
- 点击用户名打开抽屉
- 显示：用户名、邮箱、手机、性别、注册时间、最后登录时间
- 编辑按钮

---

### 6. AdminContent 内容审核页

**文件**：`src/views/admin/AdminContent.vue`

**功能需求**：

#### 待审核内容列表
- 选项卡：全部 / 帖子 / 评论
- 表格列：ID、类型、作者、标题（仅帖子）、内容摘要、提交时间、操作

#### 内容展示抽屉
- 完整内容展示（富文本渲染）
- 作者信息
- 提交时间和原因（如有）

#### 审核操作
- 通过按钮：内容发布，消息通知作者
- 拒绝按钮：打开原因输入对话框，记录拒绝原因
- 删除按钮：永久删除内容

#### 审核历史（可选）
- 显示谁、何时、做了什么审核操作

---

### 7. AdminAlerts 预警管理页

**文件**：`src/views/admin/AdminAlerts.vue`

**功能需求**：

#### 预警列表表格
- 列：ID、标题、严重级别、所在地、状态、创建时间、操作
- 严重级别标签：低（绿）/ 中（黄）/ 高（红）
- 状态指示：活跃 / 已撤销

#### 发布预警按钮
- 打开对话框或弹出页
- 输入：标题、内容、严重级别、省份（可选）

#### 编辑与撤销
- 编辑按钮：修改标题、内容、级别
- 撤销按钮：将预警状态改为已撤销
- 删除按钮：永久删除预警

#### 预警预计时间（实时）
- 显示预警活跃多久
- 计时器倒计时（如预警有过期时间）

---

### 8. Pinia 管理状态管理

**文件**：`src/stores/admin.ts`

```typescript
export const useAdminStore = defineStore('admin', () => {
  // State
  const users = ref<AdminUser[]>([])
  const pendingContents = ref<PendingContent[]>([])
  const alerts = ref<AlertInfo[]>([])
  const stats = ref<SystemStats | null>(null)
  const isLoading = ref(false)

  // Actions
  async function fetchUsers() {
    isLoading.value = true
    try {
      users.value = await getAllUsers()
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPendingContents(type?: 'post' | 'comment') {
    try {
      pendingContents.value = await getPendingContents(type)
    } catch (error) {
      ElMessage.error('加载待审核内容失败')
    }
  }

  async function fetchAlerts() {
    try {
      alerts.value = await getAlerts()
    } catch (error) {
      ElMessage.error('加载预警列表失败')
    }
  }

  async function fetchStats() {
    try {
      stats.value = await getSystemStats()
    } catch (error) {
      ElMessage.error('加载统计数据失败')
    }
  }

  async function toggleUserStatus(userId: string, currentStatus: 0 | 1) {
    try {
      if (currentStatus === 1) {
        await disableUser(userId)
      } else {
        await enableUser(userId)
      }
      await fetchUsers()
      ElMessage.success('操作成功')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  async function approveContent(contentId: string, type: 'post' | 'comment') {
    try {
      await approveContent(contentId, type)
      await fetchPendingContents()
      ElMessage.success('已通过审核')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  async function rejectContentItem(contentId: string, type: 'post' | 'comment', reason: string) {
    try {
      await rejectContent(contentId, type, reason)
      await fetchPendingContents()
      ElMessage.success('已拒绝')
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }

  return {
    users, pendingContents, alerts, stats, isLoading,
    fetchUsers, fetchPendingContents, fetchAlerts, fetchStats, toggleUserStatus, approveContent, rejectContentItem
  }
})
```

---

### 9. 样式和权限

**需要实现的样式**：
- 统一的管理后台配色（可使用深色主题）
- 卡片式样式，清晰的数据展示
- 表格：紧凑布局，支持排序和筛选
- 按钮：危险操作（删除）使用红色，确认操作（通过）使用绿色
- 响应式：平板和手机端导航可折叠

**权限检查**：
- 路由守卫确保 `role === 0` 才能访问 `/admin/*`
- API 调用时服务器也应验证权限

---

## 🏗️ 项目结构

```
src/
├── api/
│   └── admin.ts                    ✅ 管理 API 模块
├── views/
│   └── admin/
│       ├── Dashboard.vue           ✅ 仪表板
│       ├── AdminUsers.vue          ✅ 用户管理
│       ├── AdminContent.vue        ✅ 内容审核
│       └── AdminAlerts.vue         ✅ 预警管理
├── layouts/
│   └── AdminLayout.vue             ✅ 管理布局
├── stores/
│   └── admin.ts                    ✅ 管理状态
├── types/
│   └── admin.ts                    ✅ 管理类型定义
└── styles/
    └── admin.css                   ✅ 管理样式
```

---

## 📝 验收标准

- [ ] 非管理员无法访问 `/admin/*`
- [ ] Dashboard 加载统计数据正确
- [ ] AdminUsers 页面列表加载正确
- [ ] 搜索、排序功能可用
- [ ] 启用/禁用用户成功
- [ ] 修改用户角色成功
- [ ] AdminContent 页面列表加载正确
- [ ] 通过/拒绝审核成功
- [ ] AdminAlerts 页面列表加载正确
- [ ] 发布预警成功
- [ ] 编辑、撤销、删除预警成功
- [ ] 响应式设计良好
- [ ] 所有操作都有成功/失败提示

---

## ⚠️ 常见陷阱

1. **权限验证**：确保前后端都做权限检查，避免普通用户访问
2. **删除操作**：实现二次确认，避免误删
3. **数据一致性**：用户/内容删除后其他地方的引用需清理
4. **性能优化**：管理表格可能有大量数据，考虑虚拟滚动和后端分页
5. **审计日志**：记录所有管理员操作便于追溯

---

## 🤝 与系统其他部分的接口

- 待审核数在 Header 显示，链接到内容审核页
- 预警信息被 Forum 首页引用
- 用户禁用后其登录被拒绝
- 内容被删除后列表自动更新

---

## 📌 下一步扩展建议

1. 添加系统日志和审计功能
2. 实现定时任务（预警过期自动撤销）
3. 数据导出功能（用户列表、统计数据）
4. 权限灵活配置（而不是固定 0/1）
5. 操作日志详细记录和导出
