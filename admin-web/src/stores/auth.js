import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api'
import { getDefaultRoute } from '@/utils/role'
import router from '@/router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    user: JSON.parse(localStorage.getItem('admin_user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    realName: (state) => state.user?.realName || ''
  },
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      this.user = {
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_user', JSON.stringify(this.user))
      const redirect = router.currentRoute.value.query.redirect || getDefaultRoute(data.role)
      router.push(redirect)
    },
    async logout() {
      try {
        await logoutApi()
      } catch {
        // ignore
      }
      this.token = ''
      this.user = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      router.push('/login')
    }
  }
})
