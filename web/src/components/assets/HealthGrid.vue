<template>
  <view class="health-section">
    <view class="section-header">
      <text class="title">财务体检</text>
      <text class="subtitle">更新于今日</text>
    </view>
    <view class="grid-container">
       <!-- 循环渲染4个指标 -->
       <view class="grid-item" v-for="(item, i) in items" :key="i">
          <view class="item-top">
            <view class="label-box">
              <view class="dot" :class="item.colorClass"></view>
              <text class="label">{{ item.label }}</text>
            </view>
            <view class="badge" :class="`bg-${item.color}-50 text-${item.color}-600`">{{ item.status }}</view>
          </view>
          <view class="item-val">
             <text class="val-num">{{ item.val }}</text>
             <text v-if="item.unit" class="val-unit">{{ item.unit }}</text>
          </view>
          <view class="progress-bg">
             <view class="progress-fill" :class="`bg-${item.color}-500`" :style="{width: item.percent}"></view>
          </view>
       </view>
    </view>
  </view>
</template>
<script setup>
const items = [
  { label: '结余率', status: '强', val: '45%', percent: '45%', color: 'green', colorClass: 'bg-green-500' },
  { label: '安全垫', status: '足', val: '6.5', unit: '个月', percent: '65%', color: 'yellow', colorClass: 'bg-yellow-500' },
  { label: '负债率', status: '良', val: '32%', percent: '32%', color: 'blue', colorClass: 'bg-blue-500' },
  { label: '钱生钱', status: '优', val: '60%', percent: '60%', color: 'purple', colorClass: 'bg-purple-500' },
]
</script>
<style lang="scss" scoped>
/* 此处补充 CSS Grid 布局和卡片样式，参考原代码转译 */
.health-section { margin-bottom: 20px; }
.section-header { padding: 0 24px; display: flex; justify-content: space-between; margin-bottom: 10px; }
.title { font-size: 12px; font-weight: 700; color: #9ca3af; text-transform: uppercase; }
.subtitle { font-size: 10px; color: #d1d5db; }

.grid-container {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 0 16px;
}
.grid-item {
  background: white; padding: 14px; border-radius: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02); border: 1px solid #f9fafb;
}
.item-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.label-box { display: flex; align-items: center; gap: 6px; }
.dot { width: 6px; height: 6px; border-radius: 50%; }
.label { font-size: 10px; color: #6b7280; font-weight: 500; }
.badge { font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }

/* 颜色工具类模拟 (SCSS) */
.bg-green-500 { background-color: #22c55e; }
.bg-green-50 { background-color: #f0fdf4; }
.text-green-600 { color: #16a34a; }
/* ... 其他颜色类似 ... */

.item-val { display: flex; align-items: flex-end; gap: 4px; margin-bottom: 8px; }
.val-num { font-size: 20px; font-weight: 700; color: #2c3e50; }
.val-unit { font-size: 10px; color: #9ca3af; margin-bottom: 4px; }

.progress-bg { width: 100%; height: 6px; background: #f3f4f6; border-radius: 99px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 99px; }
</style>