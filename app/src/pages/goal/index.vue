<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <text class="eyebrow">累计储蓄</text>
          <view class="amount-row">
            <text class="currency">¥</text>
            <text class="hero-amount">{{ totalSaved }}</text>
          </view>
          <view class="progress-head">
            <text>总体进度</text>
            <text class="percent">{{ overallProgress }}%</text>
          </view>
          <view class="progress-track">
            <view class="progress-fill" :style="{ width: `${overallProgress}%` }"></view>
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

        <view class="goal-list" v-if="goals.length > 0">
          <view v-for="goal in goals" :key="goal.name" class="goal-card" @tap="handleTap(goal)">
            <view class="goal-head">
              <view class="round-icon">
              <image v-if="goal.icon && (goal.icon.startsWith('http') || goal.icon.startsWith('/static'))" 
                     :src="goal.icon" 
                     class="round-icon-img" 
                     mode="aspectFit" />
              <text v-else>{{ goal.icon }}</text>
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

        <view v-else class="empty-state">
          <image src="/static/goals/no_data.png" class="empty-img" mode="widthFix" />
          <text class="empty-text">还没有目标</text>
          <text class="empty-hint">从一个小目标开始，</text>
          <text class="empty-hint">让每一分钱都有方向。</text>
          <view class="create-button" @tap="handleCreate">
            <image src="/static/images/add.png" class="btn-icon" mode="aspectFit" />
            <text class="btn-text">创建第一个目标</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getGoalList } from '@/api/modules/goal.js'

const isSortOpen = ref(false)

const sortBy = ref('target')

const goals = ref([])

const totalSaved = ref('0.00')
const overallProgress = ref(0)

const fetchGoalList = async () => {
  uni.showLoading({ title: '加载中...', mask: true })
  try {
    const res = await getGoalList()
    if (res && res.data) {
      goals.value = res.data.map(item => ({
        name: item.name,
        desc: item.visibleScope === 'PRIVATE' ? '个人目标' : '家庭目标',
        saved: formatAmount(item.currentAmount || 0),
        target: formatAmount(item.targetAmount || 0),
        progress: item.targetAmount && item.targetAmount > 0 
          ? Math.round((item.currentAmount / item.targetAmount) * 100) 
          : 0,
        start: formatDate(item.startDate),
        end: formatDate(item.deadline),
        badge: getStatusBadge(item.status),
        icon: item.iconUrl,
        id: item.id,
        rawCurrentAmount: item.currentAmount || 0,
        rawTargetAmount: item.targetAmount || 0
      }))

      const totalCurrent = goals.value.reduce((sum, g) => sum + parseFloat(g.rawCurrentAmount), 0)
      const totalTarget = goals.value.reduce((sum, g) => sum + parseFloat(g.rawTargetAmount), 0)
      totalSaved.value = formatAmount(totalCurrent)
      overallProgress.value = totalTarget > 0 ? Math.round((totalCurrent / totalTarget) * 100) : 0
    }
  } catch (error) {
    console.error('获取目标列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const formatAmount = (amount) => {
  return parseFloat(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}`
}

const getStatusBadge = (status) => {
  const statusMap = {
    'ON_GOING': '进行中',
    'COMPLETED': '已完成',
    'ABANDONED': '已放弃',
  }
  return statusMap[status] || ''
}

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
      return parseFloat(b.target.replace(/,/g, '')) - parseFloat(a.target.replace(/,/g, ''))
    } else if (sortBy.value === 'progress') {
      return b.progress - a.progress
    }
    return 0
  })
}

const handleTap = (goal) => {
  uni.navigateTo({ url: `/pages/goal/detail?id=${goal.id}` })
}

const handleCreate = () => {
  uni.navigateTo({ url: '/pages/goal/guide' })
}

onShow(() => {
  fetchGoalList()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.content {
  @include flex-column;
  min-height: 100%;
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
  margin-top: $stack-gap-md;
  margin-bottom: $stack-gap-sm;
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
}

.sort-button,
.add-button {
  width: 52rpx;
  height: 52rpx;
  @include flex-center;
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
  border-radius: $rounded-lg;
  box-shadow: $shadow-soft;
  background: $surface-container-lowest;
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
  @include flex-center;
}

.round-icon-img {
  width: 48rpx;
  height: 48rpx;
}

.goal-main {
  flex: 1;
  @include flex-column;
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
  @include flex-column;
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

.empty-state {
  flex: 1;
  @include flex-center;
  flex-direction: column;
  width: 100%;
}

.empty-img {
  width: 100%;
  margin-top: -$spacing-4;
}

.empty-text {
  font-size: $font-size-2xl;
  font-weight: 900;
  color: $on-surface;
  margin-bottom: $spacing-2;
}

.empty-hint {
  font-size: $font-size-body-sm;
  color: $outline;
  line-height: 1.8;
}

.create-button {
  @include flex-center;
  gap: $spacing-2;
  width: 100%;
  max-width: 480rpx;
  height: 96rpx;
  background: $primary;
  border-radius: $rounded-full;
  margin-top: $spacing-2;
}

.btn-icon {
  width: 36rpx;
  height: 36rpx;
  filter: brightness(0) saturate(100%) invert(1);
}

.btn-text {
  font-size: $font-size-lg;
  font-weight: 900;
  color: $on-primary;
}

</style>
