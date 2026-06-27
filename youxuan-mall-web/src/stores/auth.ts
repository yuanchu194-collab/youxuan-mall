import { defineStore } from 'pinia'
import { authApi } from '@/api/modules'
import type { User } from '@/types'

const readStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('youxuan_user') || 'null') as User | null
  } catch {
    return null
  }
}

const resolveRole = (user: unknown) => {
  const raw = user as { role?: string; user?: { role?: string }; currentUser?: { role?: string } } | null
  return (raw?.role || raw?.user?.role || raw?.currentUser?.role || '').toUpperCase()
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('youxuan_token') || '',
    user: readStoredUser()
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    isAdmin: (state) => resolveRole(state.user) === 'ADMIN'
  },
  actions: {
    async login(username: string, password: string) {
      const res = await authApi.login({ username, password })
      this.token = res.token
      this.user = res.user
      localStorage.setItem('youxuan_token', res.token)
      localStorage.setItem('youxuan_user', JSON.stringify(res.user))
    },
    async loadProfile() {
      if (!this.token) return
      this.user = await authApi.me()
      localStorage.setItem('youxuan_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('youxuan_token')
      localStorage.removeItem('youxuan_user')
    }
  }
})
