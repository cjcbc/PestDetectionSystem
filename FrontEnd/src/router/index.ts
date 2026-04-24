import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { isAdmin, isLoggedIn } from '@/utils/auth'
import { ElMessage } from 'element-plus'

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
    meta: { title: '首页' }
  },
  {
    path: '/detection',
    name: 'Detection',
    component: () => import('@/views/Detection.vue'),
    meta: { title: '病虫识别' }
  },
  {
    path: '/detection/:id',
    name: 'DetectionDetail',
    component: () => import('@/views/DetectionDetail.vue'),
    meta: { title: '识别详情' }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { title: 'AI 问答' }
  },
  {
    path: '/chat/:sessionId',
    name: 'ChatDetail',
    component: () => import('@/views/ChatDetail.vue'),
    meta: { title: '会话详情' }
  },
  {
    path: '/user',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/forum',
    name: 'Forum',
    component: () => import('@/views/Forum.vue'),
    meta: { title: '交流论坛', canViewWithoutAuth: true }
  },
  {
    path: '/forum/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
    meta: { title: '帖子详情', canViewWithoutAuth: true }
  },
  {
    path: '/forum/create',
    name: 'CreatePost',
    component: () => import('@/views/CreatePost.vue'),
    meta: { title: '发布帖子', requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理仪表板', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('@/views/admin/AdminContent.vue'),
        meta: { title: '内容管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'alerts',
        name: 'AdminAlerts',
        component: () => import('@/views/admin/AdminAlerts.vue'),
        meta: { title: '告警管理', requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404 - 页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '病虫害识别与农业论坛系统'

  const loggedIn = isLoggedIn()
  const adminRole = isAdmin()

  // 检查是否需要认证
  if (to.meta.requiresAuth && !loggedIn) {
    ElMessage.warning('请先登录')
    next({ name: 'Login' })
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !adminRole) {
    ElMessage.error('您没有权限访问此页面')
    next({ name: 'Home' })
    return
  }

  // 已登录用户访问登录/注册页时跳转到首页
  if (loggedIn && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
