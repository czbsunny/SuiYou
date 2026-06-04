import { defineStore } from 'pinia'
import { wechatLogin } from '@/api/modules/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || null,
    userInfo: uni.getStorageSync('userInfo') || null,
    isLoggingIn: false,
    loginPromise: null,
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayUserInfo: (state) => state.userInfo || {}
  },
  
  actions: {
    async login() {
      if (this.isLoggingIn) return this.loginPromise

      this.isLoggingIn = true
      this.loginPromise = (async () => {
        try {
          const loginRes = await uni.login({ provider: 'weixin' })
          if (loginRes.errMsg !== 'login:ok' || !loginRes.code) throw new Error('微信登录失败')
          console.log('微信登录成功，code:', loginRes.code)

          const res = await wechatLogin(loginRes.code)
          console.log('微信登录成功，res:', res)
          const { token, user } = res.data || {}
          if (token) {
            this.setAuthData(token, user)
            return true
          }
          return false
        } catch (error) {
          console.error('登录全链路失败:', error)
          return false
        } finally {
          this.isLoggingIn = false
          this.loginPromise = null
        }
      })()

      return this.loginPromise
    },

    async silentLogin() {
      return await this.login()
    },

    setAuthData(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      uni.setStorageSync('token', token)
      uni.setStorageSync('userInfo', userInfo)
    },

    updateUserInfo(userInfo) {
      this.userInfo = userInfo
      uni.setStorageSync('userInfo', userInfo)
    },
    
    logout() {
      this.token = null
      this.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  }
})