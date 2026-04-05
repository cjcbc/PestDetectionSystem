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
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/detection',
    name: 'Detection',
    component: () => import('@/views/Detection.vue'),
    meta: { title: '病虫识别', requiresAuth: true }
  },
  {
    path: '/detection/:id',
    name: 'DetectionDetail',
    component: () => import('@/views/DetectionDetail.vue'),
    meta: { title: '识别详情', requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { title: 'AI 问答', requiresAuth: true }
  },
  {
    path: '/chat/:sessionId',
    name: 'ChatDetail',
    component: () => import('@/views/ChatDetail.vue'),
    meta: { title: '会话详情', requiresAuth: true }
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
    meta: { title: '交流论坛', requiresAuth: true }
  },
  {
    path: '/forum/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
    meta: { title: '帖子详情', requiresAuth: true }
  },
  {
    path: '/forum/create',
    name: 'CreatePost',
    component: () => import('@/views/CreatePost.vue'),
    meta: { title: '发布帖子', requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/AdminUsers.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/content',
    name: 'AdminContent',
    component: () => import('@/views/admin/AdminContent.vue'),
    meta: { title: '内容管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/alerts',
    name: 'AdminAlerts',
    component: () => import('@/views/admin/AdminAlerts.vue'),
    meta: { title: '告警管理', requiresAuth: true, requiresAdmin: true }
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

  if (to.meta.requiresAuth && !loggedIn) {
    ElMessage.warning('请先登录')
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && !adminRole) {
    ElMessage.error('您没有权限访问此页面')
    next({ name: 'Home' })
    return
  }

  if ((to.name === 'Login' || to.name === 'Register') && loggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
