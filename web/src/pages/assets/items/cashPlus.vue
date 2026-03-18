<template>
  <view class="cash-plus-page">
    <!-- 1. 核心操作卡片 (主容器) -->
    <view class="main-card card-warm animate-slide-up">
      
      <!-- 点击区：账户余额与资金明细合并块 -->
      <view class="top-balance-area" @tap="navigateTo('TransactionHistory')" hover-class="hover-grey">
        <text class="label">账户余额</text>
        <view class="amount-row">
          <text class="currency">¥</text>
          <text class="amount num-font">{{ formatMoney(totalAmount) }}</text>
        </view>
        <view class="detail-guide">
          <text>资金明细</text>
          <image src="/static/images/chevron-right.png" class="guide-arrow"></image>
        </view>
      </view>

      <!-- 收益数据网格 (内容居中) -->
      <view class="profit-grid">
        <!-- 7日年化区 -->
        <view class="p-item" @tap="navigateTo('CashChart')" hover-class="hover-grey">
          <view class="p-label-row">
            <text class="p-label">7日年化收益率</text>
            <image src="/static/images/chevron-right.png" class="arrow-xs"></image>
          </view>
          <text class="p-val gain num-font">{{ cashRate }}%</text>
          <view class="grey-tag">
            <text>{{ productName }}</text>
          </view>
        </view>
        
        <!-- 累计收益区 -->
        <view class="p-item" @tap="navigateTo('ProfitList')" hover-class="hover-grey">
          <view class="p-label-row">
            <text class="p-label">累计收益</text>
            <image src="/static/images/chevron-right.png" class="arrow-xs"></image>
          </view>
          <text class="p-val num-font">¥{{ formatMoney(totalProfit) }}</text>
          <view class="grey-tag">
            <text class="day-label">昨日</text>
            <text class="day-val num-font">￥+{{ formatMoney(yesterdayProfit) }}</text>
          </view>
        </view>
      </view>

      <!-- 内部按钮区 (转入/转出) -->
      <view class="card-action-group">
        <button class="btn-sm btn-outline" @tap="handleAction('out')" hover-class="hover-grey">转出</button>
        <button class="btn-sm btn-primary" @tap="handleAction('in')" hover-class="hover-opacity">转入</button>
      </view>

      <!-- 内部定时转入链接 -->
      <view class="card-footer-link" @tap="handleAction('plan')" hover-class="hover-opacity">
        <image src="/static/images/clock.png" class="footer-icon"></image>
        <text>设置定时转入</text>
        <image src="/static/images/chevron-right.png" class="footer-arrow"></image>
      </view>
    </view>

    <view class="safe-bottom-spacer"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const totalAmount = ref(23766.40);
const totalProfit = ref(458.20);
const cashRate = ref('1.0740');
const yesterdayProfit = ref(1.23);
const productName = ref('中银活期宝A');

onLoad((options) => {
  if (options.name) uni.setNavigationBarTitle({ title: options.name });
});

const formatMoney = (val) => Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const navigateTo = (page) => {
  uni.vibrateShort();
  console.log('跳转至：', page);
};

const handleAction = (type) => {
  uni.vibrateShort();
  uni.showToast({ title: '触发' + type, icon: 'none' });
};
</script>

<style lang="scss" scoped>
.cash-plus-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding: 20rpx 10rpx; // 距离顶部20px，左右10px
  box-sizing: border-box;
}

.main-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  overflow: hidden;
  border: 1rpx solid rgba(0, 0, 0, 0.02);
}

/* 1. 余额及明细合并块 */
.top-balance-area {
  padding: 80rpx 40rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .label {
    font-size: 24rpx;
    color: $text-muted;
    margin-bottom: 16rpx;
  }
  
  .amount-row {
    display: flex;
    align-items: baseline;
    color: $text-main;
    margin-bottom: 24rpx;
    
    .currency {
      font-size: 40rpx;
      font-weight: 700;
      margin-right: 8rpx;
    }
    .amount {
      font-size: 84rpx;
      font-weight: 800;
      line-height: 1;
    }
  }

  .detail-guide {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: 24rpx;
    color: #576b95; // 链接蓝，增加引导性
    
    .guide-arrow {
      width: 24rpx;
      height: 24rpx;
      opacity: 0.6;
    }
  }
}

/* 2. 收益网格 (居中对齐) */
.profit-grid {
  display: flex;
  padding: 50rpx 0;
  
  .p-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center; // 核心：内容居中
    text-align: center;
    
    &:first-child { 
      border-right: 1rpx solid #f0f0f0; 
    }
    
    .p-label-row {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;
      .p-label { font-size: 22rpx; color: $text-muted; }
      .arrow-xs { width: 18rpx; height: 18rpx; margin-left: 4rpx; opacity: 0.3; }
    }
    
    .p-val {
      font-size: 34rpx;
      font-weight: 700;
      color: $text-main;
      margin-bottom: 16rpx;
      &.gain { color: $color-gain; }
    }
    
    /* 灰色标签色块 */
    .grey-tag {
      background-color: #f7f7f7;
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      font-size: 20rpx;
      color: $text-sub;
      display: flex;
      align-items: center;
      gap: 6rpx;
      
      .day-label { opacity: 0.8; }
    }
  }
}

/* 3. 内部操作组 */
.card-action-group {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 40rpx;
  
  .btn-sm {
    flex: 1;
    height: 88rpx; // 稍微调小
    border-radius: $radius-base;
    font-size: 30rpx;
    font-weight: 600;
    @include flex-center;
    transition: all 0.2s;
    
    &.btn-primary {
      background-color: $primary;
      color: $white;
      border: none;
      box-shadow: 0 4rpx 12rpx rgba(42, 128, 108, 0.1);
    }
    
    &.btn-outline {
      background-color: $bg-white;
      color: $text-sub;
      border: 1rpx solid #e5e5e5;
    }
  }
}

.card-footer-link {
  @include flex-center;
  padding: 30rpx 0 50rpx;
  gap: 8rpx;
  font-size: 24rpx;
  color: $text-main;
  opacity: 0.7;

  .footer-icon { width: 30rpx; height: 30rpx; }
  .footer-arrow { width: 20rpx; height: 20rpx; opacity: 0.4; }
}

/* 交互 */
.hover-grey { background-color: #f9f9f9 !important; }
.hover-opacity { opacity: 0.7; }

.animate-slide-up {
  animation: slideUp 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.safe-bottom-spacer {
  height: env(safe-area-inset-bottom);
}
</style>