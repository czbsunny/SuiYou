<template>
  <view class="profile-container">
    <!-- 用户信息卡片 -->
    <view class="user-card" hover-class="hover-opacity" @click="handleAvatarClick">
      <view class="user-avatar">
        <!-- 使用 computed 的数据 -->
        <text class="avatar-text">{{ firstChar }}</text>
        <text v-if="!firstChar" class="avatar-icon">👤</text>
      </view>
      <view class="user-info">
        <view class="user-name">{{ userInfo.username }}</view>
      </view>
      <view class="feature-arrow">
        <image src="/static/images/arrow-right.png" mode="aspectFit" class="arrow-icon"></image> 
      </view>
    </view>
    
    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-item" hover-class="item-active" @click="handleReportClick">
        <view class="menu-item-left">
          <view class="menu-icon report-icon">
            <text class="icon-text">📊</text>
          </view>
          <text class="menu-text">资产概览</text>
        </view>
        <view class="menu-item-right">
          <text class="menu-desc">查看资产详情</text>
          <image src="/static/images/arrow-right.png" class="arrow-icon" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.js'

const authStore = useAuthStore()

// 使用响应式数据，无需手动在 onShow 里赋值
const userInfo = computed(() => authStore.displayUserInfo)
const firstChar = computed(() => {
  const name = userInfo.value.username
  return name ? Array.from(name)[0].toUpperCase() : ''
})

const handleAvatarClick = () => {
  if (!authStore.isLoggedIn) {
    authStore.login() // 如果没登录，点击触发登录
  } else {
    uni.navigateTo({ url: '/pages/profile/edit' })
  }
}

const handleReportClick = () => {
  uni.navigateTo({ url: '/pages/report/index' })
}
</script>

<style lang="scss" scoped>
.profile-container {
  padding: $spacing-md $spacing-base;
  min-height: 100vh;
  background-color: $bg-page;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 40rpx $spacing-md;
  background-color: $bg-white;
  border-radius: $radius-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-card;
  transition: all 0.2s ease;
  position: relative;
}

.user-avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);
}

.avatar-text {
  font-size: 56rpx;
  color: $text-inverse;
  font-weight: $fw-semibold;
  font-family: $font-family-money;
}

.user-name {
  font-size: 40rpx;
  font-weight: $fw-semibold;
  color: $text-main;
  margin-bottom: 8rpx;
}

.user-email {
  font-size: 26rpx;
  color: $text-placeholder;
  letter-spacing: 0.5rpx;
}

.feature-arrow {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-left: auto;
  padding-right: 10rpx;
}

.arrow-icon {
  width: 32rpx;
  height: 32rpx;
  opacity: 0.3;
}

.logout-box {
  margin-top: 60rpx;
  padding: 0 $spacing-sm;
}

.logout-btn {
  height: 100rpx;
  @include flex-center;
  background-color: $bg-white;
  color: $color-gain;
  border-radius: $radius-base;
  font-size: 30rpx;
  font-weight: $fw-medium;
  box-shadow: $shadow-card;
  transition: all 0.2s ease;
}

.hover-opacity {
  opacity: 0.9;
  transform: scale(0.99);
}

.hover-grey {
  background-color: $bg-subtle-hover !important;
}

/* 功能菜单 (来自 SuiYou) */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 40rpx;
  background-color: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(42, 128, 108, 0.03);
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.99);
    background-color: #fcfcfc;
  }
}

.menu-item-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  @include flex-center;
}

.report-icon {
  background: linear-gradient(135deg, #e0f2fe 0%, #f0fdf4 100%);
}

.icon-text {
  font-size: 40rpx;
}

.menu-text {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-main;
}

.menu-item-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.menu-desc {
  font-size: 24rpx;
  color: $text-sub;
}
</style>
