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
            <text class="balance-amount">
              <text class="currency-symbol">¥</text><text class="balance-value font-mono">{{ isVisible ? formattedBalance : '****' }}</text>
            </text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">今日盈亏</text>
              <text class="stat-value font-mono text-profit">{{ isVisible ? accountData.todayChange : '****' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">累计盈亏</text>
              <text class="stat-value font-mono text-profit">{{ isVisible ? accountData.totalChange : '****' }}</text>
            </view>
            <view class="stat-item col-span-2">
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
            <view class="action-icon-wrap" :class="action.bgClass">
              <image class="action-icon-img" :class="action.iconClass" :src="action.icon" mode="aspectFit" />
            </view>
            <text class="action-label">{{ action.label }}</text>
          </view>
        </view>

        <!-- Module List Section -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">持仓明细</text>
          </view>
          <view class="module-list">
            <view 
              v-for="module in moduleList" 
              :key="module.id" 
              class="module-item"
              @tap="handleModuleTap(module)"
            >
              <view class="module-icon-wrap" :style="{ background: module.bgColor }">
                <image class="module-icon" :src="module.icon" mode="aspectFit" />
              </view>
              <view class="module-info">
                <text class="module-name">{{ module.name }}</text>
                <text class="module-desc">{{ module.desc }}</text>
              </view>
              <view class="module-right">
                <view class="module-amount-wrap">
                  <text class="module-amount font-mono">{{ isVisible ? module.amount : '****' }}</text>
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
import { getAccountById } from '@/api/modules/asset'

const isVisible = ref(true)

const accountData = ref({
  accountId: '',
  accountType: '',
  accountName: '',
  accountNumber: '',
  
  instCode: '',
  instName: '',

  totalBalance: 0,
  todayChange: '',
  totalChange: '',
  availableBalance: '',
})

const quickActions = ref([
  { id: 'buy', icon: '/static/assets/actions/buy.png', label: '买入', bgClass: 'bg-gray', iconClass: 'rotate-right' },
  { id: 'sell', icon: '/static/assets/actions/sell.png', label: '卖出', bgClass: 'bg-gray', iconClass: 'rotate-left' },
  { id: 'transfer', icon: '/static/assets/actions/transfer.png', label: '转账', bgClass: 'bg-gray' },
  { id: 'query', icon: '/static/assets/actions/query.png', label: '查询', bgClass: 'bg-gray' }
])

const moduleList = ref([])

const formattedBalance = computed(() => {
  return accountData.value.totalBalance.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
})

const toggleVisibility = () => {
  isVisible.value = !isVisible.value
}

const handleSettings = () => {
  uni.navigateTo({
    url: `/pages/asset/account/edit?accountId=${accountData.value.accountId}`
  })
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

const handleModuleTap = (module) => {
  const paths = {
    stock: '/pages/asset/holding/stock',
    fund: '/pages/asset/holding/fund'
  }
  console.log(module.type)
  const url = paths[module.type]
  if (url) {
    uni.navigateTo({ url })
  } else {
    uni.showToast({ title: `${module.name}详情开发中`, icon: 'none' })
  }
}

onLoad((options) => {
  const accountId = options.accountId
  if (accountId) {
    getAccountById(accountId).then(res => {
      if (res.statusCode === 200) {
        const data = res.data
        accountData.value = {
          accountId: data.accountId || data.id || '',
          accountType: data.accountType || '',
          accountName: data.accountName || '',
          accountNumber: data.accountNumber || '',

          instCode: data.instCode || '',
          instName: data.instName || '',
          
          totalBalance: 0,
          todayChange: '',
          totalChange: '',
          availableBalance: '',
        }
        if (data.modules && data.modules.length > 0) {
          moduleList.value = data.modules.map(m => {
            return {
              id: m.id,
              icon: m.iconUrl,
              bgColor: m.bgColor,
              name: m.moduleName,
              desc: '',
              amount: 0,
              targetPath: ''
            }
          })
        }
      } else {
        uni.showToast({ title: '获取账户详情失败', icon: 'none' })
      }
    }).catch(err => {
      console.error('获取账户详情失败:', err)
      uni.showToast({ title: '网络请求失败', icon: 'none' })
    })
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
  margin-top: $spacing-3;
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
  display: flex;
  flex-direction: column;

  &.col-span-2 {
    grid-column: span 2;
  }
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $outline;
}

.stat-value {
  margin-top: $spacing-1;
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
  font-size: 56rpx;

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

.action-icon-img {
  width: 56rpx;
  height: 56rpx;

  &.rotate-right {
    transform: rotate(90deg);
  }
  &.rotate-left {
    transform: rotate(-90deg);
  }
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
  margin-bottom: $stack-gap-sm;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: 900;
  color: $on-surface;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.module-item {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  box-shadow: $shadow-sm;
}

.module-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $surface-container-lowest;
}

.module-icon {
  width: 40rpx;
  height: 40rpx;
}

.module-info {
  flex: 1;
  padding: 0 $spacing-4;
}

.module-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.module-desc {
  font-size: $font-size-body-sm;
  color: $outline;
  margin-top: 4rpx;
}

.module-right {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.module-amount-wrap {
  text-align: right;
}

.module-amount {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.icon-chevron {
  font-size: 28rpx;
  color: $outline-variant;
}
</style>