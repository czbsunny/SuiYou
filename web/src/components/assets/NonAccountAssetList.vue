<template>
  <view class="non-account-card-container">
    <!-- 头部操作区 -->
    <view class="section-header">
      <text class="section-title">非账户资产</text>
      <view class="header-actions">
        <view class="icon-btn" @tap="$emit('manage-click')" hover-class="btn-hover">
          <image src="/static/icons/sliders.png" mode="aspectFit" class="btn-img" />
        </view>
        <view class="icon-btn" @tap="$emit('add-non-account-click')" hover-class="btn-hover">
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="icon-img" />
        </view>
      </view>
    </view>

    <!-- 非账户资产列表 -->
    <view class="card-list">
      <!-- 资产卡片 -->
      <view 
        v-for="asset in nonAccountAssets" 
        :key="asset.id" 
        class="non-account-card"
        @tap="$emit('asset-click', asset)"
      >
        <div class="non-account-header">
          <div class="non-account-name">{{ asset.name }}</div>
          <div class="non-account-category">{{ getCategoryName(asset.category) }}</div>
        </div>
        <div class="non-account-value">{{ formatAmount(asset.value) }}</div>
        <div class="non-account-desc">{{ asset.description || '无描述' }}</div>
      </view>

      <!-- 虚拟现金资产账户 -->
      <view class="non-account-card virtual-cash-account" @tap="$emit('add-cash-asset-click')">
        <div class="non-account-header">
          <div class="non-account-name">现金资产</div>
          <div class="non-account-category">现金</div>
        </div>
        <div class="non-account-value">+ 添加</div>
        <div class="non-account-desc">添加您的现金资产</div>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { ASSET_CATEGORY_DISPLAY } from '@/configs/assets.js';

const props = defineProps({
  nonAccountAssets: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits([
  'add-non-account-click',
  'asset-click',
  'manage-click',
  'add-cash-asset-click'
]);

const getCategoryName = (category) => {
  return ASSET_CATEGORY_DISPLAY[category]?.name || '其他';
};

const formatAmount = (amount) => {
  if (typeof amount !== 'number') {
    return '¥0';
  }
  return '¥' + amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  });
};
</script>

<style lang="scss" scoped>
.non-account-card-container {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-main;
  letter-spacing: 1rpx;
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

/* 极简图标按钮 */
.icon-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-white;
  color: $text-sub;
  border: 1rpx solid $border-color;
  transition: all 0.2s;

  .btn-img, .icon-img {
    width: 28rpx;
    height: 28rpx;
  }
}

.btn-hover {
  opacity: 0.8;
  transform: scale(0.94);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.non-account-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 30rpx 30rpx 24rpx;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(50, 46, 43, 0.02);
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.985);
    background-color: #FAFAFA;
  }
}

.non-account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .non-account-name {
    font-size: 28rpx;
    font-weight: 600;
    color: $text-main;
  }
  
  .non-account-category {
    font-size: 20rpx;
    color: $text-muted;
    background: $bg-subtle;
    padding: 4rpx 16rpx;
    border-radius: 10rpx;
  }
}

.non-account-value {
  font-size: 36rpx;
  font-weight: 700;
  color: $primary;
  font-family: 'DIN Alternate', sans-serif;
  margin-bottom: 12rpx;
}

.non-account-desc {
  font-size: 22rpx;
  color: $text-muted;
  line-height: 1.4;
}

/* 虚拟现金资产账户样式 */
.virtual-cash-account {
  border: 2rpx dashed $border-color;
  background-color: $bg-page;
  
  .non-account-value {
    color: $text-muted;
  }
  
  &:active {
    background-color: $bg-white;
  }
}
</style>