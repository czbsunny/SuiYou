<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
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
            <view class="sort-button" @tap="toggleSort">
              <image src="/static/images/manage.png" class="sort-icon" mode="aspectFit" />
            </view>
            <view class="add-button" @tap="handleCreate">
              <image src="/static/images/add.png" class="add-icon" mode="aspectFit" />
            </view>
          </view>
        </view>

        <view v-if="isSortOpen" class="chips">
          <view class="chip" :class="{ active: sortBy === 'target' }" @tap="selectSort('target')">目标金额 ↕</view>
          <view class="chip" :class="{ active: sortBy === 'progress' }" @tap="selectSort('progress')">完成度 ↕</view>
        </view>

        <view class="goal-list">
          <view v-for="goal in goals" :key="goal.name" class="goal-card" @tap="handleTap(goal)">
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

            <view class="goal-progress-row">
              <view class="goal-progress">
                <view class="goal-fill" :style="{ width: `${goal.progress}%` }"></view>
              </view>
              <text class="goal-percent">{{ goal.progress }}%</text>
            </view>
            <view class="date-row">
              <text>起始: {{ goal.start }}</text>
              <text>结束: {{ goal.end }}</text>
            </view>
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

const handleTap = (goal) => {
  uni.navigateTo({ url: `/pages/goal/detail?id=${goal.name}` })
}

const handleCreate = () => {
  uni.navigateTo({ url: '/pages/goal/guide' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.goal-card {
  background: #fff;
  border-radius: 44rpx;
  box-shadow: $shadow-soft;
}

.eyebrow,
.label {
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.amount-row {
  margin-top: $stack-gap-xs;
  display: flex;
  align-items: baseline;
  gap: $stack-gap-sm;
}

.currency {
  color: $on-surface;
  font-size: $font-size-headline-md;
  font-weight: 900;
}

.hero-amount {
  font-family: $font-family-mono;
  font-size: $font-size-num-display;
  font-weight: 900;
  color: $on-surface;
}

.progress-head {
  margin-top: $section-margin;
  margin-bottom: $spacing-2;
  color: $outline;
  font-size: $font-size-body-sm;
  font-weight: 900;
  letter-spacing: 1rpx;
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
  border-radius: $rounded-full;
  background: $surface-container-low;
  overflow: hidden;
}

.progress-fill,
.goal-fill {
  height: 100%;
  border-radius: $rounded-full;
  background: $primary-container;
}

.section-heading {
  margin-top: $section-margin;
  margin-bottom: $stack-gap-md;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  color: $on-surface;
  font-size: $font-size-title-sm;
  font-weight: 900;
}

.heading-actions {
  display: flex;
  align-items: center;
  gap: $stack-gap-sm;
  color: $on-surface;
}

.sort-button,
.add-button {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
}

.sort-icon,
.add-icon {
  width: 36rpx;
  height: 36rpx;
  filter: brightness(0) saturate(100%);
}

.chips {
  display: flex;
  gap: $stack-gap-sm;
  margin-bottom: $stack-gap-md;
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
  gap: $stack-gap-md;
}

.goal-card {
  padding: $spacing-4;
  border-radius: $rounded-xl;
  box-shadow: $shadow-soft;
}

.goal-head {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.round-icon {
  width: 82rpx;
  height: 82rpx;
  border-radius: 50%;
  background: $surface-container-low;
  color: $primary;
  font-size: $font-size-xs;
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
  font-size: $font-size-lg;
  font-weight: 900;
  color: $on-surface;
}

.goal-subtitle {
  margin-top: $spacing-1;
  color: $on-surface-variant;
  font-size: $font-size-sm;
}

.badge {
  padding: $spacing-2 $spacing-3;
  border-radius: $rounded-full;
  background: rgba($primary, 0.12);
  color: $primary;
  font-size: $font-size-xs;
  font-weight: 900;
}

.goal-amounts {
  margin-top: $spacing-5;
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
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-xl;
  font-weight: 900;
}

.saved {
  color: $primary;
}

.target {
  color: $on-surface;
}

.goal-progress-row {
  margin-top: $spacing-3;
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.goal-progress {
  flex: 1;
  height: 22rpx;
  border-radius: $rounded-full;
  background: $surface-container-low;
  overflow: hidden;
}

.goal-percent {
  flex-shrink: 0;
  width: 80rpx;
  text-align: right;
  color: $primary;
  font-family: $font-family-mono;
  font-size: $font-size-sm;
  font-weight: 900;
}

.date-row {
  margin-top: $stack-gap-sm;
  color: $outline;
  font-size: $font-size-xs;
  display: flex;
  justify-content: space-between;
}

</style>
