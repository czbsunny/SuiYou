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
            <text class="balance-amount font-mono">{{ isVisible ? formattedBalance : '****' }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">{{ accountData.todayLabel }}</text>
              <text class="stat-value font-mono text-secondary">{{ isVisible ? accountData.todayChange : '****' }}</text>
            </view>
            <view class="stat-item">
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
            <view class="action-icon-wrap">
              <text class="action-icon">{{ action.icon }}</text>
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
              <text class="add-text">添加</text>
              <text class="icon-drop">▼</text>
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
  institutionName: 'Construction Bank',
  balanceLabel: '总余额 (CNY)',
  totalBalance: 842500.00,
  todayLabel: '今日增长',
  todayChange: '+1,240.50',
  availableLabel: '可用余额',
  availableBalance: '124,000.00',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=professional%20bank%20manager%20portrait&image_size=square'
})

const quickActions = ref([
  { id: 'transfer', icon: '转', label: '转账' },
  { id: 'deposit', icon: '存', label: '存款' },
  { id: 'wealth', icon: '理', label: '理财' },
  { id: 'statement', icon: '收', label: '收支' }
])

const assetList = ref([
  { id: '1', icon: '活', name: '活期资产', desc: '高收益现金管理', amount: '124,000.00', bgColor: 'rgba(132, 214, 190, 0.2)', iconColor: '#006754' },
  { id: '2', icon: '+', name: '活期+', desc: '高收益现金管理', amount: '312,500.00', bgColor: 'rgba(255, 218, 216, 0.2)', iconColor: '#b7102a' },
  { id: '3', icon: '定', name: '定期存款', desc: '稳健理财', amount: '200,000.00', bgColor: 'rgba(255, 222, 170, 0.2)', iconColor: '#705624' },
  { id: '4', icon: '基', name: '基金', desc: '长期稳健增值', amount: '206,000.00', bgColor: 'rgba(132, 214, 190, 0.2)', iconColor: '#006754' }
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
    transfer: '转账',
    deposit: '存款',
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
    accountData.value.totalBalance = parseFloat(options.balance) || 842500.00
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
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
  letter-spacing: 2rpx;
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
  display: flex;
  gap: $spacing-5;
  margin-top: $spacing-6;
}

.stat-item {
  flex: 1;
}

.stat-label {
  font-size: $font-size-label-caps;
  color: $outline;
  letter-spacing: 1rpx;
}

.stat-value {
  display: block;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  margin-top: $spacing-1;
}

.text-secondary {
  color: $secondary;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
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
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-soft;
}

.action-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: $primary;
}

.action-label {
  font-size: 22rpx;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
  letter-spacing: 1rpx;
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
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: $spacing-2 $spacing-3;
  background: rgba($primary, 0.1);
  border-radius: $rounded-full;
}

.icon-add {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
  color: $primary;
}

.add-text {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $primary;
}

.icon-drop {
  font-family: 'Material Symbols Outlined';
  font-size: 28rpx;
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
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.asset-desc {
  font-size: $font-size-body-sm;
  color: $outline;
  margin-top: 4rpx;
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