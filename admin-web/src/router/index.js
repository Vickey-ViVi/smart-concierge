import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { hasMenu, getDefaultRoute } from '@/utils/role'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '会员之声', menu: 'dashboard' }
      },
      {
        path: 'question',
        name: 'Question',
        component: () => import('@/views/question/Index.vue'),
        meta: { title: '推荐记录', menu: 'question' }
      },
      {
        path: 'complaint',
        name: 'Complaint',
        component: () => import('@/views/complaint/List.vue'),
        meta: { title: '工单管理', menu: 'complaint' }
      },
      {
        path: 'lost',
        name: 'Lost',
        component: () => import('@/views/lost/Index.vue'),
        meta: { title: '失物登记', menu: 'lost' }
      },
      {
        path: 'proposal',
        name: 'Proposal',
        component: () => import('@/views/proposal/Index.vue'),
        meta: { title: '入驻提议', menu: 'proposal' }
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('@/views/shop/Index.vue'),
        meta: { title: '店铺管理', menu: 'shop' }
      },
      {
        path: 'activity',
        name: 'Activity',
        component: () => import('@/views/activity/Index.vue'),
        meta: { title: '活动管理', menu: 'activity' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', menu: 'user' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/Index.vue'),
        meta: { title: '系统配置', menu: 'config' }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/audit/Index.vue'),
        meta: { title: '审计日志', menu: 'audit' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/Index.vue'),
        meta: { title: '系统监控', menu: 'monitor' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    if (auth.isLoggedIn && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }
  if (!auth.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  const menuKey = to.meta.menu
  if (menuKey && !hasMenu(auth.role, menuKey)) {
    next(getDefaultRoute(auth.role))
  } else {
    next()
  }
})

export default router
