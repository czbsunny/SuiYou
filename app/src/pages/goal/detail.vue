<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="content" v-if="goal">
        <view class="main-card">
          <view class="hero-bg"></view>
          <view class="hero-content">
            <view class="goal-identity">
              <view class="goal-icon-wrap">
                <text class="goal-icon">{{ goalIcon }}</text>
              </view>
              <view class="goal-title-wrap">
                <text class="goal-name">{{ goal.name }}</text>
                <text class="goal-desc">{{ goal.description || '家庭理财目标' }}</text>
              </view>
              <view class="goal-status" :class="statusClass">{{ statusLabel }}</view>
            </view>

            <view class="amount-row">
              <view class="amount-item">
                <text class="amount-label">目标金额</text>
                <text class="amount-value">¥{{ formatAmount(goal.targetAmount) }}</text>
              </view>
              <view class="amount-divider"></view>
              <view class="amount-item">
                <text class="amount-label">已达成</text>
                <text class="amount-value primary">¥{{ formatAmount(goal.currentAmount) }}</text>
              </view>
              <view class="amount-divider"></view>
              <view class="amount-item">
                <text class="amount-label">剩余缺口</text>
                <text class="amount-value remain">¥{{ formatAmount(remainAmount) }}</text>
              </view>
            </view>

            <view class="progress-section">
              <view class="progress-header">
                <text class="progress-label">完成进度</text>
                <text class="progress-percent">{{ goal.progressPercent || progressPercent }}%</text>
              </view>
              <view class="progress-track">
                <view class="progress-fill" :style="{ width: (goal.progressPercent || progressPercent) + '%' }"></view>
              </view>
            </view>

            <view class="date-row">
              <view class="date-item">
                <text class="date-label">起始日期</text>
                <text class="date-value">{{ formatDate(goal.startDate) }}</text>
              </view>
              <view class="date-arrow">
                <text class="arrow-icon">arrow_forward</text>
              </view>
              <view class="date-item right">
                <text class="date-label">目标日期</text>
                <text class="date-value">{{ formatDate(goal.deadline) }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- ===== 中间：目标跟踪折线图 ===== -->
        <view class="chart-card">
          <view class="section-header">
            <text class="section-title">目标跟踪</text>
            <view class="chart-legend">
              <view class="legend-item">
                <view class="legend-dot target"></view>
                <text>目标线</text>
              </view>
              <view class="legend-item">
                <view class="legend-dot actual"></view>
                <text>实际</text>
              </view>
            </view>
          </view>
          <view class="chart-container">
            <qiun-data-charts
              type="goalLine"
              :chartData="chartData"
              canvas2d
              :tooltipShow="true"
              :onmovetip="true"
              :ontap="true"
              background="rgba(0,0,0,0)"
              style="width: 100%; height: 560rpx;"
            />
          </view>
          <view class="chart-summary">
            <view class="summary-item">
              <text class="summary-label">月均存入</text>
              <text class="summary-value">¥{{ formatAmount(monthlyAvg) }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">预计达标</text>
              <text class="summary-value tertiary">{{ estimatedCompletion }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">坚持月数</text>
              <text class="summary-value">{{ monthsElapsed }}</text>
            </view>
          </view>
        </view>

        <!-- ===== 底部：目标关联资产列表 ===== -->
        <view class="assets-card">
          <view class="section-header">
            <text class="section-title">关联资产</text>
            <text class="section-count">共 {{ linkedAssets.length }} 项</text>
          </view>

          <view v-if="linkedAssets.length > 0" class="asset-list">
            <view v-for="asset in linkedAssets" :key="asset.id" class="asset-item">
              <view class="asset-icon-wrap" :class="asset.colorClass">
                <text class="asset-icon">{{ asset.icon }}</text>
              </view>
              <view class="asset-info">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="asset-type">{{ asset.typeLabel }}</text>
              </view>
              <view class="asset-value-wrap">
                <text class="asset-value">¥{{ formatAmount(asset.balance) }}</text>
                <text class="asset-contrib" :class="asset.trend >= 0 ? 'up' : 'down'">
                  {{ asset.trend >= 0 ? '+' : '' }}{{ asset.trend }}%
                </text>
              </view>
            </view>
          </view>

          <view v-else class="asset-empty">
            <text class="empty-icon">account_balance_wallet</text>
            <text class="empty-text">暂无关联资产</text>
            <text class="empty-hint">为目标配置储蓄账户或投资组合</text>
          </view>
        </view>
      </view>

      <!-- 加载 / 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-title">目标不存在或已删除</text>
        <view class="back-btn" @tap="goBack">
          <text>返回列表</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const goal = ref(null)
const linkedAssets = ref([])

// --------------- icons ---------------
const goalIcon = computed(() => {
  if (!goal.value?.name) return '目'
  return goal.value.name.charAt(0)
})

// --------------- status ---------------
const statusMap = { 'ON_GOING': '进行中', 'COMPLETED': '已完成', 'PAUSED': '已暂停', 'CANCELLED': '已取消' }
const statusLabel = computed(() => statusMap[goal.value?.status] || goal.value?.status || '进行中')
const statusClass = computed(() => (goal.value?.status || 'ON_GOING').toLowerCase())

// --------------- amounts ---------------
const remainAmount = computed(() => {
  if (!goal.value) return 0
  return Math.max(0, (goal.value.targetAmount || 0) - (goal.value.currentAmount || 0))
})
const progressPercent = computed(() => {
  if (!goal.value || !goal.value.targetAmount) return 0
  return Math.min(100, Math.round(((goal.value.currentAmount || 0) / goal.value.targetAmount) * 100))
})

// --------------- chart data for qiun-data-charts ---------------
const rightLabelDate = computed(() => {
  if (!goal.value?.startDate || !goal.value?.deadline) return ''
  const start = new Date(goal.value.startDate)
  const deadline = new Date(goal.value.deadline)
  const diffMonths = (deadline.getFullYear() - start.getFullYear()) * 12 + (deadline.getMonth() - start.getMonth())
  if (diffMonths <= 12) {
    // 不超过1年：开始时间+1年，不能超过截止日
    const plusYear = new Date(start.getFullYear() + 1, start.getMonth(), 1)
    const end = plusYear > deadline ? deadline : plusYear
    return `${end.getFullYear()}/${String(end.getMonth() + 1).padStart(2, '0')}`
  } else {
    // 超过1年：当前时间月份
    const now = new Date()
    return `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, '0')}`
  }
})

const chartData = computed(() => {
  if (!goal.value) return { categories: [], series: [] }
  const start = new Date(goal.value.startDate)
  const deadline = new Date(goal.value.deadline)
  const totalMonths = Math.max(1, (deadline.getFullYear() - start.getFullYear()) * 12 + (deadline.getMonth() - start.getMonth()) + 1)
  const target = Math.round((goal.value.targetAmount || 0) / 10000)
  const current = Math.round((goal.value.currentAmount || 0) / 10000)
  const elapsed = monthsElapsed.value

  const categories = []
  const targetData = []
  const actualData = []
  const baseActual = current * 0.15

  for (let i = 0; i <= totalMonths; i++) {
    const d = new Date(start.getFullYear(), start.getMonth() + i, 1)
    categories.push(`${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}`)
    targetData.push(Math.round((target / totalMonths) * i))
    if (i <= elapsed && elapsed > 0) {
      const phase = i / elapsed
      const curved = baseActual + (current - baseActual) * (1 - Math.pow(1 - phase, 2.5))
      actualData.push(Math.round(curved))
    } else {
      actualData.push(null)
    }
  }

  // 将右侧标签日期追加到末尾（若不在categories中），确保折线图能展示到deadline
  const rightLabel = rightLabelDate.value
  if (rightLabel && !categories.includes(rightLabel)) {
    categories.push(rightLabel)
    targetData.push(target)
    actualData.push(current)
  }

  return {
    categories,
    series: [
      { name: '目标线', data: targetData, color: '#bec9c4' },
      { name: '实际', data: actualData, color: '#006754' }
    ]
  }
})

// --------------- chart stats ---------------
const monthlyAvg = computed(() => {
  if (!goal.value) return 0
  const m = monthsElapsed.value
  return m > 0 ? Math.round((goal.value.currentAmount || 0) / m) : 0
})
const monthsElapsed = computed(() => {
  if (!goal.value || !goal.value.startDate) return 0
  const start = new Date(goal.value.startDate)
  const now = new Date()
  return Math.max(1, (now.getFullYear() - start.getFullYear()) * 12 + (now.getMonth() - start.getMonth()))
})
const estimatedCompletion = computed(() => {
  if (!goal.value) return '-'
  if (goal.value.status === 'COMPLETED') return '已达成'
  const r = remainAmount.value
  const avg = monthlyAvg.value
  if (avg <= 0) return '待存入'
  const remainMonths = Math.ceil(r / avg)
  const now = new Date()
  const est = new Date(now.getFullYear(), now.getMonth() + remainMonths, 1)
  return `${est.getFullYear()}年${est.getMonth() + 1}月`
})

// --------------- formatting ---------------
const formatAmount = (val) => {
  return Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

// --------------- data loading ---------------
const loadData = (id) => {
  // TODO: 接入接口：useGoalStore().fetchGoalDetail(id)
  goal.value = {
    id: id || '1',
    name: '购车计划',
    description: '家庭 SUV 升级',
    targetAmount: 600000,
    currentAmount: 450000,
    progressPercent: 75,
    status: 'ON_GOING',
    startDate: '2024-01-15',
    deadline: '2025-12-31',
    iconUrl: '',
    bgUrl: ''
  }

  linkedAssets.value = [
    { id: 1, name: '工商银行储蓄卡', typeLabel: '活期储蓄', balance: 180000, trend: 2.1, icon: '工', colorClass: 'bank' },
    { id: 2, name: '沪深300指数基金', typeLabel: '基金定投', balance: 150000, trend: 8.5, icon: '沪', colorClass: 'fund' },
    { id: 3, name: '招行定期存款', typeLabel: '1年期定存', balance: 100000, trend: 3.5, icon: '招', colorClass: 'deposit' },
    { id: 4, name: '余额宝', typeLabel: '货币基金', balance: 20000, trend: 1.8, icon: '余额', colorClass: 'wallet' }
  ]
}

onLoad((options) => {
  if (options.id) {
    loadData(options.id)
  } else {
    loadData('1')
  }
})

const goBack = () => { uni.navigateBack() }
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.hero-bg {
  position: absolute;
  top: -120rpx;
  right: -120rpx;
  width: 400rpx;
  height: 400rpx;
  background: rgba($primary, 0.04);
  border-radius: $rounded-full;
  filter: blur(80rpx);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.goal-identity {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $stack-gap-md;
}

.goal-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  border-radius: $rounded-xl;
  background: rgba($primary, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.goal-icon {
  font-size: 48rpx;
  font-weight: 800;
  color: $primary;
}

.goal-title-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goal-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $on-surface;
}

.goal-desc {
  margin-top: 4rpx;
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.goal-status {
  padding: $spacing-1 $spacing-3;
  border-radius: $rounded-full;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  background: rgba($primary, 0.1);
  color: $primary;

  &.completed { background: rgba(#52C41A, 0.1); color: #52C41A; }
  &.paused    { background: rgba(#FAAD14, 0.1); color: #FAAD14; }
  &.cancelled { background: rgba($outline, 0.15); color: $outline; }
}

// 金额三栏
.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $stack-gap-md;
  padding: $spacing-4;
  border-radius: $rounded-lg;
  background: $surface-container-low;
}

.amount-item {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
}

.amount-divider {
  width: 2rpx;
  height: 64rpx;
  background: $outline-variant;
  flex-shrink: 0;
}

.amount-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  margin-bottom: $spacing-1;
}

.amount-value {
  font-family: $font-family-mono;
  font-size: $font-size-num-data;
  font-weight: $font-weight-semibold;
  color: $on-surface;

  &.primary { color: $primary; }
  &.remain  { color: $secondary; }
}

// 进度条
.progress-section {
  margin-bottom: $stack-gap-md;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: $spacing-2;
}

.progress-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $on-surface-variant;
  letter-spacing: 1rpx;
}

.progress-percent {
  font-family: $font-family-mono;
  font-size: $font-size-title-sm;
  font-weight: $font-weight-bold;
  color: $primary;
}

.progress-track {
  height: 20rpx;
  border-radius: $rounded-full;
  background: $surface-container;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: $rounded-full;
  background: linear-gradient(90deg, $primary, $primary-container);
  transition: width 0.8s ease;
}

// 日期行
.date-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date-item {
  display: flex;
  flex-direction: column;

  &.right { align-items: flex-end; }
}

.date-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  letter-spacing: 1rpx;
  margin-bottom: 4rpx;
}

.date-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.date-arrow {
  color: $outline-variant;

  .arrow-icon {
    font-family: 'Material Symbols Outlined';
    font-size: 40rpx;
  }
}

// ===== 资金曲线卡片 =====
.chart-card {
  margin-bottom: $spacing-6;
  padding: $spacing-6 $spacing-1;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-4;
}

.section-title {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.section-count {
  font-size: $font-size-body-sm;
  color: $outline;
}

.chart-legend {
  display: flex;
  gap: $spacing-4;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-label-caps;
  color: $outline;
}

.legend-dot {
  width: 16rpx;
  height: 6rpx;
  border-radius: $rounded-full;

  &.target { background: $outline-variant; }
  &.actual { background: $primary; }
}

.chart-container {
  margin-bottom: $stack-gap-md;
}

.chart-summary {
  display: flex;
  justify-content: space-between;
  padding-top: $spacing-4;
  border-top: 1rpx solid $surface-container;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  letter-spacing: 1rpx;
  margin-bottom: $spacing-1;
}

.summary-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;

  &.tertiary { color: $tertiary; }
}

// ===== 关联资产列表 =====
.assets-card {
  margin-bottom: $spacing-6;
  padding: $spacing-6;
  border-radius: $rounded-xl;
  background: $surface-container-lowest;
  box-shadow: $shadow-soft;
}

.asset-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-sm;
}

.asset-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $rounded-lg;
  background: $surface-container-low;
}

.asset-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-xl;
  display: flex;
  align-items: center;
  justify-content: center;

  &.bank    { background: rgba($primary, 0.1); .asset-icon { color: $primary; } }
  &.fund    { background: rgba(#F59E0B, 0.1);  .asset-icon { color: #F59E0B; } }
  &.deposit { background: rgba($tertiary, 0.1); .asset-icon { color: $tertiary; } }
  &.wallet  { background: rgba(#3B82F6, 0.1);  .asset-icon { color: #3B82F6; } }
}

.asset-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 40rpx;
}

.asset-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.asset-name {
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.asset-type {
  margin-top: 4rpx;
  font-size: $font-size-body-sm;
  color: $on-surface-variant;
}

.asset-value-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.asset-value {
  font-family: $font-family-mono;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.asset-contrib {
  margin-top: 4rpx;
  font-family: $font-family-mono;
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;

  &.up   { color: $profit; }
  &.down { color: $loss; }
}

// 空资产状态
.asset-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-10 0;
}

.empty-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 80rpx;
  color: $outline-variant;
  margin-bottom: $spacing-3;
}

.empty-text {
  font-size: $font-size-body-reg;
  color: $on-surface-variant;
  margin-bottom: $spacing-1;
}

.empty-hint {
  font-size: $font-size-body-sm;
  color: $outline;
}

// ===== 空/错误状态 =====
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-title {
  font-size: $font-size-body-reg;
  color: $on-surface-variant;
  margin-bottom: $spacing-6;
}

.back-btn {
  padding: $spacing-3 $spacing-8;
  border-radius: $rounded-full;
  background: $primary;
  color: $on-primary;
  font-size: $font-size-body-reg;
  font-weight: $font-weight-bold;

  &:active {
    opacity: 0.9;
    transform: scale(0.96);
  }
}
</style>
