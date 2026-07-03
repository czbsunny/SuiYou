<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <text class="card-label">贷款总余额 (CNY)</text>
            <text class="balance-amount font-mono">1,286,450.00</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-label">待还利息</text>
              <text class="stat-value font-mono text-loss">+58,230.00</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">月供总额</text>
              <text class="stat-value font-mono">18,650.00</text>
            </view>
          </view>
        </view>

        <!-- Section Header -->
        <div class="section-header">
          <div class="section-title-wrap">
            <h2 class="section-title">贷款明细</h2>
            <span class="count-badge">3笔</span>
          </div>
          <button class="add-btn" @tap="handleAddLoan">
            <image src="/static/images/add.png" class="icon-add" />
            <span class="add-text">新增贷款</span>
          </button>
        </div>

        <!-- Loan List -->
        <div class="loan-list">
          <view
            v-for="loan in loanList"
            :key="loan.id"
            class="loan-card"
            @tap="handleLoanTap(loan)"
          >
            <div class="card-top">
              <div class="loan-info">
                <h3 class="loan-name">{{ loan.name }}</h3>
                <view class="loan-meta">
                  <span class="status-tag" :class="{ active: loan.status === '还款中', cleared: loan.status === '已结清' }">{{ loan.status }}</span>
                  <span class="type-text">{{ loan.type }}</span>
                </view>
              </div>
              <view class="rate-badge">{{ loan.rate }}%</view>
            </div>
            <div class="card-dates">
              <view class="date-item">
                <text class="date-label">放款日</text>
                <text class="date-value font-mono">{{ loan.startDate }}</text>
              </view>
              <text class="date-arrow">→</text>
              <view class="date-item">
                <text class="date-label">到期日</text>
                <text class="date-value font-mono">{{ loan.maturityDate }}</text>
              </view>
            </div>
            <div class="card-bottom">
              <div class="info-item">
                <span class="info-label">贷款总额</span>
                <span class="info-value font-mono">{{ loan.totalAmount }}</span>
              </div>
              <div class="info-item center">
                <span class="info-label">剩余本金</span>
                <span class="info-value font-mono">{{ loan.remaining }}</span>
              </div>
              <div class="info-item right">
                <span class="info-label">月供</span>
                <span class="info-value font-mono">{{ loan.monthlyPayment }}</span>
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

const loanList = ref([
  {
    id: '1',
    name: '个人住房贷款',
    status: '还款中',
    type: '商业贷款',
    rate: 3.85,
    totalAmount: '800,000.00',
    remaining: '620,000.00',
    monthlyPayment: '8,450.00',
    startDate: '2023-03-15',
    maturityDate: '2043-03-15'
  },
  {
    id: '2',
    name: '消费贷款',
    status: '还款中',
    type: '信用贷款',
    rate: 4.50,
    totalAmount: '200,000.00',
    remaining: '120,000.00',
    monthlyPayment: '6,200.00',
    startDate: '2024-08-01',
    maturityDate: '2027-08-01'
  },
  {
    id: '3',
    name: '汽车贷款',
    status: '已结清',
    type: '抵押贷款',
    rate: 4.20,
    totalAmount: '150,000.00',
    remaining: '0.00',
    monthlyPayment: '0.00',
    startDate: '2022-01-10',
    maturityDate: '2025-01-10'
  }
])

const handleAddLoan = () => {
  uni.showToast({ title: '新增贷款功能开发中', icon: 'none' })
}

const handleLoanTap = (loan) => {
  uni.navigateTo({
    url: `/pages/asset/holding/loan-detail?data=${encodeURIComponent(JSON.stringify(loan))}`
  })
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

.text-loss {
  color: $loss;
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

.loan-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.loan-card {
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

.loan-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.loan-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.loan-meta {
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

  &.active {
    background: rgba($secondary, 0.1);
    color: $secondary;
  }

  &.cleared {
    background: rgba($profit, 0.1);
    color: $profit;
  }
}

.type-text {
  font-size: $font-size-xs;
  color: rgba($on-surface-variant, 0.6);
  font-family: $font-family-mono;
}

.rate-badge {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $loss;
  font-family: $font-family-mono;
  background: rgba($loss, 0.06);
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
}
</style>
