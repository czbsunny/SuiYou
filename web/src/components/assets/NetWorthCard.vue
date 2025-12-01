<template>
  <view class="card-container">
    <!-- 1. 顶部数据区 -->
    <view class="card-header">
      <view class="header-left">
        <view class="label-row">
          <text class="label-text">家庭净资产</text>
          <view class="status-badge">稳健增长</view>
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

    <!-- 2. 装饰性图表区 (SVG) -->
    <view class="chart-wrapper">
      <!-- 
        注意：在某些小程序平台(如支付宝/百度)可能不支持内联SVG。
        如果遇到兼容性问题，建议将此 SVG 转为 Base64 背景图或使用 Canvas 绘制。
        但在 H5 和 微信小程序 中通常表现良好。
      -->
      <svg viewBox="0 0 300 100" class="svg-graph" preserveAspectRatio="none">
        <defs>
          <linearGradient id="chartGradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" style="stop-color:#2a806c;stop-opacity:0.2" />
            <stop offset="100%" style="stop-color:#2a806c;stop-opacity:0" />
          </linearGradient>
        </defs>
        <!-- 填充区域 -->
        <path d="M0,100 L0,80 Q50,90 100,60 T200,40 T300,10 L300,100 Z" fill="url(#chartGradient)" />
        <!-- 线条 -->
        <path d="M0,80 Q50,90 100,60 T200,40 T300,10" fill="none" stroke="#2a806c" stroke-width="2" stroke-linecap="round" />
        <!-- 当前点 -->
        <circle cx="300" cy="10" r="4" fill="#2a806c" stroke="white" stroke-width="2" />
      </svg>
      
      <!-- X轴 时间轴 -->
      <view class="axis-labels">
        <text>JAN</text>
        <text>MAR</text>
        <text>JUN</text>
        <text>SEP</text>
        <text>NOW</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, defineProps } from 'vue';

const props = defineProps({
  amount: {
    type: String,
    default: '¥ 2,850,200'
  },
  trend: {
    type: String,
    default: '+12.5%'
  }
});

// 判断涨跌颜色
const isPositive = computed(() => {
  return props.trend.includes('+');
});
</script>

<style lang="scss" scoped>
.card-container {
  margin: 24rpx; // 对应 mx-4
  padding: 32rpx; // 对应 p-5
  background-color: #ffffff;
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
  margin-bottom: 20rpx;
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
  color: #9ca3af; // text-gray-400
}

.status-badge {
  background-color: #f0fdf4; // bg-green-50
  color: #2a806c;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  font-weight: 700;
}

.amount-text {
  font-size: 56rpx; // text-3xl
  font-weight: 700;
  font-family: monospace, sans-serif; // font-mono
  letter-spacing: -1px;
  color: #2C3E50;
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
.trend-up { color: #ef4444; } // text-red-500
.trend-down { color: #22c55e; } // text-green-500

.trend-period {
  font-size: 20rpx;
  color: #9ca3af;
  margin-top: 4rpx;
  display: block;
}

/* 图表区 */
.chart-wrapper {
  position: relative;
  width: 100%;
  height: 160rpx; // 对应 h-24 (24 * 4 = 96px ≈ 192rpx，这里微调为160)
}

.svg-graph {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.axis-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8rpx;
}

.axis-labels text {
  font-size: 16rpx; // text-[8px]
  color: #d1d5db; // text-gray-300
  font-weight: 700;
  text-transform: uppercase;
}
</style>