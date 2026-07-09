<template>
  <view class="page monthly-page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Income & Expense Section -->
        <view class="section-header">
          <text class="section-title">收支概览</text>
          <text class="section-date">2023年10月</text>
        </view>

        <view class="section-card">
          <view class="card-subtitle">本月现金流</view>

          <!-- Cash Flow Bars -->
          <view class="cashflow-list">
            <view v-for="item in cashFlowList" :key="item.name" class="cashflow-item">
              <text class="cashflow-name">{{ item.name }}</text>
              <view class="cashflow-bar-track">
                <view class="cashflow-bar-fill" :style="{ width: item.percent + '%', background: item.color }"></view>
              </view>
              <text class="cashflow-amount">¥{{ item.amount }}</text>
            </view>
          </view>

          <!-- Expenditure Ring Chart -->
          <view class="expense-ring-section">
            <qiun-data-charts
              type="expenseRing"
              :chartData="ringChartData"
              :opts="ringChartOpts"
              style="width: 100%; height: 200rpx;"
            />
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
        <view class="section-header">
          <text class="section-title">投资收益</text>
        </view>

        <view class="section-card">
          <view class="card-header">
            <text class="card-subtitle">本月实现收益</text>
          </view>

          <view class="investment-summary">
            <view class="investment-amount">
              <text class="amount">¥12,480.00</text>
              <text class="change">+3.8%</text>
            </view>
          </view>

          <!-- Monthly Comparison Chart -->
          <view class="chart-section">
            <text class="chart-title">近六月收益对比</text>
            <qiun-data-charts
              type="investIncomeColumn"
              :chartData="monthlyChartData"
              :opts="monthlyChartOpts"
              style="width: 100%; height: 300rpx;"
            />
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
const ringChartData = {
  categories: ['居住', '教育', '餐饮', '交通', '娱乐', '购物', '医疗', '其他'],
  series: [
    { name: '居住', data: 30, color: '#006754' },
    { name: '教育', data: 18, color: '#8b6e3a' },
    { name: '餐饮', data: 15, color: '#b7102a' },
    { name: '交通', data: 10, color: '#5470C6' },
    { name: '娱乐', data: 8, color: '#91CC75' },
    { name: '购物', data: 7, color: '#FAC858' },
    { name: '医疗', data: 6, color: '#EE6666' },
    { name: '其他', data: 6, color: '#bec9c4' }
  ]
}

const ringChartOpts = {
  color: ['#006754', '#8b6e3a', '#b7102a', '#5470C6', '#91CC75', '#FAC858', '#EE6666', '#bec9c4'],
  padding: [5, 5, 5, 5],
  dataLabel: false,
  legend: {
    show: true,
    position: 'right'
  },
  title: {
      name: `55k`,
      fontSize: 16,
      color: '#1a1c1a',
    },
  subtitle: {
    name: '支出金额',
    fontSize: 16,
    color: '#1a1c1a',
  },
  extra: {
    ring: {
      ringWidth: 24,
      activeOpacity: 0.5
    }
  }
}

const topExpenses = [
  { name: '房贷', amount: '15,000', percent: 85 },
  { name: '学费', amount: '12,000', percent: 60 },
  { name: '超市', amount: '4,500', percent: 35 },
  { name: '餐饮', amount: '3,200', percent: 25 },
  { name: '交通', amount: '2,100', percent: 15 }
]

const cashFlowList = [
  { name: '收入', amount: '12,580', percent: 100, color: '#006754' },
  { name: '支出', amount: '8,460', percent: 67, color: '#EE6666' },
  { name: '结余', amount: '4,120', percent: 33, color: '#5470C6' }
]

const monthlyChartData = {
  categories: ['2月', '3月', '4月', '5月', '6月', '7月'],
  series: [
    {
      name: '月度收益',
      data: [
        { value: 15000, color: '#006754' },
        { value: -5000, color: '#EE6666' },
        { value: 8000, color: '#006754' },
        { value: -3000, color: '#EE6666' },
        { value: 12000, color: '#006754' },
        { value: -2000, color: '#EE6666' }
      ]
    }
  ]
}

// 计算y轴范围：取整到整百/整千/整万，增加10%缓冲，分4段
function niceStep(step) {
  if (step === 0) return 0
  const magnitude = Math.pow(10, Math.floor(Math.log10(Math.abs(step))))
  const residual = Math.abs(step) / magnitude
  let nice
  if (residual <= 1.5) nice = 1
  else if (residual <= 3.5) nice = 2
  else if (residual <= 7.5) nice = 5
  else nice = 10
  return nice * magnitude
}

const chartValues = monthlyChartData.series[0].data.map(d => d.value)
let rawMin = Math.min(...chartValues)
let rawMax = Math.max(...chartValues)
const pad = 0.1
const paddedMin = rawMin - Math.abs(rawMin) * pad
const paddedMax = rawMax + Math.abs(rawMax) * pad
const step = niceStep((paddedMax - paddedMin) / 4)
const axisMin = Math.floor(paddedMin / step) * step
const axisMax = Math.ceil(paddedMax / step) * step

const monthlyChartOpts = {
  dataLabel: false,
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    axisLine: false,
    gridType: 'dash',
    splitNumber: 4,
    min: axisMin,
    max: axisMax,
    data: [{
      format: 'money'
    }]
  },
  legend: {
    show: false
  }
}

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

.section-card {
  padding: $spacing-6;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
  margin-bottom: $stack-gap-lg;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: $stack-gap-sm;
}

.section-card + .section-header {
  margin-top: $stack-gap-md;
}

.section-title {
  color: $on-surface;
  font-size: $font-size-title-sm;
  font-weight: 900;
}

.section-date {
  color: $primary;
  font-size: $font-size-body-sm;
  font-weight: 500;
}

.section-link {
  color: $primary;
  font-size: $font-size-body-sm;
  font-weight: 500;
}

.card-subtitle {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.expense-ring-section {
  margin-bottom: $stack-gap-md;
}

.cashflow-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-sm;
  margin-bottom: $stack-gap-md;
}

.cashflow-item {
  display: flex;
  align-items: center;
  gap: $stack-gap-xs;
}

.cashflow-name {
  width: 60rpx;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $outline;
}

.cashflow-bar-track {
  flex: 1;
  height: 16rpx;
  border-radius: $rounded-full;
  // background: $surface-container;
  overflow: hidden;
}

.cashflow-bar-fill {
  height: 100%;
  border-radius: $rounded-full;
  transition: width 0.5s ease;
}

.cashflow-amount {
  font-family: $font-family-mono;
  font-size: 24rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.top-expenses {
  padding-top: $stack-gap-md;
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
  width: 60rpx;
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
  font-size: 56rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.change {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $secondary;
}

.chart-section {
  margin-bottom: $stack-gap-md;
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

.asset-distribution {
  padding-top: $stack-gap-md;
  margin-bottom: $stack-gap-md;
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
  width:56rpx;
  height: 56rpx;
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
  padding-top: $stack-gap-md;
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