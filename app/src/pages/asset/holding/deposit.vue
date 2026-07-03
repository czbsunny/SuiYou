<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <text class="card-label">定期存款总额 (CNY)</text>
            <text class="balance-amount font-mono">200,000.00</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">预计总利息</text>
              <text class="stat-value font-mono text-profit">+13,750.00</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">平均利率</text>
              <text class="stat-value font-mono">2.25%</text>
            </view>
          </view>
        </view>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">存款明细</h2>
            <span class="count-badge">2笔</span>
          </div>
          <button class="add-btn" @tap="handleAddDeposit">
            <image src="/static/images/add.png" class="icon-add" />
            <span class="add-text">存入定存</span>
          </button>
        </div>

        <!-- Deposit List -->
        <div class="deposit-list">
          <view
            v-for="deposit in depositList"
            :key="deposit.id"
            class="deposit-card"
            @tap="handleDepositTap(deposit)"
          >
            <div class="card-top">
              <div class="deposit-info">
                <h3 class="deposit-name">{{ deposit.name }}</h3>
                <view class="deposit-meta">
                  <span class="status-tag" :class="{ matured: deposit.status === '已到期' }">{{ deposit.status }}</span>
                  <span class="term-text">{{ deposit.term }}</span>
                </view>
              </div>
              <view class="rate-badge">{{ deposit.rate }}%</view>
            </div>
            <div class="card-dates">
              <view class="date-item">
                <text class="date-label">存入日</text>
                <text class="date-value font-mono">{{ deposit.startDate }}</text>
              </view>
              <text class="date-arrow">→</text>
              <view class="date-item">
                <text class="date-label">到期日</text>
                <text class="date-value font-mono">{{ deposit.maturityDate }}</text>
              </view>
            </div>
            <div class="card-bottom">
              <div class="info-item">
                <span class="info-label">本金</span>
                <span class="info-value font-mono">{{ deposit.principal }}</span>
              </div>
              <div class="info-item center">
                <span class="info-label">预计利息</span>
                <span class="info-value font-mono text-profit">+{{ deposit.expectedInterest }}</span>
              </div>
              <div class="info-item right">
                <span class="info-label">已持天数</span>
                <span class="info-value font-mono">{{ deposit.heldDays }}天</span>
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

const depositList = ref([
  {
    id: '1',
    name: '一年期整存整取',
    status: '未到期',
    term: '1年',
    rate: 1.80,
    principal: '100,000.00',
    expectedInterest: '1,800.00',
    startDate: '2026-06-15',
    maturityDate: '2027-06-15',
    heldDays: 18
  },
  {
    id: '2',
    name: '三年期大额存单',
    status: '未到期',
    term: '3年',
    rate: 2.70,
    principal: '100,000.00',
    expectedInterest: '8,100.00',
    startDate: '2026-04-01',
    maturityDate: '2029-04-01',
    heldDays: 93
  }
])

const handleAddDeposit = () => {
  uni.showToast({ title: '存入定存功能开发中', icon: 'none' })
}

const handleDepositTap = (deposit) => {
  uni.showToast({ title: `${deposit.name}详情`, icon: 'none' })
}
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

.text-muted {
  color: $outline;
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

.deposit-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.deposit-card {
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-5;
  box-shadow: $shadow-sm;
  border: 2rpx solid rgba($surface-variant, 0.2);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-4;
}

.deposit-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.deposit-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.deposit-meta {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.status-tag {
  background: rgba($surface-variant, 0.3);
  color: $on-surface-variant;
  font-size: 20rpx;
  padding: 4rpx 16rpx;
  border-radius: $rounded-sm;

  &.matured {
    background: rgba($profit, 0.1);
    color: $profit;
  }
}

.term-text {
  font-size: $font-size-xs;
  color: rgba($on-surface-variant, 0.6);
  font-family: $font-family-mono;
}

.rate-badge {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $secondary;
  font-family: $font-family-mono;
  background: rgba($secondary, 0.06);
  padding: $spacing-1 $spacing-2;
  border-radius: $rounded-sm;
}

.card-dates {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-3;
  background: linear-gradient(135deg, rgba($surface-variant, 0.1) 0%, rgba($surface-variant, 0.05) 100%);
  border-radius: $rounded-md;
  margin-bottom: $spacing-4;
}

.date-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.date-label {
  font-size: 22rpx;
  color: rgba($on-surface-variant, 0.6);
}

.date-value {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.date-arrow {
  color: $outline-variant;
  font-size: $font-size-lg;
  padding: 0 $spacing-1;
}

.card-bottom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-3;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6rpx;

  &.center {
    align-items: center;
  }

  &.right {
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

  &.text-profit {
    color: $profit;
  }
}
</style>
