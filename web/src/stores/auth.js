import { defineStore } from 'pinia'
import apiService from '@/services/apiService.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || null,
    userInfo: uni.getStorageSync('userInfo') || null,
    isLoggingIn: false,
    loginPromise: null,
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    // 提供一个安全的用户信息获取方式
    displayUserInfo: (state) => state.userInfo || {}
  },
  
  actions: {
    /**
     * 核心登录流程：微信 login -> 后端换取 token
     */
    async login() {
      // 如果正在登录中，返回同一个 Promise，避免并发请求
      if (this.isLoggingIn) return this.loginPromise;

      this.isLoggingIn = true;
      this.loginPromise = (async () => {
        try {
          // 1. 获取微信 Code
          const loginRes = await uni.login({ provider: 'weixin' });
          if (loginRes.errMsg !== 'login:ok' || !loginRes.code) throw new Error('微信登录失败');
          console.log('微信登录成功，code:', loginRes.code);

          // 2. 换取后端 Token
          const res = await apiService.post('/api/auth/wechat-login', 
            { code: loginRes.code }, 
            { skipAuth: true }
          );

          const { token, user } = res.data || {};
          if (token) {
            this.setAuthData(token, user);
            return true;
          }
          return false;
        } catch (error) {
          console.error('登录全链路失败:', error);
          return false;
        } finally {
          this.isLoggingIn = false;
          this.loginPromise = null;
        }
      })();

      return this.loginPromise;
    },

    /**
     * 静默登录：供拦截器或页面按需调用
     * 其实在小程序环境下，它和 login 逻辑是一致的
     */
    async silentLogin() {
      return await this.login();
    },

    setAuthData(token, userInfo) {
      this.token = token;
      this.userInfo = userInfo;
      uni.setStorageSync('token', token);
      uni.setStorageSync('userInfo', userInfo);
    },

    logout() {
      this.token = null;
      this.userInfo = null;
      uni.removeStorageSync('token');
      uni.removeStorageSync('userInfo');
    }
  }
})