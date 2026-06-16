<template>
  <view class="page budget-page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Top App Bar -->
        <view class="header">
          <view class="header-left">
            <view class="avatar-wrapper">
              <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBqKq5tUkPQkoSKNvzT1erP0x7TnHty8Cs_Ugc8DCrKOGvz9rVFw5JQmclcUqnSDHtTjF5P0dQUmRZaen-2qEZFF0FwJt1EJ4CWsXuX5MRUj1tg97WnRkPUnRDHPhOin-T8tW9SEMc3_0_PTozZf5BvsfZ9nIy7snPOIxrv9SUjXITqebHEfWZtc3bNqFEaWAPUyrS8KShoWkJsf6iBoE44XQIUWmhDrVkpzvzWd7hC29l3AhshPjPtw_IBB34EcBKnMPqjHw_Zkgun" mode="aspectFill" />
            </view>
            <text class="app-name">Stewardship</text>
          </view>
          <text class="header-title">预算</text>
          <view class="header-right">
            <view class="notification-btn" @tap="handleNotification">
              <text class="icon">notifications</text>
            </view>
          </view>
        </view>

        <!-- Portfolio Hero / Total Budget Ring -->
        <view class="hero-card">
          <view class="ring-container">
            <view class="ring-wrapper">
              <canvas canvas-id="progressRing" class="progress-ring" id="progressRing"></canvas>
              <view class="ring-center">
                <text class="ring-label">剩余可用</text>
                <view class="ring-amount">
                  <text class="currency">¥</text>
                  <text class="amount-num">12,480</text>
                </view>
                <text class="ring-total">总预算 ¥45,000</text>
              </view>
            </view>
          </view>
          <view class="stats-row">
            <view class="stat-card">
              <text class="stat-label">本月支出</text>
              <text class="stat-value">¥32,520</text>
            </view>
            <view class="stat-card">
              <text class="stat-label">预算进度</text>
              <text class="stat-value primary">72%</text>
            </view>
          </view>
        </view>

        <!-- Category Budget List -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">支出分类</text>
            <view class="settings-btn" @tap="handleSettings">
              <text class="icon">settings</text>
            </view>
          </view>

          <view class="category-list">
            <view v-for="item in categories" :key="item.name" class="category-card">
              <view class="category-header">
                <view class="category-icon-wrapper" :class="item.colorClass">
                  <text class="category-icon">{{ item.icon }}</text>
                </view>
                <view class="category-info">
                  <text class="category-name">{{ item.name }}</text>
                  <text class="category-remaining" :class="{ error: item.isOverBudget }">{{ item.remainingText }}</text>
                </view>
                <view class="category-amount">
                  <text class="amount" :class="{ secondary: item.isOverBudget }">¥{{ item.amount }}</text>
                  <text class="limit">限额 ¥{{ item.limit }}</text>
                </view>
              </view>
              <view class="progress-bar">
                <view class="progress-fill" :class="{ secondary: item.isOverBudget }" :style="{ width: item.progress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation Bar -->
    <view class="bottom-nav">
      <view class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <text class="nav-label">首页</text>
      </view>
      <view class="nav-item active" @tap="navigateTo('goal')">
        <text class="nav-icon">target</text>
        <text class="nav-label">目标</text>
      </view>
      <view class="nav-item" @tap="navigateTo('asset')">
        <text class="nav-icon">account_balance_wallet</text>
        <text class="nav-label">资产</text>
      </view>
      <view class="nav-item" @tap="navigateTo('profile')">
        <text class="nav-icon">person</text>
        <text class="nav-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const categories = ref([
  {
    name: '餐饮美食',
    icon: 'restaurant',
    amount: '4,800',
    limit: '6,000',
    progress: 80,
    remainingText: '剩余 ¥1,200',
    colorClass: 'primary',
    isOverBudget: false
  },
  {
    name: '交通出行',
    icon: 'directions_car',
    amount: '1,650',
    limit: '2,500',
    progress: 66,
    remainingText: '剩余 ¥850',
    colorClass: 'primary',
    isOverBudget: false
  },
  {
    name: '购物消费',
    icon: 'shopping_bag',
    amount: '3,230',
    limit: '3,000',
    progress: 100,
    remainingText: '超出 ¥230',
    colorClass: 'secondary',
    isOverBudget: true
  },
  {
    name: '居住物业',
    icon: 'home_work',
    amount: '12,500',
    limit: '20,000',
    progress: 62.5,
    remainingText: '剩余 ¥7,500',
    colorClass: 'primary',
    isOverBudget: false
  }
])

