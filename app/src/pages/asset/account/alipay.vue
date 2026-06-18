<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Wealth Hero Card -->
        <view class="wealth-card">
          <view class="card-header">
            <view class="label-row">
              <text class="card-label">{{ accountData.balanceLabel }}</text>
              <view class="visibility-btn" @tap="toggleVisibility">
                <text class="icon-visibility">{{ isVisible ? '显示' : '隐藏' }}</text>
              </view>
            </view>
          </view>
          <view class="balance-row">
            <text class="balance-amount font-mono">{{ isVisible ? formattedBalance : '****' }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">{{ accountData.yesterdayLabel }}</text>
              <text class="stat-value font-mono text-secondary">{{ isVisible ? accountData.yesterdayChange : '****' }}</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-label">{{ accountData.totalLabel }}</text>
              <text class="stat-value font-mono text-secondary">{{ isVisible ? accountData.totalChange : '****' }}</text>
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
            <text class="section-title">资产列表</text>
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
                <text class="asset-name">{{ asset.name }}</text>
                <text class="asset-desc">{{ asset.desc }}</text>
              </view>
              <view class="asset-right">
                <view class="asset-amount-wrap">
                  <text class="asset-amount font-mono">{{ isVisible ? asset.amount : '****' }}</text>
                  <text v-if="asset.subText" class="asset-sub" :class="asset.subClass">{{ asset.subText }}</text>
                </view>
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
  balanceLabel: '我的资产 (元)',
  totalBalance: 862450.00,
  yesterdayLabel: '昨日收益',
  yesterdayChange: '+128.45',
  totalLabel: '累计收益',
  totalChange: '+42,105.12',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=professional%20wealth%20manager%20studio%20portrait&image_size=square'
})

const quickActions = ref([
  { id: 'withdraw', icon: '提', label: '提现', bgClass: 'bg-blue', iconClass: 'text-blue-600' },
  { id: 'recharge', icon: '充', label: '充值', bgClass: 'bg-orange', iconClass: 'text-orange-600' },
  { id: 'wealth', icon: '理', label: '理财', bgClass: 'bg-green', iconClass: 'text-green-600' },
  { id: 'statement', icon: '收', label: '收支', bgClass: 'bg-purple', iconClass: 'text-purple-600' }
])

const assetList = ref([
  { id: '1', icon: '余', name: '余额', desc: '流动资金', amount: '12,450.00', subText: 'CNY', subClass: 'text-gray', bgColor: '#EFF6FF', iconColor: '#2563EB' },
  { id: '2', icon: '宝', name: '余额宝', desc: '收益稳健', amount: '256,000.00', subText: '+2.15%', subClass: 'text-secondary', bgColor: '#EFF6FF', iconColor: '#2563EB' },
  { id: '3', icon: '花', name: '花呗', desc: '下月应还', amount: '-1,240.50', subText: '10月账单', subClass: 'text-gray', bgColor: '#FFF7ED', iconColor: '#EA580C' },
  { id: '4', icon: '借', name: '借呗', desc: '额度可用', amount: '50,000.00', subText: '免息中', subClass: 'text-primary', bgColor: '#EEF2FF', iconColor: '#4F46E5' },
  { id: '5', icon: '利', name: '余利宝', desc: '企业理财', amount: '142,500.00', subText: '稳健经营', subClass: 'text-gray', bgColor: '#ECFDF5', iconColor: '#059669' },
  { id: '6', icon: '荷', name: '小荷包', desc: '共同攒钱', amount: '2,500.00', subText: '攒钱计划', subClass: 'text-pink', bgColor: '#FDF2F8', iconColor: '#DB2777' },
  { id: '7', icon: '基', name: '基金', desc: '持有3支', amount: '382,000.00', subText: '+15.2%', subClass: 'text-secondary', bgColor: '#F5F3FF', iconColor: '#7C3AED' },
  { id: '8', icon: '金', name: '黄金', desc: '实物黄金', amount: '15,000.00', subText: '32g', subClass: 'text-gray', bgColor: '#FFFBEB', iconColor: '#D97706' }
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
    statement: '收支'
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
    accountData.value.totalBalance = parseFloat(options.balance) || 862450.00
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
  padding-bottom: 40rpx;
  padding-top: $spacing-4;
}

.wealth-card {
  margin: $spacing-4;
  padding: $spacing-6;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
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
  margin-bottom: $spacing-6;
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
  display: flex;
  padding-top: $spacing-6;
  border-top: 2rpx solid $surface-container;
}

.stat-item {
  flex: 1;
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

.stat-divider {
  width: 2rpx;
  background: $outline-variant;
  margin: 0 $spacing-5;
}

.text-secondary {
  color: $secondary;
}

.text-primary {
  color: $primary;
}

.text-gray {
  color: $on-surface-variant;
}

.text-pink {
  color: #DB2777;
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
  box-shadow: $shadow-soft;

  &.bg-blue {
    background: #EFF6FF;
  }
  &.bg-orange {
    background: #FFF7ED;
  }
  &.bg-green {
    background: #ECFDF5;
  }
  &.bg-purple {
    background: #F5F3FF;
  }
}

.action-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;

  &.text-blue-600 {
    color: #2563EB;
  }
  &.text-orange-600 {
    color: #EA580C;
  }
  &.text-green-600 {
    color: #059669;
  }
  &.text-purple-600 {
    color: #7C3AED;
  }
}

.action-label {
  font-size: 24rpx;
  font-weight: $font-weight-medium;
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
  border-radius: 24rpx;
  box-shadow: $shadow-sm;
}

.asset-icon-wrap {
  width: 80rpx;
  height: 80rpx;
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
}

.asset-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.asset-desc {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  margin-top: 4rpx;
}

.asset-right {
  text-align: right;
}

.asset-amount-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.asset-amount {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.asset-sub {
  font-size: 20rpx;
  font-family: $font-family-mono;
  margin-top: 4rpx;
}
</style>