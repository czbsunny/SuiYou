<template>
  <view class="page saving-page">
    <!-- Top Navigation -->
    <view class="header">
      <view class="header-left">
        <view class="avatar-wrapper">
          <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuA2x_Ryn73VLixmuopsZLxgAeNkhYOy39eKWCL9boucQ5hthfF8WncM6mmYtvle9jGzz8Hv9cJfKKuMII2zAaiKzvlcInXeqh4iu060WPABzTgmRhkg0bQwJpFq8CqO-xLJIWbUuuaVPyF-yTznXtJHqmjD5Sj38Sa_J9wECZ6SnB45f7uMCyDwEqyj5KjYSCCDYu9EmJV-l0bpQgkpjNaW8pl8Gbpv37gtKCuXz2_Lk_rpphKsD6aAnvkKNx5INAQeQFWGSTxLTFV3" mode="aspectFill" />
        </view>
        <text class="app-name">Stewardship</text>
      </view>
      <view class="header-right">
        <view class="notification-btn" @tap="handleNotification">
          <text class="icon">notifications</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Total Savings Hero -->
        <view class="hero-card">
          <view class="hero-bg"></view>
          <view class="hero-content">
            <text class="hero-label">储蓄总额</text>
            <view class="hero-amount">
              <text class="currency">¥</text>
              <text class="amount-num" id="total-amount">128,046.50</text>
            </view>
            <view class="hero-stats">
              <view class="stat-item">
                <text class="stat-label">本月新增</text>
                <text class="stat-value">+ ¥4,200.00</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">预计达标</text>
                <text class="stat-value tertiary">12月</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Smart Save Suggestion -->
        <view class="suggestion-card" @tap="handleSuggestion">
          <view class="suggestion-content">
            <view class="suggestion-icon">
              <text class="icon">lightbulb</text>
            </view>
            <view class="suggestion-text">
              <text class="suggestion-title">智能建议</text>
              <text class="suggestion-desc">由于上周餐饮支出减少，建议将多出的 ¥350 转入"夏季旅行"。</text>
            </view>
          </view>
          <view class="suggestion-action">
            <text>立即存入</text>
          </view>
        </view>

        <!-- Savings Goals -->
        <view class="goals-section">
          <view class="section-header">
            <text class="section-title">蓄水池计划</text>
            <view class="add-btn" @tap="handleAddGoal">
              <text class="icon">add</text>
              <text>新增目标</text>
            </view>
          </view>

          <view class="goals-list">
            <view v-for="goal in goals" :key="goal.name" class="goal-card" :class="{ disabled: goal.disabled }">
              <view class="goal-header">
                <view class="goal-icon-wrapper" :class="goal.colorClass">
                  <text class="goal-icon">{{ goal.icon }}</text>
                </view>
                <view class="goal-info">
                  <text class="goal-name">{{ goal.name }}</text>
                  <text class="goal-target">目标: ¥{{ goal.target }}</text>
                </view>
                <text class="goal-progress">{{ goal.progress }}%</text>
              </view>
              <view class="goal-progress-bar">
                <view class="progress-fill" :class="goal.colorClass" :style="{ width: goal.progress + '%' }"></view>
              </view>
              <view class="goal-footer">
                <view class="goal-members" v-if="goal.members">
                  <image v-for="(member, index) in goal.members" :key="index" class="member-avatar" :src="member" mode="aspectFill" />
                  <view class="member-add">
                    <text>+1</text>
                  </view>
                </view>
                <text v-else class="goal-remaining">{{ goal.remaining }}</text>
                <view class="goal-action" @tap="handleDeposit(goal)">
                  <text>存入</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- Asymmetric Decorative Card -->
        <view class="decorative-section">
          <view class="decorative-card savings-card">
            <text class="card-icon">savings</text>
            <view class="card-content">
              <text class="card-title">小猪存钱罐</text>
              <text class="card-subtitle">本周达成度 120%</text>
            </view>
          </view>
          <view class="decorative-card image-card" @tap="handleArticle">
            <image class="card-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBh_msBHGkO2ZYWTrBbhaXa7L8dXs37m5NX-W4FFtQOVFNB5IptDZTk8dJzSB4k0o7_nIPJ6NrPBScJN2r0dikUKoMy0esjoUd4dyzWNlkJdtItpuZ7jc7xkJTS59RQqc-grwu7FjZ65NX66rAmazy-A-lURnF3aGPlSqDd1kd3BXHWiO4E-MjSvR68hpVJM33C2Mhy5QdKpXRkFIUfHiR9irV_pT-KHheachPgmyoPXVLTVGvBjEfE4rofFuNVle7venmBgFCxS1Um" mode="aspectFill" />
            <view class="card-overlay">
              <text class="overlay-label">理财周报</text>
              <text class="overlay-title">如何更快达成目标</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <text class="nav-label">首页</text>
      </view>
      <view class="nav-item active">
        <text class="nav-icon" style="font-variation-settings: 'FILL' 1">target</text>
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

