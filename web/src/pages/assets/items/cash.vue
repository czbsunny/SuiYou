<template>
  <view class="asset-detail-page">
    <!-- 1. 核心资产区 (直接在背景上，去容器) -->
    <view class="asset-hero animate-fade-in">
      <text class="inst-name">金额</text>
      <view class="balance-box">
        <text class="currency">¥</text>
        <text class="balance-val num-font">{{ formatMoney(balance) }}</text>
      </view>
    </view>

    <!-- 2. 动作按钮区 (直接在背景上) -->
    <view class="action-group">
      <button class="btn btn-primary" @tap="handleAction('recharge')" hover-class="btn-hover-active">充值</button>
      <button class="btn btn-outline" @tap="handleAction('withdraw')" hover-class="btn-hover-grey">提现</button>
    </view>

    <!-- 3. 最近记录列表 (卡片化展示) -->
    <view class="history-section">
      <view class="section-header">
        <text class="title">最近记录</text>
      </view>
      
      <view class="log-list">
        <view v-for="(log, index) in logs" :key="index" class="log-item" hover-class="item-active">
          <view class="log-info">
            <text class="title">{{ log.title }}</text>
            <text class="time">{{ log.time }}</text>
          </view>
          <view class="log-right">
            <text :class="['log-amt', 'num-font', log.type]">
              {{ log.type === 'in' ? '+' : '-' }}{{ formatMoney(log.amount) }}
            </text>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view v-if="logs.length === 0" class="empty-state">
          <text>暂无最近变动记录</text>
        </view>
      </view>
    </view>

    <view class="safe-bottom-spacer"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const balance = ref(1.51);
const logs = ref([
  { title: '美团外卖支付', time: '今天 12:30', amount: 25.50, type: 'out' },
  { title: '来自银行卡充值', time: '昨天 09:15', amount: 500.00, type: 'in' }
]);

onLoad((options) => {
  let item = null;
  if (options.data) {
    item = JSON.parse(decodeURIComponent(options.data));
    console.log('解析出的资产:', item);

    if (item.name) {
      uni.setNavigationBarTitle({ title: item.name });
    }

    if (item) {
      balance.value = item.availableBalance || 0;
    }
  }
});

const formatMoney = (val) => Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const handleAction = (type) => {
  uni.vibrateShort();
  uni.showToast({ title: '跳转至' + type, icon: 'none' });
};
</script>

<style lang="scss" scoped>
.asset-detail-page {
  min-height: 100vh;
  background-color: $bg-page; // 应用暖米白 #FAF9F6
  padding: 0 20rpx; // 统一 10px 左右内边距
}

/* 1. 核心资产区优化：直接在背景上显示 */
.asset-hero {
  text-align: center;
  padding: 100rpx 0 60rpx;

  .inst-name {
    font-size: 26rpx;
    color: $text-sub;
    margin-bottom: 20rpx;
    display: block;
    letter-spacing: 2rpx;
  }

  .balance-box {
    display: flex;
    align-items: baseline;
    justify-content: center;
    color: $text-main;

    .currency {
      font-size: 40rpx;
      font-weight: 600;
      margin-right: 8rpx;
    }
    .balance-val {
      font-size: 96rpx;
      font-weight: 800;
      line-height: 1;
    }
  }
}

/* 2. 按钮区优化：左右平排 */
.action-group {
  padding: 40rpx 0 80rpx;
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 30rpx;

  .btn {
    flex: 1;
    height: 100rpx;
    border-radius: $radius-base;
    font-size: 32rpx;
    font-weight: 600;
    @include flex-center;
    transition: all 0.2s;

    &.btn-primary {
      background-color: $primary;
      color: $text-inverse;
      border: none;
      box-shadow: $shadow-button;
    }

    &.btn-outline {
      background-color: $white;
      color: $text-sub;
      border: 1rpx solid $gray-200;
    }
  }
}

/* 3. 最近记录列表：卡片化 */
.history-section {
  .section-header {
    padding: 0 10rpx;
    margin-bottom: 24rpx;
    .title {
      font-size: 28rpx;
      font-weight: 700;
      color: $text-main;
    }
  }
}

.log-item {
  background-color: $white;
  border-radius: $radius-lg; // 32rpx
  padding: 30rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  box-shadow: $shadow-card; // 温润环境色阴影
  border: 1rpx solid rgba(50, 46, 43, 0.02);

  .log-info {
    .title {
      font-size: 28rpx;
      font-weight: 600;
      color: $text-main;
      display: block;
    }
    .time {
      font-size: 22rpx;
      color: $text-muted;
      margin-top: 8rpx;
      display: block;
    }
  }

  .log-amt {
    font-size: 32rpx;
    font-weight: 700;
    &.in { color: $color-gain; } // 红色
    &.out { color: $text-main; }
  }
}

.empty-state {
  padding: 100rpx 0;
  text-align: center;
  color: $text-muted;
  font-size: 24rpx;
}

.safe-bottom-spacer {
  height: calc(40rpx + env(safe-area-inset-bottom));
}

.btn-hover-active {
  transform: scale(0.97);
  filter: brightness(0.95);
}

.btn-hover-grey {
  background-color: $gray-50 !important;
}

.item-active {
  background-color: #fdfcfb;
  transform: scale(0.99);
}

.animate-fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>