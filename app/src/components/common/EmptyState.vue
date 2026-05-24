<template>
  <view class="empty-container">
    <view class="empty-icon">{{ icon }}</view>
    <text class="empty-text">{{ text }}</text>
    <button v-if="buttonText" class="empty-button" @tap="handleClick">
      {{ buttonText }}
    </button>
    <slot></slot>
  </view>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    default: '📭'
  },
  text: {
    type: String,
    default: '暂无数据'
  },
  buttonText: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  margin-bottom: 30rpx;
}

.empty-button {
  padding: 20rpx 60rpx;
  background: #7dc6a2;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;

  &:active {
    opacity: 0.8;
  }
}
</style>
