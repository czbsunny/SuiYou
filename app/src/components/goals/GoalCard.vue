<template>
  <view class="goal-card" @tap="handleTap">
    <view class="card-header">
      <view class="goal-icon-wrapper">
        <text class="material-symbols-outlined">{{ getIcon(goal.category) }}</text>
      </view>
      <view class="goal-info">
        <text class="goal-name">{{ goal.name }}</text>
        <text class="goal-desc">{{ goal.description }}</text>
      </view>
      <view class="goal-badge" :class="{ urgent: goal.daysLeft <= 90 }">
        <text>{{ getTimeLabel() }}</text>
      </view>
    </view>
    
    <view class="card-body">
      <view class="amount-row">
        <view class="amount-item">
          <text class="amount-label">已储蓄</text>
          <text class="amount-value primary">¥{{ formatNumber(goal.currentAmount) }}</text>
        </view>
        <view class="amount-item">
          <text class="amount-label">目标金额</text>
          <text class="amount-value">¥{{ formatNumber(goal.targetAmount) }}</text>
        </view>
      </view>
      
      <view class="progress-wrapper">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: `${goal.progress}%` }"></view>
        </view>
        <view class="progress-percent">
          <text>{{ goal.progress }}%</text>
        </view>
      </view>
      
      <view class="date-row">
        <text class="date-item">{{ formatDate(goal.startDate) }}</text>
        <text class="date-divider">→</text>
        <text class="date-item">{{ formatDate(goal.deadline) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { formatNumber } from '@/utils/format'

const props = defineProps({
  goal: {
    type: Object,
    required: true
  }
})

const getIcon = (category) => {
  const iconMap = {
    '房产': 'directions_car',
    '旅行': 'beach_access',
    '教育': 'school',
    '医疗': 'medical_services',
    '投资': 'trending_up',
    '购物': 'shopping_cart',
    '娱乐': 'gamepad_2'
  }
  return iconMap[category] || 'target'
}

const getTimeLabel = () => {
  const days = props.goal.daysLeft
  if (days <= 0) return '已完成'
  if (days <= 30) return `还剩 ${days} 天`
  if (days <= 365) {
    const months = Math.ceil(days / 30)
    return `还剩 ${months} 个月`
  }
  const years = Math.floor(days / 365)
  const months = Math.floor((days % 365) / 30)
  if (months > 0) {
    return `倒计时 ${years} 年 ${months} 个月`
  }
  return `倒计时 ${years} 年`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}`
}

const handleTap = () => {
  uni.navigateTo({ url: `/pages/goal/detail?id=${props.goal.id}` })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.goal-card {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-4;
  box-shadow: $shadow-soft;
  
  &:active {
    transform: scale(0.98);
    transition: transform 0.15s ease;
  }
  
  .card-header {
    display: flex;
    align-items: flex-start;
    gap: $spacing-4;
    margin-bottom: $spacing-4;
  }
  
  .goal-icon-wrapper {
    width: 96rpx;
    height: 96rpx;
    border-radius: $rounded-full;
    background: $surface-container-low;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .material-symbols-outlined {
      color: $primary;
      font-size: 40rpx;
    }
  }
  
  .goal-info {
    flex: 1;
    
    .goal-name {
      font-family: $font-family-primary;
      font-size: $font-size-title-sm;
      font-weight: $font-weight-semibold;
      color: $on-surface;
    }
    
    .goal-desc {
      font-family: $font-family-primary;
      font-size: $font-size-body-sm;
      color: $on-surface-variant;
      margin-top: 4rpx;
    }
  }
  
  .goal-badge {
    padding: 8rpx $spacing-3;
    border-radius: $rounded-full;
    background: rgba($primary, 0.1);
    
    text {
      font-family: $font-family-primary;
      font-size: $font-size-label-caps;
      font-weight: $font-weight-bold;
      letter-spacing: 0.05em;
      color: $primary;
      text-transform: uppercase;
    }
    
    &.urgent {
      background: rgba($secondary-container, 0.1);
      
      text {
        color: $secondary-container;
      }
    }
  }
  
  .card-body {
    .amount-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: $stack-gap-md;
    }
    
    .amount-item {
      display: flex;
      flex-direction: column;
      
      .amount-label {
        font-family: $font-family-primary;
        font-size: $font-size-label-caps;
        font-weight: $font-weight-bold;
        letter-spacing: 0.05em;
        color: $outline;
        text-transform: uppercase;
        margin-bottom: 4rpx;
      }
      
      .amount-value {
        font-family: $font-family-mono;
        font-size: $font-size-title-sm;
        font-weight: $font-weight-semibold;
        color: $on-surface;
        
        &.primary {
          color: $primary;
        }
      }
    }
    
    .progress-wrapper {
      position: relative;
      margin-bottom: $stack-gap-sm;
    }
    
    .progress-bar {
      height: 24rpx;
      background: $surface-container-low;
      border-radius: $rounded-full;
      overflow: hidden;
    }
    
    .progress-fill {
      height: 100%;
      background: $primary-container;
      border-radius: $rounded-full;
      transition: width 0.5s ease;
    }
    
    .progress-percent {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      
      text {
        font-family: $font-family-mono;
        font-size: 20rpx;
        font-weight: $font-weight-bold;
        color: $on-primary-container;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }
    
    .date-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .date-item {
        font-family: $font-family-primary;
        font-size: $font-size-body-sm;
        color: $outline;
      }
      
      .date-divider {
        color: $outline-variant;
      }
    }
  }
}
</style>