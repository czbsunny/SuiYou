<template>
  <view class="trend-chart">
    <svg class="chart-svg" viewBox="0 0 100 40" preserveAspectRatio="none">
      <defs>
        <linearGradient id="chartGradient" x1="0%" x2="0%" y1="0%" y2="100%">
          <stop offset="0%" :stop-color="lineColor" stop-opacity="0.1"></stop>
          <stop offset="100%" :stop-color="lineColor" stop-opacity="0"></stop>
        </linearGradient>
      </defs>
      <path :d="areaPath" fill="url(#chartGradient)"></path>
      <path :d="linePath" fill="none" :stroke="lineColor" stroke-width="1.5" class="trend-line"></path>
      <circle 
        v-for="(point, index) in points" 
        :key="index"
        :cx="point.x" 
        :cy="point.y" 
        :fill="lineColor" 
        r="1.2"
      ></circle>
    </svg>
    <view class="chart-labels">
      <span v-for="(label, index) in labels" :key="index">{{ label }}</span>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  color: {
    type: String,
    default: '#2a806c'
  }
})

const lineColor = computed(() => props.color)

const points = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }
  const values = props.data.map(d => d.value)
  const min = Math.min(...values)
  const max = Math.max(...values)
  const range = max - min || 1
  
  return props.data.map((d, i) => ({
    x: (i / (props.data.length - 1)) * 100,
    y: 40 - ((d.value - min) / range) * 35 - 2.5
  }))
})

const linePath = computed(() => {
  if (points.value.length < 2) return ''
  return points.value.map((p, i) => 
    `${i === 0 ? 'M' : 'T'} ${p.x} ${p.y}`
  ).join(' ')
})

const areaPath = computed(() => {
  if (points.value.length < 2) return ''
  const line = points.value.map((p, i) => 
    `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`
  ).join(' ')
  const lastX = points.value[points.value.length - 1].x
  const firstX = points.value[0].x
  return `${line} L ${lastX} 40 L ${firstX} 40 Z`
})

const labels = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }
  const step = Math.ceil(props.data.length / 5)
  return props.data.filter((_, i) => i % step === 0).map(d => {
    const date = new Date(d.month)
    return `'${String(date.getFullYear()).slice(-2)}.${String(date.getMonth() + 1).padStart(2, '0')}`
  })
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.trend-chart {
  width: 100%;
  height: 192rpx;
  position: relative;
  
  .chart-svg {
    width: 100%;
    height: 100%;
  }
  
  .trend-line {
    stroke-dasharray: 1000;
    stroke-dashoffset: 1000;
    animation: drawLine 2s ease-in-out forwards;
  }
  
  .chart-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8rpx;
    
    span {
      font-family: $font-family-primary;
      font-size: 20rpx;
      color: $on-surface-variant;
    }
  }
}

@keyframes drawLine {
  to {
    stroke-dashoffset: 0;
  }
}
</style>