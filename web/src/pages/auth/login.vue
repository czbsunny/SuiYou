<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="background-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>
    
    <view class="login-container">
      <view class="logo">
        <h1 class="logo-title">岁有长赢</h1>
        <div class="logo-subtitle">个人资产记账</div>
      </view>
      
      <!-- 微信登录部分 -->
      <view class="login-wechat-container">
        <!-- 微信登录按钮 -->
        <button 
          :disabled="isLoading" 
          @tap="handleWechatLogin" 
          class="wechat-login-button"
          hover-class="btn-hover"
        >
          <image src="/static/images/wechat.png" class="btn-icon"></image>
          <text>{{ isLoading ? '安全登录中...' : '微信一键登录' }}</text>
        </button>
        
        <!-- 错误提示 -->
        <view class="error-message" v-if="errorMessage">{{ errorMessage }}</view>
      </view>
    </view>
  </view>
</template>

<script>
import { post, saveToken } from '../../services/apiService.js';

export default {
  data() {
    return {
      isLoading: false,
      errorMessage: ''
    }
  },
  onLoad() {
    // 页面加载
  },
  onUnload() {
    // 清理资源
  },
  methods: {
    // 返回上一页
    goBack() {
      // 获取当前页面栈信息
      const pages = getCurrentPages();
      // 如果页面栈长度大于1，说明有前一个页面可以返回
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        // 如果是第一个页面，则跳转到首页
        uni.reLaunch({ url: '/pages/home/index' });
      }
    },
    
    // 处理微信登录
    async handleWechatLogin() {
      try {
        this.isLoading = true;
        this.errorMessage = '';
        
        // 在微信小程序中调用登录API
        uni.login({
          provider: 'weixin',
          success: async (loginRes) => {
            try {
              // 调用后端API进行微信登录
              const res = await post('/api/auth/wechat-login', {
                code: loginRes.code
              });
              
              if (res.statusCode >= 200 && res.statusCode < 300) {
                try {
                  const json = res.data;
                  
                  // 登录成功后保存token和用户信息
                  if (json.token) {
                    saveToken(json.token);
                    if (json.user) {
                      uni.setStorageSync('userInfo', json.user);
                    }
                  }
                  
                  // 微信登录成功后跳转到首页
                  uni.reLaunch({ url: '/pages/home/index' });
                } catch (e) {
                  console.error('解析响应JSON失败:', e);
                  throw new Error('响应解析失败');
                }
              } else {
                // 处理错误状态
                let errorText = '微信登录失败，请稍后重试';
                try {
                  errorText = res.data?.message || res.data?.detail || res.data?.error || errorText;
                } catch (e) {
                  console.error('解析错误响应失败:', e);
                }
                console.log('微信登录失败，状态码:', res.statusCode, '错误信息:', errorText);
                throw new Error(errorText);
              }
            } catch (error) {
              console.error('微信登录错误:', error);
              this.errorMessage = error.message || '微信登录失败，请稍后重试';
              this.isLoading = false;
            }
          },
          fail: (err) => {
            console.error('调用微信登录失败:', err);
            this.errorMessage = '微信登录失败，请检查网络或重新尝试';
            this.isLoading = false;
          }
        });
      } catch (error) {
        console.error('微信登录错误:', error);
        this.errorMessage = error.message || '微信登录失败，请稍后重试';
        this.isLoading = false;
      }
    }
  }
}
</script>

<style lang="scss">
/* 登录页面根容器样式 */
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: $spacing-lg;
  background-color: $bg-page;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background-color: rgba($primary, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 250px;
  height: 250px;
  bottom: -100px;
  right: -100px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: -50px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-20px) translateX(10px);
  }
}

/* 登录页面特有样式 */
.login-container {
  background: $bg-white;
  padding: $spacing-lg $spacing-md;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  backdrop-filter: blur(10px);
}

/* Logo样式 */
.logo {
  text-align: center;
  margin-bottom: $spacing-xl;
  position: relative;
}

.logo-title {
  font-size: 32px;
  font-weight: $fw-bold;
  margin: 0;
  color: $primary;
  display: inline-block;
  letter-spacing: 0;
  font-family: $font-family-money;
}

.logo-subtitle {
  font-size: 14px;
  color: $text-sub;
  margin-top: $spacing-xs;
  opacity: 0.9;
}

.logo::after {
  content: '';
  display: block;
  width: 50px;
  height: 3px;
  background-color: $primary;
  margin: 10px auto 0;
  border-radius: 1.5px;
}

/* LoginWechat 组件样式 (Merged) */
.login-wechat-container {
  width: 100%;
  text-align: center;
}

/* 标题样式 */
.form-title {
  text-align: center;
  margin-bottom: $spacing-xl;
  color: $text-main;
  font-size: 20px;
  font-weight: $fw-semibold;
  letter-spacing: 0;
}

/* 微信按钮优化 */
.wechat-login-button {
  height: 100rpx;
  width: 85%;
  margin-left: auto;
  margin-right: auto;
  background: linear-gradient(135deg, #07C160 0%, #06AD56 100%);
  color: #FFFFFF;
  border-radius: $radius-base;
  @include flex-center;
  font-size: 30rpx;
  font-weight: $fw-semibold;
  box-shadow: 0 12rpx 24rpx rgba(7, 193, 96, 0.2);
  border: none;
  margin-bottom: 40rpx;
  
  .btn-icon {
    width: 44rpx;
    height: 44rpx;
    margin-right: 16rpx;
  }
  
  &::after { border: none; }
}

.btn-hover {
  transform: translateY(2rpx);
  filter: brightness(0.95);
}

/* 错误提示 */
.error-message {
  background-color: rgba($color-gain, 0.1);
  color: $color-gain;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-base;
  margin-top: $spacing-md;
  font-size: 14px;
  text-align: center;
  animation: slideInLeft 0.3s ease-out;
  transition: all 0.3s ease;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 页面加载动画 */
@keyframes pageFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.login-page {
  animation: pageFadeIn 0.6s ease-out;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-container {
    padding: $spacing-xl $spacing-lg;
    margin: $spacing-sm;
    box-shadow: $shadow-card;
  }
  
  .logo-title {
    font-size: 28px;
  }
  
  .form-title {
    font-size: 20px;
  }
  
  .wechat-login-button {
    padding: 16px;
    font-size: 16px;
  }
}
</style>