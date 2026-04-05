<template>
  <view class="report-page">
    <!-- 1. 紧凑型顶部栏 -->
    <view class="header-section animate-fade-in">
      <view class="title-bar">
        <picker mode="date" fields="month" :value="currentMonth" @change="onMonthChange">
          <view class="month-selector">
            <text class="month-text">{{ formatMonthDisplay(currentMonth) }}</text>
            <uni-icons type="bottom" size="14" color="#322E2B"></uni-icons>
          </view>
        </picker>
        <text class="report-tag">复盘报告</text>
      </view>
    </view>

    <scroll-view scroll-y class="report-content">
      <!-- 2. 核心数据看板 -->
      <view class="dashboard-card card-warm animate-fade-in">
        <view class="main-metric">
          <text class="m-label">月末净资产</text>
          <view class="m-amount">
            <text class="m-currency">￥</text>
            <text class="m-value num-font">{{ formatMoney(reportData.netWorth) }}</text>
          </view>
        </view>
        
        <view class="metric-grid">
          <view class="g-item">
            <text class="g-label">本月增长</text>
            <text class="g-val num-font" :class="reportData.netWorthChange >= 0 ? 'text-up' : 'text-down'">
              {{ reportData.netWorthChange >= 0 ? '+' : '' }}{{ formatMoney(reportData.netWorthChange) }}
            </text>
          </view>
          <view class="g-item">
            <text class="g-label">本月结余率</text>
            <text class="g-val num-font">{{ reportData.savingsRate }}%</text>
          </view>
          <view class="g-item">
            <text class="g-label">本月投资收益</text>
            <text class="g-val num-font" :class="reportData.investProfit >= 0 ? 'text-up' : 'text-down'">
              {{ reportData.investProfit >= 0 ? '+' : '' }}{{ formatMoney(reportData.investProfit) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 3. 收支复盘：精美对比 -->
      <view class="section-title">收支复盘</view>
      <view class="cashflow-card card-warm">
        <view class="bar-chart">
          <view class="bar-wrapper">
            <view class="bar-income" :style="{ height: '180rpx' }">
              <text class="bar-val num-font">{{ formatMoney(reportData.totalIncome, 0) }}</text>
            </view>
            <text class="bar-label">收入</text>
          </view>
          <view class="bar-wrapper">
            <view class="bar-expense" :style="{ height: getExpenseHeight() }">
              <text class="bar-val num-font">{{ formatMoney(reportData.totalExpense, 0) }}</text>
            </view>
            <text class="bar-label">支出</text>
          </view>
        </view>
        
        <view class="expense-rank">
          <view v-for="(item, index) in reportData.topExpenses" :key="index" class="rank-item">
            <view class="r-info">
              <text class="r-name">{{ item.name }}</text>
              <text class="r-percent">{{ item.percent }}%</text>
            </view>
            <view class="r-track"><view class="r-bar" :style="{ width: item.percent + '%' }"></view></view>
          </view>
        </view>
      </view>

      <!-- 4. 投资英雄榜 (Top 3) -->
      <view class="section-title">投资盈亏排行</view>
      <view class="ranking-card card-warm">
        <view v-for="(item, index) in reportData.topInvestments" :key="index" class="rank-row">
          <view class="rank-num" :class="'rank-' + (index+1)">{{ index + 1 }}</view>
          <image :src="item.icon" class="inst-icon" mode="aspectFit" />
          <text class="inst-name">{{ item.name }}</text>
          <view class="inst-profit">
            <text class="p-val num-font" :class="item.profit >= 0 ? 'text-up' : 'text-down'">
              {{ item.profit >= 0 ? '+' : '' }}{{ formatMoney(item.profit) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 5. 资产健康度 -->
      <view class="section-title">健康度监测</view>
      <view class="health-card card-warm">
        <view class="health-grid">
          <view class="h-item">
            <text class="h-label">紧急预备金</text>
            <view class="h-status-box" :class="reportData.emergencyStatus">
              <text>{{ reportData.emergencyMonths }}个月开支</text>
            </view>
          </view>
          <view class="h-item">
            <text class="h-label">杠杆率 (负债比)</text>
            <view class="h-status-box safe">
              <text>{{ reportData.debtRatio }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 6. 财务自由度 (长期指标) -->
      <view class="section-title">财务自由进度</view>
      <view class="freedom-card card-warm">
        <view class="freedom-header">
          <view>
            <text class="f-title">被动收入覆盖率</text>
            <text class="f-desc">最近一年投资收益可覆盖 {{ reportData.passiveRatio }}% 的支出</text>
          </view>
          <text class="f-percent num-font">{{ reportData.passiveRatio }}%</text>
        </view>
        <view class="freedom-progress">
          <view class="f-inner" :style="{ width: Math.min(reportData.passiveRatio, 100) + '%' }"></view>
        </view>
        <view class="freedom-milestone">
          <uni-icons type="flag" size="14" color="#a3b0ad"></uni-icons>
          <text>距离终点：财务自由 (100%)</text>
        </view>
      </view>

      <view class="report-footer">
        <text>理财不是为了发财，而是为了生活得更从容。</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const currentMonth = ref('2024-10');

const reportData = reactive({
  netWorth: 1285600.42,
  netWorthChange: 12500.00,
  savingsRate: 42.5,
  investProfit: 8450.20,
  totalIncome: 35000,
  totalExpense: 20125,
  emergencyMonths: 6.5,
  emergencyStatus: 'safe', // safe, warn
  debtRatio: 18.5,
  passiveRatio: 41.9, // (投资收益 / 支出)
  topExpenses: [
    { name: '居家物业', percent: 35 },
    { name: '食品餐饮', percent: 22 },
    { name: '休闲娱乐', percent: 15 }
  ],
  topInvestments: [
    { name: '招商银行', profit: 4200.50, icon: '/static/images/account.png' },
    { name: '中信证券', profit: 3250.00, icon: '/static/images/account.png' },
    { name: '支付宝理财', profit: 1250.80, icon: '/static/images/account.png' }
  ]
});

const onMonthChange = (e) => {
  currentMonth.value = e.detail.value;
};

const formatMonthDisplay = (m) => {
  const [y, mm] = m.split('-');
  return `${y}年${parseInt(mm)}月`;
};

const formatMoney = (val, fixed = 2) => {
  return Math.abs(val || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: fixed,
    maximumFractionDigits: fixed
  });
};

const getExpenseHeight = () => {
  const ratio = reportData.totalExpense / reportData.totalIncome;
  return (180 * ratio) + 'rpx';
};
</script>

<style lang="scss" scoped>
.report-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

/* 1. 紧凑头部 */
.header-section {
  padding: 20rpx $spacing-md;
  .title-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .month-selector {
      display: flex;
      align-items: center;
      gap: 12rpx;
      .month-text { font-size: 36rpx; font-weight: $fw-bold; color: $text-main; }
    }
    .report-tag {
      font-size: 20rpx; color: $primary; font-weight: $fw-semibold;
      background: $primary-subtle; padding: 4rpx 16rpx; border-radius: 40rpx;
    }
  }
}

.report-content {
  flex: 1;
  padding: 0 $spacing-base;
}

/* 2. 看板卡片 */
.dashboard-card {
  padding: 40rpx;
  .main-metric {
    text-align: center;
    margin-bottom: 40rpx;
    .m-label { font-size: 24rpx; color: $text-muted; }
    .m-amount {
      display: flex; justify-content: center; align-items: baseline; margin-top: 8rpx;
      .m-currency { font-size: 32rpx; color: $text-main; margin-right: 4rpx; font-weight: $fw-bold; }
      .m-value { font-size: 56rpx; color: $text-main; font-weight: $fw-bold; }
    }
  }
  .metric-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    .g-item {
      display: flex; flex-direction: column; align-items: center;
      .g-label { font-size: 20rpx; color: $text-muted; margin-bottom: 8rpx; }
      .g-val { font-size: 26rpx; font-weight: $fw-semibold; color: $text-main; }
    }
  }
}

/* 3. 收支流向卡片 */
.cashflow-card {
  padding-top: $spacing-lg;
  .bar-chart {
    display: flex; justify-content: space-around; align-items: flex-end;
    height: 240rpx; padding: $spacing-sm 0rpx; border-bottom: 1rpx solid $gray-50;
    .bar-wrapper {
      display: flex; flex-direction: column; align-items: center; gap: 12rpx;
      .bar-income { 
        width: 64rpx; background: $bg-income; border: 2rpx solid $color-income; border-radius: 12rpx; 
        position: relative;
      }
      .bar-expense { 
        width: 64rpx; background: $bg-expense; border: 2rpx solid $color-expense; border-radius: 12rpx; 
        position: relative;
      }
      .bar-val { position: absolute; top: -36rpx; font-size: 18rpx; color: $text-muted; width: 150rpx; text-align: center;}
      .bar-label { font-size: 22rpx; color: $text-sub; }
    }
  }
  .expense-rank {
    padding-top: 24rpx;
    .rank-item {
      margin-bottom: 16rpx;
      .r-info { display: flex; justify-content: space-between; margin-bottom: 8rpx; font-size: 22rpx; color: $text-sub; }
      .r-track { height: 6rpx; background: $gray-100; border-radius: 10rpx; }
      .r-bar { height: 100%; background: $bar-orange; border-radius: 10rpx; }
    }
  }
}

/* 4. 排行榜卡片 */
.ranking-card {
  padding: 20rpx 32rpx;
  .rank-row {
    display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid $gray-50;
    &:last-child { border-bottom: none; }
    .rank-num {
      width: 36rpx; height: 36rpx; border-radius: 50%; font-size: 20rpx; font-weight: $fw-bold;
      background: $gray-100; color: $text-muted; @include flex-center; margin-right: 20rpx;
      &.rank-1 { background: $bg-profit; color: $color-profit; }
      &.rank-2 { background: $primary-subtle; color: $primary; }
      &.rank-3 { background: $gray-100; color: $text-sub; }
    }
    .inst-icon { width: 40rpx; height: 40rpx; margin-right: 16rpx; }
    .inst-name { font-size: 26rpx; color: $text-main; flex: 1; }
    .p-val { font-size: 28rpx; font-weight: $fw-bold; }
  }
}

/* 5. 健康度 */
.health-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 32rpx; padding: 24rpx;
  .h-item {
    display: flex; flex-direction: column; gap: 12rpx;
    .h-label { font-size: 22rpx; color: $text-muted; }
    .h-status-box {
      padding: 16rpx; border-radius: $radius-base; font-size: 24rpx; font-weight: $fw-bold;
      &.safe { background: $primary-subtle; color: $primary; }
      &.warn { background: $orange-50; color: $orange-600; }
    }
  }
}

/* 6. 财务自由度 */
.freedom-card {
  padding: 40rpx 32rpx;
  .freedom-header {
    display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24rpx;
    .f-title { font-size: 28rpx; font-weight: $fw-bold; color: $text-main; display: block;}
    .f-desc { font-size: 20rpx; color: $text-muted; }
    .f-percent { font-size: 40rpx; font-weight: $fw-bold; color: $primary; }
  }
  .freedom-progress {
    height: 12rpx; background: $gray-100; border-radius: 10rpx; margin-bottom: 20rpx;
    .f-inner { height: 100%; background: linear-gradient(to right, $primary-pressed, $primary); border-radius: 10rpx; }
  }
  .freedom-milestone {
    display: flex; align-items: center; gap: 8rpx;
    text { font-size: 20rpx; color: $text-placeholder; }
  }
}

.section-title { padding: $spacing-sm 0rpx; font-size: 28rpx; font-weight: $fw-bold; color: $text-main; }
.report-footer { padding: 60rpx 40rpx 100rpx; text-align: center; text { font-size: 22rpx; color: $text-placeholder; } }

.animate-fade-in { animation: fadeIn 0.8s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>