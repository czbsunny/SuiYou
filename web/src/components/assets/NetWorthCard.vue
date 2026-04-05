<template>
  <view class="card-container">
    <!-- 1. 顶部数据区 -->
    <view class="card-header">
      <view class="header-left">
        <view class="label-row">
          <text class="label-text">家庭净资产</text>
          <view class="status-badge" :class="trendClass">{{ trendLabel }}</view>
        </view>
        <text class="amount-text">{{ amount }}</text>
      </view>
      
      <view class="header-right">
        <text class="trend-text" :class="isPositive ? 'trend-up' : 'trend-down'">
          {{ trend }}
        </text>
        <text class="trend-period">近12个月</text>
      </view>
    </view>

    <!-- 2. 动态图表区 (SVG) -->
    <view class="chart-wrapper">
      <svg viewBox="0 0 300 100" class="svg-graph" preserveAspectRatio="none">
        <defs>
          <linearGradient id="chartGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:#2a806c;stop-opacity:0.2" />
            <stop offset="100%" style="stop-color:#2a806c;stop-opacity:0" />
          </linearGradient>
          <!-- 虚线样式 -->
          <pattern id="dashPattern" width="8" height="8" patternUnits="userSpaceOnUse">
            <path d="M 0 4 L 8 4" stroke="#ccc" stroke-width="1" />
          </pattern>
        </defs>
        <!-- 0 轴线 -->
        <line x1="0" y1="50" x2="300" y2="50" stroke="url(#dashPattern)" stroke-width="1" />
        <!-- 填充区域 -->
        <path :d="fillPath" fill="url(#chartGradient)" />
        <!-- 线条 -->
        <path :d="linePath" fill="none" :stroke="lineColor" stroke-width="2" stroke-linecap="round" />
        <!-- 虚线部分（当数据不足12个月时） -->
        <path v-if="hasPartialData" :d="dashPath" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-dasharray="4,4" />
        <!-- 当前点 -->
        <circle v-if="chartPoints.length > 0" :cx="lastPoint.x" :cy="lastPoint.y" r="4" fill="#2a806c" stroke="white" stroke-width="2" />
      </svg>
      
      <!-- X轴 时间轴 -->
      <view class="axis-labels">
        <text v-for="(label, index) in axisLabels" :key="index">{{ label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  amount: {
    type: String,
    default: '¥ 2,850,200'
  },
  trend: {
    type: String,
    default: '+12.5%'
  },
  // 历史数据，格式：[{ date: '2024-01-15', value: 2500000 }, ...]
  historyData: {
    type: Array,
    default: () => []
  }
});

// 判断涨跌颜色
const isPositive = computed(() => {
  return props.trend.includes('+');
});

// 趋势标签
const trendLabel = computed(() => {
  if (!props.historyData || props.historyData.length < 2) return '暂无数据';
  
  const firstValue = props.historyData[0].value;
  const lastValue = props.historyData[props.historyData.length - 1].value;
  const change = ((lastValue - firstValue) / firstValue) * 100;
  
  if (change > 5) return '稳健增长';
  if (change > 0) return '小幅增长';
  if (change > -5) return '小幅下降';
  return '明显下降';
});

// 趋势样式类
const trendClass = computed(() => {
  if (!props.historyData || props.historyData.length < 2) return '';
  
  const firstValue = props.historyData[0].value;
  const lastValue = props.historyData[props.historyData.length - 1].value;
  const change = ((lastValue - firstValue) / firstValue) * 100;
  
  if (change > 0) return 'trend-up';
  return 'trend-down';
});

// 生成最近12个月的时间轴标签
const axisLabels = computed(() => {
  const labels = [];
  const now = new Date();
  
  // 生成5个标签（每3个月一个）
  for (let i = 4; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i * 3, 1);
    if (i === 0) {
      labels.push('现在');
    } else {
      labels.push(`${date.getMonth() + 1}月`);
    }
  }
  
  return labels;
});

// 计算是否有部分数据（不足12个月）
const hasPartialData = computed(() => {
  return props.historyData && props.historyData.length > 0 && props.historyData.length < 12;
});

