<template>
  <view class="record-btn-container">
    <button class="record-btn" hover-class="btn-hover" @click="handleRecordAction()">
      <view class="btn-icon">
        <image src="/static/assets/actions/plus-light.png" class="iconfont" />
      </view>
      <text class="btn-label">记一笔</text>
    </button>
  </view>
</template>

<script setup>
const handleRecordAction = () => {
  uni.navigateTo({ url: '/pages/home/record' })
}
</script>

<style lang="scss" scoped>
.record-btn-container {
  margin: 0 $spacing-base;
  display: flex;
}

.record-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  background-color: $primary;
  color: $text-inverse;
  border: none;
  border-radius: $radius-lg;
  width: 100%;
  padding: 16rpx 24rpx;
  font-size: 32rpx;
  font-weight: 500;
  box-shadow: $shadow-button;
  transition: all 0.2s;
  
  .btn-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48rpx;
    height: 48rpx;
    .iconfont {
      width: 48rpx;
      height: 48rpx;
    }
  }
  
  .btn-label {
    color: $text-inverse;
  }
}

/* 点击反馈 */
.btn-hover {
  background-color: $primary-dark;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  transform: translateY(-2rpx);
}

</style>