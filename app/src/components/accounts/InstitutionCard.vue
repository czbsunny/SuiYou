
<template>
  <view class="institution-card" @click="handleClick">
    <view class="card-content">
      <view class="institution-icon" :class="iconClass">
        <text class="icon-text">{{ iconLetter }}</text>
      </view>
      <view class="institution-info">
        <view class="name-row">
          <text class="institution-name">{{ institution.instName || institution.shortName }}</text>
          <view v-if="institution.isHot" class="hot-tag">热门</view>
        </view>
        <view v-if="institutionType" class="type-tag">
          {{ institutionType.typeName }}
        </view>
      </view>
    </view>
    <view class="arrow">
      <text class="arrow-icon">›</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  institution: {
    type: Object,
    required: true
  },
  institutionType: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['click'])

const iconLetter = computed(() => {
  const name = props.institution.instName || props.institution.shortName || ''
  return name.charAt(0)
})

const iconClass = computed(() => {
  const typeCode = props.institution.instType || ''
  const typeClasses = {
    'BANK': 'bank-icon',
    'INTERNET_BANK': 'internet-bank-icon',
    'FOREIGN_BANK': 'foreign-bank-icon',
    'SECURITY': 'security-icon',
    'FUND_PLATFORM': 'fund-icon',
    'INSURANCE': 'insurance-icon',
    'PAYMENT': 'payment-icon',
    'FINTECH': 'fintech-icon'
  }
  return typeClasses[typeCode] || 'default-icon'
})

const handleClick = () => {
  emit('click', props.institution)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.institution-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: background 0.2s;
  
  &:active {
    background: $surface-container-low;
  }
}

.card-content {
  display: flex;
  align-items: center;
  gap: 28rpx;
  flex: 1;
}

.institution-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bank-icon {
  background: rgba(183, 16, 42, 0.08);
}

.internet-bank-icon {
  background: rgba(35, 102, 232, 0.08);
}

.foreign-bank-icon {
  background: rgba(100, 116, 139, 0.08);
}

.security-icon {
  background: rgba(250, 166, 26, 0.08);
}

.fund-icon {
  background: rgba(79, 70, 229, 0.08);
}

.insurance-icon {
  background: rgba(6, 182, 212, 0.08);
}

.payment-icon {
  background: rgba(0, 103, 84, 0.08);
}

.fintech-icon {
  background: rgba(139, 92, 246, 0.08);
}

.default-icon {
  background: $surface-container;
}

.icon-text {
  font-size: 36rpx;
  font-weight: 700;
  color: $on-surface;
}

.institution-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.institution-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $on-surface;
}

.hot-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: $error-container;
  color: $error;
  font-size: 20rpx;
  font-weight: 700;
}

.type-tag {
  margin-top: 8rpx;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  background: $surface-container;
  color: $on-surface-variant;
  font-size: 20rpx;
  font-weight: 500;
  display: inline-block;
}

.arrow {
  display: flex;
  align-items: center;
}

.arrow-icon {
  font-size: 48rpx;
  font-weight: 300;
  color: $outline-variant;
}
</style>

