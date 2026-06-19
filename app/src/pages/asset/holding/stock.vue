<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Portfolio Hero Card -->
        <view class="hero-card">
          <view class="card-header">
            <text class="card-label">股票总市值 (HKD)</text>
            <text class="balance-amount font-mono">2,485,920.00</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">累计盈亏</text>
              <text class="stat-value font-mono text-profit">+458,200.00</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">今日盈亏</text>
              <text class="stat-value font-mono text-profit">+12,430.50</text>
            </view>
          </view>
        </view>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">持仓列表</h2>
            <span class="count-badge">3只</span>
          </div>
          <button class="add-btn" @tap="handleAddStock">
            <image src="/static/images/add.png" class="icon-add" />
            <span class="add-text">录入持仓</span>
          </button>
        </div>

        <!-- Holdings List -->
        <div class="holding-list">
          <view 
            v-for="stock in stockList" 
            :key="stock.id" 
            class="holding-card"
            @tap="handleStockTap(stock)"
          >
            <div class="card-top">
              <div class="stock-info">
                <h3 class="stock-name">{{ stock.name }}</h3>
                <span class="stock-code font-mono">{{ stock.code }}</span>
              </div>
              <span class="pnl-badge" :class="{ up: stock.pnl > 0, down: stock.pnl < 0 }">
                {{ stock.pnl > 0 ? '+' : '' }}{{ stock.pnl }}%
              </span>
            </div>
            <div class="card-bottom">
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">市值</span>
                  <span class="info-value font-mono">{{ stock.marketValue }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">今日盈亏</span>
                  <span class="info-value font-mono" :class="{ profit: stock.todayPnl > 0, loss: stock.todayPnl < 0 }">
                    {{ stock.todayPnl > 0 ? '+' : '' }}{{ stock.todayPnl }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">总盈亏</span>
                  <span class="info-value font-mono" :class="{ profit: stock.totalPnl > 0, loss: stock.totalPnl < 0 }">
                    {{ stock.totalPnl > 0 ? '+' : '' }}{{ stock.totalPnl }}
                  </span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">现价: <span class="font-mono">{{ stock.price }}</span></span>
                </div>
                <div class="info-item center">
                  <span class="info-label">成本: <span class="font-mono">{{ stock.cost }}</span></span>
                </div>
                <div class="info-item right">
                  <span class="info-label">仓位: <span class="font-mono">{{ stock.position }}</span></span>
                </div>
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

const stockList = ref([
  {
    id: '1',
    name: '腾讯控股',
    code: '00700.HK',
    pnl: 1.24,
    marketValue: '458,880.00',
    todayPnl: 5640,
    totalPnl: 50580,
    price: '382.40',
    cost: '340.25',
    position: '18.4%'
  },
  {
    id: '2',
    name: '美团-W',
    code: '03690.HK',
    pnl: -3.82,
    marketValue: '435,500.00',
    todayPnl: -1240,
    totalPnl: -17300,
    price: '174.20',
    cost: '181.12',
    position: '17.5%'
  },
  {
    id: '3',
    name: '阿里巴巴-W',
    code: '09988.HK',
    pnl: 0.45,
    marketValue: '460,750.00',
    todayPnl: 850,
    totalPnl: 36000,
    price: '92.15',
    cost: '84.95',
    position: '18.5%'
  }
])

const handleAddStock = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/add-stock'
  })
}

const handleStockTap = (stock) => {
  uni.showToast({ title: `${stock.name}详情`, icon: 'none' })
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
  padding: $spacing-6;
  box-shadow: $shadow-soft;
  border: 2rpx solid $surface-container;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  margin-top: $spacing-6;
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
  background: $surface-container;
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
  background: $primary-container;
  color: $on-primary-container;
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
  font-weight: $font-weight-medium;
  line-height: 1;
}

.holding-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  padding-bottom: $spacing-8;
}

.holding-card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-4;
  border: 2rpx solid $surface-container;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-3;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.stock-name {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.stock-code {
  font-size: $font-size-xs;
  color: $outline;
}

.pnl-badge {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  padding: 4rpx 16rpx;
  border-radius: $rounded-sm;
  font-family: $font-family-mono;
  
  &.up {
    background: rgba($profit, 0.1);
    color: $profit;
  }
  
  &.down {
    background: rgba($loss, 0.1);
    color: $loss;
  }
}

.card-bottom {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-2;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  
  &.center {
    align-items: center;
  }
  
  &.right {
    align-items: flex-end;
  }
}

.info-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.info-value {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
  
  &.profit {
    color: $profit;
  }
  
  &.loss {
    color: $loss;
  }
}
</style>