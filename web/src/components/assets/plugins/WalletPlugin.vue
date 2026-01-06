<!-- plugins/WalletPlugin.vue -->
<template>
  <view class="data-grid-card">
    <view class="grid-item full">
      <text class="label">绑定账号</text>
      <text class="value">{{ extData.institutionIdentifier || '-' }}</text>
    </view>
    <view class="grid-item">
      <text class="label">收益属性</text>
      <text class="value text-gray">无收益零钱</text>
    </view>
    <view class="grid-item">
      <text class="label">支付场景</text>
      <text class="value">日常消费</text>
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