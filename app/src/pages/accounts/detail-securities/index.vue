<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Top App Bar -->
        <view class="top-bar">
          <view class="top-bar-left">
            <view class="back-btn" @tap="handleBack">
              <text class="icon-arrow">arrow_back</text>
            </view>
            <text class="page-title">{{ accountData.institutionName }}</text>
          </view>
          <view class="top-bar-right">
            <view class="notification-btn" @tap="handleNotification">
              <text class="icon-notification">notifications</text>
            </view>
            <view class="avatar-wrap">
              <image class="avatar" :src="accountData.avatarUrl" mode="aspectFill" />
            </view>
          </view>
        </view>

        <!-- Wealth Hero Card -->
        <view class="wealth-card">
          <view class="card-header">
            <text class="card-label">{{ accountData.balanceLabel }}</text>
            <view class="visibility-btn" @tap="toggleVisibility">
              <text class="icon-visibility">{{ isVisible ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>
          <view class="balance-row">
            <text class="balance-amount font-mono">¥{{ isVisible ? formattedBalance : '****' }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">{{ accountData.todayLabel }}</text>
              <text class="stat-value font-mono text-profit">{{ isVisible ? accountData.todayChange : '****' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">{{ accountData.totalLabel }}</text>
              <text class="stat-value font-mono text-profit">{{ isVisible ? accountData.totalChange : '****' }}</text>
            </view>
            <view class="stat-item col-span-2">
              <text class="stat-label">{{ accountData.availableLabel }}</text>
              <text class="stat-value font-mono">{{ isVisible ? accountData.availableBalance : '****' }}</text>
            </view>
          </view>
        </view>

        <!-- Quick Actions -->
        <view class="quick-actions">
          <view 
            v-for="action in quickActions" 
            :key="action.id" 
            class="action-item"
            @tap="handleAction(action.id)"
          >
            <view class="action-icon-wrap" :class="action.bgClass">
              <text class="action-icon" :class="action.iconClass">{{ action.icon }}</text>
            </view>
            <text class="action-label">{{ action.label }}</text>
          </view>
        </view>

        <!-- Asset List Section -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">持仓明细</text>
          </view>
          <view class="asset-list">
            <view 
              v-for="asset in assetList" 
              :key="asset.id" 
              class="asset-item"
              @tap="handleAssetTap(asset)"
            >
              <view class="asset-icon-wrap" :style="{ background: asset.bgColor }">
                <text class="asset-icon" :style="{ color: asset.iconColor }">{{ asset.icon }}</text>
              </view>
              <view class="asset-info">
                <text class="asset-name">{{ asset.name }}</text>
              </view>
              <view class="asset-right">
                <view class="asset-amount-wrap">
                  <text class="asset-amount font-mono">{{ isVisible ? asset.amount : '****' }}</text>
                </view>
                <text class="icon-chevron">chevron_right</text>
              </view>
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
      <view class="nav-item" @tap="navigateTo('goals')">
        <text class="nav-icon">track_changes</text>
        <text class="nav-label">目标</text>
      </view>
      <view class="nav-item active">
        <text class="nav-icon" style="font-variation-settings: 'FILL' 1;">account_balance_wallet</text>
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const isVisible = ref(true)

const accountData = ref({
  institutionName: '中信证券',
  balanceLabel: '总资产 (CNY)',
  totalBalance: 4120500.00,
  todayLabel: '今日盈亏',
  todayChange: '+12,400.50',
  totalLabel: '累计盈亏',
  totalChange: '+450,210.00',
  availableLabel: '可用余额',
  availableBalance: '128,450.00',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=professional%20stock%20trader%20portrait&image_size=square'
})

const quickActions = ref([
  { id: 'buy', icon: 'add_circle', label: '买入', bgClass: 'bg-secondary', iconClass: 'text-secondary' },
  { id: 'sell', icon: 'do_not_disturb_on', label: '卖出', bgClass: 'bg-primary', iconClass: 'text-primary' },
  { id: 'transfer', icon: 'sync_alt', label: '转账', bgClass: 'bg-gray', iconClass: 'text-gray' },
  { id: 'query', icon: 'search', label: '查询', bgClass: 'bg-gray', iconClass: 'text-gray' }
])

const assetList = ref([
  { id: '1', icon: 'show_chart', name: '股票', amount: '¥3,250,000.00', bgColor: 'rgba(183, 16, 42, 0.1)', iconColor: '#b7102a' },
  { id: '2', icon: 'finance', name: '基金', amount: '¥742,050.00', bgColor: 'rgba(0, 103, 84, 0.1)', iconColor: '#006754' }
])

const formattedBalance = computed(() => {
  return accountData.value.totalBalance.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
})

const toggleVisibility = () => {
  isVisible.value = !isVisible.value
}

const handleBack = () => {
  uni.navigateBack()
}

const handleNotification = () => {
  uni.showToast({ title: '暂无新通知', icon: 'none' })
}

const handleAction = (actionId) => {
  const actionNames = {
    buy: '买入',
    sell: '卖出',
    transfer: '转账',
    query: '查询'
  }
  const paths = {
    buy: '/pages/accounts/stock-buy/index',
    sell: '/pages/accounts/stock-sell/index',
    transfer: '/pages/accounts/transfer/index',
    query: '/pages/accounts/transaction-query/index'
  }
  if (paths[actionId]) {
    uni.navigateTo({ url: paths[actionId] })
  } else {
    uni.showToast({ title: `${actionNames[actionId]}功能开发中`, icon: 'none' })
  }
}

const handleAssetTap = (asset) => {
  uni.showToast({ title: `${asset.name}详情开发中`, icon: 'none' })
}

const navigateTo = (page) => {
  const paths = {
    home: '/pages/home/index',
    goals: '/pages/goals/index',
    assets: '/pages/assets/index',
    profile: '/pages/profile/index'
  }
  uni.switchTab({ url: paths[page] })
}

onLoad((options) => {
  if (options?.institutionName) {
    accountData.value.institutionName = decodeURIComponent(options.institutionName)
  }
  if (options?.balance) {
    accountData.value.totalBalance = parseFloat(options.balance) || 4120500.00
  }
})
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
  padding-bottom: 180rpx;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $background;
  padding: 60rpx $spacing-4 $spacing-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-arrow {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $on-surface;
}

.page-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.notification-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-notification {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $on-surface-variant;
}

.avatar-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  overflow: hidden;
  background: $outline-variant;
}

.avatar {
  width: 100%;
  height: 100%;
}

.wealth-card {
  margin: $spacing-4;
  padding: $spacing-6;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  box-shadow: $shadow-soft;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-medium;
  color: $on-surface-variant;
}

.visibility-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-visibility {
  font-family: 'Material Symbols Outlined';
  font-size: 32rpx;
  color: $on-surface-variant;
}

.balance-row {
  margin-top: $spacing-4;
}

.balance-amount {
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  letter-spacing: -2rpx;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-6;
  margin-top: $spacing-6;
  padding-top: $spacing-6;
  border-top: 2rpx solid $surface-container;
}

.stat-item {
  &.col-span-2 {
    grid-column: span 2;
  }
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  margin-bottom: $spacing-1;
}

.stat-value {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.text-profit {
  color: $secondary;
}

.text-primary {
  color: $primary;
}

.text-gray {
  color: $on-surface-variant;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-4;
  padding: 0 $spacing-4;
  margin-top: $spacing-6;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
}

.action-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: $rounded-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-sm;

  &.bg-secondary {
    background: rgba($secondary, 0.1);
  }
  &.bg-primary {
    background: rgba($primary, 0.1);
  }
  &.bg-gray {
    background: $surface-container-lowest;
  }
}

.action-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;

  &.text-secondary {
    color: $secondary;
  }
  &.text-primary {
    color: $primary;
  }
  &.text-gray {
    color: $on-surface-variant;
  }
}

.action-label {
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.section {
  padding: $spacing-4;
  margin-top: $spacing-8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-4;
}

.section-title {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.asset-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.asset-item {
  display: flex;
  align-items: center;
  padding: $spacing-5;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  box-shadow: $shadow-sm;
}

.asset-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.asset-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
}

.asset-info {
  flex: 1;
  padding: 0 $spacing-4;
}

.asset-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.asset-right {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.asset-amount-wrap {
  text-align: right;
}

.asset-amount {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.icon-chevron {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
  color: $outline-variant;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: $surface-container-lowest;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.04);
  border-radius: $rounded-lg $rounded-lg 0 0;
  padding: $spacing-3 $spacing-4;
  padding-bottom: calc(#{$spacing-3} + env(safe-area-inset-bottom));
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  color: $on-surface-variant;

  &.active {
    color: $primary;
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.nav-label {
  font-family: $font-family-mono;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
}
</style>