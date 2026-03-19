<template>
  <view class="account-detail-page">
    <!-- 1. 顶部总览卡片 -->
    <view class="summary-section animate-fade-in">
      <view class="summary-card card-warm">
        <view class="brand-info">
          <!-- Logo 容器：使用项目主色 $primary 保持视觉闭环 -->
          <view class="brand-logo-box">
            <image src="/static/images/alipay-logo.png" mode="aspectFit" class="brand-icon"></image>
          </view>
          <text class="account-name">支付宝</text>
        </view>
        
        <view class="balance-content">
          <view class="label-row" @tap="toggleVisibility">
            <text class="label">合计资产 (元)</text>
            <view class="icon-eye-wrapper">
              <image 
                :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" 
                class="eye-icon"
              ></image>
            </view>
          </view>
          <text class="amount num-font" :class="{ 'is-masked': isAssetHidden }">
            {{ isAssetHidden ? '******' : formatMoney(totalBalance) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 2. 快捷操作区：统一使用 uni.scss 定义的语义背景 -->
    <view class="action-bar">
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('record')">
        <view class="icon-box bg-income-box">
          <image src="/static/images/edit.png" class="action-icon"></image>
        </view>
        <text class="action-label">记一笔</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-box bg-transfer-box">
          <image src="/static/images/transfer.png" class="action-icon"></image>
        </view>
        <text class="action-label">转账</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('adjust')">
        <view class="icon-box bg-subtle-box">
          <image src="/static/images/check.png" class="action-icon"></image>
        </view>
        <text class="action-label">校准</text>
      </view>
    </view>

    <!-- 3. 资产列表 -->
    <view class="items-section">
      <view class="section-header">
        <text class="title">资产列表</text>
      </view>

      <view class="item-list">
        <view 
          v-for="(item, index) in items" 
          :key="index" 
          class="asset-item-card card-warm" 
          hover-class="item-active"
          @tap="navToItemDetail(item)"
        >
          <view class="item-left">
            <!-- 资产项图标：矩形框填充 -->
            <view class="item-icon-rect">
              <text class="item-emoji">{{ item.emoji }}</text>
            </view>
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-desc">{{ item.desc }}</text>
            </view>
          </view>
          
          <view class="item-right">
            <view class="amount-box">
              <text class="item-amount num-font">{{ isAssetHidden ? '****' : formatMoney(item.value) }}</text>
              <text v-if="item.profit" class="item-profit num-font" :class="item.profit >= 0 ? 'text-up' : 'text-down'">
                {{ item.profit >= 0 ? '+' : '' }}{{ item.profit }}
              </text>
            </view>
            <image src="/static/images/arrow-right.png" class="arrow-icon"></image>
          </view>
        </view>
      </view>
    </view>

    <view class="safe-area-bottom" style="height: 40rpx;"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const isAssetHidden = ref(false);
const totalBalance = ref(52680.12);

const items = ref([
  { name: '余额', desc: '即时可用', value: 1250.50, emoji: '💰' },
  { name: '余额宝', desc: '七日年化1.85%', value: 35429.62, profit: 1.68, emoji: '⚡' },
  { name: '蚂蚁财富', desc: '理财持有中', value: 16000.00, profit: -85.20, emoji: '📈' },
  { name: '花呗', desc: '下月应还 1,200.00', value: -1200.00, emoji: '💳' }
]);

onLoad((options) => {
  uni.setNavigationBarTitle({ title: options.name || '支付宝账户' });
});

const formatMoney = (val) => {
  return Math.abs(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const toggleVisibility = () => {
  isAssetHidden.value = !isAssetHidden.value;
  uni.vibrateShort();
};

const handleAction = (type) => {
  uni.showToast({ title: '跳转至' + type, icon: 'none' });
};
</script>

<style lang="scss" scoped>
.account-detail-page {
  min-height: 100vh;
  background-color: $bg-page; // 全局统一暖白
}

/* 1. 总览卡片 */
.summary-section {
  padding: $spacing-md $spacing-md 0;
}

.summary-card {
  /* card-warm 已带基础样式 */
  padding: 48rpx 40rpx;
  
  .brand-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 32rpx;
    
    .brand-logo-box {
      width: 52rpx;
      height: 52rpx;
      background-color: $primary; // 使用全局主色墨绿
      border-radius: $radius-sm;
      @include flex-center;
      .brand-icon { width: 34rpx; height: 34rpx; filter: brightness(100); }
    }
    .account-name { font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; }
  }

  .balance-content {
    .label-row {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 12rpx;
      .label { font-size: 24rpx; color: $text-muted; }
      .eye-icon { width: 30rpx; height: 30rpx; opacity: 0.3; }
    }
    .amount { 
      font-size: 64rpx; 
      font-weight: $fw-bold; 
      color: $text-main; 
      &.is-masked { color: $text-placeholder; letter-spacing: 4rpx; }
    }
  }
}

/* 2. 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-around;
  padding: $spacing-lg $spacing-md;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    .action-label { font-size: 24rpx; color: $text-regular; font-weight: $fw-medium; }
  }
}

.icon-box {
  width: 100rpx;
  height: 100rpx;
  border-radius: $radius-base; // 统一矩形圆角
  @include flex-center;
  box-shadow: $shadow-card;
  
  .action-icon { width: 48rpx; height: 48rpx; }
  
  // 使用 uni.scss 已有的背景色
  &.bg-income-box { background-color: $bg-income; color: $color-income; }
  &.bg-transfer-box { background-color: $bg-transfer; color: $color-transfer; }
  &.bg-subtle-box { background-color: $bg-subtle; color: $text-sub; }
}

/* 3. 资产列表 */
.items-section {
  padding: 0 $spacing-md;
  .section-header {
    margin-bottom: $spacing-base;
    .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.asset-item-card {
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-base;
  margin-left: 0; margin-right: 0;

  .item-left {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .item-icon-rect {
      width: 88rpx; 
      height: 88rpx;
      background-color: $bg-page; // 使用页面背景色产生深浅对比
      border-radius: $radius-base;
      @include flex-center;
      .item-emoji { font-size: 44rpx; }
    }
    .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
    .item-desc { font-size: 22rpx; color: $text-muted; margin-top: 4rpx; }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
    .amount-box {
      text-align: right;
      .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; }
      .item-profit { font-size: 20rpx; font-weight: $fw-semibold; margin-top: 4rpx; display: block; }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.2; }
  }
}

.item-active { background-color: $bg-subtle; transform: scale(0.98); }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>