<template>
  <view class="account-detail-page">
    <!-- 1. 顶部总览 -->
    <view class="summary-section animate-fade-in">
      <view class="summary-card card-warm">
        <view class="brand-info">
          <view class="brand-logo-box">
            <image src="/static/images/bank-logo.png" mode="aspectFit" class="brand-icon"></image>
          </view>
          <text class="account-name">招商银行</text>
        </view>
        
        <view class="balance-content">
          <view class="label-row" @tap="toggleVisibility">
            <text class="label">净资产 (元)</text>
            <view class="icon-eye-wrapper">
              <image :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" class="eye-icon"></image>
            </view>
          </view>
          <!-- 净资产计算：总资产 - 总负债 -->
          <text class="amount num-font" :class="{ 'is-masked': isAssetHidden }">
            {{ isAssetHidden ? '******' : formatMoney(netAsset) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 2. 快捷操作区 -->
    <view class="action-bar">
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('record')">
        <view class="icon-box bg-income-box"><image src="/static/images/edit.png" class="action-icon"></image></view>
        <text class="action-label">记账</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-box bg-transfer-box"><image src="/static/images/transfer.png" class="action-icon"></image></view>
        <text class="action-label">转账</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('adjust')">
        <view class="icon-box bg-subtle-box"><image src="/static/images/check.png" class="action-icon"></image></view>
        <text class="action-label">校准</text>
      </view>
    </view>

    <!-- 3. 资产/负债列表 -->
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
            <view class="item-icon-rect">
              <text class="item-emoji">{{ getEmoji(item.type) }}</text>
            </view>
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-desc">{{ item.desc }}</text>
            </view>
          </view>
          
          <view class="item-right">
            <view class="amount-box">
              <text class="item-amount num-font" :class="{'text-debt': isDebt(item.type)}">
                {{ isAssetHidden ? '****' : (isDebt(item.type) ? '-' : '') + formatMoney(item.value) }}
              </text>
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

const isAssetHidden = ref(false);

// 模拟后端返回的数据结构
const items = ref([
  { type: 'CASH', name: '活期存款', desc: '尾号 8899', value: 28500.42 },
  { type: 'CASH_PLUS', name: '朝朝宝', desc: '活期+ | 7日年化2.1%', value: 50000.00, profit: 3.25 },
  { type: 'FIXED', name: '定期存款', desc: '2025-06-12 到期', value: 100000.00 },
  { type: 'WEALTH', name: '理财产品', desc: '招银稳健系列', value: 35000.00, profit: -120.50 },
  { type: 'CREDIT', name: '信用卡', desc: '本期应还 15,200.00', value: 15200.00 },
  { type: 'LOAN', name: '个人贷款', desc: '剩余本金 240,000.00', value: 240000.00 }
]);

// 计算净资产
const netAsset = computed(() => {
  return items.value.reduce((acc, item) => {
    return isDebt(item.type) ? acc - item.value : acc + item.value;
  }, 0);
});

// 类型判断辅助
const isDebt = (type) => ['CREDIT', 'LOAN'].includes(type);

const getEmoji = (type) => {
  const map = {
    'CASH': '🏦',
    'CASH_PLUS': '⚡',
    'FIXED': '🔒',
    'WEALTH': '📈',
    'CREDIT': '💳',
    'LOAN': '📝'
  };
  return map[type] || '💰';
};

const formatMoney = (val) => {
  return Math.abs(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const toggleVisibility = () => {
  isAssetHidden.value = !isAssetHidden.value;
  uni.vibrateShort();
};

const handleAction = (type) => {
  uni.showToast({ title: '跳转中...', icon: 'none' });
};
</script>

<style lang="scss" scoped>
/* 保持与项目全局一致的暖白视觉体系 */
.account-detail-page {
  min-height: 100vh;
  background-color: $bg-page;
}

.summary-section { padding: $spacing-md $spacing-md 0; }
.summary-card { padding: 48rpx 40rpx; }

.brand-info {
  display: flex; align-items: center; gap: 20rpx; margin-bottom: 32rpx;
  .brand-logo-box {
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
}

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
  &.bg-income-box { background-color: $bg-income; color: $color-income; }
  &.bg-transfer-box { background-color: $bg-transfer; color: $color-transfer; }
  &.bg-subtle-box { background-color: $bg-subtle; color: $text-sub; }
}

.items-section {
  padding: 0 $spacing-md;
  .section-header {
    margin-bottom: $spacing-base;
    .title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.asset-item-card {
  padding: 24rpx; display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-base;
  
  .item-left {
    display: flex; align-items: center; gap: 24rpx;
    .item-icon-rect {
      width: 88rpx; height: 88rpx; background-color: $bg-page; border-radius: $radius-base;
      @include flex-center; .item-emoji { font-size: 44rpx; }
    }
    .item-name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
    .item-desc { font-size: 22rpx; color: $text-muted; margin-top: 4rpx; }
  }

  .item-right {
    display: flex; align-items: center; gap: 12rpx;
    .amount-box {
      text-align: right;
      .item-amount { 
        font-size: 30rpx; font-weight: $fw-bold; color: $text-main; 
        &.text-debt { color: $text-sub; } // 负债项颜色稍浅
      }
      .item-profit { font-size: 20rpx; font-weight: $fw-semibold; margin-top: 4rpx; display: block; }
    }
    .arrow-icon { width: 24rpx; height: 24rpx; opacity: 0.2; }
  }
}

.item-active { background-color: $bg-subtle; transform: scale(0.98); }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>