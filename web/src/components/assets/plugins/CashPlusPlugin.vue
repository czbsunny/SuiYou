<!-- plugins/CashPlusPlugin.vue -->
<template>
  <view class="data-grid-card">
    <view class="grid-item">
      <text class="label">七日年化收益</text>
      <text class="value text-orange">{{ extData.yield || '1.85' }}%</text>
    </view>
    <view class="grid-item">
      <text class="label">昨日参考收益</text>
      <text class="value text-emerald">+{{ extData.lastProfit || '0.00' }}</text>
    </view>
    <view class="grid-item">
      <text class="label">底层货币基金</text>
      <text class="value truncate">{{ extData.fundName || '天弘余额宝' }}</text>
    </view>
    <view class="grid-item">
      <text class="label">累计赚钱</text>
      <text class="value">¥{{ extData.totalProfit || '0.00' }}</text>
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