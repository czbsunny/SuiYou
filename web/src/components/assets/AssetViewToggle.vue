<!-- components/assets/AssetViewToggle.vue -->
<template>
  <view class="asset-view-header">
    <!-- 1. 视角切换滑块 (资产类别 vs 机构平台) -->
    <view class="view-tab-container">
      <view class="view-tab">
        <view 
          class="slider" 
          :style="{ transform: `translateX(${viewMode === 'category' ? '0' : '100%'})` }"
        ></view>
        <view 
          class="tab-item" 
          :class="{ active: viewMode === 'category' }" 
          @tap="$emit('update:viewMode', 'category')"
        >资产类别</view>
        <view 
          class="tab-item" 
          :class="{ active: viewMode === 'institution' }" 
          @tap="$emit('update:viewMode', 'institution')"
        >机构平台</view>
      </view>
    </view>

    <!-- 2. 下方功能控制行 (标题 + 模式切换 + 一键折叠) -->
    <view class="control-row">
      <text class="section-title">资产明细</text>
      
      <view class="btn-group">
        <!-- 明细/简略模式切换 -->
        <view class="ctrl-btn" @tap="$emit('update:displayMode', displayMode === 'detailed' ? 'simple' : 'detailed')">
          <uni-icons 
            :type="displayMode === 'detailed' ? 'list' : 'sections'" 
            size="14" 
            :color="displayMode === 'simple' ? '#2A806C' : '#9CA3AF'" 
          />
          <text :class="{ 'text-active': displayMode === 'simple' }">
            {{ displayMode === 'detailed' ? '明细模式' : '简略模式' }}
          </text>
        </view>

        <view class="divider"></view>

        <!-- 一键展开/收缩 -->
        <view class="ctrl-btn" @tap="$emit('toggle-all')">
          <uni-icons 
            :type="isAllExpanded ? 'arrow-up' : 'arrow-down'" 
            size="14" 
            color="#9CA3AF" 
          />
          <text>{{ isAllExpanded ? '一键收起' : '一键展开' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * @property {String} viewMode - 视角模式: category | institution
 * @property {String} displayMode - 展示模式: detailed | simple
 * @property {Boolean} isAllExpanded - 是否全部展开
 */
defineProps({
  viewMode: { type: String, default: 'category' },
  displayMode: { type: String, default: 'detailed' },
  isAllExpanded: { type: Boolean, default: true }
});

defineEmits(['update:viewMode', 'update:displayMode', 'toggle-all']);
</script>

<script>
// 声明组件名方便调试
export default { name: 'AssetViewToggle' }
</script>

<style lang="scss" scoped>
.asset-view-header {
  padding: 0 32rpx;
  margin-top: 20rpx;
}

/* 视角切换滑块样式 */
.view-tab-container {
  padding: 0 16rpx;
  margin-bottom: 32rpx;
}

.view-tab {
  display: flex;
  background: rgba(0, 0, 0, 0.05);
  padding: 6rpx;
  border-radius: 24rpx;
  position: relative;
  height: 88rpx;
  border: 1rpx solid rgba(0, 0, 0, 0.02);
}

.tab-item {
  flex: 1;
  text-align: center;
  line-height: 72rpx;
  font-size: 32rpx;
  font-weight: 900;
  color: #9CA3AF;
  z-index: 2;
  transition: all 0.3s ease;
  
  &.active {
    color: #1F2937;
  }
}

.slider {
  position: absolute;
  left: 6rpx;
  top: 6rpx;
  width: calc(50% - 6rpx);
  height: 72rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

/* 下方功能控制行 */
.control-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 0 8rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 900;
    color: #1F2937;
    letter-spacing: 1rpx;
  }

  .btn-group {
    display: flex;
    align-items: center;
    background: #FFFFFF;
    padding: 10rpx 24rpx;
    border-radius: 100rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
    border: 1rpx solid rgba(0, 0, 0, 0.02);

    .ctrl-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      text {
        font-size: 22rpx;
        color: #9CA3AF;
        font-weight: 700;
        transition: color 0.2s;
        
        &.text-active {
          color: #2A806C;
        }
      }

      &:active {
        opacity: 0.7;
      }
    }

    .divider {
      width: 1rpx;
      height: 20rpx;
      background: #E5E7EB;
      margin: 0 16rpx;
    }
  }
}
</style>