// 计算图表数据点
const chartPoints = computed(() => {
  if (!props.historyData || props.historyData.length === 0) {
    return [];
  }
  
  // 计算时间范围
  const now = new Date();
  const startDate = new Date(now.getFullYear(), now.getMonth() - 11, 1); // 12个月前
  
  // 找出最大值和最小值，包含0
  const values = props.historyData.map(item => item.value);
  const maxValue = Math.max(...values, 0);
  const minValue = Math.min(...values, 0);
  const range = maxValue - minValue || 1;
  
  // 将数据映射到图表坐标 (300x100)
  return props.historyData.map((item, index) => {
    // 计算该数据点在12个月时间轴上的位置
    const itemDate = new Date(item.date);
    const totalMonths = 12;
    const monthsSinceStart = (itemDate.getFullYear() - startDate.getFullYear()) * 12 + (itemDate.getMonth() - startDate.getMonth());
    const x = (monthsSinceStart / (totalMonths - 1)) * 300;
    
    // 计算Y坐标，将0放在中间
    const normalizedValue = (item.value - minValue) / range;
    const y = 100 - (normalizedValue * 80 + 10); // 留出上下边距
    return { x, y };
  });
});

// 线条颜色
const lineColor = computed(() => {
  if (!props.historyData || props.historyData.length < 2) return '#2a806c';
  
  const firstValue = props.historyData[0].value;
  const lastValue = props.historyData[props.historyData.length - 1].value;
  return lastValue >= firstValue ? '#2a806c' : '#ff4d4f';
});

// 生成线条路径
const linePath = computed(() => {
  if (chartPoints.value.length === 0) return '';
  
  const points = chartPoints.value;
  let path = `M${points[0].x},${points[0].y}`;
  
  // 使用贝塞尔曲线平滑连接
  for (let i = 1; i < points.length; i++) {
    const prev = points[i - 1];
    const curr = points[i];
    const cx = (prev.x + curr.x) / 2;
    path += ` Q${cx},${prev.y} ${curr.x},${curr.y}`;
  }
  
  return path;
});

// 生成虚线路径（当数据不足12个月时）
const dashPath = computed(() => {
  if (!hasPartialData.value || chartPoints.value.length === 0) return '';
  
  const firstPoint = chartPoints.value[0];
  return `M0,50 Q${firstPoint.x / 2},50 ${firstPoint.x},${firstPoint.y}`;
});

// 生成填充区域路径
const fillPath = computed(() => {
  if (chartPoints.value.length === 0) return '';
  
  const line = linePath.value;
  const lastPoint = chartPoints.value[chartPoints.value.length - 1];
  const firstPoint = chartPoints.value[0];
  
  return `${line} L${lastPoint.x},100 L${firstPoint.x},100 Z`;
});

// 最后一个点（用于显示当前点）
const lastPoint = computed(() => {
  if (chartPoints.value.length === 0) return { x: 300, y: 50 };
  return chartPoints.value[chartPoints.value.length - 1];
});
</script>

<style lang="scss" scoped>
.card-container {
  margin: $spacing-sm $spacing-base $spacing-base;
  padding: $spacing-base;
  background-color: $bg-white;
  border-radius: 32rpx; // 对应 rounded-3xl
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}

/* 顶部布局 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: $spacing-base;
  position: relative;
  z-index: 10;
}

.label-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.label-text {
  font-size: 24rpx;
  color: $text-muted;
}

.status-badge {
  background-color: rgba($green, 0.1);
  color: $green;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-weight: 700;
  
  &.trend-up {
    background-color: rgba($red, 0.1);
    color: $red;
  }
  
  &.trend-down {
    background-color: rgba($green, 0.1);
    color: $green;
  }
}

.amount-text {
  font-size: 56rpx; // text-3xl
  font-weight: 700;
  font-family: monospace, sans-serif; // font-mono
  letter-spacing: -1px;
  color: $text-main;
  line-height: 1;
}

/* 右侧涨跌幅 */
.header-right {
  text-align: right;
}

.trend-text {
  font-size: 28rpx;
  font-weight: 700;
}
.trend-up { color: $red; }
.trend-down { color: $green; }

.trend-period {
  font-size: 20rpx;
  color: $text-muted;
  margin-top: 4rpx;
  display: block;
}

/* 图表区 */
.chart-wrapper {
  position: relative;
  width: 100%;
  height: 160rpx; // 对应 h-24
  display: flex;
  flex-direction: column;
}

.svg-graph {
  width: 100%;
  height: calc(100% - 24rpx); // 留出时间轴空间
  overflow: visible;
}

.axis-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8rpx;
  height: 24rpx; // 固定时间轴高度
}

.axis-labels text {
  font-size: 18rpx;
  color: $text-muted;
  font-weight: 500;
}
</style>