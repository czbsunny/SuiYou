<template>
  <view class="account-detail-page">
    <!-- 1. 顶部资产总览卡片 -->
    <view class="summary-section animate-fade-in">
      <view class="summary-card">
        <view class="brand-info">
          <view class="wechat-logo">
            <text class="iconfont"></text> <!-- 建议换成微信图标 -->
          </view>
          <text class="account-name">微信支付</text>
        </view>
        
        <view class="balance-content">
          <text class="label">合计资产 (元)</text>
          <text class="amount num-font">{{ formatMoney(totalBalance) }}</text>
        </view>
        
        <!-- 装饰性元素：增加温润感 -->
        <view class="card-bg-decoration"></view>
      </view>
    </view>

    <!-- 2. 快捷操作区：圆圈式简洁入口 -->
    <view class="action-bar">
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('record')">
        <view class="icon-circle bg-income"><text class="iconfont"></text></view>
        <text class="action-label">记一笔</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('transfer')">
        <view class="icon-circle bg-transfer"><text class="iconfont">⇄</text></view>
        <text class="action-label">转入/出</text>
      </view>
      <view class="action-item" hover-class="hover-opacity" @tap="handleAction('adjust')">
        <view class="icon-circle bg-subtle"><text class="iconfont">✓</text></view>
        <text class="action-label">校准</text>
      </view>
    </view>

    <!-- 3. 资产项构成列表 -->
    <view class="items-section">
      <view class="section-header">
        <text class="title">资产构成</text>
        <text class="count-tag">{{ items.length }}项</text>
      </view>

      <view class="item-list">
        <view 
          v-for="(item, index) in items" 
          :key="index" 
          class="asset-item-card" 
          hover-class="item-active"
          @tap="navToItemDetail(item)"
        >
          <view class="item-left">
            <view class="item-icon-box">
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
              <text v-if="item.profit" class="item-profit num-font" :class="item.profit >= 0 ? 'up' : 'down'">
                {{ item.profit >= 0 ? '+' : '' }}{{ item.profit }}
              </text>
            </view>
            <text class="iconfont arrow"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部垫片 -->
    <view class="safe-bottom"></view>
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
  background-color: $bg-page; // #FAF9F6
}

/* 1. 顶部总览卡片：沉浸式墨绿 */
.summary-section {
  padding: 40rpx 32rpx 80rpx;
  background: linear-gradient(180deg, $primary 0%, $bg-page 100%);
}

.summary-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 48rpx 40rpx;
  box-shadow: $shadow-card;
  position: relative;
  overflow: hidden;

  .brand-info {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 40rpx;
    
    .wechat-logo {
      width: 44rpx; height: 44rpx;
      background-color: #07c160; // 微信绿仅在此处点缀
      border-radius: 10rpx;
      @include flex-center;
      color: $white;
      .iconfont { font-size: 24rpx; }
    }
    .account-name { font-size: 28rpx; font-weight: 600; color: $text-main; }
  }

  .balance-content {
    .label { font-size: 24rpx; color: $text-sub; display: block; margin-bottom: 12rpx; }
    .amount { font-size: 64rpx; font-weight: 800; color: $primary; letter-spacing: -1rpx; }
  }
}

/* 2. 操作栏：极简圆圈 */
.action-bar {
  display: flex;
  justify-content: space-around;
  padding: 0 40rpx;
  margin-top: -40rpx; // 产生卡片悬浮效果
  position: relative;
  z-index: 10;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    
    .action-label { font-size: 24rpx; color: $text-sub; font-weight: 500; }
  }
}

.icon-circle {
  width: 96rpx; height: 96rpx;
  border-radius: 50%;
  @include flex-center;
  background-color: $bg-white;
  box-shadow: $shadow-card;
  .iconfont { font-size: 40rpx; }
  
  &.bg-income { color: $color-income; }
  &.bg-transfer { color: $color-transfer; }
  &.bg-subtle { color: $text-sub; }
}

/* 3. 构成列表：白卡片流 */
.items-section {
  padding: 60rpx 32rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding: 0 8rpx;
    
    .title { font-size: 30rpx; font-weight: 600; color: $text-main; }
    .count-tag { font-size: 22rpx; color: $text-muted; }
  }
}

.asset-item-card {
  background-color: $bg-white;
  border-radius: $radius-base;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.01);
  border: 1rpx solid rgba(0,0,0,0.02);
  transition: all 0.2s;

  .item-left {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .item-icon-box {
      width: 80rpx; height: 80rpx;
      background-color: $bg-page;
      border-radius: 20rpx;
      @include flex-center;
      .item-emoji { font-size: 40rpx; }
    }
    .item-name { font-size: 28rpx; font-weight: 600; color: $text-main; display: block; }
    .item-desc { font-size: 22rpx; color: $text-muted; margin-top: 4rpx; display: block; }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .amount-box {
      text-align: right;
      .item-amount { font-size: 30rpx; font-weight: 700; color: $text-main; display: block; }
      .item-profit {
        font-size: 20rpx; font-weight: 600;
        &.up { color: $color-gain; }
        &.down { color: $color-loss; }
      }
    }
    .arrow { color: $text-placeholder; font-size: 24rpx; }
  }
}

.item-active {
  background-color: #fcfcfc;
  transform: scale(0.99);
}

.hover-opacity { opacity: 0.7; }
.animate-fade-in { animation: fadeIn 0.6s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>