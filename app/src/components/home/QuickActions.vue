<template>
  <view class="quick-actions">
    <view 
      v-for="action in actions" 
      :key="action.id" 
      class="action-item"
      @tap="handleAction(action)"
    >
      <view class="action-icon-wrapper">
        <span class="material-symbols-outlined">{{ action.icon }}</span>
      </view>
      <span class="action-text">{{ action.label }}</span>
    </view>
  </view>
</template>

<script setup>
const actions = [
  { id: 'budget', icon: 'account_balance', label: '预算' },
  { id: 'record', icon: 'add_circle', label: '记账' },
  { id: 'save', icon: 'savings', label: '存钱' },
  { id: 'report', icon: 'insights', label: '报告' }
]

const handleAction = (action) => {
  switch (action.id) {
    case 'record':
      uni.showToast({ title: '记账功能', icon: 'none' })
      break
    case 'budget':
      uni.showToast({ title: '预算功能', icon: 'none' })
      break
    case 'save':
      uni.showToast({ title: '存钱功能', icon: 'none' })
      break
    case 'report':
      uni.showToast({ title: '报告功能', icon: 'none' })
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-4;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
  
  .action-icon-wrapper {
    width: 112rpx;
    height: 112rpx;
    border-radius: $rounded-lg;
    background: $surface-container-lowest;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;
    
    .material-symbols-outlined {
      color: $primary;
      font-size: 48rpx;
    }
    
    &:active {
      transform: scale(0.95);
      transition: transform 0.15s ease;
    }
  }
  
  .action-text {
    font-family: $font-family-primary;
    font-size: $font-size-label-caps;
    font-weight: $font-weight-bold;
    letter-spacing: 0.05em;
    color: $on-surface-variant;
    text-transform: uppercase;
  }
}
</style>