<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="label-row">
            <view class="label-left">
              <text class="card-label">总资产 (CNY)</text>
              <view class="visibility-btn" @tap="toggleVisibility">
                <image class="icon-visibility" :src="isVisible ? '/static/images/visibility_off.png' : '/static/images/visibility.png'" />
              </view>
            </view>
            <view class="settings-btn" @tap="handleSettings">
              <image class="icon-settings" src="/static/images/settings.png" mode="aspectFit" />
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
              <text class="stat-label">昨日收益</text>
              <text class="stat-value font-mono text-secondary">{{ isVisible ? accountData.yesterdayChange : '****' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">可用余额</text>
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
              <image class="action-icon" :src="action.icon" />
            </view>
            <text class="action-label">{{ action.label }}</text>
          </view>
        </view>

        <!-- Asset List Section -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">资产列表</text>
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
  totalBalance: 842500.00,
  yesterdayChange: '+1,240.50',
  availableBalance: '124,000.00',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=professional%20bank%20manager%20portrait&image_size=square'
})

const quickActions = ref([
  { id: 'transfer', icon: '/static/assets/actions/transfer.png', label: '转账' },
  { id: 'quota', icon: '/static/assets/actions/quota.png', label: '专款' },
  { id: 'event', icon: '/static/assets/actions/event.png', label: '待办' },
  { id: 'bill', icon: '/static/assets/actions/bill.png', label: '收支' }
])

const assetList = ref([
  { id: '1', icon: '活', name: '活期资产', desc: '高收益现金管理', amount: '124,000.00', bgColor: 'rgba(132, 214, 190, 0.2)', iconColor: '#006754', targetPath: '/pages/asset/holding/current' },
  { id: '2', icon: '+', name: '活期+', desc: '高收益现金管理', amount: '312,500.00', bgColor: 'rgba(255, 218, 216, 0.2)', iconColor: '#b7102a', targetPath: '/pages/asset/holding/current-plus' },
  { id: '3', icon: '定', name: '定期存款', desc: '稳健理财', amount: '200,000.00', bgColor: 'rgba(255, 222, 170, 0.2)', iconColor: '#705624', targetPath: '/pages/asset/holding/deposit' },
  { id: '4', icon: '基', name: '基金', desc: '长期稳健增值', amount: '206,000.00', bgColor: 'rgba(132, 214, 190, 0.2)', iconColor: '#006754', targetPath: '/pages/asset/holding/fund' },
  { id: '5', icon: '贷', name: '贷款', desc: '待还贷款', amount: '1,286,450.00', bgColor: 'rgba(239, 68, 68, 0.1)', iconColor: '#DC2626', targetPath: '/pages/asset/holding/loan' }
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

const currentAccountType = ref('bank')

const handleSettings = () => {
  uni.navigateTo({
    url: `/pages/asset/account/edit?accountType=${currentAccountType.value}&institutionName=${encodeURIComponent(accountData.value.institutionName || '')}`
  })
}

const handleAction = (actionId) => {
  const actionNames = {
    transfer: '转账',
    quota: '专款',
    event: '待办',
    bill: '收支'
  }
  uni.showToast({ title: `${actionNames[actionId]}功能开发中`, icon: 'none' })
}

const handleAssetTap = (asset) => {
  if (asset.targetPath) {
    uni.navigateTo({
      url: `${asset.targetPath}?data=${encodeURIComponent(JSON.stringify({
        id: asset.id,
        name: asset.name,
        availableBalance: parseFloat(String(asset.amount).replace(/,/g, '')) || 0
      }))}`,
      fail: () => {
        uni.showToast({ title: `${asset.name}详情开发中`, icon: 'none' })
      }
    })
  } else {
    uni.showToast({ title: `${asset.name}详情开发中`, icon: 'none' })
  }
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
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-2;
}

.label-left {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.settings-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    opacity: 0.7;
  }
}

.icon-settings {
  width: 40rpx;
  height: 40rpx;
}

.card-label {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
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
  width: 32rpx;
  height: 32rpx;
}

.balance-row {
  margin-top: $stack-gap-xs;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: $stack-gap-sm;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: 900;
}

.balance-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-grid {
  margin-top: $section-margin;
  display: flex;
  gap: $spacing-5;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-label {
  color: $outline;
  font-size: $font-size-body-sm;
}

.stat-value {
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: 600;
}

.text-secondary {
  color: $secondary;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $stack-gap-md;
  margin-top: $stack-gap-md;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
}

.action-icon-wrap {
  width: 112rpx;
  height: 112rpx;
  background: #fff;
  border-radius: $rounded-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-sm;
}

.action-icon {
  width: 56rpx;
  height: 56rpx;
}

.action-label {
  font-size: $font-size-xs;
  font-weight: 700;
  color: $on-surface-variant;
  letter-spacing: 0.5rpx;
}

.section {
  margin-top: $stack-gap-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-3;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: 900;
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