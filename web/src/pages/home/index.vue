<template>
  <view class="page-container">
    <!-- 1. 核心净资产 (墨绿渐变大卡片) -->
    <NetWorthCard v-model:isPrivacyOn="isPrivacyOn" />

    <!-- 2. 快捷操作 (记一笔/对账/月报) -->
    <QuickActions />

    <!-- 3. 最新动态 (记账流/动态) -->
    <ActivityFeed :is-privacy-on="isPrivacyOn" />
    
    <!-- 底部垫片：确保内容不被 TabBar 遮挡 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import ActivityFeed from '@/components/home/ActivityFeed.vue';
import QuickActions from '@/components/home/QuickActions.vue';
import NetWorthCard from '@/components/home/NetWorthCard.vue';

// 全局隐私模式状态
const isPrivacyOn = ref(false);
</script>

<style lang="scss" scoped>
/* 
 * 首页样式重构
 * 遵循"温润如玉"原则：暖底、浮动卡片、墨绿点缀
 */

.page-container {
  min-height: 100vh;
  /* 应用暖米白背景 #FAF9F6 */
  background-color: $bg-page;
  /* 使用步进间距系统 */
  padding-bottom: $spacing-xl;
  box-sizing: border-box;
  
  /* 入场动画：轻微上浮感 */
  animation: pageSlideUp 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes pageSlideUp {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* 适配微信小程序/移动端全面屏底部安全区 */
.safe-area-bottom {
  height: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  width: 100%;
}

</style>