<template>
  <view class="list-section">
    <!-- 1. 头部区域 -->
    <view class="section-header">
      <text class="section-title">资产明细</text>
      <view class="header-icon" @click="$emit('header-click')">
        <image class="icon-img" src="/static/icons/sliders.png" mode="aspectFit"></image>
      </view>
    </view>

    <!-- 2. 列表区域 -->
    <view class="asset-list">
      <view 
        v-for="(item, index) in list" 
        :key="index" 
        class="asset-card"
        @click="$emit('item-click', item)"
      >
        <view class="card-left">
          <view class="logo-box">
            <image class="icon-img" :src="item.icon" mode="aspectFit"></image>
          </view>
        </view>

        <view class="card-center">
          <view class="asset-title">{{ item.name }}</view>
          <view class="asset-sub">{{ item.platform }}</view>
        </view>

        <view class="card-right">
          <view class="amount">
            <text class="currency">¥</text>
            <text class="num">{{ item.balance }}</text>
          </view>
          <view v-if="item.tag" class="status-tag" :class="item.type">
            {{ item.tag }}
          </view>
        </view>
      </view>

      <!-- 3. 新增：底部的添加资产项入口 (Apple 风格虚线框) -->
      <view class="add-asset-entry" @click="$emit('add-click')">
        <view class="add-content">
          <view class="plus-icon">
            <image class="plus-img" src="/static/images/plus-gray.png" mode="aspectFit"></image>
          </view>
          <text class="add-text">添加资产项</text>
        </view>
      </view>
    </view>
    
    <!-- 底部留白 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
const props = defineProps({
  list: {
    type: Array,
    default: () => []
  }
});

// 定义事件：增加 add-click 供父组件监听跳转
defineEmits(['item-click', 'header-click', 'add-click']);
</script>

<style lang="scss" scoped>
$bg-color: #F9F8F4;
$text-main: #1f2937;
$text-muted: #9ca3af;

.list-section {
  padding: 0 32rpx 40rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 40rpx 8rpx 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #4b5563;
}

.header-icon {
  padding: 10rpx;
  display: flex;
  align-items: center;
  .icon-img {
    width: 32rpx;
    height: 32rpx;
    opacity: 0.6;
  }
}

/* 资产卡片样式 */
.asset-card {
  background: white;
  border-radius: 40rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  border: 1rpx solid rgba(0,0,0,0.03);
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.02);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.97);
    background-color: #fafafa;
  }
}

/* 添加资产入口样式：精致虚线感 */
.add-asset-entry {
  margin-top: 32rpx;
  height: 120rpx;
  border: 2rpx dashed #e5e7eb; // 使用虚线
  border-radius: 40rpx;
  background-color: rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
    background-color: #ffffff;
    border-color: $text-muted;
  }

  .add-content {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .plus-icon {
    width: 32rpx;
    height: 32rpx;
    display: flex;
    align-items: center;
    .plus-img {
      width: 100%;
      height: 100%;
      opacity: 0.4;
    }
  }

  .add-text {
    font-size: 28rpx;
    color: $text-muted;
    font-weight: 500;
  }
}

/* 卡片内部元素 */
.card-left { margin-right: 32rpx; }
.logo-box {
  width: 88rpx; height: 88rpx;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; color: white; font-size: 36rpx;
  .icon-img {
    width: 72rpx;
    height: 72rpx;
  }
}
.card-center { flex: 1; }
.asset-title { font-size: 30rpx; font-weight: 600; color: #111827; margin-bottom: 4rpx; }
.asset-sub { font-size: 24rpx; color: $text-muted; }
.card-right { text-align: right; }
.amount {
  font-size: 34rpx; font-weight: 700; color: #111827;
  .currency { font-size: 22rpx; margin-right: 4rpx; }
}
.status-tag {
  font-size: 22rpx; font-weight: 600; margin-top: 4rpx;
  &.up { color: #ef4444; }
  &.down { color: #10b981; }
  &.neutral { color: $text-muted; }
}
.safe-area-bottom { height: 60rpx; }
</style>