<template>
  <uni-popup ref="popup" type="bottom" border-radius="20px 20px 0 0">
    <view class="template-picker">
      <view class="picker-header">
        <text class="title">选择目标类型</text>
        <text class="subtitle">通过模板快速规划财务方案</text>
      </view>
      
      <view class="template-list">
        <view class="t-item" v-for="t in templates" :key="t.id" @click="select(t)">
          <view class="t-icon" :style="{ background: t.bg }">{{ t.icon }}</view>
          <view class="t-info">
            <text class="t-name">{{ t.name }}</text>
            <text class="t-desc">{{ t.desc }}</text>
          </view>
          <uni-icons type="chevron-right" size="16" color="#ccc"></uni-icons>
        </view>
      </view>
      
      <view class="custom-entry" @click="select(null)">
        <text>直接创建自定义目标</text>
      </view>
    </view>
  </uni-popup>
</template>

<script setup>
import { ref } from 'vue';
const popup = ref(null);
const templates = [
  { id: 1, name: '短期·应急金', desc: '预留6个月生活费，防范意外风险', icon: '🛡️', bg: '#e3f2fd', target: 20000 },
  { id: 2, name: '中期·心愿单', desc: '旅行、数码产品或大件购置计划', icon: '🎒', bg: '#e8f5e9', target: 15000 },
  { id: 3, name: '长期·财富池', desc: '教育、养老或核心资产积累', icon: '📈', bg: '#fff8e1', target: 100000 }
];

const open = () => popup.value.open();
const select = (t) => {
  popup.value.close();
  emit('confirm', t);
};
const emit = defineEmits(['confirm']);
defineExpose({ open });
</script>

<style lang="scss" scoped>
.template-picker {
  background: #fff; padding: 40rpx; padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  .picker-header { margin-bottom: 40rpx;
    .title { font-size: 32rpx; font-weight: bold; color: #2c3e50; display: block; }
    .subtitle { font-size: 22rpx; color: #95a5a6; margin-top: 4rpx; }
  }
}
.t-item {
  display: flex; align-items: center; padding: 30rpx; background: #f8fafc;
  border-radius: 24rpx; margin-bottom: 20rpx;
  .t-icon { width: 80rpx; height: 80rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; font-size: 40rpx; margin-right: 24rpx; }
  .t-info { flex: 1;
    .t-name { font-size: 28rpx; font-weight: bold; color: #2c3e50; display: block; }
    .t-desc { font-size: 22rpx; color: #7f8c8d; }
  }
}
.custom-entry { text-align: center; padding: 20rpx; font-size: 26rpx; color: #2a806c; margin-top: 10rpx; }
</style>