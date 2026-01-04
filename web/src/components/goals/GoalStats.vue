<template>
  <view class="header-dashboard">
    <view class="dashboard-top">
      <view class="summary-stats">
        <view class="stat-item" v-for="(item, index) in stats" :key="index">
          <text class="stat-label">{{ item.label }}</text>
          <text :class="['stat-value', item.valueClass]">{{ item.value }}</text>
        </view>
      </view>
      
      <view class="add-btn-mini" @click="$emit('add')">
        <image src="/static/images/plus-gray.png" class="add-icon-img" mode="aspectFit" />
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  stats: { type: Array, default: () => [] }
});
defineEmits(['add']);
</script>

<style lang="scss" scoped>
.header-dashboard {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba(#F5F7F9, 0.96);
  backdrop-filter: blur(10px);
  padding: 30rpx 48rpx;
  z-index: 99;
  border-bottom: 1rpx solid rgba(0,0,0,0.03);

  .dashboard-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.summary-stats {
  display: flex;
  gap: 32rpx;
  align-items: center;

  .stat-item {
    display: flex;
    flex-direction: column;
    .stat-label {
      font-size: 20rpx;
      color: #95A5A6;
      font-weight: bold;
      margin-bottom: 6rpx;
    }
    .stat-value {
      font-size: 36rpx;
      font-weight: bold;
      color: #2C3E50;
      &.text-primary { color: #2a806c; }
      &.text-gold { color: #D4AF37; }
    }
    // 模拟分割线
    &:not(:last-child)::after {
      content: "";
      position: absolute;
      right: -16rpx;
      top: 20%;
      height: 60%;
      width: 2rpx;
      background-color: #e2e8f0;
    }
    position: relative;
    padding-right: 32rpx;
  }
}

.add-btn-mini {
  width: 74rpx;
  height: 74rpx;
  background: #fff;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 15rpx rgba(0,0,0,0.06);
  &:active { transform: scale(0.92); }
  .add-icon-img { width: 32rpx; height: 32rpx; opacity: 0.8; }
}
</style>