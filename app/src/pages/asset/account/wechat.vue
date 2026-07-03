<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="label-row">
            <view class="label-left">
              <text class="card-label">我的钱包</text>
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
              <text class="stat-label">累计收益</text>
              <text class="stat-value font-mono">{{ isVisible ? accountData.totalChange : '****' }}</text>
            </view>
          </view>
        </view>

        <!-- Asset List Section -->
        <view class="section">
          <view class="section-header">
            <span class="section-title">资产列表</span>
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
  totalBalance: 862490.52,
  yesterdayChange: '+124.80',
  totalChange: '+42,931.15',
  avatarUrl: 'https://neeko-copilot.bytedance.net/api/text2image?prompt=mature%20professional%20man%20portrait%20warm%20lighting&image_size=square'
})

const assetList = ref([
  { id: '1', icon: '零', name: '零钱', desc: '可用余额', amount: '12,490.52', bgColor: '#EFF6FF', iconColor: '#2A806C', targetPath: '/pages/asset/holding/current' },
  { id: '2', icon: '通', name: '零钱通', desc: '收益稳健', amount: '250,000.00', bgColor: '#FFF7ED', iconColor: '#b7102a', targetPath: '/pages/asset/holding/current-plus' },
  { id: '3', icon: '基', name: '基金', desc: '长期稳健增值', amount: '500,000.00', bgColor: '#ECFDF5', iconColor: '#006754' },
  { id: '4', icon: '金', name: '黄金', desc: '实物黄金', amount: '100,000.00', bgColor: '#FFFBEB', iconColor: '#705624' },
  { id: '5', icon: '微', name: '微粒贷', desc: '额度可用', amount: '可借 50,000', bgColor: '#ECFEFF', iconColor: '#0D9488', targetPath: '/pages/asset/holding/loan' }
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

const currentAccountType = ref('wechat')

const handleSettings = () => {
  uni.navigateTo({
    url: `/pages/asset/account/edit?accountType=${currentAccountType.value}&institutionName=${encodeURIComponent(accountData.value.institutionName || '')}`
  })
}

const handleAssetTap = (asset) => {
  console.log(asset)
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
    accountData.value.totalBalance = parseFloat(options.balance) || 862490.52
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
  margin-top: 24rpx;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
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