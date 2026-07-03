<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <view class="title-row">
              <text class="card-label">{{ stock.name }}</text>
              <text class="stock-code font-mono">{{ stock.code }}</text>
            </view>
            <text class="balance-amount font-mono">¥{{ stock.marketValue }}</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">总盈亏</text>
              <text class="stat-value font-mono" :class="stock.totalPnl > 0 ? 'text-profit' : 'text-loss'">{{ stock.totalPnl > 0 ? '+' : '' }}{{ stock.totalPnl }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">今日盈亏</text>
              <text class="stat-value font-mono" :class="stock.todayPnl > 0 ? 'text-profit' : 'text-loss'">{{ stock.todayPnl > 0 ? '+' : '' }}{{ stock.todayPnl }}</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">基本信息</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">股票名称</text>
              <text class="info-value">{{ stock.name }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">股票代码</text>
              <text class="info-value font-mono">{{ stock.code }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">市值</text>
              <text class="info-value font-mono">¥{{ stock.marketValue }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">现价</text>
              <text class="info-value font-mono">¥{{ stock.price }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">成本价</text>
              <text class="info-value font-mono">¥{{ stock.cost }}</text>
            </view>
          </view>
        </view>

        <view class="info-section">
          <view class="section-header">
            <h2 class="section-title">盈亏分析</h2>
          </view>
          <view class="info-list">
            <view class="info-row">
              <text class="info-label">持仓盈亏</text>
              <text class="info-value font-mono" :class="stock.totalPnl > 0 ? 'text-profit' : 'text-loss'">{{ stock.totalPnl > 0 ? '+' : '' }}{{ stock.totalPnl }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">盈亏比例</text>
              <text class="info-value font-mono" :class="stock.pnl > 0 ? 'text-profit' : 'text-loss'">{{ stock.pnl > 0 ? '+' : '' }}{{ stock.pnl }}%</text>
            </view>
            <view class="info-row">
              <text class="info-label">今日盈亏</text>
              <text class="info-value font-mono" :class="stock.todayPnl > 0 ? 'text-profit' : 'text-loss'">{{ stock.todayPnl > 0 ? '+' : '' }}{{ stock.todayPnl }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">仓位占比</text>
              <text class="info-value font-mono">{{ stock.position }}</text>
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

const stock = ref({})

onLoad((options) => {
  if (options?.data) {
    try {
      stock.value = JSON.parse(decodeURIComponent(options.data))
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

.title-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.card-label {
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: $outline;
}

.stock-code {
  font-size: $font-size-xs;
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
  margin-bottom: $spacing-4;
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
