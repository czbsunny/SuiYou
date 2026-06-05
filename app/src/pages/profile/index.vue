<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="profile-head">
          <view class="profile-avatar-wrap">
            <view class="profile-avatar">img</view>
            <view class="edit">edit</view>
          </view>
          <text class="name">陈伟</text>
          <text class="role">{{ roleText }}</text>
        </view>

        <view class="menu-list">
          <view v-for="item in menuItems" :key="item.label" class="menu-card" @tap="tapMenu(item.label)">
            <image :src="`/static/images/${item.icon}.png`" class="menu-icon" mode="aspectFit" />
            <text class="menu-label">{{ item.label }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const startDate = new Date('2012-01-01')

const daysTogether = computed(() => {
  const now = new Date()
  const diff = now.getTime() - startDate.getTime()
  return Math.floor(diff / (1000 * 60 * 60 * 24))
})

const roleText = computed(() => {
  return `志同道合，同行${daysTogether.value}天`
})

const menuItems = [
  { label: '账户设置', icon: 'settings' },
  { label: '个人测评', icon: 'assess' },
  { label: '账单导出', icon: 'bill' },
  { label: '关于我们', icon: 'about' },
  { label: '反馈意见', icon: 'feedback' }
]

const tapMenu = (label) => {
  uni.showToast({ title: label, icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
}

.scroll {
  height: 100vh;
}

.content {
  padding: 32rpx 32rpx 170rpx;
}

.profile-head {
  padding: 28rpx 0 64rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-avatar-wrap {
  position: relative;
}

.profile-avatar {
  width: 164rpx;
  height: 164rpx;
  border-radius: 50%;
  background: $surface-container-highest;
  border: 8rpx solid #fff;
  box-shadow: $shadow-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $on-surface;
  font-size: 28rpx;
}

.edit {
  position: absolute;
  right: -4rpx;
  bottom: 4rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: $primary;
  border: 4rpx solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18rpx;
  font-weight: 900;
}

.name {
  margin-top: 28rpx;
  color: $on-surface;
  font-size: 44rpx;
  font-weight: 900;
}

.role {
  margin-top: 4rpx;
  color: $on-surface-variant;
  font-size: 28rpx;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.menu-card {
  min-height: 96rpx;
  padding: 24rpx 28rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  filter: brightness(0) saturate(100%) invert(8%) sepia(4%) saturate(2240%) hue-rotate(120deg) brightness(100%) contrast(89%);
}

.menu-label {
  flex: 1;
  color: $on-surface;
  font-size: 30rpx;
}

.arrow {
  color: $outline;
  font-size: 42rpx;
}
</style>
