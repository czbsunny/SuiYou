import { defineStore } from 'pinia'
import { getUserInfo, logout as apiLogout } from '@/api/modules/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: null,
    isLoggedIn: false
  }),

  getters: {
    getToken: (state) => state.token,
    getUserInfo: (state) => state.userInfo,
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    setToken(token) {
      this.token = token
      this.isLoggedIn = !!token
      if (token) {
        uni.setStorageSync('token', token)
      } else {
        uni.removeStorageSync('token')
      }
    },

    async fetchUserInfo() {
      try {
        const userInfo = await getUserInfo()
        this.userInfo = userInfo
        return userInfo
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    },

    async login(token) {
      this.setToken(token)
      await this.fetchUserInfo()
    },

    async logout() {
      try {
        await apiLogout()
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        this.setToken('')
        this.userInfo = null
        this.isLoggedIn = false
        uni.reLaunch({ url: '/pages/home/index' })
      }
    },

    clearAuth() {
      this.setToken('')
      this.userInfo = null
      this.isLoggedIn = false
    }
  }
})
