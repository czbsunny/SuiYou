<template>
  <view class="page detail-page">
    <!-- Header -->
    <view class="header">
      <view class="back-btn" @tap="handleBack">
        <text class="icon">arrow_back</text>
      </view>
      <text class="header-title">明细</text>
      <view class="more-btn" @tap="handleMore">
        <text class="icon">more_horiz</text>
      </view>
    </view>

    <!-- Controls Row -->
    <view class="controls-row">
      <view class="date-btn" @tap="handleDate">
        <text class="date-text">2024年5月</text>
        <text class="icon">expand_more</text>
      </view>
      <view class="filter-btn" @tap="handleFilter">
        <text class="icon">filter_list</text>
        <text class="filter-text">筛选</text>
      </view>
    </view>

    <!-- Summary Cards -->
    <view class="summary-cards">
      <view class="summary-card expense">
        <text class="summary-label">本月支出</text>
        <view class="summary-amount">
          <text class="currency">¥</text>
          <text class="amount">12,480.00</text>
        </view>
      </view>
      <view class="summary-card income">
        <text class="summary-label">本月收入</text>
        <view class="summary-amount">
          <text class="currency">¥</text>
          <text class="amount">42,000.00</text>
        </view>
      </view>
    </view>

    <!-- Transaction List -->
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Today Group -->
        <view class="transaction-group">
          <view class="group-header">
            <view class="date-info">
              <text class="date">5月21日</text>
              <text class="day">今天</text>
            </view>
            <view class="group-summary">
              <text class="exp-summary">支: 156.00</text>
              <text class="inc-summary">收: 0.00</text>
            </view>
          </view>
          <view class="group-content">
            <view v-for="item in todayTransactions" :key="item.id" class="transaction-item" @tap="handleTransaction(item)">
              <view class="item-icon" :class="{ income: item.type === 'income' }">
                <text>{{ item.icon }}</text>
              </view>
              <view class="item-info">
                <text class="item-title">{{ item.title }}</text>
                <text class="item-time">{{ item.time }}</text>
              </view>
              <text class="item-amount" :class="{ income: item.type === 'income' }">{{ item.amount }}</text>
            </view>
          </view>
        </view>

        <!-- Yesterday Group -->
        <view class="transaction-group">
          <view class="group-header">
            <view class="date-info">
              <text class="date">5月20日</text>
              <text class="day">昨天</text>
            </view>
            <view class="group-summary">
              <text class="exp-summary">支: 420.00</text>
              <text class="inc-summary">收: 30,000.00</text>
            </view>
          </view>
          <view class="group-content">
            <view v-for="item in yesterdayTransactions" :key="item.id" class="transaction-item" @tap="handleTransaction(item)">
              <view class="item-icon" :class="{ income: item.type === 'income' }">
                <text>{{ item.icon }}</text>
              </view>
              <view class="item-info">
                <text class="item-title">{{ item.title }}</text>
                <text class="item-time">{{ item.time }}</text>
              </view>
              <text class="item-amount" :class="{ income: item.type === 'income' }">{{ item.amount }}</text>
            </view>
          </view>
        </view>

        <!-- May 19 Group -->
        <view class="transaction-group">
          <view class="group-header">
            <view class="date-info">
              <text class="date">5月19日</text>
            </view>
            <view class="group-summary">
              <text class="exp-summary">支: 1,200.00</text>
              <text class="inc-summary">收: 0.00</text>
            </view>
          </view>
          <view class="group-content">
            <view v-for="item in may19Transactions" :key="item.id" class="transaction-item" @tap="handleTransaction(item)">
              <view class="item-icon" :class="{ income: item.type === 'income' }">
                <text>{{ item.icon }}</text>
              </view>
              <view class="item-info">
                <text class="item-title">{{ item.title }}</text>
                <text class="item-time">{{ item.time }}</text>
              </view>
              <text class="item-amount" :class="{ income: item.type === 'income' }">{{ item.amount }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="nav-item" @tap="navigateTo('home')">
        <text class="nav-icon">home</text>
        <text class="nav-label">首页</text>
      </view>
      <view class="nav-item active" @tap="navigateTo('detail')">
        <text class="nav-icon" :class="{ filled: true }">list_alt</text>
        <text class="nav-label">明细</text>
      </view>
      <view class="nav-item" @tap="navigateTo('trend')">
        <text class="nav-icon">trending_up</text>
        <text class="nav-label">趋势</text>
      </view>
      <view class="nav-item" @tap="navigateTo('profile')">
        <text class="nav-icon">person</text>
        <text class="nav-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const todayTransactions = [
  { id: 1, title: '餐饮 · 午餐', time: '12:30', amount: '-88.00', icon: 'restaurant', type: 'expense' },
  { id: 2, title: '出行 · 网约车', time: '08:45', amount: '-68.00', icon: 'local_taxi', type: 'expense' }
]

