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
              <view class="stat-tags">
                <view class="tag-chip">
                  <text class="tag-text">{{ fundName }}</text>
                </view>
                <text class="stat-label">7日年化收益率</text>
                <text class="stat-value font-mono" @tap="handleAnnualized">{{ annualizedRate }}</text>
              </view>
            </view>
            <view class="stat-item right">
              <view class="stat-tags">
                <view class="tag-chip light">
                  <text class="tag-text">{{ yesterdayProfit }}</text>
                </view>
                <text class="stat-label">累计收益</text>
                <text class="stat-value font-mono" @tap="handleProfit">{{ totalProfit }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">最近7天收益</text>
            <text class="chart-unit">单位: 元</text>
          </view>
          <view class="chart-body">
            <view class="bar-chart">
              <view v-for="item in profitData" :key="item.date" class="bar-wrapper">
                <text class="bar-value font-mono" :class="{ profit: item.value > 0 }">
                  {{ item.value.toFixed(2) }}
                </text>
                <view class="bar-track">
                  <view
                    class="bar-fill"
                    :class="item.value >= 0 ? 'profit' : 'loss'"
                    :style="{ height: getBarHeight(item.value) + '%' }"
                  ></view>
                </view>
                <text class="bar-label">{{ item.label }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const balance = ref('0.00')
const fundName = ref('中银活期宝A')
const annualizedRate = ref('1.0100%')
const totalProfit = ref('¥0.00')
const yesterdayProfit = ref('昨日 ¥0.00')

const profitData = ref([
  { date: '06-14', label: '周六', value: 0.12 },
  { date: '06-15', label: '周日', value: 0.15 },
  { date: '06-16', label: '周一', value: 0.08 },
  { date: '06-17', label: '周二', value: 0.22 },
  { date: '06-18', label: '周三', value: 0.18 },
  { date: '06-19', label: '周四', value: 0.25 },
  { date: '06-20', label: '周五', value: 0.30 }
])

const maxValue = ref(Math.max(...profitData.value.map(i => Math.abs(i.value)), 1))

const getBarHeight = (value) => {
  const abs = Math.abs(value)
  const height = (abs / maxValue.value) * 100
  return Math.max(height, 6)
}

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

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.icon-edit {
  width: 40rpx;
  height: 40rpx;
}

.card-title {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
  letter-spacing: 2rpx;
}

.balance-row {
  margin-top: $spacing-3;
}

.amount-wrapper {
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

  &.right {
    display: flex;
    justify-content: flex-end;
  }
}

.stat-tags {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.stat-item.right .stat-tags {
  align-items: flex-end;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
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

.chart-card {
  margin-top: $spacing-5;
  background: $surface-container-lowest;
  border-radius: $rounded-lg;
  padding: $spacing-5;
  box-shadow: $shadow-soft;
  border: 2rpx solid $surface-container;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-5;
}

.chart-title {
  font-size: $font-size-title-sm;
  color: $on-surface;
  font-weight: $font-weight-bold;
}

.chart-unit {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.chart-body {
  padding: 0 $spacing-2;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 320rpx;
  gap: $spacing-1;
}

.bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-1;
  height: 100%;
}

.bar-value {
  font-size: 20rpx;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
  white-space: nowrap;

  &.profit {
    color: $profit;
  }
}

.bar-track {
  flex: 1;
  width: 100%;
  background: $surface-container;
  border-radius: $rounded-sm;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: $rounded-sm $rounded-sm 0 0;
  transition: height 0.4s ease;

  &.profit {
    background: linear-gradient(180deg, $profit 0%, rgba($profit, 0.6) 100%);
  }

  &.loss {
    background: linear-gradient(180deg, $loss 0%, rgba($loss, 0.6) 100%);
  }
}

.bar-label {
  font-size: 20rpx;
  color: $on-surface-variant;
  font-weight: $font-weight-medium;
}
</style>