const goals = ref([
  {
    name: '夏季旅行',
    icon: 'beach_access',
    target: '15,000',
    progress: 80,
    remaining: '还差 ¥3,000',
    colorClass: 'primary',
    disabled: false,
    members: [
      'https://lh3.googleusercontent.com/aida-public/AB6AXuC-QdMF7FkXT_BBluQINYyyGn1a7lNQ7QNLUvt99lIcXh33iqGhG7cAAEBsh6u6mylJ2ZgpdoPUc18ZpCiq6DpDU6Jx31q-LQTCAQkIbN8aZWnpvWwAsEfeEn-gRYV1ouHnjwRE8VtD9a_JsylGv_YQo4RcxbmfpTdjSubb_dtiVqYGgIS1aJvrCcIgM5D_6V1acSBbz9LYSuNqf7j_ha9svJnCy4YcRQe2elz9isSidQoDyGuD2FZAAXYaoOlPAf0QHOyxOUymrjkD',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuApOZJAytzv_wZIhi3yGyUInAsbb5cUPlbqY_bLxy5XZz6MUs1KXuzKqoUd66jpgMFEuhOh3hj_7r8dPSZWxBTm5skfVpA5vox4QcV1ib3xK44WwIQe7dZIF0j3iDbA8ADFpeTs9MFt9q0FExiqfDAO5rGBp0cG6NTeMibDk_uQj-DAgwW-zdX3B2tubh0Cc3GkPdTwHZ2oF-IYHuFAzRC2EShPlkTSSNkAcfENjbh85nTJPtZvEV2ch7Z3ih0zFkD7XLjvitNGq9W-'
    ]
  },
  {
    name: '新冰箱',
    icon: 'kitchen',
    target: '8,000',
    progress: 35,
    remaining: '还差 ¥5,200',
    colorClass: 'tertiary',
    disabled: false,
    members: null
  },
  {
    name: '备用基金',
    icon: 'home_repair_service',
    target: '50,000',
    progress: 12,
    remaining: '定期存入中',
    colorClass: 'outline',
    disabled: true,
    members: null
  }
])

const handleNotification = () => {
  uni.showToast({ title: '通知', icon: 'none' })
}

const handleSuggestion = () => {
  uni.showToast({ title: '转入夏季旅行', icon: 'none' })
}

const handleAddGoal = () => {
  uni.showToast({ title: '新增目标', icon: 'none' })
}

const handleDeposit = (goal) => {
  uni.showToast({ title: `存入 ${goal.name}`, icon: 'none' })
}

const handleArticle = () => {
  uni.showToast({ title: '打开理财周报', icon: 'none' })
}

const navigateTo = (page) => {
  const pages = {
    home: '/pages/home/index',
    asset: '/pages/asset/index',
    profile: '/pages/profile/index'
  }
  uni.navigateTo({ url: pages[page] })
}

onMounted(() => {
  animateAmount()
})

const animateAmount = () => {
  const amountEl = document.getElementById('total-amount')
  if (!amountEl) return
  
  const targetAmount = 128046.50
  let currentAmount = 128000.00
  
  const timer = setInterval(() => {
    if (currentAmount >= targetAmount) {
      clearInterval(timer)
      amountEl.innerText = targetAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
    } else {
      currentAmount += 15.5
      amountEl.innerText = currentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
    }
  }, 10)
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
  padding: $spacing-4 $container-padding;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $container-padding;
  background: $background;
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

.header-right {
  display: flex;
  align-items: center;
}

.notification-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $primary;
}

