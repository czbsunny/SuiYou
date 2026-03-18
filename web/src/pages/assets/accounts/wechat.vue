<template>
  <view class="account-detail-page">
    <!-- 1. 顶部资产总览卡片 -->
    <view class="summary-section animate-fade-in">
      <view class="summary-card card-warm">       
        <view class="balance-content">
          <text class="label">总资产 (元)</text>
          <text class="amount num-font">{{ formatMoney(totalBalance) }}</text>
        </view>
      </view>
    </view>

    <!-- 2. 快捷操作区：矩形圆角入口 -->
    <view class="action-bar">
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('record')">
        <view class="icon-box-filled bg-income-light">
          <image src="/static/images/edit.png" class="action-icon"></image>
        </view>
        <text class="action-label">记一笔</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-box-filled bg-transfer-light">
          <image src="/static/images/transfer.png" class="action-icon"></image>
        </view>
        <text class="action-label">转账</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('adjust')">
        <view class="icon-box-filled bg-subtle-light">
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
            <!-- 资产项图标：完整矩形框 -->
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
              <text class="item-amount num-font">{{ formatMoney(item.value) }}</text>
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
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

// 模拟数据
const totalBalance = ref(15862.45);
const items = ref([
  { name: '微信零钱', desc: '即时可用', value: 862.45, emoji: '👛', type: 'CASH' },
  { name: '零钱通', desc: '中银活期宝货币A', value: 5000.00, profit: 0.28, emoji: '⚡', type: 'CASHPLUS' },
  { name: '理财通投资', desc: '广发稳健理财', value: 10000.00, profit: -12.40, emoji: '📈', type: 'FUND' }
]);

onLoad((options) => {
  uni.setNavigationBarTitle({ title: options.name || '微信账户' });
});

const formatMoney = (val) => {
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const handleAction = (type) => {
  uni.showToast({ title: '功能开发中', icon: 'none' });
};

const navToItemDetail = (item) => {
    switch (item.type) {
        case 'CASH':
            uni.navigateTo({ url: `/pages/assets/items/cash?name=${item.name}` });
            break;
        case 'CASHPLUS':
            uni.navigateTo({ url: `/pages/assets/items/cashPlus?name=${item.name}` });
            break;
        case 'FUND':
            uni.navigateTo({ url: `/pages/assets/items/fund?name=${item.name}` });
            break;
        default:
            uni.showToast({ title: '资产类型未知', icon: 'none' });
            break;
    }
};
</script>

<style lang="scss" scoped>
.account-detail-page {
  min-height: 100vh;
  background-color: $bg-page; // 米白色背景
}

/* 1. 顶部总览：不再使用渐变 */
.summary-section {
  padding: $spacing-md $spacing-md 0;
}

.summary-card {
  /* card-warm 已带白底、圆角、阴影 */
  padding: 48rpx 40rpx;

  .balance-content {
    .label { font-size: 24rpx; color: $text-muted; display: block; margin-bottom: 8rpx; }
    .amount { font-size: 64rpx; font-weight: $fw-bold; color: $primary; }
  }
}

/* 2. 操作栏：矩形填充风格 */
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

.icon-box-filled {
  width: 100rpx;
  height: 100rpx;
  border-radius: $radius-base; // 矩形圆角
  @include flex-center;
  box-shadow: $shadow-card;
  
  .action-icon { width: 48rpx; height: 48rpx; }
  
  // 对应的语义化背景（10%透明度）
  &.bg-income-light { background-color: $bg-income; color: $color-income; }
  &.bg-transfer-light { background-color: $bg-transfer; color: $color-transfer; }
  &.bg-subtle-light { background-color: $bg-subtle; color: $text-sub; }
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
  /* 使用 card-warm 保持一致 */
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-base;
  // 覆盖 card-warm 的默认 margin
  margin-left: 0;
  margin-right: 0;

  .item-left {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .item-icon-rect {
      width: 88rpx; 
      height: 88rpx;
      background-color: $bg-page;
      border-radius: $radius-base; // 矩形圆角
      @include flex-center;
      .item-emoji { font-size: 44rpx; }
    }
    .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; display: block; }
    .item-desc { font-size: 22rpx; color: $text-muted; margin-top: 4rpx; display: block; }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .amount-box {
      text-align: right;
      .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; display: block; }
      .item-profit { font-size: 20rpx; font-weight: $fw-semibold; margin-top: 4rpx; display: block; }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.3; }
  }
}

.item-active {
  background-color: $bg-subtle;
  transform: scale(0.98);
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>