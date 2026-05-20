<template>
  <view class="main-container">
    <scroll-view class="main-scroll" scroll-y>
      <view class="content-wrapper">
        
        <!-- 1. 头部资产看板 (紧凑调整) -->
        <view class="card-warm summary-card animate-fade-in">
          <view class="asset-main">
            <view class="label-row" @tap="toggleAssetHidden">
              <text class="label">总资产 (元)</text>
              <uni-icons :type="isAssetHidden ? 'eye-slash' : 'eye'" size="14" color="#94a3b8"></uni-icons>
            </view>
            <text class="value-large num-font" :class="{ 'is-blur': isAssetHidden }">
              {{ isAssetHidden ? '******' : '685,896.66' }}
            </text>
          </view>

          <view class="indicator-grid">
            <view class="indicator-item">
              <text class="label">累计盈亏</text>
              <text class="value-mid num-font text-down" :class="{ 'is-blur': isAssetHidden }">
                {{ isAssetHidden ? '******' : '-13,220.74' }}
              </text>
            </view>
            <view class="indicator-item text-right">
              <text class="label">今日收益</text>
              <view class="value-group" :class="{ 'is-blur': isAssetHidden }">
                <text class="value-mid num-font text-up">{{ isAssetHidden ? '******' : '+1,562.20 (+2.22%)' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 2. 标题行 (更紧凑) -->
        <view class="section-header">
          <view class="header-left">
            <text class="title">我的持仓</text>
            <text class="count-tag">{{ holdings.length }}只</text>
          </view>
          <view class="header-right">
            <view class="icon-btn" @tap="onSort">
              <uni-icons type="tune" size="18" color="#64748b"></uni-icons>
            </view>
            <view class="icon-btn" @tap="onAdd">
              <uni-icons type="plusempty" size="20" color="#64748b"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 3. 持仓列表 (高度压缩，字号适中) -->
        <view class="card-list">
          <view 
            class="card-warm stock-card" 
            v-for="(item, index) in holdings" 
            :key="item.id"
            @tap="onCardTap(item)"
          >
            <!-- 第一行：基本信息 -->
            <view class="card-row">
              <view class="name-box">
                <text class="name truncate">{{ item.name }}</text>
                <text class="code num-font">{{ item.symbol }}</text>
              </view>
              <view class="rate-tag" :class="getReturnClass(item.dayProfitRate)">
                <text class="num-font">{{ (item.dayProfitRate > 0 ? '+' : '') + item.dayProfitRate }}%</text>
              </view>
            </view>

            <!-- 第二行：数额信息 -->
            <view class="card-row mt-12">
              <view class="data-group">
                <text class="d-label">市值</text>
                <text class="d-val num-font" :class="{ 'is-blur': isAssetHidden }">{{ isAssetHidden ? '****' : formatNumber(item.marketValue) }}</text>
              </view>
              <view class="data-group center">
                <text class="d-label">今日盈亏</text>
                <text class="d-val num-font" :class="getReturnClass(item.dayProfit)">{{ (item.dayProfit > 0 ? '+' : '') + formatNumber(item.dayProfit) }}</text>
              </view>
              <view class="data-group right">
                <text class="d-label">总盈亏</text>
                <text class="d-val num-font" :class="getReturnClass(item.totalProfit)">{{ (item.totalProfit > 0 ? '+' : '') + formatNumber(item.totalProfit) }}</text>
              </view>
            </view>

            <!-- 第三行：明细辅助 (极简) -->
            <view class="card-row mt-12 pt-12 border-t">
              <text class="f-info">现价: <text class="num-font">{{ item.currentPrice }}</text></text>
              <text class="f-info">成本: <text class="num-font">{{ item.costPrice }}</text></text>
              <text class="f-info">仓位: <text class="num-font">{{ item.position }}%</text></text>
            </view>
          </view>
        </view>

        <view class="safe-area-bottom"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const isAssetHidden = ref(false);
const holdings = ref([
  { id: 1, name: '招商中证500指数增强', marketValue: 35189.0, dayProfit: 250.4, dayProfitRate: 0.71, totalProfit: -1260.75, costPrice: 0.798, currentPrice: 0.770, position: 12.5, symbol: '159928' },
  { id: 2, name: '纳斯达克100ETF(QDII)', marketValue: 130849.0, dayProfit: 1102.32, dayProfitRate: 3.58, totalProfit: 1202.32, costPrice: 1.262, currentPrice: 1.469, position: 45.8, symbol: '513100' },
  { id: 3, name: '腾讯控股', marketValue: 85200.0, dayProfit: -1242.0, dayProfitRate: -1.45, totalProfit: 5400.0, costPrice: 310.2, currentPrice: 325.4, position: 22.1, symbol: '00700.HK' },
  { id: 4, name: '中国平安', marketValue: 12500.0, dayProfit: 150.0, dayProfitRate: 1.2, totalProfit: 800.0, costPrice: 42.5, currentPrice: 45.2, position: 5.2, symbol: '601318' }
]);

const formatNumber = (val) => Math.abs(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const getReturnClass = (val) => val > 0 ? 'text-up' : (val < 0 ? 'text-down' : '');
const toggleAssetHidden = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };

const onSort = () => {};
const onAdd = () => {};
const onCardTap = (item) => console.log('详情', item.name);
</script>

<style lang="scss" scoped>
// 1. 通用变量
$text-main: #1e293b;
$text-sub: #64748b;
$up: #ef4444;
$down: #22c55e;

.main-container {
  height: 100vh;
  background-color: $bg-page;
}

.content-wrapper {
  padding: $spacing-sm; 
}

// 头部看板 (紧凑)
.summary-card {
  .asset-main {
    margin-bottom: 24rpx;
    .label { font-size: 24rpx; color: $text-sub; }
    .value-large { font-size: 56rpx; font-weight: 700; color: $text-main; }
  }
  .indicator-grid {
    display: flex; justify-content: space-between;
    .indicator-item {
      .label { font-size: 22rpx; color: $text-sub; margin-bottom: 4rpx; display: block; }
      .value-mid { font-size: 30rpx; font-weight: 600; }
    }
  }
}

// 标题行
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10rpx 10rpx 20rpx;
  .header-left {
    display: flex; align-items: center; gap: 10rpx;
    .title { font-size: 30rpx; font-weight: 700; color: $text-main; }
    .count-tag { font-size: 22rpx; color: $text-sub; background-color: $bg-subtle; padding: 4rpx 16rpx; border-radius: 20rpx; }
  }
  .header-right { display: flex; gap: 24rpx; }
}

// 持仓卡片 (紧凑型)
.stock-card {
  .card-row {
    display: flex; justify-content: space-between; align-items: center;
    
    // 第一行样式
    .name-box {
      display: flex; align-items: center; gap: 12rpx;
      .name { font-size: 28rpx; font-weight: 600; color: $text-main; max-width: 320rpx; }
      .code { font-size: 22rpx; color: #94a3b8; }
    }
    .rate-tag {
      font-size: 24rpx; font-weight: 700; border-radius: 8rpx; padding: 2rpx 12rpx;
      &.text-up { background-color: #fef2f2; color: $up; }
      &.text-down { background-color: #f0fdf4; color: $down; }
    }

    // 第二行样式
    .data-group {
      flex: 1; display: flex; flex-direction: column;
      &.center { text-align: center; }
      &.right { text-align: right; }
      .d-label { font-size: 20rpx; color: #94a3b8; margin-bottom: 2rpx; }
      .d-val { font-size: 28rpx; font-weight: 600; color: $text-main; }
    }

    // 第三行样式
    .f-info { font-size: 22rpx; color: #94a3b8; }
  }
  
  .mt-12 { margin-top: 12rpx; }
  .pt-12 { padding-top: 12rpx; }
  .border-t { border-top: 1rpx solid #f1f5f9; }
}

// 工具
.num-font { font-family: 'DIN Alternate', sans-serif; }
.text-up { color: $up; }
.text-down { color: $down; }
.is-blur { filter: blur(10rpx); opacity: 0.5; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.safe-area-bottom { height: 40rpx; }
</style>