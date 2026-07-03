<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="card-header">
            <view class="label-row">
              <view class="label-left">
                <text class="card-label">可用余额 (元)</text>
                <image src="/static/assets/actions/edit.png" class="icon-edit" @tap="handleEdit" />
              </view>
            </view>
            <view class="balance-row">
              <view class="amount-wrapper">
                <text class="currency">¥</text>
                <text class="balance-amount font-mono">13.37</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-header">
          <view class="section-title-wrap">
            <h2 class="section-title">余额变动明细</h2>
            <span class="count-badge">{{ transactionList.length }}笔</span>
          </view>
          <text class="view-all" @tap="handleViewAll">全部</text>
        </view>

        <view class="transaction-list">
          <view
            v-for="item in transactionList"
            :key="item.id"
            class="transaction-card"
            @tap="handleItemTap(item)"
          >
            <view class="icon-wrap" :style="{ backgroundColor: item.iconBg }">
              <image :src="item.icon" class="item-icon" mode="aspectFit" />
            </view>
            <view class="item-info">
              <text class="item-title">{{ item.title }}</text>
              <text class="item-time">{{ item.time }}</text>
            </view>
            <view class="item-right">
              <text class="item-amount font-mono" :class="{ income: item.isIncome, expense: !item.isIncome }">
                {{ item.isIncome ? '+' : '-' }}{{ item.amount }}
              </text>
              <text class="item-balance font-mono">余额{{ item.balance }}</text>
            </view>
          </view>
        </view>

        <view v-if="transactionList.length === 0" class="empty-state">
          <text class="empty-text">暂无余额变动记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const transactionList = ref([
  {
    id: '1',
    title: '余额宝-转出到余额',
    time: '2024-05-09 14:00',
    amount: '0.24',
    balance: '13.37',
    isIncome: true,
    icon: '/static/assets/actions/transfer.png',
    iconBg: '#ECFDF5'
  },
  {
    id: '2',
    title: '余额提现',
    time: '2024-01-23 13:10',
    amount: '25.71',
    balance: '13.13',
    isIncome: false,
    icon: '/static/assets/actions/withdraw.png',
    iconBg: '#FEF2F2'
  },
  {
    id: '3',
    title: '小红书激励金提取',
    time: '2024-01-23 11:45',
    amount: '25.00',
    balance: '38.84',
    isIncome: true,
    icon: '/static/assets/actions/finance.png',
    iconBg: '#FFF7ED'
  }
])

const handleEdit = () => {
  uni.showToast({ title: '编辑余额', icon: 'none' })
}

const handleViewAll = () => {
  uni.showToast({ title: '查看全部明细', icon: 'none' })
}

const handleItemTap = (item) => {
  uni.showToast({ title: `${item.title}详情`, icon: 'none' })
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
}

.label-row {
  display: flex;
  align-items: center;
}

.label-left {
  display: flex;
  align-items: center;
  gap: $spacing-1;
}

.card-label {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.icon-edit {
  width: 40rpx;
  height: 40rpx;
}

.balance-row {
  margin-top: 24rpx;
}

.amount-wrapper {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: 900;
}

.balance-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
}

.font-mono {
  font-family: $font-family-mono;
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

.view-all {
  font-size: $font-size-body-sm;
  color: $primary;
  font-weight: $font-weight-medium;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  padding-bottom: $spacing-8;
}

.transaction-card {
  display: flex;
  align-items: center;
  background: $surface-container-lowest;
  border-radius: $rounded-md;
  padding: $spacing-4;
  border: 2rpx solid $surface-container;
}

.icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: $rounded-md;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-3;
  flex-shrink: 0;
}

.item-icon {
  width: 48rpx;
  height: 48rpx;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.item-title {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-time {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;
  flex-shrink: 0;
}

.item-amount {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;
  color: $on-surface;

  &.income {
    color: $profit;
  }

  &.expense {
    color: $loss;
  }
}

.item-balance {
  font-size: $font-size-xs;
  color: $on-surface-variant;
}

.empty-state {
  padding: 120rpx 0;
  text-align: center;
}

.empty-text {
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}
</style>
