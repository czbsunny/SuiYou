<template>
  <view class="structure-section">
    <view class="section-header">
      <text class="title">资产结构</text>
    </view>
    <view class="section-card">
      <!-- 资产负债比 -->
      <view class="chart-block">
        <view class="chart-header">
          <text class="chart-title">资产负债比</text>
          <text class="chart-value">
            <text class="unit">权益占比</text>
            {{ isPrivacyOn ? '**%' : '72%' }} 
          </text>
        </view>
        <view class="balance-bar-container">
          <view class="balance-track">
            <view class="balance-fill" style="width: 72%"></view>
          </view>
          <view class="balance-labels">
            <text>{{ isPrivacyOn ? '****' : '总资产 ¥405w' }}</text>
            <text>{{ isPrivacyOn ? '****' : '负债 ¥120w' }}</text>
          </view>
        </view>
      </view>

      <!-- 资产分布 -->
      <view class="chart-block">
        <view class="chart-header">
          <text class="chart-title">资产分布</text>
        </view>
        <view class="stack-bar">
          <view class="stack-segment seg-liquid" style="width: 15%"></view>
          <view class="stack-segment seg-invest" style="width: 45%"></view>
          <view class="stack-segment seg-fixed" style="width: 30%"></view>
          <view class="stack-segment seg-other" style="width: 10%"></view>
        </view>
        <view class="legend-row">
          <view class="legend-item"><view class="dot seg-liquid"></view>流动 15%</view>
          <view class="legend-item"><view class="dot seg-invest"></view>投资 45%</view>
          <view class="legend-item"><view class="dot seg-fixed"></view>固定 30%</view>
          <view class="legend-item"><view class="dot seg-other"></view>其他 10%</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ASSET_CATEGORY_DISPLAY, ASSET_CATEGORY } from '../../configs/assets.js';

defineProps({ isPrivacyOn: Boolean });

// 获取各分类颜色配置
const liquidColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LIQUID].color;
const investColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.INVEST].color;
const fixedColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.FIXED].color;
const otherColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.OTHER].color;
const loanColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LOAN].color;

</script>

<style lang="scss" scoped>
.structure-section {
  margin-bottom: 24px;
}

.section-header {
  padding: 0 $spacing-base;
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.title {
  font-size: 12px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
}

.section-card {
  margin: 0 $spacing-base;
  background: #FFFFFF;
  border-radius: $radius-base;
  padding: $spacing-base;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.chart-block {
  margin-bottom: 24px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 14px;
  font-weight: 700;
  color: #2C3E50;
}

.chart-value {
  font-size: 16px;
  font-weight: 700;
  color: $primary;
  
  .unit {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 400;
    margin-right: 4px;
  }
}

.balance-track {
  height: 12px;
  background: v-bind(loanColor);
  border-radius: 6px;
  overflow: hidden;
  width: 100%;
}

.balance-fill {
  height: 100%;
  background: $primary;
  border-radius: 6px;
}

.balance-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6B7280;
  margin-top: 8px;
  font-weight: 500;
}

/* Stack Bar Colors */
.stack-bar {
  height: 12px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  width: 100%;
  margin-bottom: 16px;
}

.stack-segment {
  height: 100%;
  position: relative;
  
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #fff;
  }
}

.seg-liquid { background: v-bind(liquidColor); }
.seg-invest { background: v-bind(investColor); }
.seg-fixed { background: v-bind(fixedColor); }
.seg-other { background: v-bind(otherColor); }

.legend-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
  flex: 1;
  justify-content: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
</style>