<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="asset-hero">
          <text class="eyebrow">家庭资产净值</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">12,480,500.00</text>
          </view>
          <view class="chart">
            <svg class="chart-svg" viewBox="0 0 400 140" preserveAspectRatio="none">
              <polyline
                :points="chartPoints"
                fill="none"
                stroke="#006754"
                stroke-width="3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle v-for="(point, index) in chartDots" :key="index" :cx="point.x" :cy="point.y" r="6" fill="#2a806c" />
            </svg>
          </view>
          <view class="chart-labels">
            <text
              v-for="(label, index) in timelineLabels"
              :key="index"
              class="chart-label"
              :style="{ left: label.x + 'px' }"
            >{{ label.text }}</text>
          </view>
        </view>

        <view class="section-heading">
          <text class="section-title">账户列表</text>
          <view class="heading-actions">
            <text>filter</text>
            <text class="plus">+</text>
          </view>
        </view>

        <view class="account-list">
          <view v-for="account in accounts" :key="account.name" class="account-card">
            <view class="round-icon" :class="account.tone">{{ account.icon }}</view>
            <view class="account-info">
              <text class="account-name">{{ account.name }}</text>
              <text class="account-type">{{ account.type }}</text>
            </view>
            <view class="account-money">
              <text class="balance">{{ account.balance }}</text>
              <text v-if="account.change" class="change" :class="{ positive: account.change.startsWith('+') }">
                {{ account.change }}
              </text>
              <text v-else class="change neutral">--</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const getMonthLabel = (date) => {
  const month = date.getMonth() + 1
  return `${month}月`
}

const timelineLabels = computed(() => {
  const now = new Date()
  const labels = []
  for (let i = 11; i >= 0; i -= 3) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    labels.push({
      text: getMonthLabel(date),
      x: 20 + (11 - i) / 11 * 360
    })
  }
  return labels
})

const chartPoints = computed(() => {
  const points = [
    { x: 20, y: 110 },
    { x: 60, y: 105 },
    { x: 100, y: 95 },
    { x: 140, y: 85 },
    { x: 180, y: 75 },
    { x: 220, y: 65 },
    { x: 260, y: 55 },
    { x: 300, y: 45 },
    { x: 340, y: 38 },
    { x: 380, y: 30 }
  ]
  return points.map(p => `${p.x},${p.y}`).join(' ')
})

const chartDots = computed(() => {
  return [
    { x: 20, y: 110 },
    { x: 100, y: 95 },
    { x: 180, y: 75 },
    { x: 260, y: 55 },
    { x: 380, y: 30 }
  ]
})

const accounts = [
  { name: '招商银行 (私行)', type: '储蓄账户 ·· 8842', balance: '¥2,450,000.00', change: '-¥12,400.00', icon: 'bank', tone: 'primary' },
  { name: '中信证券', type: '美股/港股投资', balance: '¥4,120,500.00', change: '-¥29,700.50', icon: 'sec', tone: 'cream' },
  { name: '上海静安府', type: '固定资产 (评估值)', balance: '¥3,744,150.00', change: '', icon: 'apt', tone: 'default' },
  { name: '微信支付', type: '个人零钱', balance: '¥12,500.00', change: '+¥240.00', icon: 'pay', tone: 'primary' },
  { name: '支付宝', type: '余额宝/余额', balance: '¥45,800.00', change: '+¥1,120.00', icon: 'wal', tone: 'blue' },
  { name: '抖音支付', type: '抖音钱包', balance: '¥3,200.00', change: '-¥45.00', icon: 'dy', tone: 'default' },
  { name: '中国建设银行信用卡', type: '账单日: 15日', balance: '-¥8,450.00', change: '已入账', icon: 'card', tone: 'red' }
]

const notify = () => {
  uni.showToast({ title: '暂无新通知', icon: 'none' })
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
  padding: 32rpx 32rpx 170rpx;
}

.asset-hero {
  padding: 52rpx 46rpx 36rpx;
  border-radius: 32rpx;
  background: #fff;
  box-shadow: $shadow-soft;
}

.eyebrow {
  color: $outline;
  font-size: 28rpx;
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
  color: $primary;
  font-size: 48rpx;
  font-weight: 900;
}

.hero-amount {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: 58rpx;
  font-weight: 900;
}

.chart {
  position: relative;
  height: 140rpx;
  margin-top: 38rpx;
}

.chart-svg {
  width: 100%;
  height: 100%;
}

.chart-labels {
  position: relative;
  height: 40rpx;
  margin-top: 6rpx;
}

.chart-label {
  position: absolute;
  transform: translateX(-50%);
  color: $on-surface-variant;
  font-size: 24rpx;
  white-space: nowrap;
}

.section-heading {
  margin-top: 64rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  color: $on-surface;
  font-size: 44rpx;
  font-weight: 900;
}

.heading-actions {
  color: $on-surface-variant;
  display: flex;
  gap: 32rpx;
  align-items: center;
  font-size: 22rpx;
  font-weight: 900;
}

.plus {
  font-size: 42rpx;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.account-card {
  min-height: 110rpx;
  padding: 28rpx 32rpx;
  border-radius: 26rpx;
  background: #fff;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.round-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 900;
}

.primary {
  background: rgba($primary, 0.1);
  color: $primary;
}

.cream {
  background: #f3f0ea;
  color: $tertiary;
}

.default {
  background: $surface-container-low;
  color: $on-surface-variant;
}

.blue {
  background: #eaf1ff;
  color: #2366e8;
}

.red {
  background: rgba($profit, 0.09);
  color: $profit;
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.account-name {
  color: $on-surface;
  font-size: 30rpx;
  font-weight: 900;
}

.account-type {
  margin-top: 4rpx;
  color: $on-surface-variant;
  font-size: 26rpx;
}

.account-money {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.balance {
  color: $on-surface;
  font-family: $font-family-mono;
  font-size: 28rpx;
  font-weight: 900;
}

.change {
  margin-top: 4rpx;
  color: $primary;
  font-family: $font-family-mono;
  font-size: 24rpx;
  font-weight: 800;
}

.change.positive {
  color: $profit;
}

.change.neutral {
  color: $outline;
}
</style>
