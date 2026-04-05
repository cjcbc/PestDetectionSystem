# 阶段一：基础架构与认证系统

## 🎯 阶段目标
建立前端基础架构，包括 API 封装、状态管理、路由和认证系统。这是后续所有功能的基础。

**预期工作量**：2-3 天 | **优先级**：P0（阻塞后续）

---

## 📋 核心需求

### 1. API 请求层封装

**目标**：统一的 HTTP 客户端，支持拦截、错误处理、token 自动添加

**文件**：`src/api/request.ts`

**需要实现**：
- 创建 Axios 实例，配置基础 URL：`http://localhost:8888/api`
- 超时设置：10 秒
- 请求拦截器：自动添加 `Authorization: Bearer {token}`
- 响应拦截器：
  - 解析统一的 `Result<T>` 结构
  - 处理 3 类认证错误（见下表）
  - 处理网络异常和超时
- 响应类型定义

**TypeScript 类型**：
```typescript
// src/types/api.ts
export interface Result<T = any> {
  code: number;
  message: string;
  data?: T;
}

export interface ApiError {
  code: number;
  message: string;
  status?: number;
}
```

**错误处理映射表**：

| HTTP 状态 | 响应体 | 处理方案 |
|----------|-------|--------|
| 401 | `Bearer token is required` | 重定向登录 |
| 401 | `Token out of date` | 清除 token，重定向登录 |
| 401 | `Invalid Token` | 清除 token，重定向登录 |
| 403 | `Access denied` | 显示权限不足提示，停留当前页 |
| 其他 | 任意 | 显示 message 错误并记录 |

---

### 2. 用户认证模块

**目标**：Token 获取、存储、清除、登录状态判断

**文件**：`src/utils/auth.ts`

**需要实现**：
```typescript
// Token 管理
export function setToken(token: string): void
export function getToken(): string | null
export function clearToken(): void
export function isLoggedIn(): boolean

// 角色判断
export function getUserRole(): 0 | 1 | null  // 0=管理员，1=普通用户
export function isAdmin(): boolean
```

**存储介质**：localStorage（键名为 `token`）

---

### 3. 全局状态管理（Pinia）

**目标**：管理用户信息、认证状态、全局配置

**文件**：`src/stores/app.ts`

**Store 结构**：
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, clearToken } from '@/utils/auth'

export const useAppStore = defineStore('app', () => {
  // State
  const userInfo = ref(null)          // 用户信息
  const token = ref(getToken())       // JWT token
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 0)

  // Actions
  function setUser(info: any, newToken: string) {
    userInfo.value = info
    token.value = newToken
    setToken(newToken)
  }

  function logout() {
    userInfo.value = null
    token.value = null
    clearToken()
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    setUser,
    logout,
    setUserInfo
  }
})
```

---

### 4. 用户接口模块

**文件**：`src/api/user.ts`

**需要实现的函数**：
```typescript
export interface LoginPayload {
  account: string;      // 邮箱或手机号
  password: string;
}

export interface LoginResponse {
  id: number;
  role: 0 | 1;
  username: string;
  email: string;
  phone: string;
  sex: 0 | 1 | 2;
  image: string;         // Base64 Data URI
  status: number;
  flag: number;
  token: string;
}

export function login(payload: LoginPayload): Promise<LoginResponse>

export interface RegisterPayload {
  username?: string;
  password: string;
  email?: string;
  phone?: string;
}

export function register(payload: RegisterPayload): Promise<{ code: 200; message: string }>

export function getUserInfo(): Promise<LoginResponse>

export function updateUsername(newUsername: string): Promise<LoginResponse>

export function updateSex(sex: 0 | 1 | 2): Promise<LoginResponse>

export function updateAvatar(file: File): Promise<{ code: 200; message: string }>

export interface BindPayload {
  phone?: string;
  email?: string;
  bindType: 'PHONE' | 'EMAIL' | 'BOTH';
}

