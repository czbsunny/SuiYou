<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Wealth Hero Card -->
        <view class="wealth-card">
          <view class="card-header">
            <text class="card-label">{{ accountData.balanceLabel }}</text>
            <view class="visibility-btn" @tap="toggleVisibility">
              <text class="icon-visibility">{{ isVisible ? '显示' : '隐藏' }}</text>
            </view>
          </view>
          <view class="balance-row">
            <text class="balance-amount">
              <text class="currency-symbol">¥</text><text class="balance-value font-mono">{{ isVisible ? formattedBalance : '****' }}</text>
            </text>
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
                <text class="icon-chevron">›</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
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
  { id: 'buy', icon: '买', label: '买入', bgClass: 'bg-secondary', iconClass: 'text-secondary' },
  { id: 'sell', icon: '卖', label: '卖出', bgClass: 'bg-primary', iconClass: 'text-primary' },
  { id: 'transfer', icon: '转', label: '转账', bgClass: 'bg-gray', iconClass: 'text-gray' },
  { id: 'query', icon: '查', label: '查询', bgClass: 'bg-gray', iconClass: 'text-gray' }
])

const assetList = ref([
  { id: '1', type: 'stock', icon: '股', name: '股票', amount: '¥3,250,000.00', bgColor: 'rgba(183, 16, 42, 0.1)', iconColor: '#b7102a' },
  { id: '2', type: 'fund', icon: '基', name: '基金', amount: '¥742,050.00', bgColor: 'rgba(0, 103, 84, 0.1)', iconColor: '#006754' }
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

const handleAction = (actionId) => {
  const paths = {
    buy: '/pages/trade/stock?type=buy',
    sell: '/pages/trade/stock?type=sell',
    transfer: '/pages/trade/transfer',
    query: '/pages/trade/query'
  }
  if (paths[actionId]) {
    uni.navigateTo({ url: paths[actionId] })
  } else {
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}

const handleAssetTap = (asset) => {
  const paths = {
    stock: '/pages/asset/holding/stock',
    fund: '/pages/asset/holding/fund'
  }
  console.log(asset.type)
  const url = paths[asset.type]
  if (url) {
    uni.navigateTo({ url })
  } else {
    uni.showToast({ title: `${asset.name}详情开发中`, icon: 'none' })
  }
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
  padding: $spacing-4 $spacing-4 170rpx;
}

.wealth-card {
  padding: $spacing-6 $spacing-5 $spacing-4;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: $shadow-soft;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: $outline;
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
  display: inline-flex;
  align-items: baseline;
  font-size: $font-size-num-display;
  font-weight: 900;
  color: $on-surface;
  letter-spacing: -2rpx;
}

.currency-symbol {
  font-family: $font-family-primary;
  font-size: $font-size-headline-md;
  font-weight: 900;
  color: $on-surface;
}

.balance-value {
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
  color: $on-surface;
  letter-spacing: -2rpx;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-5;
  margin-top: $spacing-6;
}

.stat-item {
  &.col-span-2 {
    grid-column: span 2;
  }
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $outline;
  margin-bottom: $spacing-1;
}

.stat-value {
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $secondary;
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
  margin-top: $spacing-8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-3;
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
</style>