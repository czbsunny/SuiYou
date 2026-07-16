<template>
  <view class="page">
    <scroll-view scroll-y class="scroll" @refresherrefresh="onRefresh" :refresher-enabled="true" :refresher-triggered="refreshing">
      <view class="content">
        <view class="main-card">
          <text class="eyebrow">家庭资产净值</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">{{ formatNumber(totalNetWorth) }}</text>
          </view>
          <view class="chart-container">
            <qiun-data-charts
              type="assetArea"
              :chartData="chartData"
              :tooltipShow="false"
              :enable-scroll="false"
              style="width: 100%; height: 240rpx;"
            />
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

const chartData = computed(() => {
  const categories = []
  const data = []
  const now = new Date()
  const baseValue = 800000
  for (let i = 11; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    categories.push(`${date.getMonth() + 1}月`)
    const growth = Math.round((11 - i) / 11 * 400000)
    data.push(baseValue + growth + Math.round(Math.random() * 50000 - 25000))
  }
  return { categories, series: [{ name: '净值', data, color: '#006754' }] }
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
    const accountId = account.id
    uni.navigateTo({
      url: `${pagePath}?accountId=${accountId}`
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

.eyebrow {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.amount-row {
  margin-top: $stack-gap-xs;
  display: flex;
  align-items: baseline;
  gap: $stack-gap-sm;
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

.chart-container {
  position: relative;
  height: 240rpx;
  margin-top: $spacing-2;
}

.section-heading {
  margin-top: $stack-gap-md;
  margin-bottom: $stack-gap-sm;
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