.hero-card {
  position: relative;
  margin-bottom: $stack-gap-lg;
  padding: $spacing-8;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: -80rpx;
  right: -80rpx;
  width: 320rpx;
  height: 320rpx;
  background: rgba($primary, 0.05);
  border-radius: $rounded-full;
  filter: blur(60rpx);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.hero-amount {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: $spacing-2;
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
  color: $on-surface;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-md;
  margin-top: $section-margin;
}

.stat-item {
  padding: $spacing-4;
  border-radius: $rounded-xl;
  background: $surface-container-low;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-medium;
  color: $on-surface-variant;
}

.stat-value {
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-num-data;
  font-weight: $font-weight-medium;
  color: $primary;

  &.tertiary {
    color: $tertiary;
  }
}

.suggestion-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-6;
  border-radius: $rounded-lg;
  background: $primary-container;
  color: $on-primary-container;
  margin-bottom: $stack-gap-lg;
}

.suggestion-content {
  display: flex;
  align-items: flex-start;
  gap: $stack-gap-md;
  flex: 1;
}

.suggestion-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  .icon {
    font-size: 40rpx;
  }
}

.suggestion-text {
  display: flex;
  flex-direction: column;
}

.suggestion-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
}

.suggestion-desc {
  margin-top: $spacing-1;
  font-size: $font-size-body-sm;
  opacity: 0.9;
}

.suggestion-action {
  padding: $spacing-2 $spacing-4;
  border-radius: $rounded-full;
  background: $surface-container-lowest;
  color: $primary;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
}

.goals-section {
  margin-bottom: $section-margin;
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

.add-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  color: $primary;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;

  .icon {
    font-size: 36rpx;
  }
}

.goals-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
}

.goal-card {
  padding: $spacing-5;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;
  border: 1rpx solid $surface-container-low;

  &.disabled {
    opacity: 0.6;
  }
}

.goal-header {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
  margin-bottom: $stack-gap-md;
}

.goal-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: $rounded-xl;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary {
    background: rgba($primary, 0.1);
    .goal-icon {
      color: $primary;
    }
  }

  &.tertiary {
    background: rgba($tertiary, 0.1);
    .goal-icon {
      color: $tertiary;
    }
  }

  &.outline {
    background: rgba($outline-variant, 0.2);
    .goal-icon {
      color: $outline;
    }
  }
}

.goal-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 56rpx;
}

.goal-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goal-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.goal-target {
  margin-top: 4rpx;
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.goal-progress {
  font-family: $font-family-mono;
  font-size: $font-size-num-data;
  font-weight: $font-weight-medium;
  color: $primary;
}

.goal-progress-bar {
  height: 16rpx;
  border-radius: $rounded-full;
  background: $surface-container;
  overflow: hidden;
  margin-bottom: $section-margin;
}

.progress-fill {
  height: 100%;
  border-radius: $rounded-full;
  transition: width 1s ease;

  &.primary {
    background: $primary;
  }

  &.tertiary {
    background: $tertiary;
  }

  &.outline {
    background: $outline;
  }
}

.goal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goal-members {
  display: flex;
  align-items: center;
  gap: -8rpx;
}

.member-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: $rounded-full;
  border: 3rpx solid $surface-container-lowest;
  margin-left: -12rpx;

  &:first-child {
    margin-left: 0;
  }
}

.member-add {
  width: 48rpx;
  height: 48rpx;
  border-radius: $rounded-full;
  background: $surface-container-highest;
  border: 3rpx solid $surface-container-lowest;
  margin-left: -12rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  text {
    font-size: 16rpx;
    font-weight: $font-weight-bold;
    color: $on-surface-variant;
  }
}

.goal-remaining {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.goal-action {
  padding: $spacing-2 $spacing-6;
  border-radius: $rounded-full;
  background: $primary;
  color: $on-primary;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;

  &:active {
    opacity: 0.9;
    transform: scale(0.95);
  }
}

.decorative-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-md;
  height: 200rpx;
}

.decorative-card {
  border-radius: $rounded-lg;
  padding: $spacing-4;
}

.savings-card {
  background: rgba($secondary-container, 0.1);
  border: 1rpx solid rgba($secondary-container, 0.2);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 64rpx;
  color: $secondary;
}

.card-content {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.card-subtitle {
  font-size: 20rpx;
  color: $secondary;
  font-weight: $font-weight-bold;
}

.image-card {
  position: relative;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s ease;
}

.image-card:active .card-image {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  padding: $spacing-4;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.overlay-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: rgba(255, 255, 255, 0.8);
}

.overlay-title {
  margin-top: $spacing-1;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: #fff;
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