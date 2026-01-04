<!-- components/assets/AssetViewToggle.vue -->
<template>
  <view class="view-tab">
    <view class="slider" :style="{ transform: `translateX(${modelValue === 'category' ? '0' : '100%'})` }"></view>
    <view class="tab-item" :class="{ active: modelValue === 'category' }" @click="$emit('update:modelValue', 'category')">资产类别</view>
    <view class="tab-item" :class="{ active: modelValue === 'institution' }" @click="$emit('update:modelValue', 'institution')">机构平台</view>
  </view>
</template>

<script setup>
defineProps(['modelValue']);
defineEmits(['update:modelValue']);
</script>

<style lang="scss" scoped>
.view-tab {
  display: flex; background: rgba(0,0,0,0.05); margin: 20rpx 48rpx 40rpx;
  padding: 4rpx; border-radius: 24rpx; position: relative; height: 72rpx;
}
.tab-item {
  flex: 1; text-align: center; line-height: 64rpx; font-size: 26rpx;
  font-weight: 700; color: #9CA3AF; z-index: 2; transition: color 0.3s;
  &.active { color: #1F2937; }
}
.slider {
  position: absolute; left: 4rpx; top: 4rpx; width: calc(50% - 4rpx); height: 64rpx;
  background: white; border-radius: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); z-index: 1;
}
</style>