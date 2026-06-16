<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- TopAppBar -->
        <header class="top-bar">
          <view class="top-bar-left">
            <button class="back-btn" @tap="handleBack">
              <text class="icon-back">chevron_left</text>
            </button>
            <h1 class="page-title">基金</h1>
          </view>
          <view class="top-bar-right">
            <button class="action-btn">
              <text class="icon-action">chevron_left</text>
            </button>
            <button class="action-btn active">
              <text class="icon-action">radio_button_checked</text>
            </button>
          </view>
        </header>

        <!-- Portfolio Hero Card -->
        <section class="hero-card">
          <div class="card-header">
            <span class="card-label">基金总资产 (元)</span>
            <span class="balance-amount font-mono">504,285.00</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">持有收益</span>
              <span class="stat-value font-mono text-profit">+24,800.50</span>
            </div>
            <div class="stat-item text-right">
              <span class="stat-label">今日收益</span>
              <span class="stat-value font-mono text-profit">+1,562.20</span>
            </div>
          </div>
        </section>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">持有明细</h2>
            <span class="count-badge">2只</span>
          </div>
          <button class="add-btn" @tap="handleAddFund">
            <text class="icon-add">add</text>
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

const handleBack = () => {
  uni.navigateBack()
}

const handleAddFund = () => {
  uni.navigateTo({
    url: '/pages/asset/holding/add-fund'
  })
}

const handleFundTap = (fund) => {
  uni.showToast({ title: `${fund.name}详情`, icon: 'none' })
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
  padding-bottom: 180rpx;
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
  gap: $spacing-2;
}

.action-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba($surface-variant, 0.2);
  padding: 0;
  
  &.active .icon-action {
    font-size: 56rpx;
  }
}

.icon-action {
  font-family: 'Material Symbols Outlined';
  font-size: 32rpx;
  color: $on-surface;
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
  font-weight: $font-weight-medium;
  color: $on-surface-variant;
}

.balance-amount {
  font-size: $font-size-display-lg;
  font-weight: $font-weight-bold;
  color: $on-surface;
  letter-spacing: -1rpx;
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
  border-top: 2rpx solid $surface-container;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  
  &.text-right {
    align-items: flex-end;
  }
}

.stat-label {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.stat-value {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
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
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
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
  gap: $spacing-1;
  background: $primary;
  color: $on-primary;
  padding: $spacing-2 $spacing-4;
  border-radius: $rounded-full;
}

.icon-add {
  font-family: 'Material Symbols Outlined';
  font-size: 36rpx;
}

.add-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
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
  color: $on-surface-variant;
}
</style>