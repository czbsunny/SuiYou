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
.page-container {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-xl;
  box-sizing: border-box;

  animation: pageSlideUp 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes pageSlideUp {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.safe-area-bottom {
  height: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  width: 100%;
}

</style>