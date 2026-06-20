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
                <image class="icon-visibility" :src="isVisible ? '/static/images/visibility_off.png' : '/static/images/visibility.png'" />
              </view>
            </view>
          </view>
          <view class="balance-row">
            <view class="amount-wrapper">
              <text class="currency">¥</text>
              <text class="balance-amount font-mono">{{ isVisible ? formattedBalance : '****' }}</text>
            </view>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">{{ accountData.yesterdayLabel }}</text>
              <text class="stat-value">{{ isVisible ? accountData.yesterdayChange : '****' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">{{ accountData.totalLabel }}</text>
              <text class="stat-value">{{ isVisible ? accountData.totalChange : '****' }}</text>
            </view>
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
  width: 32rpx;
  height: 32rpx;
}

.balance-row {
  margin-bottom: $spacing-6;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.currency {
  font-family: $font-family-primary;
  font-size: $font-size-headline-md;
  font-weight: 900;
  color: $on-surface;
}

.balance-amount {
  font-size: $font-size-num-display;
  font-weight: 900;
  font-family: $font-family-mono;
  color: $on-surface;
  letter-spacing: -2rpx;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-grid {
  display: flex;
  gap: $spacing-5;
  padding-top: $spacing-6;
}

.stat-item {
  flex: 1;
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

.section {
  margin-top: $spacing-6;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-3;
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