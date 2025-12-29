<template>
  <view class="login-page">
    <!-- Logo & Title -->
    <view class="login-header">
      <view class="logo-box">
        <image src="/static/images/trending-up.png" class="logo-icon" />
      </view>
      <text class="page-title">欢迎来到岁有长赢</text>
      <text class="page-subtitle">您的专业基金管家</text>
    </view>

    <!-- Messages -->
    <view v-if="errorMessage" class="message-box error">
        {{ errorMessage }}
    </view>
    <view v-if="successMessage" class="message-box success">
        {{ successMessage }}
    </view>

    <!-- Login Form -->
    <view class="form-area">
      <view class="input-group">
          <text class="input-label">手机号码</text>
          <view class="input-wrapper">
            <text class="prefix-code">+86</text>
            <input 
              type="number" 
              v-model="phoneNumber" 
              placeholder="请输入手机号" 
              class="custom-input" 
              placeholder-class="input-placeholder"
            />
          </view>
      </view>
      
      <view class="input-group">
          <text class="input-label">密码</text>
          <view class="input-wrapper">
            <image src="/static/images/shield-check.png" class="input-icon" />
            <input 
              type="password" 
              v-model="password" 
              placeholder="请输入密码" 
              class="custom-input"
              placeholder-class="input-placeholder"
            />
          </view>
      </view>

      <!-- Action Buttons -->
      <view class="btn-group">
          <button 
            @click="handlePhoneLogin" 
            :disabled="isLoading" 
            class="btn btn-primary"
            :class="{ 'is-loading': isLoading }"
          >
            {{ isLoading ? '登录中...' : '登录' }}
          </button>

          <button 
              @click="handleWechatLogin" 
              :disabled="isLoading"
              class="btn btn-wechat"
          >
              <view class="wechat-icon-bg">We</view>
              <text>微信一键登录</text>
          </button>
      </view>
    </view>

    <!-- Footer -->
    <view class="footer-area">
      <view @click="goToRegister" class="footer-text">
          没有账号？<text class="highlight">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { post, saveToken } from '../../services/apiService';

const phoneNumber = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const handlePhoneLogin = async () => {
    if (!phoneNumber.value.trim()) {
        errorMessage.value = '请输入手机号码';
        return;
    }
    if (!/^1[3-9]\d{9}$/.test(phoneNumber.value.trim())) {
        errorMessage.value = '请输入有效的手机号码';
        return;
    }
    if (!password.value.trim()) {
        errorMessage.value = '请输入密码';
        return;
    }

    isLoading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    try {
        console.log('Sending login request for:', phoneNumber.value);
        const res = await post('/api/auth/login', {
            phoneNumber: phoneNumber.value,
            password: password.value
        });

        console.log('Login response status:', res.statusCode);

        if (res.statusCode >= 200 && res.statusCode < 300) {
            const json = res.data;
            
            // Save User Info
            uni.setStorageSync('userInfo', JSON.stringify(json));
            
            // Save Token
            if (json.token) {
                saveToken(json.token);
            }

            successMessage.value = '登录成功，正在跳转...';
            setTimeout(() => {
                uni.switchTab({ url: '/pages/home/index' });
            }, 1000);
        } else {
            let errorText = '登录失败，请检查用户名和密码';
            if (res.data?.message) errorText = res.data.message;
            else if (res.data?.detail) errorText = res.data.detail;
            else if (res.data?.error) errorText = res.data.error;
            throw new Error(errorText);
        }
    } catch (error) {
        console.error('Login error:', error);
        errorMessage.value = error.message || '登录请求异常，请稍后重试';
    } finally {
        isLoading.value = false;
    }
};

const handleWechatLogin = async () => {
    isLoading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    try {
        console.log('Starting WeChat Login...');
        
        // Get User Profile
        const userRes = await new Promise((resolve, reject) => {
            uni.getUserProfile({
                desc: '用于完善会员资料',
                success: resolve,
                fail: (err) => {
                    console.log('getUserProfile fail', err);
                    reject(new Error('需要授权才能登录')); 
                }
            });
        });

        const userInfo = userRes.userInfo;
        
        // Login to get code
        const loginRes = await new Promise((resolve, reject) => {
            uni.login({
                provider: 'weixin',
                success: resolve,
                fail: reject
            });
        });

        const code = loginRes.code;
        console.log('WeChat Code:', code);

        const res = await post('/api/auth/wechat-login', {
            code: code,
            nickname: userInfo.nickName,
            avatarUrl: userInfo.avatarUrl,
            gender: userInfo.gender
        });

        if (res.statusCode >= 200 && res.statusCode < 300) {
            const json = res.data;
            if (json.token) saveToken(json.token);
            if (json.user) uni.setStorageSync('userInfo', JSON.stringify(json.user));
            else uni.setStorageSync('userInfo', JSON.stringify(json));

            successMessage.value = '登录成功，正在跳转...';
            setTimeout(() => {
                uni.switchTab({ url: '/pages/home/index' });
            }, 1000);
        } else {
            let errorText = '微信登录失败';
            if (res.data?.message) errorText = res.data.message;
            throw new Error(errorText);
        }

    } catch (error) {
        console.error('WeChat Login Error:', error);
        if (error.errMsg && error.errMsg.includes('auth deny')) {
             errorMessage.value = '用户取消授权';
        } else {
             errorMessage.value = error.message || '微信登录失败，请稍后重试';
        }
    } finally {
        isLoading.value = false;
    }
};

