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

        <!-- Module List Section -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">资产列表</text>
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
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { getAccountById } from '@/api/modules/asset'
import { MODULE_ROUTES } from '@/configs/routes'

const isVisible = ref(true)

const accountData = ref({
  accountId: '',
  accountType: '',
  accountName: '',
  accountNumber: '',
  
  instCode: '',
  instName: '',

  totalBalance: 0,
  yesterdayChange: '',
  totalChange: ''
})

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

const handleModuleTap = (mod) => {
  const targetPath = MODULE_ROUTES[mod.type]
  if (targetPath) {
    uni.navigateTo({
      url: `${targetPath}?data=${encodeURIComponent(JSON.stringify({
        id: mod.id,
        name: mod.name,
        availableBalance: parseFloat(String(mod.amount).replace(/,/g, '')) || 0
      }))}`,
      fail: () => {
        uni.showToast({ title: `${mod.name}详情开发中`, icon: 'none' })
      }
    })
  } else {
    uni.showToast({ title: `${mod.name}详情开发中`, icon: 'none' })
  }
}

const accountIdRef = ref('')

const loadAccountData = (accountId) => {
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
          yesterdayChange: '',
          totalChange: ''
        }
        if (data.modules && data.modules.length > 0) {
          moduleList.value = data.modules.map(m => {
            return {
              id: m.id,
              icon: m.iconUrl,
              bgColor: m.bgColor,
              name: m.moduleName,
              type: m.moduleType,
              desc: '',
              amount: 0
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
}

onLoad((options) => {
  accountIdRef.value = options.accountId
  loadAccountData(accountIdRef.value)

  uni.$on('refreshAccountDetail', () => { 
    loadAccountData(accountIdRef.value) 
  })
})

onUnload(() => { 
  uni.$off('refreshAccountDetail') 
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

.section {
  margin-top: $stack-gap-md;
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