<!-- components/assets/AssetViewToggle.vue -->
<template>
  <view class="view-tab-container">
    <view class="view-tab">
      <!-- 背景滑块：根据 modelValue 左右平移 -->
      <view 
        class="slider" 
        :style="{ transform: `translateX(${modelValue === 'category' ? '0' : '100%'})` }"
      ></view>
      
      <!-- 选项按钮 -->
      <view 
        class="tab-item" 
        :class="{ active: modelValue === 'category' }" 
        @tap="$emit('update:modelValue', 'category')"
      >
        资产类别
      </view>
      
      <view 
        class="tab-item" 
        :class="{ active: modelValue === 'institution' }" 
        @tap="$emit('update:modelValue', 'institution')"
      >
        机构平台
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * @property {String} modelValue - 视角模式: category | institution
 */
defineProps({
  modelValue: { type: String, default: 'category' }
});

defineEmits(['update:modelValue']);
</script>

<style lang="scss" scoped>
.view-tab-container {
  padding: 12rpx 24rpx 16rpx; // 适当的上下边距，保持呼吸感
}

.view-tab {
  display: flex;
  background: rgba(0, 0, 0, 0.05); // iOS 标准灰色背景
  padding: 6rpx;
  border-radius: 24rpx;
  position: relative;
  height: 80rpx; // 稍微调低高度，显得更精致
  border: 1rpx solid rgba(0, 0, 0, 0.02);
}

.tab-item {
  flex: 1;
  text-align: center;
  line-height: 68rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: #9CA3AF;
  z-index: 2; // 确保文字在滑块上方
  transition: color 0.3s ease;
  
  &.active {
    color: #1F2937; // 选中态文字颜色
  }
}

.slider {
  position: absolute;
  left: 6rpx;
  top: 6rpx;
  width: calc(50% - 6rpx);
  height: 68rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); // 经典的系统级曲线
  z-index: 1;
}
</style>