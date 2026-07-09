<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <text class="card-label">{{ fund.name }}</text>
            <text class="balance-amount font-mono">¥{{ fund.amount }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">持有收益</text>
              <text class="stat-value font-mono" :class="fund.totalPnl && fund.totalPnl.startsWith('+') ? 'text-profit' : 'text-loss'">{{ fund.totalPnl }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">今日收益</text>
              <text class="stat-value font-mono" :class="fund.todayPnl && fund.todayPnl.startsWith('+') ? 'text-profit' : 'text-loss'">{{ fund.todayPnl }}</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">基本信息</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">基金名称</text>
              <text class="info-value">{{ fund.name }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">持有金额</text>
              <text class="info-value font-mono">¥{{ fund.amount }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">持有收益率</text>
              <text class="info-value font-mono" :class="fund.pnl > 0 ? 'text-profit' : 'text-loss'">{{ fund.pnl > 0 ? '+' : '' }}{{ fund.pnl }}%</text>
            </view>
            <view class="info-row">
              <text class="info-label">今日收益</text>
              <text class="info-value font-mono" :class="fund.todayPnl && fund.todayPnl.startsWith('+') ? 'text-profit' : 'text-loss'">{{ fund.todayPnl }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">持有收益</text>
              <text class="info-value font-mono" :class="fund.totalPnl && fund.totalPnl.startsWith('+') ? 'text-profit' : 'text-loss'">{{ fund.totalPnl }}</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">交易信息</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">状态</text>
              <text class="info-value">{{ fund.status }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">净值日期</text>
              <text class="info-value font-mono">{{ fund.date }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const fund = ref({})

onLoad((options) => {
  if (options?.data) {
    try {
      fund.value = JSON.parse(decodeURIComponent(options.data))
    } catch (e) {
      console.error('解析数据失败', e)
    }
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
  padding-bottom: $spacing-8;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: $outline;
}

.balance-amount {
  font-size: $font-size-num-display;
  font-weight: 900;
  color: $on-surface;
  letter-spacing: -2rpx;
}

.font-mono {
  font-family: $font-family-mono;
}

.stats-grid {
  display: flex;
  gap: $spacing-8;
  margin-top: $spacing-4;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.stat-label {
  font-size: $font-size-body-sm;
  color: $outline;
}

.stat-value {
  font-size: $font-size-lg;
  font-weight: 600;
}

.text-profit {
  color: $profit;
}

.text-loss {
  color: $loss;
}

.info-section {
  margin-top: $spacing-6;
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-5;
  box-shadow: $shadow-sm;
  border: 2rpx solid rgba($surface-variant, 0.2);
}

.section-header {
  margin-bottom: $stack-gap-sm;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-3 0;
  border-bottom: 2rpx solid rgba($surface-variant, 0.15);

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.info-value {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}
</style>