const yesterdayTransactions = [
  { id: 3, title: '薪资 · 月度发放', time: '10:00', amount: '+30,000.00', icon: 'payments', type: 'income' },
  { id: 4, title: '购物 · Apple Store', time: '15:20', amount: '-420.00', icon: 'shopping_bag', type: 'expense' }
]

const may19Transactions = [
  { id: 5, title: '住房 · 物业管理费', time: '14:00', amount: '-1,200.00', icon: 'home', type: 'expense' }
]

const handleBack = () => {
  uni.navigateBack()
}

const handleMore = () => {
  uni.showToast({ title: '更多选项', icon: 'none' })
}

const handleDate = () => {
  uni.showToast({ title: '选择日期', icon: 'none' })
}

const handleFilter = () => {
  uni.showToast({ title: '筛选', icon: 'none' })
}

const handleTransaction = (item) => {
  uni.showToast({ title: item.title, icon: 'none' })
}

const navigateTo = (page) => {
  const pages = {
    home: '/pages/home/index',
    detail: '/pages/bill/detail',
    trend: '/pages/home/index',
    profile: '/pages/profile/index'
  }
  if (pages[page] !== '/pages/bill/detail') {
    uni.navigateTo({ url: pages[page] })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $background;
  padding-bottom: 140rpx;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-2 $container-padding;
  padding-top: calc($spacing-2 + env(safe-area-inset-top));
  background: rgba($background, 0.8);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid $surface-container-low;
}

.back-btn,
.more-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
  color: $on-surface;
}

.header-title {
  font-size: 34rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.controls-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $container-padding;
  padding-top: calc($spacing-4 + env(safe-area-inset-top) + 88rpx);
}

.date-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-2 $spacing-4;
  border-radius: $rounded-full;
  background: $surface-container-low;
}

.date-text {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  color: $on-surface-variant;
}

.filter-text {
  font-size: 28rpx;
  font-weight: $font-weight-medium;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-md;
  padding: 0 $container-padding;
}

.summary-card {
  padding: $spacing-5;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;

  &.expense .currency,
  &.expense .amount {
    color: $primary;
  }

  &.income .currency,
  &.income .amount {
    color: $secondary;
  }
}

.summary-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.summary-amount {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-top: $spacing-2;
}

.currency {
  font-size: 28rpx;
  font-weight: $font-weight-bold;
}

.amount {
  font-family: $font-family-mono;
  font-size: 44rpx;
  font-weight: $font-weight-bold;
}

.scroll {
  height: calc(100vh - 420rpx - env(safe-area-inset-top));
}

.content {
  padding: $section-margin $container-padding;
}

.transaction-group {
  margin-bottom: $section-margin;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: $spacing-2;
  border-bottom: 1rpx solid $surface-container-low;
}

.date-info {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
}

.date {
  font-size: 26rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.day {
  font-size: 24rpx;
  font-weight: $font-weight-medium;
  color: $outline;
}

.group-summary {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
}

.exp-summary,
.inc-summary {
  font-size: 20rpx;
  font-weight: $font-weight-medium;
  color: $outline;
}

.group-content {
  padding-top: $stack-gap-md;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
  padding: $spacing-4;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;
  margin-bottom: $stack-gap-md;

  &:active {
    opacity: 0.6;
  }
}

.item-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $rounded-full;
  background: $surface-container-low;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $on-surface-variant;
  font-size: 36rpx;
  font-weight: $font-weight-semibold;

  &.income {
    background: rgba($secondary, 0.1);
    color: $secondary;
  }
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 30rpx;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.item-time {
  margin-top: 4rpx;
  font-size: 24rpx;
  color: $outline;
}

.item-amount {
  font-family: $font-family-mono;
  font-size: 32rpx;
  font-weight: $font-weight-bold;
  color: $primary;

  &.income {
    color: $secondary;
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
  padding: $spacing-2 $spacing-4;
  padding-bottom: calc($spacing-2 + env(safe-area-inset-bottom));
  background: $surface-container-lowest;
  border-top: 1rpx solid $surface-container-low;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-1;
  color: $outline;
  transition: color 0.2s;
  flex: 1;

  &.active {
    color: $primary;
  }
}

.nav-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 52rpx;

  &.filled {
    font-variation-settings: 'FILL' 1;
  }
}

.nav-label {
  margin-top: 2rpx;
  font-size: 20rpx;
  font-weight: $font-weight-medium;
}
</style>