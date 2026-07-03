<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
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
              <image class="icon-image" :src="`/static/images/${action.icon}.png`" mode="aspectFit" />
            </view>
            <text class="quick-label">{{ action.label }}</text>
          </view>
        </view>

        <!-- Recent Transactions -->
        <view class="section-header">
          <text class="section-title">资金明细</text>
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
  { label: '预算', icon: 'budget' },
  { label: '记账', icon: 'booking' },
  { label: '存钱', icon: 'savings' },
  { label: '报告', icon: 'analytics' }
]

const transactions = [
  { title: '精品百货', category: '购物', amount: '-¥1,280.00', account: '招商银行卡', time: '14:20', icon: 'S', type: 'expense' },
  { title: '股票红利发放', category: '投资', amount: '+¥24,500.00', account: '证券账户', time: '昨天', icon: 'T', type: 'income' },
  { title: '物业管理费', category: '住宅', amount: '-¥4,600.00', account: '自动转账', time: '昨天', icon: 'H', type: 'expense' },
  { title: '米其林餐厅订座', category: '餐饮', amount: '-¥2,880.00', account: '运通百夫长', time: '3天前', icon: 'F', type: 'expense' }
]

const tapAction = (label) => {
  const pageMap = {
    '预算': '/pages/bill/budget',
    '记账': '/pages/bill/record',
    '存钱': '/pages/goal/saving',
    '报告': '/pages/report/monthly',
    '查看全部': '/pages/bill/detail'
  }
  const url = pageMap[label]
  if (url) {
    uni.navigateTo({ url })
  } else {
    uni.showToast({ title: label, icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.eyebrow {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.amount-row {
  margin-top: 24rpx;
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: 900;
}

.hero-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
}

.growth-section {
  margin-top: $section-margin;
}

.growth-label {
  color: $outline;
  font-size: $font-size-body-sm;
}

.growth-value {
  margin-top: $spacing-1;
  display: flex;
  align-items: baseline;
  gap: $spacing-1;
}

.growth-amount {
  color: $secondary;
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: 600;
}

.growth-percent {
  color: rgba($secondary, 0.8);
  font-size: $font-size-xs;
  font-weight: 700;
}

/* Quick Actions */
.quick-actions {
  margin-top: $stack-gap-lg;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $stack-gap-md;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $stack-gap-sm;
}

.quick-icon {
  width: 112rpx;
  height: 112rpx;
  border-radius: $rounded-lg;
  background: #fff;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-image {
  width: 56rpx;
  height: 56rpx;
}

.quick-label {
  color: $on-surface-variant;
  font-size: $font-size-xs;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* Section Header */
.section-header {
  margin-top: $section-margin;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.section-title {
  color: $on-surface;
  font-size: $font-size-title-sm;
  font-weight: 900;
}

.section-link {
  color: $primary;
  font-size: $font-size-body-sm;
  font-weight: 500;
}

/* Transaction List */
.transaction-list {
  margin-top: $spacing-3;
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
}

.transaction-item {
  padding: $spacing-4;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
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
  font-size: $font-size-lg;
  font-weight: 600;
}

.transaction-subtitle {
  margin-top: $spacing-1;
  color: $outline;
  font-size: $font-size-xs;
}

.transaction-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.amount {
  color: $primary;
  font-family: $font-family-mono;
  font-size: $font-size-lg;
  font-weight: 600;
}

.amount.income {
  color: $secondary;
}

.account {
  margin-top: $spacing-1;
  color: $outline-variant;
  font-size: $font-size-xs;
  font-weight: 700;
}
</style>
