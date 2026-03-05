<template>
  <view class="header-container">
    <view class="header-card card-warm">
      <!-- 顶部：年份与状态标签 -->
      <view class="header-top">
        <text class="plan-year">{{ year }} 年度理财规划</text>
        <view class="status-tag">
          <text class="status-text">{{ statusIcon }} {{ statusText }}</text>
        </view>
      </view>
      
      <view class="header-main">
        <view class="main-data">
          <text class="label">待完成资金缺口 (元)</text>
          <!-- 数值回归深咖啡黑，应用 DIN 字体 -->
          <text class="val-remain num-font">{{ formatMoney(remainAmount) }}</text>
          <view class="target-info">
            <text class="target-label">年度目标</text>
            <text class="target-val num-font">¥{{ formatMoney(totalAmount) }}</text>
          </view>
        </view>
        
        <!-- 进度圆环：色调回归墨绿系 -->
        <view class="chart-box">
          <svg class="ring-svg" width="120" height="120" viewBox="0 0 100 100">
            <circle class="ring-track" cx="50" cy="50" r="42"></circle>
            <circle 
              class="ring-fill" 
              cx="50" cy="50" r="42"
              :style="{ strokeDashoffset: strokeDashoffset }"
            ></circle>
          </svg>
          <view class="chart-inner">
            <text class="percent num-font">{{ progressPercent }}<text class="unit">%</text></text>
            <text class="progress-label">已达成</text>
          </view>
        </view>
      </view>

      <!-- 底部统计：简洁的横向排布 -->
      <view class="header-footer">
        <view class="footer-stat">
          <view class="indicator-dot active"></view>
          <text class="stat-text">{{ activeCount }} 个进行中</text>
        </view>
        <view class="footer-stat">
          <view class="indicator-dot pending"></view>
          <text class="stat-text">{{ pendingCount }} 个待开启</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  year: { type: Number, default: new Date().getFullYear() },
  remainAmount: { type: Number, default: 0 },
  totalAmount: { type: Number, default: 0 },
  activeCount: { type: Number, default: 0 },
  pendingCount: { type: Number, default: 0 },
  statusIcon: { type: String, default: '📈' },
  statusText: { type: String, default: '进度正常' }
});

const progressPercent = computed(() => {
  if (!props.totalAmount) return 0;
  const percent = Math.round(((props.totalAmount - props.remainAmount) / props.totalAmount) * 100);
  return Math.min(100, Math.max(0, percent));
});

const circumference = 263.8;
const strokeDashoffset = computed(() => {
  return circumference - (progressPercent.value / 100) * circumference;
});

const formatMoney = (val) => {
  return Number(val).toLocaleString('zh-CN', { 
    minimumFractionDigits: 2, 
    maximumFractionDigits: 2 
  });
};
</script>

<style lang="scss" scoped>
.header-container {
  /* 增加顶部间距，彻底脱离导航栏 */
  padding: $spacing-base $spacing-md 0;
  background-color: transparent;
}

.header-card {
  /* 回归白底卡片风格 */
  background-color: $bg-white;
  padding: 40rpx;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(50, 46, 43, 0.02);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.plan-year {
  font-size: 24rpx;
  color: $text-sub;
  font-weight: 500;
}

.status-tag {
  /* 浅色背景标签 */
  background-color: $bg-subtle;
  padding: 6rpx 20rpx;
  border-radius: $radius-full;
  
  .status-text {
    font-size: 22rpx;
    color: $primary;
    font-weight: 600;
  }
}

.header-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40rpx;
}

.main-data {
  .label {
    font-size: 24rpx;
    color: $text-muted;
    margin-bottom: 12rpx;
    display: block;
  }
  .val-remain {
    font-size: 48rpx;
    font-weight: 700;
    color: $text-main; // 回归深咖啡黑
    font-family: $font-family-money;
    display: block;
    line-height: 1;
  }
  .target-info {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
    gap: 8rpx;
    
    .target-label { font-size: 22rpx; color: $text-muted; }
    .target-val { font-size: 24rpx; font-weight: 500; color: $text-sub; }
  }
}

.chart-box {
  position: relative;
  width: 120rpx;
  height: 120rpx;
}

.ring-svg {
  transform: rotate(-90deg);
  width: 120rpx;
  height: 120rpx;
}

.ring-track {
  fill: transparent;
  stroke: $bg-subtle; // 极浅绿灰轨道
  stroke-width: 8;
}

.ring-fill {
  fill: transparent;
  stroke: $primary; // 墨绿进度条
  stroke-width: 10;
  stroke-linecap: round;
  stroke-dasharray: 263.8;
  transition: stroke-dashoffset 1s ease-out;
}

.chart-inner {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  @include flex-center;
  flex-direction: column;
  
  .percent {
    font-size: 32rpx;
    font-weight: 700;
    color: $primary;
    line-height: 1;
    .unit { font-size: 18rpx; margin-left: 2rpx; }
  }
  .progress-label {
    font-size: 16rpx;
    color: $text-muted;
    margin-top: 4rpx;
  }
}

.header-footer {
  display: flex;
  gap: 48rpx;
  border-top: 1rpx solid $bg-subtle;
  padding-top: 32rpx;
}

.footer-stat {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .indicator-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    
    &.active { background-color: $primary; }
    &.pending { background-color: $text-placeholder; }
  }
  
  .stat-text {
    font-size: 24rpx;
    color: $text-sub;
  }
}

.num-font {
  font-family: $font-family-money;
  @include tabular-nums;
}
</style>