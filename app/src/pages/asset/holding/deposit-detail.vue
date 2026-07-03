<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <text class="card-label">{{ deposit.name }}</text>
            <text class="balance-amount font-mono">¥{{ deposit.principal }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">状态</text>
              <text class="stat-value" :class="{ matured: deposit.status === '已到期' }">{{ deposit.status }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">年利率</text>
              <text class="stat-value font-mono text-secondary">{{ deposit.rate }}%</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">基本信息</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">产品名称</text>
              <text class="info-value">{{ deposit.name }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">存款类型</text>
              <text class="info-value">{{ deposit.term || '—' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">存入金额</text>
              <text class="info-value font-mono">¥{{ deposit.principal }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">年利率</text>
              <text class="info-value font-mono text-secondary">{{ deposit.rate }}%</text>
            </view>
            <view class="info-row">
              <text class="info-label">预计利息</text>
              <text class="info-value font-mono text-profit">+¥{{ deposit.expectedInterest }}</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">时间信息</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">存入日</text>
              <text class="info-value font-mono">{{ deposit.startDate }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">到期日</text>
              <text class="info-value font-mono">{{ deposit.maturityDate }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">已持天数</text>
              <text class="info-value font-mono">{{ deposit.heldDays }}天</text>
            </view>
            <view class="info-row">
              <text class="info-label">剩余天数</text>
              <text class="info-value font-mono">{{ remainingDays }}天</text>
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

const deposit = ref({})

const remainingDays = computed(() => {
  if (!deposit.value.maturityDate) return '—'
  const now = new Date()
  const maturity = new Date(deposit.value.maturityDate)
  const diff = Math.ceil((maturity - now) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

onLoad((options) => {
  if (options?.data) {
    try {
      deposit.value = JSON.parse(decodeURIComponent(options.data))
    } catch (e) {
      console.error('解析数据失败', e)
    }
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
  padding-bottom: $spacing-8;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: $outline;
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
  display: flex;
  gap: $spacing-8;
  margin-top: $spacing-4;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $outline;
}

.stat-value {
  font-size: $font-size-lg;
  font-weight: 600;

  &.matured {
    color: $profit;
  }

  &.text-secondary {
    color: $secondary;
  }
}

.text-secondary {
  color: $secondary;
}

.text-profit {
  color: $profit;
}

.info-section {
  margin-top: $spacing-6;
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-5;
  box-shadow: $shadow-sm;
  border: 2rpx solid rgba($surface-variant, 0.2);
}

.section-header {
  margin-bottom: $spacing-4;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-3 0;
  border-bottom: 2rpx solid rgba($surface-variant, 0.15);

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.info-value {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}
</style>
