<template>
  <view class="transaction-item" @tap="handleTap">
    <view class="item-left">
      <view class="icon-wrapper" :class="{ income: item.amount > 0 }">
        <text class="material-symbols-outlined">{{ getIcon(item.category) }}</text>
      </view>
      <view class="item-info">
        <text class="item-title">{{ item.title }}</text>
        <text class="item-meta">{{ item.time }} · {{ item.category }}</text>
      </view>
    </view>
    <view class="item-right">
      <text class="item-amount" :class="{ income: item.amount > 0 }">
        {{ item.amount > 0 ? '+' : '' }}¥{{ formatNumber(Math.abs(item.amount)) }}
      </text>
      <text class="item-account">{{ item.account }}</text>
    </view>
  </view>
</template>

<script setup>
import { formatNumber } from '@/utils/format'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const getIcon = (category) => {
  const iconMap = {
    '购物': 'shopping_bag',
    '投资': 'trending_up',
    '餐饮': 'restaurant',
    '住宅': 'home',
    '工资': 'paycheck',
    '转账': 'transfer',
    '娱乐': 'gamepad_2',
    '医疗': 'medical_services',
    '教育': 'school'
  }
  return iconMap[category] || 'receipt_long'
}

const handleTap = () => {
  uni.showToast({ title: props.item.title, icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-4;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  
  &:active {
    background: $surface-container-low;
    transition: background 0.15s ease;
  }
  
  .item-left {
    display: flex;
    align-items: center;
    gap: $spacing-4;
  }
  
  .icon-wrapper {
    width: 96rpx;
    height: 96rpx;
    border-radius: $rounded-full;
    background: $surface-container-low;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .material-symbols-outlined {
      color: $outline;
      font-size: 40rpx;
    }
    
    &.income {
      background: rgba($secondary-container, 0.1);
      
      .material-symbols-outlined {
        color: $secondary-container;
      }
    }
  }
  
  .item-info {
    display: flex;
    flex-direction: column;
    
    .item-title {
      font-family: $font-family-primary;
      font-size: $font-size-body-reg;
      font-weight: $font-weight-semibold;
      color: $on-surface;
    }
    
    .item-meta {
      font-family: $font-family-primary;
      font-size: $font-size-body-sm;
      color: $outline;
      margin-top: 4rpx;
    }
  }
  
  .item-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    
    .item-amount {
      font-family: $font-family-mono;
      font-size: $font-size-num-data;
      font-weight: $font-weight-semibold;
      color: $primary-container;
      
      &.income {
        color: $secondary-container;
      }
    }
    
    .item-account {
      font-family: $font-family-primary;
      font-size: 20rpx;
      font-weight: $font-weight-bold;
      letter-spacing: 0.05em;
      color: $outline-variant;
      text-transform: uppercase;
      margin-top: 4rpx;
    }
  }
}
</style>