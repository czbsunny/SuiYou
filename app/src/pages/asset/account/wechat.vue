<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Wealth Hero Card -->
        <view class="wealth-card">
          <view class="card-header">
            <view class="label-row">
              <span class="card-label">{{ accountData.balanceLabel }}</span>
              <view class="visibility-btn" @tap="toggleVisibility">
                <text class="icon-visibility">{{ isVisible ? '显示' : '隐藏' }}</text>
              </view>
            </view>
          </view>
          <view class="balance-row">
            <span class="currency">¥</span>
            <h1 class="balance-amount font-mono">{{ isVisible ? formattedBalance : '****' }}</h1>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <p class="stat-label">{{ accountData.yesterdayLabel }}</p>
              <div class="stat-value-row">
                <span class="stat-value font-mono text-secondary">{{ isVisible ? accountData.yesterdayChange : '****' }}</span>
                <text class="icon-trend">↑</text>
              </div>
            </view>
            <view class="stat-item">
              <p class="stat-label">{{ accountData.totalLabel }}</p>
              <span class="stat-value font-mono text-secondary">{{ isVisible ? accountData.totalChange : '****' }}</span>
            </view>
          </view>
        </view>

        <!-- Quick Actions -->
        <view class="quick-actions">
          <button 
            v-for="action in quickActions" 
            :key="action.id" 
            class="action-item"
            @tap="handleAction(action.id)"
          >
            <view class="action-icon-wrap">
              <text class="action-icon">{{ action.icon }}</text>
            </view>
            <span class="action-label">{{ action.label }}</span>
          </button>
        </view>

        <!-- Asset List Section -->
        <view class="section">
          <view class="section-header">
            <span class="section-title">资产列表</span>
            <view class="add-btn" @tap="handleAddAsset">
              <text class="icon-add">+</text>
            </view>
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
                <span class="asset-name">{{ asset.name }}</span>
                <span class="asset-desc">{{ asset.desc }}</span>
              </view>
              <view class="asset-right">
                <span class="asset-amount font-mono">{{ isVisible ? asset.amount : '****' }}</span>
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
  institutionName: 'Heritage Hearth',
  balanceLabel: '我的钱包',
  totalBalance: 862490.52,
  yesterdayLabel: '昨日收益',
  yesterdayChange: '+124.80',
  totalLabel: '累计收益',
  totalChange: '+42,931.15',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=mature%20professional%20man%20portrait%20warm%20lighting&image_size=square'
})

const quickActions = ref([
  { id: 'withdraw', icon: '提', label: '提现' },
  { id: 'recharge', icon: '充', label: '充值' },
  { id: 'wealth', icon: '理', label: '理财' },
  { id: 'statement', icon: '账', label: '账单' }
])

const assetList = ref([
  { id: '1', icon: '零', name: '零钱', desc: '可用余额', amount: '12,490.52', bgColor: '#EFF6FF', iconColor: '#2A806C' },
  { id: '2', icon: '通', name: '零钱通', desc: '收益稳健', amount: '250,000.00', bgColor: '#FFF7ED', iconColor: '#b7102a' },
  { id: '3', icon: '基', name: '基金', desc: '长期稳健增值', amount: '500,000.00', bgColor: '#ECFDF5', iconColor: '#006754' },
  { id: '4', icon: '金', name: '黄金', desc: '实物黄金', amount: '100,000.00', bgColor: '#FFFBEB', iconColor: '#705624' },
  { id: '5', icon: '微', name: '微粒贷', desc: '额度可用', amount: '可借 50,000', bgColor: '#ECFEFF', iconColor: '#0D9488' }
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
  const actionNames = {
    withdraw: '提现',
    recharge: '充值',
    wealth: '理财',
    statement: '账单'
  }
  uni.showToast({ title: `${actionNames[actionId]}功能开发中`, icon: 'none' })
}

const handleAddAsset = () => {
  uni.showToast({ title: '添加资产功能开发中', icon: 'none' })
}

const handleAssetTap = (asset) => {
  uni.showToast({ title: `${asset.name}详情开发中`, icon: 'none' })
}

onLoad((options) => {
  if (options?.institutionName) {
    accountData.value.institutionName = decodeURIComponent(options.institutionName)
  }
  if (options?.balance) {
    accountData.value.totalBalance = parseFloat(options.balance) || 862490.52
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
  margin-bottom: $spacing-4;
}

.label-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: 900;
  color: $outline;
  letter-spacing: 1rpx;
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
  font-size: 28rpx;
  color: $on-surface-variant;
}

.balance-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: $spacing-6;
}

.currency {
  font-size: $font-size-headline-md;
  font-weight: 900;
  color: $on-surface;
}

.balance-amount {
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
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $outline;
  letter-spacing: 0;
  margin-bottom: $spacing-1;
}

.stat-value-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $secondary;
}

.text-secondary {
  color: $secondary;
}

.icon-trend {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
  color: $secondary;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-6;
  padding: 0 $spacing-4;
  margin-top: $spacing-6;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
  background: transparent;
  padding: 0;
  margin: 0;
}

.action-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  background: #fff;
  border-radius: $rounded-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-sm;
}

.action-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 44rpx;
  color: #2a806c;
}

.action-label {
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.section {
  margin-top: $spacing-6;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-4;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.add-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-add {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
  color: $primary;
}

.asset-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.asset-item {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
}

.asset-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-4;
}

.asset-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.asset-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.asset-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.asset-desc {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  margin-top: 4rpx;
}

.asset-right {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.asset-amount {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.icon-chevron {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
  color: $outline-variant;
}
</style>