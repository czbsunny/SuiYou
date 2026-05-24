<template>
  <view class="wealth-card">
    <text class="card-label">家庭资产净值</text>
    <view class="amount-wrapper">
      <text class="currency">¥</text>
      <text class="amount">{{ formatNumber(total) }}</text>
    </view>
    <view class="change-info">
      <view class="change-item">
        <text class="change-label">本月增长</text>
        <view class="change-value" :class="{ positive: monthChange >= 0 }">
          <text class="change-amount">{{ monthChange >= 0 ? '+' : '' }}¥{{ formatNumber(Math.abs(monthChange)) }}</text>
          <text class="change-percent">{{ monthChange >= 0 ? '+' : '' }}{{ monthChangePercent }}%</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { formatNumber } from '@/utils/format'

defineProps({
  total: {
    type: Number,
    default: 0
  },
  monthChange: {
    type: Number,
    default: 0
  },
  monthChangePercent: {
    type: Number,
    default: 0
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wealth-card {
  background: $surface-container-lowest;
  border-radius: $rounded-xl;
  padding: $spacing-8;
  box-shadow: $shadow-soft;
  
  .card-label {
    font-family: $font-family-primary;
    font-size: $font-size-label-caps;
    font-weight: $font-weight-bold;
    letter-spacing: 0.05em;
    color: $outline;
    text-transform: uppercase;
  }
  
  .amount-wrapper {
    display: flex;
    align-items: baseline;
    gap: 4rpx;
    margin-top: $stack-gap-sm;
    
    .currency {
      font-family: $font-family-primary;
      font-size: $font-size-headline-md;
      font-weight: $font-weight-semibold;
      color: $primary;
    }
    
    .amount {
      font-family: $font-family-mono;
      font-size: $font-size-display-lg;
      font-weight: $font-weight-bold;
      letter-spacing: -0.02em;
      color: $on-surface;
    }
  }
  
  .change-info {
    margin-top: $spacing-8;
    
    .change-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .change-label {
      font-family: $font-family-primary;
      font-size: $font-size-body-sm;
      color: $outline;
    }
    
    .change-value {
      display: flex;
      align-items: baseline;
      gap: 8rpx;
      
      &.positive {
        .change-amount, .change-percent {
          color: $secondary-container;
        }
      }
      
      .change-amount {
        font-family: $font-family-mono;
        font-size: $font-size-num-data;
        font-weight: $font-weight-semibold;
        color: $primary-container;
      }
      
      .change-percent {
        font-family: $font-family-mono;
        font-size: 24rpx;
        font-weight: $font-weight-bold;
        color: $primary-container;
        opacity: 0.8;
      }
    }
  }
}
</style>