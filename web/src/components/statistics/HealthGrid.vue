<template>
  <view class="health-section">
    <view class="section-header">
      <text class="title">核心指标</text>
      <text class="subtitle">更新于今日</text>
    </view>
    <view class="grid-container">
       <!-- 循环渲染4个指标 -->
       <view class="grid-item" v-for="(item, i) in items" :key="i">
          <view class="item-top">
            <view class="label-box">
              <view class="dot" :style="{ backgroundColor: item.color }"></view>
              <text class="label">{{ item.label }}</text>
            </view>
            <view class="badge" :style="{ backgroundColor: getBgColor(item.color), color: item.color }">{{ item.status }}</view>
          </view>
          <view class="item-val">
             <text class="val-num">{{ item.val }}</text>
             <text v-if="item.unit" class="val-unit">{{ item.unit }}</text>
          </view>
          <view class="progress-bg">
             <view class="progress-fill" :style="{ width: item.percent, backgroundColor: item.color }"></view>
          </view>
       </view>
    </view>
  </view>
</template>

<script setup>
import { ASSET_CATEGORY_DISPLAY, ASSET_CATEGORY } from '../../configs/assets.js';

// 获取各分类颜色配置
const liquidColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LIQUID].color;
const investColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.INVEST].color;
const loanColor = ASSET_CATEGORY_DISPLAY[ASSET_CATEGORY.LOAN].color;

// 辅助函数：生成带透明度的背景色
const getBgColor = (hexColor) => {
  // 简单的hex转rgba，透明度0.1
  let c = hexColor.substring(1).split('');
  if(c.length === 3){
      c= [c[0], c[0], c[1], c[1], c[2], c[2]];
  }
  c = '0x' + c.join('');
  return 'rgba(' + [(c>>16)&255, (c>>8)&255, c&255].join(',') + ',0.1)';
}

const items = [
  { label: '结余率', status: '强', val: '45%', percent: '45%', color: '#22c55e' }, // 保持原有绿色
  { label: '安全垫', status: '足', val: '6.5', unit: '个月', percent: '65%', color: liquidColor }, // 流动资产颜色
  { label: '负债率', status: '良', val: '32%', percent: '32%', color: loanColor }, // 负债颜色
  { label: '钱生钱', status: '优', val: '60%', percent: '60%', color: investColor }, // 投资资产颜色
]
</script>

<style lang="scss" scoped>
.health-section { margin-bottom: 20px; }
.section-header { padding: 0 $spacing-base; display: flex; justify-content: space-between; margin-bottom: 10px; }
.title { font-size: 12px; font-weight: 700; color: #9ca3af; text-transform: uppercase; }
.subtitle { font-size: 10px; color: #d1d5db; }

.grid-container {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 0 $spacing-base;
}
.grid-item {
  background: white; padding: $spacing-base; border-radius: $radius-base;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02); border: 1px solid #f9fafb;
}
.item-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.label-box { display: flex; align-items: center; gap: 6px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.label { font-size: 12px; color: #6b7280; font-weight: 500; }
.badge { font-size: 12px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }

.item-val { display: flex; align-items: flex-end; gap: 4px; margin-bottom: 8px; }
.val-num { font-size: 16px; font-weight: 700; color: #2c3e50; }
.val-unit { font-size: 12px; color: #9ca3af; margin-bottom: 2px; }

.progress-bg { width: 100%; height: 6px; background: #f3f4f6; border-radius: 99px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 99px; }
</style>