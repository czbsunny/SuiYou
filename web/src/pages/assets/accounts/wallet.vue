<template>
  <view class="cash-account-container">
    <!-- 1. 顶部资产看板 -->
    <view class="summary-section animate-fade-in">
      <view class="asset-card card-warm">
        <view class="brand-info">
          <view class="cash-logo-box">
            <image src="/static/images/cash-icon.png" mode="aspectFit" class="brand-icon"></image>
          </view>
          <text class="account-name">家庭现金汇总</text>
        </view>
        
        <view class="balance-content">
          <view class="label-row" @click="isAssetHidden = !isAssetHidden">
            <text class="label">当前可用现金 (元)</text>
            <image :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" class="eye-icon"></image>
          </view>
          <text class="amount num-font" :class="{ 'is-masked': isAssetHidden }">
            {{ isAssetHidden ? '******' : formatMoney(totalCash) }}
          </text>
          <view class="last-update">
            <text>上次对账：{{ lastAuditDate }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 快捷操作 -->
    <view class="action-bar">
      <view class="action-item" @click="handleAction('out')">
        <view class="icon-box bg-expense-box">
          <image src="/static/images/expense.png" class="action-icon"></image>
        </view>
        <text class="action-label">支出记账</text>
      </view>
      <view class="action-item" @click="handleAction('in')">
        <view class="icon-box bg-income-box">
          <image src="/static/images/income.png" class="action-icon"></image>
        </view>
        <text class="action-label">存入/取现</text>
      </view>
      <view class="action-item" @click="handleAction('adjust')">
        <view class="icon-box bg-subtle-box">
          <image src="/static/images/audit.png" class="action-icon"></image>
        </view>
        <text class="action-label">盘点对账</text>
      </view>
    </view>

    <!-- 3. 现金分布列表 -->
    <view class="items-section">
      <view class="section-header">
        <text class="title">现金存放点</text>
      </view>

      <view class="item-list">
        <view 
          v-for="(item, index) in items" 
          :key="index" 
          class="cash-item-card card-warm"
          hover-class="item-active"
        >
          <view class="item-left">
            <view class="item-icon-rect">
              <text class="item-emoji">{{ item.emoji }}</text>
            </view>
            <view class="item-info">
              <view class="name-row">
                <text class="item-name">{{ item.name }}</text>
                <text class="owner-tag">{{ item.owner }}</text>
              </view>
              <text class="item-desc">最后变动：{{ item.lastChange }}</text>
            </view>
          </view>
          
          <view class="item-right">
            <view class="amount-box">
              <text class="item-amount num-font">{{ isAssetHidden ? '****' : formatMoney(item.value) }}</text>
              <text class="item-status" v-if="item.isUrgent">需补充</text>
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

const isAssetHidden = ref(false);
const lastAuditDate = ref('2024-10-20');

// 模拟现金资产数据
const items = ref([
  { name: '我的钱包', owner: '本人', value: 856.50, emoji: '👛', lastChange: '昨天 18:20', isUrgent: false },
  { name: '夫人钱包', owner: '妻子', value: 1200.00, emoji: '👜', lastChange: '10-18', isUrgent: false },
  { name: '家庭保险箱', owner: '公共', value: 5000.00, emoji: '🔐', lastChange: '09-12', isUrgent: false },
  { name: '应急备用金', owner: '书房', value: 200.00, emoji: '✉️', lastChange: '10-01', isUrgent: true },
  { name: '孩子存钱罐', owner: '儿子', value: 352.40, emoji: '🐷', lastChange: '10-15', isUrgent: false }
]);

const totalCash = computed(() => {
  return items.value.reduce((acc, item) => acc + item.value, 0);
});

const formatMoney = (val) => {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const handleAction = (type) => {
  uni.showToast({ title: '记录功能跳转', icon: 'none' });
};
</script>

<style lang="scss" scoped>
.cash-account-container {
  min-height: 100vh;
  background-color: $bg-page;
}

/* 1. 总览卡片 */
.summary-section { padding: $spacing-md $spacing-md 0; }

.asset-card {
  padding: 48rpx 40rpx;
  
  .brand-info {
    display: flex; align-items: center; gap: 20rpx; margin-bottom: 32rpx;
    .cash-logo-box {
      width: 52rpx; height: 52rpx; background-color: $primary; border-radius: $radius-sm;
      @include flex-center;
      .brand-icon { width: 34rpx; height: 34rpx; filter: brightness(100); }
    }
    .account-name { font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; }
  }

  .balance-content {
    .label-row {
      display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx;
      .label { font-size: 24rpx; color: $text-muted; }
      .eye-icon { width: 30rpx; height: 30rpx; opacity: 0.3; }
    }
    .amount { 
      font-size: 64rpx; font-weight: $fw-bold; color: $text-main; 
      &.is-masked { color: $text-placeholder; letter-spacing: 4rpx; }
    }
    .last-update { font-size: 22rpx; color: $text-placeholder; margin-top: 16rpx; }
  }
}

/* 2. 操作栏 */
.action-bar {
  display: flex; justify-content: space-around; padding: $spacing-lg $spacing-md;
  .action-item {
    display: flex; flex-direction: column; align-items: center; gap: 16rpx;
    .action-label { font-size: 24rpx; color: $text-regular; font-weight: $fw-medium; }
  }
}

.icon-box {
  width: 100rpx; height: 100rpx; border-radius: $radius-base;
  @include flex-center; box-shadow: $shadow-card;
  .action-icon { width: 48rpx; height: 48rpx; }
  &.bg-expense-box { background-color: $bg-expense; }
  &.bg-income-box { background-color: $bg-income; }
  &.bg-subtle-box { background-color: $bg-subtle; }
}

/* 3. 现金列表 */
.items-section {
  padding: 0 $spacing-md;
  .section-header { margin-bottom: $spacing-base; .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; } }
}

.cash-item-card {
  padding: 24rpx; display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-base;

  .item-left {
    display: flex; align-items: center; gap: 24rpx;
    .item-icon-rect {
      width: 88rpx; height: 88rpx; background-color: $bg-page; border-radius: $radius-base;
      @include flex-center; .item-emoji { font-size: 44rpx; }
    }
    .item-info {
      .name-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 6rpx; }
      .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
      .owner-tag { font-size: 18rpx; color: $text-muted; background: $gray-100; padding: 2rpx 8rpx; border-radius: 4rpx; }
      .item-desc { font-size: 22rpx; color: $text-muted; }
    }
  }

  .item-right {
    display: flex; align-items: center; gap: 12rpx;
    .amount-box {
      text-align: right;
      .item-amount { font-size: 30rpx; font-weight: $fw-bold; color: $text-main; }
      .item-status { font-size: 18rpx; color: $red; margin-top: 4rpx; display: block; font-weight: $fw-semibold; }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.2; }
  }
}

.item-active { background-color: $bg-subtle; transform: scale(0.98); }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>