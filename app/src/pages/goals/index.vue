<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="summary-card">
          <text class="eyebrow">累计储蓄</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">1,248,500.00</text>
          </view>
          <view class="progress-head">
            <text>总体进度</text>
            <text class="percent">64%</text>
          </view>
          <view class="progress-track">
            <view class="progress-fill" style="width: 64%"></view>
          </view>
        </view>

        <view class="section-heading">
          <text class="section-title">愿望清单</text>
          <view class="heading-actions">
            <text class="action-mark" @tap="toggleSort">filter</text>
            <text class="action-plus" @tap="tapAction('创建新目标')">+</text>
          </view>
        </view>

        <view v-if="isSortOpen" class="chips">
          <view class="chip" :class="{ active: sortBy === 'target' }" @tap="selectSort('target')">目标金额 ↕</view>
          <view class="chip" :class="{ active: sortBy === 'progress' }" @tap="selectSort('progress')">完成度 ↕</view>
        </view>

        <view class="goal-list">
          <view v-for="goal in goals" :key="goal.name" class="goal-card">
            <view class="goal-head">
              <view class="round-icon">
                <text>{{ goal.icon }}</text>
              </view>
              <view class="goal-main">
                <text class="goal-title">{{ goal.name }}</text>
                <text class="goal-subtitle">{{ goal.desc }}</text>
              </view>
              <text class="badge">{{ goal.badge }}</text>
            </view>

            <view class="goal-amounts">
              <view>
                <text class="label">已储蓄</text>
                <text class="saved">¥{{ goal.saved }}</text>
              </view>
              <view class="right">
                <text class="label">目标金额</text>
                <text class="target">¥{{ goal.target }}</text>
              </view>
            </view>

            <view class="goal-progress">
              <view class="goal-fill" :style="{ width: `${goal.progress}%` }"></view>
              <text class="goal-percent">{{ goal.progress }}%</text>
            </view>
            <view class="date-row">
              <text>起始: {{ goal.start }}</text>
              <text>结束: {{ goal.end }}</text>
            </view>
          </view>

          <view class="create-goal" @tap="tapAction('创建新目标')">
            <text class="create-plus">⊕</text>
            <text>创建新目标</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isSortOpen = ref(false)

const sortBy = ref('target')

const goals = ref([
  { name: '购车计划', desc: '家庭 SUV 升级', saved: '450,000', target: '600,000', progress: 75, start: '2023.01', end: '2024.12', badge: '还剩 45 天', icon: 'car' },
  { name: '教育基金', desc: '常春藤盟校储备', saved: '680,000', target: '1,600,000', progress: 42, start: '2022.09', end: '2030.09', badge: '倒计时 6 年', icon: 'edu' },
  { name: '度假基金', desc: '2025 地中海邮轮游', saved: '118,500', target: '125,000', progress: 95, start: '2024.04', end: '2025.06', badge: '还剩 14 个月', icon: 'sea' }
])

const toggleSort = () => {
  isSortOpen.value = !isSortOpen.value
}

const selectSort = (type) => {
  sortBy.value = type
  isSortOpen.value = false
  sortGoals()
}

const sortGoals = () => {
  goals.value = [...goals.value].sort((a, b) => {
    if (sortBy.value === 'target') {
      return parseInt(b.target.replace(/,/g, '')) - parseInt(a.target.replace(/,/g, ''))
    } else if (sortBy.value === 'progress') {
      return b.progress - a.progress
    }
    return 0
  })
}

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
  padding: 32rpx 32rpx 170rpx;
}

.summary-card,
.goal-card {
  background: #fff;
  border-radius: 44rpx;
  box-shadow: $shadow-soft;
}

.summary-card {
  padding: 48rpx;
}

.eyebrow,
.label {
  color: $outline;
  font-size: 24rpx;
  font-weight: 800;
  letter-spacing: 1rpx;
}

.amount-row {
  margin-top: 22rpx;
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.currency {
  color: $primary;
  font-size: 44rpx;
  font-weight: 900;
}

.hero-amount {
  font-family: $font-family-mono;
  font-size: 54rpx;
  font-weight: 900;
  color: $on-surface;
}

.progress-head {
  margin-top: 64rpx;
  margin-bottom: 18rpx;
  color: $outline;
  font-size: 28rpx;
  display: flex;
  justify-content: space-between;
}

.percent {
  color: $primary;
  font-family: $font-family-mono;
  font-weight: 900;
}

.progress-track {
  height: 12rpx;
  border-radius: 999rpx;
  background: $surface-container-low;
  overflow: hidden;
}

.progress-fill,
.goal-fill {
  height: 100%;
  border-radius: 999rpx;
  background: $primary-container;
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
  display: flex;
  align-items: center;
  gap: 34rpx;
  color: $primary;
  font-weight: 900;
}

.action-mark {
  font-size: 22rpx;
}

.action-plus {
  font-size: 42rpx;
}

.chips {
  display: flex;
  gap: 16rpx;
  margin-bottom: 34rpx;
}

.chip {
  padding: 16rpx 24rpx;
  border-radius: 999rpx;
  color: $on-surface-variant;
  background: $surface-container-low;
  border: 1rpx solid rgba($outline-variant, 0.5);
  font-size: 24rpx;
  font-weight: 800;
}

.chip.active {
  color: $primary;
  background: rgba($primary, 0.1);
  border-color: rgba($primary, 0.25);
}

.goal-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.goal-card {
  padding: 32rpx;
}

.goal-head {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.round-icon {
  width: 82rpx;
  height: 82rpx;
  border-radius: 50%;
  background: $surface-container-low;
  color: $primary;
  font-size: 22rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

.goal-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goal-title {
  font-size: 32rpx;
  font-weight: 900;
  color: $on-surface;
}

.goal-subtitle {
  margin-top: 4rpx;
  color: $on-surface-variant;
  font-size: 26rpx;
}

.badge {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: rgba($primary, 0.12);
  color: $primary;
  font-size: 24rpx;
  font-weight: 900;
}

.goal-amounts {
  margin-top: 36rpx;
  display: flex;
  justify-content: space-between;
}

.goal-amounts view {
  display: flex;
  flex-direction: column;
}

.right {
  align-items: flex-end;
}

.saved,
.target {
  margin-top: 8rpx;
  font-family: $font-family-mono;
  font-size: 34rpx;
  font-weight: 900;
}

.saved {
  color: $primary;
}

.target {
  color: $on-surface;
}

.goal-progress {
  position: relative;
  height: 22rpx;
  margin-top: 20rpx;
  border-radius: 999rpx;
  background: $surface-container-low;
  overflow: hidden;
}

.goal-percent {
  position: absolute;
  left: 0;
  right: 0;
  top: -2rpx;
  text-align: center;
  color: #fff;
  font-size: 20rpx;
  font-weight: 900;
}

.date-row {
  margin-top: 16rpx;
  color: $outline;
  font-size: 24rpx;
  display: flex;
  justify-content: space-between;
}

.create-goal {
  height: 106rpx;
  border-radius: 44rpx;
  border: 3rpx dashed rgba($outline, 0.45);
  color: $outline;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  font-size: 26rpx;
  font-weight: 800;
}

.create-plus {
  font-size: 34rpx;
}
</style>
