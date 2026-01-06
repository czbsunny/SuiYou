<!-- plugins/CashPlugin.vue -->
<template>
  <view class="data-grid-card">
    <view class="grid-item">
      <text class="label">存放位置</text>
      <text class="value">{{ extData.location || '随身钱包' }}</text>
    </view>
    <view class="grid-item">
      <text class="label">上次清点</text>
      <text class="value">{{ extData.lastCountDate || '2026-01-01' }}</text>
    </view>
    <view class="grid-item full">
      <text class="label">现金管理提示</text>
      <text class="value text-gray">现金不产生收益，建议仅保留少量日常备用金</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  extData: {
    type: Object,
    default: () => ({})
  }
});
</script>

<style lang="scss" scoped>
.data-grid-card {
  background: #fff; margin: 0 32rpx 24rpx; border-radius: 32rpx;
  display: flex; flex-wrap: wrap; padding: 20rpx 0;
  
  .grid-item {
    width: 50%; padding: 24rpx 40rpx; border-bottom: 1rpx solid #F9FAFB;
    &.full { width: 100%; }
    
    .label { font-size: 22rpx; color: #9CA3AF; margin-bottom: 8rpx; display: block; }
    .value { font-size: 28rpx; font-weight: 700; color: #374151; }
    .text-emerald { color: #2A806C; }
    .text-orange { color: #F59E0B; }
  }
}
</style>