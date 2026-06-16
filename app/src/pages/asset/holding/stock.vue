<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- TopAppBar -->
        <header class="top-bar">
          <view class="top-bar-left">
            <button class="back-btn" @tap="handleBack">
              <text class="icon-back">arrow_back</text>
            </button>
            <h1 class="page-title">股票持仓</h1>
          </view>
          <view class="top-bar-right">
            <button class="notification-btn" @tap="handleNotification">
              <text class="icon-notification">notifications</text>
            </button>
            <view class="avatar-wrap">
              <image 
                class="avatar" 
                src="https://neeko-copilot.bytedance.net/api/text2image?prompt=professional%20business%20woman%20portrait&image_size=square" 
                mode="aspectFill" 
              />
            </view>
          </view>
        </header>

        <!-- Portfolio Hero Card -->
        <section class="hero-card">
          <div class="card-header">
            <span class="card-label">股票总市值 (HKD)</span>
            <span class="balance-amount font-mono">2,485,920.00</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">累计盈亏</span>
              <span class="stat-value font-mono text-profit">+458,200.00</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">今日盈亏</span>
              <span class="stat-value font-mono text-profit">+12,430.50</span>
            </div>
          </div>
        </section>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">持仓列表</h2>
            <span class="count-badge">3只</span>
          </div>
          <button class="add-btn" @tap="handleAddStock">
            <text class="icon-add">add</text>
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

    <!-- BottomNavBar -->
    <nav class="bottom-nav">
      <button class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <span class="nav-label">Home</span>
      </button>
      <button class="nav-item" @tap="navigateTo('goals')">
        <text class="nav-icon">target</text>
        <span class="nav-label">Goals</span>
      </button>
      <button class="nav-item active" @tap="navigateTo('assets')">
        <text class="nav-icon active">account_balance_wallet</text>
        <span class="nav-label active">Assets</span>
      </button>
      <button class="nav-item" @tap="navigateTo('mine')">
        <text class="nav-icon">person</text>
        <span class="nav-label">Mine</span>
      </button>
    </nav>
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

const handleBack = () => {
  uni.navigateBack()
}

const handleNotification = () => {
  uni.showToast({ title: '暂无新通知', icon: 'none' })
}

const handleAddStock = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/add-stock'
  })
}

const handleStockTap = (stock) => {
  uni.showToast({ title: `${stock.name}详情`, icon: 'none' })
}

const navigateTo = (page) => {
  const pages = {
    home: '/pages/home/index',
    goals: '/pages/goal/index',
    assets: '/pages/asset/index',
    mine: '/pages/profile/index'
  }
  uni.navigateTo({ url: pages[page] })
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
  padding-bottom: 200rpx;
}

.content {
  padding: $spacing-4;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-5 $spacing-4 $spacing-4;
  background: $surface;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  padding: 0;
}

.icon-back {
  font-family: 'Material Symbols Outlined';
  font-size: 44rpx;
  color: $on-surface;
}

.page-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.notification-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  padding: 0;
}

.icon-notification {
  font-family: 'Material Symbols Outlined';
  font-size: 44rpx;
  color: $on-surface-variant;
}

.avatar-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  border: 4rpx solid $tertiary-fixed;
  padding: 2rpx;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.hero-card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-6;
  box-shadow: $shadow-soft;
  margin-top: $spacing-2;
  border: 2rpx solid $surface-container;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.card-label {
  font-size: $font-size-xs;
  color: rgba($on-surface-variant, 0.7);
}

.balance-amount {
  font-size: $font-size-display-lg;
  font-weight: $font-weight-semibold;
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
  padding-top: $spacing-4;
  border-top: 2rpx solid rgba($surface-variant, 0.3);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.stat-label {
  font-size: $font-size-xs;
  color: rgba($on-surface-variant, 0.7);
}

.stat-value {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.text-profit {
  color: $profit;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-8;
  margin-bottom: $spacing-3;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
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
  gap: $spacing-1;
  background: $primary-container;
  color: $on-primary-container;
  padding: $spacing-2 $spacing-4;
  border-radius: $rounded-full;
}

.icon-add {
  font-family: 'Material Symbols Outlined';
  font-size: 32rpx;
}

.add-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
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

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: $spacing-3;
  padding-bottom: calc($spacing-3 + env(safe-area-inset-bottom));
  background: $surface-container-lowest;
  box-shadow: 0 -4px 20px 0 rgba(0, 0, 0, 0.04);
  border-radius: $rounded-lg 0 0 0;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-2;
  background: transparent;
  
  &.active {
    .nav-icon {
      font-variation-settings: 'FILL' 1;
    }
    
    .nav-label {
      color: $primary;
    }
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: $on-surface-variant;
  
  &.active {
    color: $primary;
  }
}

.nav-label {
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  color: $on-surface-variant;
}
</style>