<!-- components/assets/plugins/DebitCardPlugin.vue -->
<template>
  <view class="data-grid-card">
    <view class="grid-item">
      <text class="label">卡号/尾号</text>
      <text class="value">{{ extData?.institutionIdentifier || '未绑定' }}</text>
    </view>
    <view class="grid-item">
      <text class="label">活期年利率</text>
      <text class="value text-emerald">{{ extData?.rate || '0.30' }}%</text>
    </view>
    <view class="grid-item">
      <text class="label">账户类型</text>
      <text class="value">借记卡储蓄</text>
    </view>
    <view class="grid-item">
      <text class="label">存取便利性</text>
      <text class="value">随时存取 (T+0)</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  extData: { type: Object, default: () => ({}) }
});
</script>

<style lang="scss" scoped>
.data-grid-card {
  background: #fff; margin: 0 32rpx 24rpx; border-radius: 32rpx;
  display: flex; flex-wrap: wrap; padding: 10rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  
  .grid-item {
    width: 50%; padding: 32rpx 40rpx; border-bottom: 1rpx solid #F8FAFC;
    // 去掉最后两项的下边框
    &:nth-last-child(-n+2) { border-bottom: none; }
    
    .label { font-size: 22rpx; color: #9CA3AF; margin-bottom: 12rpx; display: block; }
    .value { font-size: 28rpx; font-weight: 800; color: #374151; }
    .text-emerald { color: #2A806C; }
  }
}
</style>