const goToRegister = () => {
    uni.redirectTo({ url: '/pages/auth/register' });
};
</script>

<style lang="scss" scoped>

.login-page {
  min-height: 100vh;
  background-color: $bg-white;
  padding: 32px; /* p-8 */
  padding-top: 80px; /* pt-20 */
  box-sizing: border-box;
}

/* 头部样式 */
.login-header {
  text-align: center;
  margin-bottom: 40px; /* mb-10 */
}

.logo-box {
  width: 64px; /* w-16 */
  height: 64px;
  background-color: $primary;
  border-radius: 16px; /* rounded-2xl */
  margin: 0 auto 16px auto; /* mx-auto mb-4 */
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.3); /* shadow-primary/30 */
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.page-title {
  font-size: 24px; /* text-2xl */
  font-weight: bold;
  color: $text-main;
  display: block;
}

.page-subtitle {
  font-size: 14px; /* text-sm */
  color: $text-muted;
  margin-top: 8px; /* mt-2 */
  display: block;
}

/* 消息提示框 */
.message-box {
  padding: 12px; /* p-3 */
  border-radius: 12px; /* rounded-xl */
  text-align: center;
  font-size: 14px; /* text-sm */
  margin-bottom: 16px; /* mb-4 */
  border: 1px solid transparent;
  
  &.error {
    background-color: $bg-error;
    color: $color-error;
    border-color: $border-error;
  }
  
  &.success {
    background-color: $bg-success;
    color: $color-success;
    border-color: $border-success;
  }
}

/* 表单区域 */
.form-area {
  display: flex;
  flex-direction: column;
  gap: 16px; /* space-y-4 */
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 4px; /* space-y-1 */
}

.input-label {
  font-size: 12px; /* text-xs */
  color: $text-regular;
  margin-left: 4px; /* ml-1 */
}

.input-wrapper {
  background-color: $bg-input; /* bg-page */
  padding: 16px; /* p-4 */
  border-radius: 12px; /* rounded-xl */
  display: flex;
  align-items: center;
  border: 1px solid transparent;
  transition: border-color 0.2s;
  
  /* focus-within logic needs JS or relying on native input focus styles, 
     but here we set a border on the wrapper */
  /* UniApp/CSS specific hack: :focus-within works in modern webviews */
  &:focus-within {
    border-color: $border-focus;
  }
}

.prefix-code {
  color: $text-regular;
  margin-right: 12px; /* mr-3 */
  font-weight: bold;
}

.input-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px; /* mr-3 */
  opacity: 0.5; /* text-gray-400 effect */
}

.custom-input {
  flex: 1;
  background-color: transparent;
  border: none;
  outline: none;
  font-size: 16px; /* text-base */
  color: $text-main;
  height: 24px; /* h-6 */
}

/* UniApp placeholder styling */
:deep(.input-placeholder) {
  color: $text-placeholder;
}

/* 按钮组 */
.btn-group {
  display: flex;
  flex-direction: column;
  gap: 16px; /* space-y-4 */
}

.btn {
  width: 100%;
  padding: 16px 0; /* py-4 */
  border-radius: 12px; /* rounded-xl */
  font-weight: bold;
  font-size: 16px; /* text-base */
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  transition: transform 0.1s;
  
  &:active {
    transform: scale(0.98);
  }
  
  /* Disabled state */
  &[disabled] {
    opacity: 0.7;
  }
}

.btn-primary {
  background-color: $primary;
  color: white;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2); /* shadow-primary/20 */
}

.btn-wechat {
  background-color: $color-wechat;
  color: white;
  box-shadow: 0 10px 15px -3px rgba(7, 193, 96, 0.2);
  gap: 8px; /* space-x-2 */
}

.wechat-icon-bg {
  background-color: white;
  color: $color-wechat;
  border-radius: 50%;
  width: 20px; /* w-5 */
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px; /* text-xs */
  font-weight: bold;
  margin-right: 8px; /* mr-2 */
}

/* 底部区域 */
.footer-area {
  text-align: center;
  margin-top: 16px; /* mt-4 */
}

.footer-text {
  font-size: 14px; /* text-sm */
  color: $text-regular;
}

.highlight {
  color: $primary;
  font-weight: bold;
}
</style>