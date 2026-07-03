<template>
  <view class="page">
    <scroll-view scroll-y class="scroll" @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="refreshing">
      <view class="content">
        <view class="asset-hero">
          <text class="eyebrow">家庭资产净值</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">{{ formatNumber(totalNetWorth) }}</text>
          </view>
          <view class="chart">
            <svg class="chart-svg" viewBox="0 0 400 140" preserveAspectRatio="none">
              <polyline
                :points="chartPoints"
                fill="none"
                stroke="#006754"
                stroke-width="3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle v-for="(point, index) in chartDots" :key="index" :cx="point.x" :cy="point.y" r="6" fill="#2a806c" />
            </svg>
          </view>
          <view class="chart-labels">
            <text
              v-for="(label, index) in timelineLabels"
              :key="index"
              class="chart-label"
              :style="{ left: label.x + 'px' }"
            >{{ label.text }}</text>
          </view>
        </view>

        <view class="section-heading">
          <text class="section-title">账户列表</text>
          <view class="heading-actions">
            <view class="manage-button">
              <image src="/static/images/manage.png" class="manage-icon" mode="aspectFit" />
            </view>
            <view class="add-button" @tap="handleAddAccount">
              <image src="/static/images/add.png" class="add-icon" mode="aspectFit" />
            </view>
          </view>
        </view>

        <view v-if="loading" class="loading-wrap">
          <text class="loading-text">加载中...</text>
        </view>
        <view v-else-if="accounts.length === 0" class="empty-wrap">
          <text class="empty-text">暂无账户，点击右上角 + 添加</text>
        </view>
        <view v-else class="account-list">
          <view v-for="account in accounts" :key="account.id" class="account-card" @tap="handleAccountTap(account)">
            <view class="account-logo-wrap">
              <image :src="account.logoUrl" class="account-logo" mode="aspectFit"/>
               </view>
            <view class="account-info">
              <view class="account-title">
                <text class="account-name">{{ account.instName || '未知机构' }}</text>
                <text class="account-dot">·</text>
                <text class="account-type-tag">{{ account.accountTypeName || account.instTypeName || '' }}</text>
              </view>
              <view class="account-sub">
                <text class="account-sub-name">{{ account.accountName }}</text>
                <text class="account-sub-no">{{ account.accountNo }}</text>
              </view>
            </view>
            <view class="account-money">
              <text class="balance">¥{{ formatNumber(account.amount) }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAccountList } from '@/api/modules/asset'

const loading = ref(false)
const refreshing = ref(false)
const accounts = ref([])

const getMonthLabel = (date) => {
  const month = date.getMonth() + 1
  return `${month}月`
}

const timelineLabels = computed(() => {
  const now = new Date()
  const labels = []
  for (let i = 11; i >= 0; i -= 3) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    labels.push({
      text: getMonthLabel(date),
      x: 20 + (11 - i) / 11 * 360
    })
  }
  return labels
})

const chartPoints = computed(() => {
  const points = [
    { x: 20, y: 110 },
    { x: 60, y: 105 },
    { x: 100, y: 95 },
    { x: 140, y: 85 },
    { x: 180, y: 75 },
    { x: 220, y: 65 },
    { x: 260, y: 55 },
    { x: 300, y: 45 },
    { x: 340, y: 38 },
    { x: 380, y: 30 }
  ]
  return points.map(p => `${p.x},${p.y}`).join(' ')
})

const chartDots = computed(() => {
  return [
    { x: 20, y: 110 },
    { x: 100, y: 95 },
    { x: 180, y: 75 },
    { x: 260, y: 55 },
    { x: 380, y: 30 }
  ]
})

