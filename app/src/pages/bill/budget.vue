<template>
  <view class="page budget-page">
    <scroll-view scroll-y class="scroll">
      <view class="content">
        <view class="main-card">
          <view class="ring-container">
            <view class="ring-wrapper">
              <qiun-data-charts
                type="ring"
                :chartData="ringChartData"
                :opts="ringChartOpts"
                canvasId="budgetRing"
                :inScrollView="false"
                :resscale="false"
              />
              <!-- 中心文字区域：添加 pointer-events: none 避免阻挡图表层级 -->
              <view class="ring-center">
                <text class="ring-label">剩余可用</text>
                <view class="ring-amount">
                  <text class="currency">¥</text>
                  <text class="amount-num">12,480</text>
                </view>
                <text class="ring-total">总预算 ¥45,000</text>
              </view>
            </view>
          </view>
          <view class="stats-row">
            <view class="stat-card">
              <text class="stat-label">本月支出</text>
              <text class="stat-value">¥32,520</text>
            </view>
            <view class="stat-card">
              <text class="stat-label">预算进度</text>
              <text class="stat-value primary">72%</text>
            </view>
          </view>
        </view>

        <!-- Category Budget List -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">支出分类</text>
            <view class="settings-btn" @tap="handleSettings">
              <text class="icon-text">设置</text>
            </view>
          </view>

          <view class="category-list">
            <view v-for="item in categories" :key="item.name" class="category-card">
              <view class="category-header">
                <view class="category-icon-wrapper" :class="item.colorClass">
                  <text class="category-icon-text">{{ item.iconText }}</text>
                </view>
                <view class="category-info">
                  <text class="category-name">{{ item.name }}</text>
                  <text class="category-remaining" :class="{ error: item.isOverBudget }">{{ item.remainingText }}</text>
                </view>
                <view class="category-amount">
                  <text class="amount" :class="{ secondary: item.isOverBudget }">¥{{ item.amount }}</text>
                  <text class="limit">限额 ¥{{ item.limit }}</text>
                </view>
              </view>
              <view class="progress-bar">
                <view class="progress-fill" :class="{ secondary: item.isOverBudget }" :style="{ width: item.progress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const categories = ref([
  {
    name: '餐饮美食',
    iconText: '餐饮',
    amount: '4,800',
    limit: '6,000',
    progress: 80,
    remainingText: '剩余 ¥1,200',
    colorClass: 'primary',
    isOverBudget: false
  },
  {
    name: '交通出行',
    iconText: '交通',
    amount: '1,650',
    limit: '2,500',
    progress: 66,
    remainingText: '剩余 ¥850',
    colorClass: 'primary',
    isOverBudget: false
  },
  {
    name: '购物消费',
    iconText: '购物',
    amount: '3,230',
    limit: '3,000',
    progress: 100,
    remainingText: '超出 ¥230',
    colorClass: 'secondary',
    isOverBudget: true
  },
  {
    name: '居住物业',
    iconText: '居住',
    amount: '12,500',
    limit: '20,000',
    progress: 62.5,
    remainingText: '剩余 ¥7,500',
    colorClass: 'primary',
    isOverBudget: false
  }
])

// 环形图数据
const ringChartData = ref({
  series: [{
    data: [
      { value: 32520, name: '已使用' },
      { value: 12480, name: '剩余' }
    ]
  }]
})

// 图表配置 - 隐藏所有标签，只保留圆环
const ringChartOpts = ref({
  color: ['#006754', '#e3e2e0'],
  padding: [0, 0, 0, 0],
  legend: {
    show: false
  },
  tooltip: {
    show: false
  },
  title: {
    name: "" 
  },
  subtitle: {
    name: "" 
  },
  series: {
    dataLabel: false,
    label: {
      show: false
    },
    labelLine: {
      show: false
    }
  },
  extra: {
    ring: {
      ringWidth: 14,
      activeOpacity: 1,
      activeRadius: 0,
      offsetAngle: -90,
      customRadius: 75,
      labelWidth: 0
    }
  }
})

const handleSettings = () => {
  uni.showToast({ title: '设置', icon: 'none' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.scroll {
  height: 100vh;
}

.ring-container {
  display: flex;
  justify-content: center;
}

.ring-wrapper {
  position: relative;
  width: 320rpx;
  height: 320rpx;
  
  /* 确保图表容器撑满父级 */
  :deep(.chartsbox) {
    width: 100% !important;
    height: 100% !important;
  }
}

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  pointer-events: none; /* 穿透点击事件，防止遮挡图表交互 */
}

.ring-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.ring-amount {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.currency {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-bold;
  color: $primary;
}

.amount-num {
  font-family: $font-family-mono;
  font-size: $font-size-headline-md;
  font-weight: $font-weight-semibold;
  color: $primary;
}

.ring-total {
  font-size: $font-size-body-sm;
  color: $outline;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $stack-gap-md;
  margin-top: $section-margin;
}

.stat-card {
  padding: $spacing-4;
  border-radius: $rounded-lg;
  background: $surface;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: $font-size-label-caps;
  font-weight: $font-weight-bold;
  color: $outline;
  text-transform: uppercase;
}

.stat-value {
  margin-top: $spacing-1;
  font-family: $font-family-mono;
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;

  &.primary {
    color: $primary;
  }
}

.section {
  margin-top: $section-margin;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $stack-gap-md;
}

.section-title {
  font-size: $font-size-headline-md;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: $stack-gap-md;
}

.category-card {
  padding: $spacing-5;
  border-radius: $rounded-lg;
  background: $surface-container-lowest;
  box-shadow: $shadow-sm;
}

.category-header {
  display: flex;
  align-items: center;
  gap: $stack-gap-md;
  margin-bottom: $stack-gap-sm;
}

.category-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: $rounded-full;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary {
    background: rgba($primary-container, 0.1);
    .category-icon-text {
      color: $primary;
    }
  }

  &.secondary {
    background: rgba($secondary-container, 0.1);
    .category-icon-text {
      color: $secondary;
    }
  }
}

.category-icon-text {
  font-size: $font-size-body-sm;
  font-weight: $font-weight-semibold;
}

.category-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-name {
  font-size: $font-size-title-sm;
  font-weight: $font-weight-semibold;
  color: $on-surface;
}

.category-remaining {
  margin-top: 4rpx;
  font-size: $font-size-body-sm;
  color: $outline;

  &.error {
    color: $error;
  }
}

.category-amount {
  text-align: right;
}

.amount {
  display: block;
  font-family: $font-family-mono;
  font-size: $font-size-num-data;
  font-weight: $font-weight-medium;
  color: $on-surface;

  &.secondary {
    color: $secondary;
  }
}

.limit {
  font-size: $font-size-body-sm;
  color: $outline;
}

.progress-bar {
  height: 12rpx;
  border-radius: $rounded-full;
  background: $surface-container-highest;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: $rounded-full;
  background: $primary;
  transition: width 0.7s ease;

  &.secondary {
    background: $secondary;
  }
}
</style>