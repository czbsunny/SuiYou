<template>
  <view class="broker-account-container">
    <!-- 1. 资产看板卡片 -->
    <view class="summary-section animate-fade-in">
      <view class="asset-card card-warm">
        <view class="asset-header">
          <view class="brand-box">
            <image src="/static/images/broker-logo.png" mode="aspectFit" class="logo"></image>
            <text class="name">国投证券</text>
            <text class="account-id">**3501</text>
          </view>
        </view>

        <view class="main-assets">
          <view class="label-row">
            <text class="label">总资产 (元)</text>
            <uni-icons type="eye" size="16" color="#a3b0ad"></uni-icons>
          </view>
          <text class="value num-font">68,896.66</text>
        </view>

        <!-- 盈亏网格 -->
        <view class="profit-info-grid">
          <view class="info-item">
            <text class="label">总盈亏</text>
            <text class="val num-font text-down">-13,220.74</text>
          </view>
          <view class="info-item right-align">
            <text class="label">当日参考盈亏</text>
            <view class="val-group">
              <text class="val num-font text-down">-1,562.20</text>
              <text class="sub-val num-font text-down">-2.22%</text>
            </view>
          </view>
        </view>

        <!-- 资金详情行 -->
        <view class="funds-sub-row">
          <view class="sub-item">
            <text class="label">总市值</text>
            <text class="val num-font">68,859.20</text>
          </view>
          <view class="sub-item center">
            <text class="label">可用/国债</text>
            <text class="val num-font">36.46</text>
          </view>
          <view class="sub-item right">
            <text class="label">可取/转账</text>
            <text class="val num-font">36.46</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 操作快捷键 -->
    <view class="action-grid">
      <view class="action-btn" v-for="btn in actions" :key="btn.label" @click="handleAction(btn)">
        <view class="icon-wrapper">
          <text class="btn-text">{{ btn.iconText }}</text>
        </view>
        <text class="btn-label">{{ btn.label }}</text>
      </view>
    </view>

    <!-- 3. 持仓列表区域 -->
    <view class="holdings-section card-warm">
      <view class="section-header">
        <text class="title">持仓股</text>
        <view class="view-modes">
          <uni-icons type="list" size="20" color="#2a806c"></uni-icons>
        </view>
      </view>

      <!-- 横向滚动包裹层 -->
      <scroll-view class="horizontal-scroll" scroll-x enable-flex>
        <view class="table-container">
          <!-- 自定义表头 -->
          <view class="table-row header">
            <view class="col col-fixed first">名称 / 市值</view>
            <view class="col col-data">当日盈亏 / 率</view>
            <view class="col col-data">总盈亏 / 率</view>
            <view class="col col-data">成本 / 现价</view>
            <view class="col col-sm">持仓数量</view>
            <view class="col col-sm">个股仓位</view>
            <view class="col col-sm">持股天数</view>
            <view class="col col-md">证券代码</view>
          </view>

          <!-- 数据行 -->
          <view class="table-row data-row" v-for="(item, index) in holdings" :key="index">
            <!-- 固定列：名称和市值 -->
            <view class="col col-fixed first">
              <text class="stock-name">{{ item.name }}</text>
              <text class="stock-market-val num-font">{{ formatNumber(item.marketValue) }}</text>
            </view>

            <!-- 第二列：当日盈亏/率 -->
            <view class="col col-data">
              <text class="val-top num-font" :class="getReturnClass(item.dayProfit)">
                {{ item.dayProfit > 0 ? '+' : '' }}{{ formatNumber(item.dayProfit) }}
              </text>
              <text class="val-bottom num-font" :class="getReturnClass(item.dayProfitRate)">
                {{ item.dayProfitRate > 0 ? '+' : '' }}{{ item.dayProfitRate }}%
              </text>
            </view>

            <!-- 第三列：总盈亏/率 -->
            <view class="col col-data">
              <text class="val-top num-font" :class="getReturnClass(item.totalProfit)">
                {{ item.totalProfit > 0 ? '+' : '' }}{{ formatNumber(item.totalProfit) }}
              </text>
              <text class="val-bottom num-font" :class="getReturnClass(item.totalProfitRate)">
                {{ item.totalProfitRate > 0 ? '+' : '' }}{{ item.totalProfitRate }}%
              </text>
            </view>

            <!-- 第四列：成本/现价 -->
            <view class="col col-data">
              <text class="val-top num-font">{{ item.costPrice }}</text>
              <text class="val-bottom num-font">{{ item.currentPrice }}</text>
            </view>

            <!-- 第五列：持仓 -->
            <view class="col col-sm">
              <text class="val-top num-font">{{ item.holdCount }}</text>
              <text class="val-bottom">股</text>
            </view>

            <!-- 第六列：仓位 -->
            <view class="col col-sm">
              <text class="val-top num-font">{{ item.position }}%</text>
            </view>

            <!-- 第七列：天数 -->
            <view class="col col-sm">
              <text class="val-top num-font">{{ item.holdDays }}</text>
            </view>

            <!-- 第八列：代码 -->
            <view class="col col-md">
              <text class="val-top num-font code">{{ item.symbol }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="safe-area-bottom" style="height: 40rpx;"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const actions = [
  { label: '买入', iconText: '买' },
  { label: '卖出', iconText: '卖' },
  { label: '撤单', iconText: '撤' },
  { label: '查询', iconText: '查' }
];

const holdings = ref([
  {
    name: '消费 ETF',
    marketValue: 35189.00,
    dayProfit: -250.40,
    dayProfitRate: -0.71,
    totalProfit: -1260.75,
    totalProfitRate: -3.45,
    costPrice: 0.798,
    currentPrice: 0.770,
    holdCount: 45700,
    position: 51.2,
    holdDays: 128,
    symbol: '159928.SZ'
  },
  {
    name: '新城控股',
    marketValue: 30849.00,
    dayProfit: 1102.32,
    dayProfitRate: 3.58,
    totalProfit: 1202.32,
    totalProfitRate: 3.75,
    costPrice: 15.262,
    currentPrice: 14.690,
    holdCount: 2100,
    position: 44.8,
    holdDays: 45,
    symbol: '601155.SH'
  },
  {
    name: '隆基绿能',
    marketValue: 1852.00,
    dayProfit: -42.32,
    dayProfitRate: -2.23,
    totalProfit: -1882.32,
    totalProfitRate: -50.40,
    costPrice: 37.343,
    currentPrice: 18.520,
    holdCount: 100,
    position: 2.7,
    holdDays: 312,
    symbol: '601012.SH'
  }
]);

const formatNumber = (val) => {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const getReturnClass = (val) => {
  if (val > 0) return 'text-up';
  if (val < 0) return 'text-down';
  return '';
};

const handleAction = (btn) => {
  uni.showToast({ title: '点击了' + btn.label, icon: 'none' });
};
</script>

<style lang="scss" scoped>
.broker-account-container {
  min-height: 100vh;
  background-color: $bg-page;
}

/* 1. 资产卡片 */
.summary-section {
  padding: $spacing-md $spacing-md 0;
}

.asset-card {
  padding: 40rpx;

  .brand-box {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 32rpx;

    .logo { width: 32rpx; height: 32rpx; background: $primary; border-radius: 6rpx; }
    .name { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
    .account-id { font-size: 24rpx; color: $text-placeholder; }
  }

  .main-assets {
    margin-bottom: 32rpx;
    .label-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 8rpx; }
    .label { font-size: 24rpx; color: $text-muted; }
    .value { font-size: 60rpx; font-weight: $fw-bold; color: $text-main; }
  }

  .profit-info-grid {
    display: flex;
    border-top: 1rpx solid $gray-100;
    padding-top: 24rpx;
    .info-item {
      flex: 1;
      .label { font-size: 22rpx; color: $text-muted; display: block; margin-bottom: 4rpx; }
      .val { font-size: 32rpx; font-weight: $fw-semibold; }
      .val-group {
        display: flex; align-items: baseline; justify-content: flex-end; gap: 8rpx;
        .sub-val { font-size: 24rpx; }
      }
    }
    .right-align { text-align: right; }
  }

  .funds-sub-row {
    display: flex;
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 1rpx dashed $gray-100;
    .sub-item {
      flex: 1;
      .label { font-size: 20rpx; color: $text-muted; display: block; }
      .val { font-size: 24rpx; color: $text-sub; font-weight: $fw-medium; }
      &.center { text-align: center; }
      &.right { text-align: right; }
    }
  }
}

/* 2. 操作区 */
.action-grid {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 0;
  .action-btn {
    display: flex; flex-direction: column; align-items: center; gap: 12rpx;
    .icon-wrapper {
      width: 90rpx; height: 90rpx; background: $bg-white; border-radius: 20rpx;
      box-shadow: $shadow-card; @include flex-center;
      .btn-text { font-size: 28rpx; font-weight: $fw-bold; color: $primary; }
    }
    .btn-label { font-size: 24rpx; color: $text-sub; }
  }
}

/* 3. 持仓列表 - 核心滚动设计 */
.holdings-section {
  margin: 0 $spacing-md 40rpx;
  padding: 0; // 内部由表格撑开
  overflow: hidden;

  .section-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 24rpx 32rpx; border-bottom: 1rpx solid $gray-100;
    .title { font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; }
  }
}

.horizontal-scroll {
  width: 100%;
  .table-container {
    display: flex;
    flex-direction: column;
    min-width: 950rpx; // 确保总宽度够 8 列显示
  }
}

.table-row {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid $bg-page;
  
  &.header {
    background-color: $gray-50;
    height: 70rpx;
    .col { font-size: 22rpx; color: $text-muted; }
  }
  
  &.data-row:last-child { border-bottom: none; }
}

.col {
  flex-shrink: 0;
  padding: 20rpx 16rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  // 第一列固定逻辑
  &.col-fixed {
    width: 220rpx;
    position: sticky;
    left: 0;
    background-color: $bg-white;
    z-index: 5;
    &::after {
      content: ''; position: absolute; right: 0; top: 0; bottom: 0;
      width: 6rpx; background: linear-gradient(to right, rgba(0,0,0,0.03), transparent);
    }
  }
  
  &.col-data { width: 180rpx; text-align: right; align-items: flex-end; }
  &.col-sm { width: 130rpx; text-align: right; align-items: flex-end; }
  &.col-md { width: 180rpx; text-align: right; align-items: flex-end; }
}

.stock-name { font-size: 28rpx; font-weight: $fw-semibold; color: $primary; margin-bottom: 4rpx; }
.stock-market-val { font-size: 22rpx; color: $text-muted; }

.val-top { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; }
.val-bottom { font-size: 22rpx; color: $text-muted; margin-top: 4rpx; }
.code { color: $text-placeholder; font-size: 20rpx; }

.animate-fade-in { animation: fadeIn 0.6s ease-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>