const formatNumber = (value) => {
  if (value === null || value === undefined || isNaN(Number(value))) return '0.00'
  const num = Number(value)
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const totalNetWorth = computed(() => {
  return '--'
})

const loadAccounts = async () => {
  loading.value = true
  try {
    const resp = await getAccountList()
    console.log('加载账户列表响应:', resp)
    accounts.value = resp?.data || []
  } catch (error) {
    console.error('加载账户列表失败:', error)
    uni.showToast({ title: '加载账户失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  refreshing.value = true
  try {
    await loadAccounts()
  } finally {
    refreshing.value = false
  }
}

const handleAddAccount = () => {
  uni.navigateTo({
    url: '/pages/asset/account/search'
  })
}

const handleAccountTap = (account) => {
  console.log('点击账户:', account)
  let pagePath = null
  switch (account.instType) {
    case 'SECURITY':
      pagePath = '/pages/asset/account/security'
      break
    case 'BANK':
      pagePath = '/pages/asset/account/bank'
      break
    case 'FINTECH':
      switch (account.instCode) {
        case 'ALIPAY':
          pagePath = '/pages/asset/account/alipay'
          break
        case 'WECHAT':
          pagePath = '/pages/asset/account/wechat'
          break
        default:
          break
      }
      break
    default:
      break
  }

  if (pagePath) {
    const institutionName = encodeURIComponent(account.instName || account.accountName || account.instCode || '账户详情')
    const accountId = account.id
    uni.navigateTo({
      url: `${pagePath}?institutionName=${institutionName}&instCode=${account.instCode}&accountId=${accountId}`
    })
  } else {
    uni.showToast({ title: '该账户类型开发中', icon: 'none' })
  }
}

onShow(() => {
  loadAccounts()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}


.asset-hero {
  padding: $spacing-6 $spacing-5 $spacing-4;
  border-radius: 32rpx;
  background: #fff;
  box-shadow: $shadow-soft;
}

.eyebrow {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.amount-row {
  margin-top: 24rpx;
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: 900;
}

.hero-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
}

.chart {
  position: relative;
  height: 140rpx;
  margin-top: $spacing-5;
}

.chart-svg {
  width: 100%;
  height: 100%;
}

.chart-labels {
  position: relative;
  height: 40rpx;
  margin-top: $spacing-1;
}

.chart-label {
  position: absolute;
  transform: translateX(-50%);
  color: $on-surface-variant;
  font-size: $font-size-xs;
  white-space: nowrap;
}

.section-heading {
  margin-top: $section-margin;
  margin-bottom: $stack-gap-md;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  color: $on-surface;
  font-size: $font-size-title-sm;
  font-weight: 900;
}

.heading-actions {
  color: $on-surface;
  display: flex;
  gap: $stack-gap-sm;
  align-items: center;
  font-size: $font-size-xs;
  font-weight: 900;
}

.manage-button,
.add-button {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
}

.manage-icon,
.add-icon {
  width: 36rpx;
  height: 36rpx;
  filter: brightness(0) saturate(100%);
}

.loading-wrap,
.empty-wrap {
  padding: 80rpx 0;
  display: flex;
  justify-content: center;
}

.loading-text,
.empty-text {
  color: $on-surface-variant;
  font-size: $font-size-body-sm;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.account-card {
  min-height: 110rpx;
  padding: $font-size-body-sm $spacing-4;
  border-radius: $rounded-md;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.account-logo-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.account-logo {
  width: 88rpx;
  height: 88rpx;
}

.account-logo-placeholder {
  color: rgba(#000, 0.55);
  font-size: $font-size-headline-md;
  font-weight: 900;
  line-height: 1;
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.account-name {
  color: $on-surface;
  font-size: $font-size-lg;
  font-weight: 900;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.account-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  overflow: hidden;
  white-space: nowrap;
}

.account-dot {
  color: $on-surface-variant;
  font-size: $font-size-sm;
  flex-shrink: 0;
}

.account-type-tag {
  color: $on-surface-variant;
  font-size: $font-size-sm;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.account-sub {
  margin-top: $spacing-1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  overflow: hidden;
}

.account-sub-name {
  color: $on-surface-variant;
  font-size: $font-size-sm;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.account-sub-no {
  color: $on-surface-variant;
  font-size: $font-size-sm;
  flex-shrink: 0;
}

.account-money {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.balance {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: 900;
}

.change {
  margin-top: $spacing-1;
  color: $primary;
  font-family: $font-family-mono;
  font-size: $font-size-xs;
  font-weight: 800;
}

.change.positive {
  color: $profit;
}

.change.neutral {
  color: $outline;
}
</style>