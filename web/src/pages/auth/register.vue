<template>
  <view class="register-page">
    <!-- 头部区域 -->
    <view class="header-section">
      <view class="logo-box">
        <image src="/static/images/user.png" class="logo-icon" />
      </view>
      <text class="page-title">注册新账号</text>
      <text class="page-subtitle">开启您的财富增值之旅</text>
    </view>

    <!-- 消息提示 -->
    <view v-if="errorMessage" class="message-box error">
        {{ errorMessage }}
    </view>
    <view v-if="successMessage" class="message-box success">
        {{ successMessage }}
    </view>

    <!-- 表单区域 -->
    <view class="form-area">
      <!-- 手机号 -->
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

      <!-- 密码 -->
      <view class="input-group">
          <text class="input-label">设置密码</text>
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

      <!-- 确认密码 -->
      <view class="input-group">
          <text class="input-label">确认密码</text>
          <view class="input-wrapper">
            <image src="/static/images/shield-check.png" class="input-icon" />
            <input 
              type="password" 
              v-model="confirmPassword" 
              placeholder="请再次输入密码" 
              class="custom-input"
              placeholder-class="input-placeholder"
            />
          </view>
      </view>
      
      <!-- 注册按钮 -->
      <button 
        @click="handleRegister" 
        :disabled="loading" 
        class="submit-btn"
        :class="{ 'is-loading': loading }"
      >
        {{ loading ? '注册中...' : '立即注册' }}
      </button>

      <!-- 底部链接 -->
      <view class="footer-area">
        <view @click="goToLogin" class="footer-text">
            已有账号？<text class="highlight">去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { post, saveToken } from '../../services/apiService';

const phoneNumber = ref('');
const password = ref('');
const confirmPassword = ref('');
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const handleRegister = async () => {
    if (!phoneNumber.value) {
        errorMessage.value = '请输入手机号';
        return;
    }
    if (!password.value) {
        errorMessage.value = '请输入密码';
        return;
    }
    if (!confirmPassword.value) {
        errorMessage.value = '请确认密码';
        return;
    }
    if (password.value !== confirmPassword.value) {
        errorMessage.value = '两次输入的密码不一致';
        return;
    }
    
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    try {
        const res = await post('/api/auth/register', {
            phoneNumber: phoneNumber.value,
            password: password.value
        });

        if (res.statusCode >= 200 && res.statusCode < 300) {
            const json = res.data;
            
            // Automatically log in or redirect to login
            uni.setStorageSync('userInfo', JSON.stringify(json));
            if (json.token) saveToken(json.token);
            
            successMessage.value = '注册成功，正在跳转登录...';
            setTimeout(() => {
                uni.redirectTo({ url: '/pages/auth/login' });
            }, 1500);
        } else {
            let errorText = '注册失败';
            if (res.data?.message) errorText = res.data.message;
            else if (res.data?.detail) errorText = res.data.detail;
            throw new Error(errorText);
        }
    } catch (e) {
        errorMessage.value = e.message || '注册请求失败';
    } finally {
        loading.value = false;
    }
};

const goToLogin = () => {
    uni.redirectTo({ url: '/pages/auth/login' });
};
</script>

<style lang="scss" scoped>

.register-page {
  min-height: 100vh;
  background-color: $bg-white;
  padding: 32px; /* p-8 */
  padding-top: 80px; /* pt-20 */
  box-sizing: border-box;
}

/* 头部样式 */
.header-section {
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
  background-color: $bg-input;
  padding: 16px; /* p-4 */
  border-radius: 12px; /* rounded-xl */
  display: flex;
  align-items: center;
  border: 1px solid transparent;
  transition: border-color 0.2s;
  
  /* focus-within support */
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
  opacity: 0.5; /* text-gray-400 */
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

/* 注册按钮 */
.submit-btn {
  width: 100%;
  padding: 16px 0; /* py-4 */
  background-color: $primary;
  color: white;
  border-radius: 12px; /* rounded-xl */
  font-weight: bold;
  font-size: 16px; /* text-base */
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2); /* shadow-primary/20 */
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  transition: transform 0.1s;
  
  &:active {
    transform: scale(0.98);
  }
  
  &[disabled] {
    opacity: 0.7;
  }
}

/* 底部区域 */
.footer-area {
  text-align: center;
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