const handleNotification = () => {
  uni.showToast({ title: '通知', icon: 'none' })
}

const handleSettings = () => {
  uni.showToast({ title: '设置', icon: 'none' })
}

const navigateTo = (page) => {
  const pages = {
    home: '/pages/home/index',
    goal: '/pages/goal/index',
    asset: '/pages/asset/index',
    profile: '/pages/profile/index'
  }
  uni.navigateTo({ url: pages[page] })
}

onMounted(() => {
  drawProgressRing()
})

const drawProgressRing = () => {
  const ctx = uni.createCanvasContext('progressRing')
  const centerX = 100
  const centerY = 100
  const radius = 80
  const lineWidth = 8
  const progress = 0.72
  const circumference = 2 * Math.PI * radius

  ctx.setStrokeStyle('#e3e2e0')
  ctx.setLineWidth(lineWidth)
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI)
  ctx.stroke()

  ctx.setStrokeStyle('#006754')
  ctx.setLineWidth(lineWidth)
  ctx.setLineCap('round')
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius, -Math.PI / 2, -Math.PI / 2 + progress * 2 * Math.PI)
  ctx.stroke()

  ctx.draw()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
  padding-bottom: 140rpx;
}

.scroll {
  height: 100vh;
}

.content {
  padding: 0 $container-padding;
}

.header {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $container-padding;
  background: rgba($background, 0.8);
  backdrop-filter: blur(20rpx);
}

.header-left {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
}

.avatar-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-full;
  border: 4rpx solid #C5A36A;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
}

.app-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.header-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: $font-size-headline-md;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.header-right {
  display: flex;
  align-items: center;
}

.notification-btn,
.settings-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $rounded-full;
  background: $surface-container-low;
}

.icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $primary;
}

.hero-card {
  margin-top: $stack-gap-md;
  padding: $spacing-6;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
}

.ring-container {
  display: flex;
  justify-content: center;
}

.ring-wrapper {
  position: relative;
  width: 320rpx;
  height: 320rpx;
}

.progress-ring {
  width: 100%;
  height: 100%;
}

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ring-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.ring-amount {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.currency {
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $primary;
}

.amount-num {
  font-family: $font-family-mono;
  font-size: $font-size-display-lg;
  font-weight: $font-weight-semibold;
  color: $primary;
}

.ring-total {
  font-size: $font-size-body-sm;
  color: $outline;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-md;
  margin-top: $section-margin;
}

.stat-card {
  padding: $spacing-4;
  border-radius: $rounded-lg;
  background: $surface;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.stat-value {
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;

  &.primary {
    color: $primary;
  }
}

.section {
  margin-top: $section-margin;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $stack-gap-md;
}

.section-title {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
}

.category-card {
  padding: $spacing-5;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;
}

.category-header {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
  margin-bottom: $stack-gap-sm;
}

.category-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-full;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary {
    background: rgba($primary-container, 0.1);
    .category-icon {
      color: $primary;
    }
  }

  &.secondary {
    background: rgba($secondary-container, 0.1);
    .category-icon {
      color: $secondary;
    }
  }
}

.category-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
}

.category-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.category-remaining {
  margin-top: 4rpx;
  font-size: $font-size-body-sm;
  color: $outline;

  &.error {
    color: $error;
  }
}

.category-amount {
  text-align: right;
}

.amount {
  display: block;
  font-family: $font-family-mono;
  font-size: $font-size-num-data;
  font-weight: $font-weight-medium;
  color: $on-surface;

  &.secondary {
    color: $secondary;
  }
}

.limit {
  font-size: $font-size-body-sm;
  color: $outline;
}

.progress-bar {
  height: 12rpx;
  border-radius: $rounded-full;
  background: $surface-container-highest;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: $rounded-full;
  background: $primary;
  transition: width 0.7s ease;

  &.secondary {
    background: $secondary;
  }
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: $spacing-3 $spacing-4;
  padding-bottom: calc($spacing-3 + env(safe-area-inset-bottom));
  background: $surface-container-lowest;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
  border-radius: $rounded-xl $rounded-xl 0 0;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-2 $spacing-4;
  color: $outline;
  transition: all 0.2s;

  &.active {
    background: $primary-container;
    border-radius: $rounded-full;
    color: $on-primary-container;
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
}

.nav-label {
  margin-top: 4rpx;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
}
</style>