<template>
  <view class="wallet-container">
    <!-- 头部操作区 -->
    <view class="view-header">
      <text class="view-title">非账户资产</text>
      <view class="action-group">
        <view class="icon-btn" @tap="$emit('manage-click')">
          <image src="/static/icons/sliders.png" mode="aspectFit" class="btn-img" />
        </view>
        <view class="icon-btn primary" @tap="$emit('add-non-account-click')">
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="btn-img primary-icon" />
        </view>
      </view>
    </view>

    <!-- 非账户资产列表 -->
    <view class="wallet-stream">
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

      <!-- 添加非账户资产按钮 -->
      <view class="add-non-account-btn" @tap="$emit('add-non-account-click')">
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="add-icon"><line x1="12" x2="12" y1="5" y2="19"></line><line x1="5" x2="19" y1="12" y2="12"></line></svg>
        <div class="add-text">添加非账户资产</div>
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
  'manage-click'
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

<style lang="scss">
.wallet-container {
  padding: 0 8rpx;
}

/* 🟢 视图头部样式 */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100rpx;
  padding: 0 8rpx;
  margin-bottom: 10rpx;
  .view-title {
    font-size: 36rpx;
    font-weight: 800;
    color: #1F2937;
    letter-spacing: 2rpx;
  }
  .action-group {
    display: flex;
    gap: 16rpx;
    .icon-btn {
      width: 64rpx;
      height: 64rpx;
      background: #fff;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.04);
      .btn-img {
        width: 32rpx;
        height: 32rpx;
        opacity: 0.6;
      }
      &.primary {
        .primary-icon {
          opacity: 0.8;
          filter: brightness(0.2);
        }
      }
      &:active {
        transform: scale(0.9);
      }
    }
  }
}

.wallet-stream {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 非账户资产卡片 */
.non-account-card {
  background: #F9FAFB;
  border-radius: 24rpx;
  padding: 24rpx;
  border: 1rpx solid #E5E7EB;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  &:hover {
    border-color: #2a806c;
    box-shadow: 0 8rpx 24rpx rgba(42, 128, 108, 0.1);
    transform: translateY(-2rpx);
  }
}

.non-account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  .non-account-name {
    font-size: 28rpx;
    font-weight: 600;
    color: #1F2937;
  }
  .non-account-category {
    font-size: 20rpx;
    color: #6B7280;
    background: #E5E7EB;
    padding: 4rpx 16rpx;
    border-radius: 16rpx;
  }
}

.non-account-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #2a806c;
  font-family: 'DIN Alternate', sans-serif;
  margin-bottom: 12rpx;
}

.non-account-desc {
  font-size: 22rpx;
  color: #6B7280;
  line-height: 1.4;
}

/* 添加按钮 */
.add-non-account-btn {
  width: 100%;
  background: #F3F4F6;
  border: 2rpx dashed #D1D5DB;
  border-radius: 24rpx;
  padding: 40rpx 24rpx;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  &:hover {
    background: #E5E7EB;
    border-color: #9CA3AF;
  }
  .add-icon {
    margin: 0 auto 16rpx;
    opacity: 0.6;
  }
  .add-text {
    font-size: 24rpx;
    color: #6B7280;
    font-weight: 500;
  }
}
</style>