export function bindPhoneEmail(payload: BindPayload): Promise<{ code: 200; message: string }>
```

---

### 5. 路由和导航守卫

**目标**：定义路由、实现认证守卫、处理权限检查

**文件**：`src/router/index.ts`

**修改需求**：
- 扩展现有路由，添加 meta 字段（`requiresAuth`, `requiresAdmin`）
- 实现 beforeEach 守卫：
  - 检查 token 有效性
  - 检查权限（是否需要管理员）
  - 重定向登录或权限不足页面
- 设置页面 title

**路由表结构**：
```typescript
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/detection',
    name: 'Detection',
    component: () => import('@/views/Detection.vue'),
    meta: { title: '图片识别', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { title: 'AI 对话', requiresAuth: true }
  },
  {
    path: '/chat/:sessionId',
    name: 'ChatDetail',
    component: () => import('@/views/ChatDetail.vue'),
    meta: { title: '对话', requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '用户中心', requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/AdminUsers.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  }
]
```

---

### 6. 全局错误处理和提示

**目标**：统一的用户反馈机制

**需要实现**：
- API 错误时使用 Element Plus 的 `ElMessage.error()` 显示错误信息
- 成功操作时显示 `ElMessage.success()`
- 创建 `src/utils/message.ts` 封装常见提示

---

### 7. 全局样式和主题

**目标**：建立设计系统基础

**需要实现**：
- 创建 `src/styles/variables.css` - CSS 变量定义
  - 主色：`--color-primary: #2ecc71`
  - 深色：`--color-dark-primary: #27ae60`
  - 浅色：`--color-light-primary: #d5f4e6`
  - 其他颜色变量
  - 间距、排版变量
- 创建 `src/styles/global.css` - 全局样式
  - 重置默认样式
  - 响应式断点定义
- 在 `main.ts` 中导入全局样式

---

## 🏗️ 项目结构更新

```
src/
├── api/
│   ├── request.ts          ✅ 核心：Axios 实例 + 拦截器
│   └── user.ts             ✅ 用户接口模块
├── stores/
│   └── app.ts              ✅ Pinia 全局状态
├── router/
│   └── index.ts            ✅ 路由 + 守卫
├── utils/
│   ├── auth.ts             ✅ Token 管理
│   └── message.ts          ✅ 提示信息封装（可选）
├── styles/
│   ├── variables.css       ✅ CSS 变量
│   └── global.css          ✅ 全局样式
├── types/
│   ├── api.ts              ✅ API 通用类型
│   └── user.ts             ✅ 用户相关类型
├── views/
│   ├── Login.vue           ⏳ 对接 API（已有模板）
│   ├── Register.vue        ⏳ 对接 API（需创建）
│   ├── 404.vue             ✅ 创建权限不足/404 页面
│   └── ... 其他页面
└── main.ts
```

---

## 📝 验收标准

### Axios 和 API 层
- [ ] `request.ts` 创建成功，Axios 实例配置正确
- [ ] 请求拦截器正确添加 `Authorization` 头
- [ ] 响应拦截器正确解析 `Result<T>`
- [ ] 三种认证错误（401）处理正确
- [ ] 网络超时（10s）触发错误提示
- [ ] TypeScript 类型无红线

### 状态管理
- [ ] Pinia store 创建成功，能正常访问
- [ ] 用户登录后 store 中存储 userInfo 和 token
- [ ] token 持久化到 localStorage
- [ ] 页面刷新后 token 和 userInfo 仍存在

### 认证流程
- [ ] 未登录用户访问受保护页面时重定向到登录
- [ ] 登录成功后重定向到首页
- [ ] Token 过期显示提示并重定向登录
- [ ] 普通用户访问管理页面被拦截
- [ ] 管理员用户能访问管理页面

### 用户接口
- [ ] `login()` 函数可调用，返回类型正确
- [ ] `register()` 函数可调用
- [ ] `getUserInfo()` 函数可调用
- [ ] `updateUsername()` 等更新接口可调用
- [ ] 调用时自动添加 token

### UI 和样式
- [ ] CSS 变量成功加载，能通过 var(--color-primary) 引用
- [ ] 全局样式应用（字体、间距、默认样式）
- [ ] 登录页面样式正常

---

## ⚠️ 常见陷阱

1. **Token 存储和清除**：确保登出时彻底清除 token 和 userInfo
2. **循环重定向**：避免 beforeEach 中重复判断导致死循环
3. **类型安全**：API 响应类型务必完整，避免 any 类型
4. **请求超时**：确保超时时间合理（后端响应通常 < 10s）
5. **CORS 问题**：确认后端已配置 CORS，本地开发通常需要配置代理

---

## 🤝 与下一阶段的接口

完成此阶段后，后续阶段可以：
- 直接使用 `api/user.ts` 的函数实现登录和注册页面
- 使用 `useAppStore()` 获取用户信息和检查登录状态
- 使用路由守卫保护需要认证的页面
- 使用 `request.ts` 创建新的 API 模块（detect、chat 等）
