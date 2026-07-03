<template>
  <view class="page monthly-page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Date Header -->
        <view class="date-header">
          <text class="date-text">2023年10月 · 月度报告</text>
        </view>

        <!-- Income & Expense Section -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">收支概览</text>
            <text class="section-subtitle">本月净储蓄</text>
          </view>

          <view class="net-savings">
            <text class="savings-amount">¥42,850</text>
            <text class="savings-change">+12.4%</text>
          </view>

          <!-- Expenditure Doughnut Chart -->
          <view class="doughnut-section">
            <view class="doughnut-wrapper">
              <canvas canvas-id="doughnutChart" class="doughnut-chart"></canvas>
              <view class="doughnut-center">
                <text class="doughnut-label">支出总额</text>
                <text class="doughnut-amount">55k</text>
              </view>
            </view>
            <view class="doughnut-legend">
              <view v-for="item in doughnutData" :key="item.name" class="legend-item">
                <view class="legend-dot" :style="{ background: item.color }"></view>
                <text class="legend-name">{{ item.name }}</text>
                <text class="legend-percent">{{ item.percent }}</text>
              </view>
            </view>
          </view>

          <!-- Income & Expense Bars -->
          <view class="bars-section">
            <view class="bar-item">
              <view class="bar-header">
                <text class="bar-label">总收入</text>
                <text class="bar-value">¥98,200</text>
              </view>
              <view class="bar-track">
                <view class="bar-fill income" style="width: 100%"></view>
              </view>
            </view>
            <view class="bar-item">
              <view class="bar-header">
                <text class="bar-label">总支出</text>
                <text class="bar-value">¥55,350</text>
              </view>
              <view class="bar-track">
                <view class="bar-fill expense" style="width: 56%"></view>
              </view>
            </view>
          </view>

          <!-- Top 5 Expenditure -->
          <view class="top-expenses">
            <text class="top-title">支出 Top 5</text>
            <view class="expense-list">
              <view v-for="item in topExpenses" :key="item.name" class="expense-item">
                <text class="expense-name">{{ item.name }}</text>
                <view class="expense-bar-track">
                  <view class="expense-bar-fill" :style="{ width: item.percent + '%' }"></view>
                </view>
                <text class="expense-amount">¥{{ item.amount }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Investment Income Section -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">投资收益</text>
            <text class="icon-text">[趋势]</text>
          </view>

          <view class="investment-summary">
            <text class="investment-label">本月实现收益</text>
            <view class="investment-amount">
              <text class="amount">¥12,480.00</text>
              <text class="change">+3.8%</text>
            </view>
          </view>

          <!-- Monthly Comparison Chart -->
          <view class="chart-section">
            <text class="chart-title">近三月收益对比</text>
            <view class="bar-chart">
              <view v-for="month in monthlyData" :key="month.name" class="chart-bar-wrapper">
                <view class="chart-bar" :class="{ active: month.active }" :style="{ height: month.height + '%' }"></view>
                <text class="chart-label" :class="{ active: month.active }">{{ month.name }}</text>
              </view>
            </view>
          </view>

          <!-- Asset Distribution -->
          <view class="asset-distribution">
            <text class="distribution-title">资产分布 (市值)</text>
            <view class="asset-grid">
              <view v-for="asset in assetData" :key="asset.name" class="asset-card">
                <text class="asset-label">{{ asset.name }}</text>
                <view class="asset-info">
                  <text class="asset-value">¥{{ asset.value }}</text>
                  <text class="asset-change" :class="asset.changeClass">{{ asset.change }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Top Performing Accounts -->
          <view class="top-accounts">
            <text class="top-title">收益领先账户</text>
            <view class="account-list">
              <view v-for="account in topAccounts" :key="account.name" class="account-item">
                <view class="account-icon">
                  <text class="icon-text">[图表]</text>
                </view>
                <view class="account-info">
                  <text class="account-name">{{ account.name }}</text>
                  <text class="account-type">{{ account.type }}</text>
                </view>
                <view class="account-earnings">
                  <text class="earnings-amount">{{ account.amount }}</text>
                  <text class="earnings-change">{{ account.change }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Summary Row -->
          <view class="summary-row">
            <view class="summary-item">
              <text class="summary-label">年度累计收益</text>
              <text class="summary-value">¥84,200</text>
            </view>
            <view class="summary-item right">
              <text class="summary-label">年化收益率</text>
              <text class="summary-value">8.4%</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const doughnutData = [
  { name: '居住', percent: '40%', color: '#006754' },
  { name: '教育', percent: '24%', color: '#8b6e3a' },
  { name: '餐饮', percent: '20%', color: '#b7102a' },
  { name: '其他', percent: '16%', color: '#bec9c4' }
]

const topExpenses = [
  { name: '房贷', amount: '15,000', percent: 85 },
  { name: '学费', amount: '12,000', percent: 60 },
  { name: '超市', amount: '4,500', percent: 35 },
  { name: '餐饮', amount: '3,200', percent: 25 },
  { name: '交通', amount: '2,100', percent: 15 }
]

const monthlyData = [
  { name: '8月', height: 60 },
  { name: '9月', height: 75 },
  { name: '10月', height: 100, active: true }
]

const assetData = [
  { name: '股票', value: '450k', change: '+5.2%', changeClass: 'profit' },
  { name: '基金', value: '280k', change: '-1.2%', changeClass: 'loss' },
  { name: '保险', value: '120k', change: '0.0%', changeClass: '' },
  { name: '现金', value: '85k', change: '+0.4%', changeClass: 'profit' }
]

const topAccounts = [
  { name: '招商证券 A 股', type: '股票投资', amount: '+¥8,400', change: '+12.4%', icon: 'show_chart' },
  { name: '腾讯理财通', type: '中高风险基金', amount: '+¥2,150', change: '+4.2%', icon: 'analytics' }
]
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.date-header {
  margin-bottom: $stack-gap-md;
}

.date-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-medium;
  color: $outline;
}

.section-card {
  padding: $spacing-6;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
  margin-bottom: $stack-gap-lg;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $stack-gap-md;
}

.section-title {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
  color: $primary;
}

.section-subtitle {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.net-savings {
  display: flex;
  align-items: baseline;
  gap: $stack-gap-sm;
  margin-bottom: $section-margin;
}

.savings-amount {
  font-family: $font-family-mono;
  font-size: 64rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.savings-change {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $secondary;
}

.doughnut-section {
  display: flex;
  align-items: center;
  gap: $stack-gap-lg;
  margin-bottom: $section-margin;
}

.doughnut-wrapper {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.doughnut-chart {
  width: 100%;
  height: 100%;
}

.doughnut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.doughnut-label {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  color: $outline;
}

.doughnut-amount {
  font-family: $font-family-mono;
  font-size: 28rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.doughnut-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $stack-gap-sm;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: $rounded-full;
}

.legend-name {
  flex: 1;
  font-size: 24rpx;
  color: $outline;
}

.legend-percent {
  font-family: $font-family-mono;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.bars-section {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
  margin-bottom: $section-margin;
}

.bar-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bar-label {
  font-size: 24rpx;
  color: $outline;
}

.bar-value {
  font-family: $font-family-mono;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.bar-track {
  height: 12rpx;
  border-radius: $rounded-full;
  background: $surface-container-high;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: $rounded-full;
  transition: width 0.5s ease;

  &.income {
    background: $secondary;
  }

  &.expense {
    background: $primary;
  }
}

.top-expenses {
  padding-top: $section-margin;
  border-top: 1rpx solid $surface-variant;
}

.top-title {
  display: block;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $primary;
  margin-bottom: $stack-gap-md;
}

.expense-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-sm;
}

.expense-item {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
}

.expense-name {
  width: 80rpx;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $outline;
}

.expense-bar-track {
  flex: 1;
  height: 16rpx;
  border-radius: $rounded-full;
  background: $surface-container;
  overflow: hidden;
}

.expense-bar-fill {
  height: 100%;
  border-radius: $rounded-full;
  background: rgba($primary, 0.6);
}

.expense-amount {
  font-family: $font-family-mono;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.investment-summary {
  margin-bottom: $section-margin;
}

.investment-label {
  display: block;
  font-size: 24rpx;
  color: $outline;
  margin-bottom: $spacing-1;
}

.investment-amount {
  display: flex;
  align-items: baseline;
  gap: $stack-gap-sm;
}

.amount {
  font-family: $font-family-mono;
  font-size: 64rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.change {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $secondary;
}

.chart-section {
  margin-bottom: $section-margin;
}

.chart-title {
  display: block;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  margin-bottom: $stack-gap-md;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200rpx;
  padding: 0 $spacing-2;
}

.chart-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
}

.chart-bar {
  width: 48rpx;
  border-radius: $rounded-lg $rounded-lg 0 0;
  background: $surface-container-high;
  transition: height 0.5s ease;

  &.active {
    background: rgba($secondary, 0.8);
  }
}

.chart-label {
  font-size: 20rpx;
  color: $outline;

  &.active {
    font-weight: $font-weight-bold;
    color: $secondary;
  }
}

.asset-distribution {
  padding-top: $section-margin;
  border-top: 1rpx solid $surface-variant;
  margin-bottom: $section-margin;
}

.distribution-title {
  display: block;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $primary;
  margin-bottom: $stack-gap-md;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-sm;
}

.asset-card {
  padding: $spacing-3;
  border-radius: $rounded-lg;
  background: $surface-container-low;
}

.asset-label {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.asset-info {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: $spacing-1;
}

.asset-value {
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.asset-change {
  font-size: 20rpx;
  font-weight: $font-weight-bold;

  &.profit {
    color: $secondary;
  }

  &.loss {
    color: $primary;
  }
}

.top-accounts {
  padding-top: $section-margin;
  border-top: 1rpx solid $surface-variant;
  margin-bottom: $section-margin;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
}

.account-item {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
}

.account-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: $rounded-full;
  background: $surface-container;
  display: flex;
  align-items: center;
  justify-content: center;

  .icon {
    font-size: 28rpx;
  }
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.account-name {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.account-type {
  font-size: 20rpx;
  color: $outline;
}

.account-earnings {
  text-align: right;
}

.earnings-amount {
  display: block;
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $secondary;
}

.earnings-change {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  color: $secondary;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding-top: $section-margin;
  border-top: 1rpx solid $surface-variant;
}

.summary-item {
  display: flex;
  flex-direction: column;

  &.right {
    align-items: flex-end;
  }
}

.summary-label {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.summary-value {
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $on-surface;

  &.profit {
    color: $secondary;
  }
}


</style>