<template>
  <view class="page home-page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <!-- Hero Wealth Card -->
        <view class="hero-card">
          <text class="eyebrow">家庭资产净值</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">12,482,930.00</text>
          </view>
          <view class="growth-section">
            <text class="growth-label">本月增长</text>
            <view class="growth-value">
              <text class="growth-amount">+¥42,105.20</text>
              <text class="growth-percent">+3.2%</text>
            </view>
          </view>
        </view>

        <!-- Quick Actions -->
        <view class="quick-actions">
          <view v-for="action in quickActions" :key="action.label" class="quick-item" @tap="tapAction(action.label)">
            <view class="quick-icon">
              <text class="icon-text">{{ action.icon }}</text>
            </view>
            <text class="quick-label">{{ action.label }}</text>
          </view>
        </view>

        <!-- Recent Transactions -->
        <view class="section-header">
          <text class="section-title">近期交易</text>
          <text class="section-link" @tap="tapAction('查看全部')">查看全部</text>
        </view>

        <view class="transaction-list">
          <view v-for="item in transactions" :key="item.title" class="transaction-item">
            <view class="transaction-icon" :class="{ income: item.type === 'income' }">
              <text>{{ item.icon }}</text>
            </view>
            <view class="transaction-info">
              <text class="transaction-title">{{ item.title }}</text>
              <text class="transaction-subtitle">{{ item.time }} · {{ item.category }}</text>
            </view>
            <view class="transaction-amount">
              <text class="amount" :class="{ income: item.type === 'income' }">{{ item.amount }}</text>
              <text class="account">{{ item.account }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
const quickActions = [
  { label: '预算', icon: 'B' },
  { label: '记账', icon: '+' },
  { label: '存钱', icon: '$' },
  { label: '报告', icon: 'R' }
]

const transactions = [
  { title: '精品百货', category: '购物', amount: '-¥1,280.00', account: '招商银行卡', time: '14:20', icon: 'S', type: 'expense' },
  { title: '股票红利发放', category: '投资', amount: '+¥24,500.00', account: '证券账户', time: '昨天', icon: 'T', type: 'income' },
  { title: '物业管理费', category: '住宅', amount: '-¥4,600.00', account: '自动转账', time: '昨天', icon: 'H', type: 'expense' },
  { title: '米其林餐厅订座', category: '餐饮', amount: '-¥2,880.00', account: '运通百夫长', time: '3天前', icon: 'F', type: 'expense' }
]

const tapAction = (label) => {
  uni.showToast({ title: label, icon: 'none' })
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
}

.content {
  padding: 32rpx 32rpx 160rpx;
}

/* Hero Card */
.hero-card {
  padding: 64rpx;
  border-radius: 48rpx;
  background: $surface-container-lowest;
  box-shadow: 0 8rpx 40rpx 0 rgba(0, 0, 0, 0.04);
}

.eyebrow {
  color: $outline;
  font-size: 24rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.amount-row {
  margin-top: 16rpx;
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.currency {
  color: $primary;
  font-size: 44rpx;
  font-weight: 600;
}

.hero-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: 56rpx;
  font-weight: 600;
  letter-spacing: -2rpx;
}

.growth-section {
  margin-top: 64rpx;
}

.growth-label {
  color: $outline;
  font-size: 28rpx;
}

.growth-value {
  margin-top: 8rpx;
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.growth-amount {
  color: $secondary;
  font-family: $font-family-mono;
  font-size: 32rpx;
  font-weight: 600;
}

.growth-percent {
  color: rgba($secondary, 0.8);
  font-size: 24rpx;
  font-weight: 700;
}

/* Quick Actions */
.quick-actions {
  margin-top: 48rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.quick-icon {
  width: 112rpx;
  height: 112rpx;
  border-radius: 32rpx;
  background: #fff;
  box-shadow: 0 4rpx 12rpx 0 rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-text {
  color: $primary;
  font-size: 36rpx;
  font-weight: 600;
}

.quick-label {
  color: $on-surface-variant;
  font-size: 24rpx;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* Section Header */
.section-header {
  margin-top: 64rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.section-title {
  color: $on-surface;
  font-size: 44rpx;
  font-weight: 600;
}

.section-link {
  color: $primary;
  font-size: 28rpx;
  font-weight: 500;
}

/* Transaction List */
.transaction-list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.transaction-item {
  padding: 32rpx;
  border-radius: 32rpx;
  background: $surface-container-lowest;
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.transaction-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: rgba($outline, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: $outline;
  font-size: 28rpx;
  font-weight: 600;
}

.transaction-icon.income {
  background: rgba($secondary, 0.1);
  color: $secondary;
}

.transaction-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.transaction-title {
  color: $on-surface;
  font-size: 30rpx;
  font-weight: 600;
}

.transaction-subtitle {
  margin-top: 6rpx;
  color: $outline;
  font-size: 24rpx;
}

.transaction-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.amount {
  color: $primary;
  font-family: $font-family-mono;
  font-size: 30rpx;
  font-weight: 600;
}

.amount.income {
  color: $secondary;
}

.account {
  margin-top: 6rpx;
  color: $outline-variant;
  font-size: 20rpx;
  font-weight: 700;
}
</style>
