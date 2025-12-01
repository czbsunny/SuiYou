<template>
  <view class="asset-card">
     <!-- 背景装饰图标 -->
     <view class="bg-decoration">
         <img src="/static/images/trending-up.png" class="bg-icon" />
     </view>

     <!-- 顶部总资产区域 -->
     <view class="card-section mb-large">
         <view class="header-row">
             <text class="label-text">总资产 (元)</text>
             <view @click="$emit('toggleBalance')" class="eye-btn">
                  <img :src="showBalance ? '/static/images/eye.png' : '/static/images/eye-closed.png'" class="eye-icon" />
             </view>
         </view>
         <view class="amount-row">
              <text class="main-amount">{{ showBalance ? formatCurrency(totalAssets) : '****' }}</text>
         </view>
     </view>

     <!-- 底部收益区域 -->
     <view class="card-section flex-row">
         <!-- 左侧：当日收益 -->
         <view class="stat-item">
              <text class="sub-label">当日收益 (元)</text>
              <text :class="['stat-value', dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
                  {{ dailyReturn > 0 ? '+' : '' }}{{ showBalance ? formatNumber(dailyReturn) : '***' }}
              </text>
         </view>
         
         <!-- 分割线 -->
         <view class="divider"></view>
         
         <!-- 右侧：累计收益 -->
         <view class="stat-item">
              <text class="sub-label">累计收益 (元)</text>
              <text :class="['stat-value', totalReturn >= 0 ? 'text-gain' : 'text-loss']">
                  {{ totalReturn > 0 ? '+' : '' }}{{ showBalance ? formatNumber(totalReturn) : '***' }}
              </text>
         </view>
     </view>
  </view>
</template>

<script setup>
import { formatCurrency, formatNumber } from '../../services/formatUtil';

defineProps(['totalAssets', 'totalReturn', 'dailyReturn', 'showBalance']);
defineEmits(['toggleBalance']);
formatCurrency

</script>

<style lang="scss" scoped>

.asset-card {
  background-color: rgba($bg-white, 0.1);
  backdrop-filter: blur(12px); /* backdrop-blur-md */
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba($border-bottom, 0.1);
  border-radius: 16px;
  padding: 12px; /* p-6 */
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05); /* shadow-xl */
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: -48px;
  right: -12px;
  opacity: 0.08;
  transform: rotate(15deg);
  pointer-events: none;
  z-index: 0;
}

.bg-icon {
  width: 140px; /* :size="140" */
  height: 140px;
}

/* 通用布局层级控制 */
.card-section {
  position: relative;
  z-index: 10;
}

.mb-large {
  margin-bottom: 12px; /* mb-6 */
}

.flex-row {
  margin-top: 12px; /* mt-6 */
  display: flex;
  flex-direction: row;
  align-items: center;
}

/* 顶部 Header */
.header-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 8px; /* mb-2 */
}

.label-text {
  font-size: 12px; /* text-xs */
  font-weight: 500; /* font-medium */
  color: $text-main;
  margin-right: 8px; /* space-x-2 */
}

.eye-btn {
  opacity: 0.6;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;

  /* hover 效果 */
  &:active {
    opacity: 1;
  }
}

.eye-icon {
  width: 16px;
  height: 16px;
}

.amount-row {
  /* text-4xl font-bold font-mono tracking-tight */
}

.main-amount {
  font-size: 36px; /* text-4xl (approx 36px) */
  line-height: 40px;
  font-weight: bold;
  color: $text-inverse;
  font-family: monospace; /* font-mono */
  letter-spacing: -0.025em; /* tracking-tight */
}

/* 底部统计区域 */
.stat-item {
  flex: 1;
}

.sub-label {
  font-size: 12px; /* text-[12px] */
  color: $text-main;
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

.stat-value {
  font-size: 18px; /* text-lg */
  font-weight: bold;
  font-family: monospace; /* font-mono */
  
  &.text-gain {
    color: $text-gain-light;
  }
  
  &.text-loss {
    color: $text-loss-light;
  }
}

.divider {
  width: 1px;
  height: 32px; /* h-8 */
  background-color: rgba($bg-white, 0.1);
  margin: 0 16px; /* mx-4 */
}
</style>