<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="hero-card">
          <view class="card-header">
            <view class="title-row">
              <text class="card-title">账户余额</text>
              <image src="/static/assets/actions/edit.png" class="icon-edit" @tap="handleEdit" />
            </view>
            <view class="balance-row">
              <view class="amount-wrapper">
                <text class="currency">¥</text>
                <text class="balance-amount font-mono" @tap="handleEdit">{{ balance }}</text>
              </view>
            </view>
          </view>

          <view class="stats-row">
            <view class="stat-item">
              <text class="stat-label">7日年化收益率</text>
              <text class="stat-value font-mono" @tap="handleAnnualized">{{ annualizedRate }}</text>
              <view class="tag-wrap">
                <view class="tag-chip">
                  <text class="tag-text">{{ fundName }}</text>
                </view>
              </view>
            </view>
            <view class="stat-item right">
              <text class="stat-label">累计收益</text>
              <text class="stat-value font-mono" @tap="handleProfit">{{ totalProfit }}</text>
              <view class="tag-wrap">
                <view class="tag-chip light">
                  <text class="tag-text">{{ yesterdayProfit }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="section-header margin-top-lg">
          <view class="section-title-wrap">
            <h2 class="section-title">最近7天收益</h2>
          </view>
          <text class="chart-unit">单位: 元</text>
        </view>
        <view class="chart-card">
          <view class="line-chart">
            <qiun-data-charts
              v-if="profitChartData.series && profitChartData.series.length > 0"
              type="profitArea"
              :chartData="profitChartData"
              canvasId="profitAreaChart"
              :inScrollView="false"
              :reshow="false"
            />
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const balance = ref('0.00')
const fundName = ref('中银活期宝A')
const annualizedRate = ref('1.0100%')
const totalProfit = ref('¥0.00')
const yesterdayProfit = ref('昨日 ¥0.00')

const profitData = ref([
  { date: '06-14', label: '周六', value: 123.12 },
  { date: '06-15', label: '周日', value: 112.15 },
  { date: '06-16', label: '周一', value: 108.08 },
  { date: '06-17', label: '周二', value: 106.22 },
  { date: '06-18', label: '周三', value: 104.18 },
  { date: '06-19', label: '周四', value: 102.25 },
  { date: '06-20', label: '周五', value: 125.38 }
])

const profitChartData = ref({ categories: [], series: [] })

onMounted(() => {
  const rawData = profitData.value
  profitChartData.value = JSON.parse(JSON.stringify({
    categories: rawData.map(i => i.label),
    series: [{
      name: '收益',
      data: rawData.map(i => Number(i.value.toFixed(2)))
    }]
  }))
})

const handleEdit = () => {
  uni.showModal({
    title: '编辑账户余额',
    editable: true,
    placeholderText: '请输入余额',
    content: balance.value,
    success: (res) => {
      if (res.confirm && res.content !== undefined) {
        balance.value = res.content
      }
    }
  })
}

const handleAnnualized = () => {
  uni.showModal({
    title: '编辑7日年化收益率',
    editable: true,
    placeholderText: '请输入年化收益率',
    content: annualizedRate.value,
    success: (res) => {
      if (res.confirm && res.content !== undefined) {
        annualizedRate.value = res.content
      }
    }
  })
}

const handleProfit = () => {
  uni.showModal({
    title: '编辑累计收益',
    editable: true,
    placeholderText: '请输入累计收益',
    content: totalProfit.value,
    success: (res) => {
      if (res.confirm && res.content !== undefined) {
        totalProfit.value = res.content
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
}

.scroll {
  height: 100vh;
  padding-bottom: $spacing-8;
}

.content {
  padding: $spacing-4;
}

.hero-card {
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-6;
  box-shadow: $shadow-soft;
  border: 2rpx solid $surface-container;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-3;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  flex: 1;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
  line-height: 40rpx;
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  justify-content: center;
}

.card-title {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
  letter-spacing: 2rpx;
}

.icon-edit {
  width: 40rpx;
  height: 40rpx;
}

.balance-row {
  margin-top: 0;
}

.amount-wrapper {
  padding: $spacing-2 0;
  text-align: center;
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
}

.balance-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: 96rpx;
  font-weight: $font-weight-bold;
  line-height: 1;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-top: $spacing-6;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-1;

  &.right {
    align-items: center;
  }
}

.tag-wrap {
  display: inline-flex;
  justify-content: center;
  margin-top: $spacing-1;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: $primary-container;
  padding: 8rpx 24rpx;
  border-radius: $rounded-full;
  box-shadow: 0 4rpx 12rpx rgba(0, 103, 84, 0.15);

  &.light {
    background: $surface-container;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.tag-text {
  font-size: $font-size-xs;
  color: $on-primary;
  font-weight: $font-weight-medium;

  .light & {
    color: $on-surface-variant;
  }
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
}

.stat-value {
  font-size: $font-size-num-data;
  color: $on-surface;
  font-weight: $font-weight-bold;
}

.margin-top-lg {
  margin-top: $spacing-6;
}

.chart-unit {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.chart-card {
  margin-top: 0;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-3;
  box-shadow: $shadow-soft;
  border: 2rpx solid $surface-container;
}

.line-chart {
  position: relative;
  height: 420rpx;
  width: 100%;

  :deep(.chartsview),
  :deep(.chartsbox) {
    width: 100% !important;
    height: 100% !important;
  }
}
</style>
