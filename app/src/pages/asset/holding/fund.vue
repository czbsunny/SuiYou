<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Portfolio Hero Card -->
        <view class="hero-card">
          <view class="card-header">
            <text class="card-label">基金总资产 (元)</text>
            <text class="balance-amount font-mono">504,285.00</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">持有收益</text>
              <text class="stat-value font-mono text-profit">+24,800.50</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">今日收益</text>
              <text class="stat-value font-mono text-profit">+1,562.20</text>
            </view>
          </view>
        </view>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">持有明细</h2>
            <span class="count-badge">2只</span>
          </div>
          <button class="add-btn" @tap="handleAddFund">
            <image src="/static/images/add.png" class="icon-add" />
            <span class="add-text">添加基金</span>
          </button>
        </div>

        <!-- Holdings List -->
        <div class="holding-list">
          <view 
            v-for="fund in fundList" 
            :key="fund.id" 
            class="holding-card"
            @tap="handleFundTap(fund)"
          >
            <div class="card-top">
              <div class="fund-info">
                <h3 class="fund-name">{{ fund.name }}</h3>
                <div class="fund-meta">
                  <span class="status-tag">{{ fund.status }}</span>
                  <span class="date-text">{{ fund.date }}</span>
                </div>
              </div>
              <span class="pnl-value" :class="{ profit: fund.pnl > 0 }">{{ fund.pnl > 0 ? '+' : '' }}{{ fund.pnl }}%</span>
            </div>
            <div class="card-bottom">
              <div class="info-item">
                <span class="info-label">持有金额</span>
                <span class="info-value font-mono">{{ fund.amount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">今日收益</span>
                <span class="info-value font-mono" :class="{ profit: fund.todayPnl > 0 }">
                  {{ fund.todayPnl > 0 ? '+' : '' }}{{ fund.todayPnl }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">总收益</span>
                <span class="info-value font-mono" :class="{ profit: fund.totalPnl > 0, loss: fund.totalPnl < 0 }">
                  {{ fund.totalPnl > 0 ? '+' : '' }}{{ fund.totalPnl }}
                </span>
              </div>
            </div>
          </view>
        </div>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const fundList = ref([
  {
    id: '1',
    name: '招商中证500指数增强',
    status: '盘中估值',
    date: '10-27',
    pnl: 0.71,
    amount: '35,189.00',
    todayPnl: '+250.40',
    totalPnl: '-1,260.75'
  },
  {
    id: '2',
    name: '纳斯达克100ETF(QDII)',
    status: '盘中估值',
    date: '10-27',
    pnl: 3.58,
    amount: '130,849.00',
    todayPnl: '+1,102.32',
    totalPnl: '+1,202.32'
  }
])

const handleAddFund = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/add-fund'
  })
}

const handleFundTap = (fund) => {
  uni.showToast({ title: `${fund.name}详情`, icon: 'none' })
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
  border-radius: $rounded-md;
  padding: $spacing-5;
  box-shadow: $shadow-soft;
  margin-bottom: $spacing-6;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
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
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-4;
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
  margin-bottom: $spacing-1;
}

.stat-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $secondary;
}

.text-profit {
  color: $profit;
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

.count-badge {
  background: rgba($surface-variant, 0.3);
  color: $on-surface-variant;
  font-size: $font-size-xs;
  padding: 4rpx 16rpx;
  border-radius: $rounded-default;
  font-family: $font-family-mono;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-1;
  background: $primary;
  color: $on-primary;
  border-radius: $rounded-full;
  padding: $spacing-2 $spacing-4;
  height: 56rpx;
  line-height: 1;
  margin-left: auto;
}

.icon-add {
  height: 24rpx;
  width: 24rpx;
}

.add-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  line-height: 1;
}

.holding-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.holding-card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-4;
  box-shadow: $shadow-sm;
  border: 2rpx solid rgba($surface-variant, 0.2);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.fund-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.fund-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.fund-meta {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.status-tag {
  background: rgba($surface-variant, 0.3);
  color: $on-surface-variant;
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: $rounded-sm;
}

.date-text {
  font-size: 20rpx;
  color: rgba($on-surface-variant, 0.6);
  font-family: $font-family-mono;
}

.pnl-value {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $loss;
  
  &.profit {
    color: $profit;
  }
}

.card-bottom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-2;
  margin-top: $spacing-4;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  
  &:nth-child(2) {
    align-items: center;
  }
  
  &:nth-child(3) {
    align-items: flex-end;
  }
}

.info-label {
  font-size: 22rpx;
  color: rgba($on-surface-variant, 0.7);
}

.info-value {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
  
  &.profit {
    color: $profit;
  }
  
  &.loss {
    color: $loss;
  }
}
</style>