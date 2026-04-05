<template>
  <view class="page-container">
    <!-- 1. 核心净资产 (墨绿渐变大卡片) -->
    <NetWorthCard v-model:isPrivacyOn="isPrivacyOn" />

    <!-- 2. 快捷操作 (记一笔/对账/月报) -->
    <QuickActions />

    <!-- 3. 最新动态 (记账流/动态) -->
    <ActivityFeed ref="activityFeedRef" :is-privacy-on="isPrivacyOn" />
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onReachBottom } from '@dcloudio/uni-app'; 
import ActivityFeed from '@/components/home/ActivityFeed.vue';
import QuickActions from '@/components/home/QuickActions.vue';
import NetWorthCard from '@/components/home/NetWorthCard.vue';

// 全局隐私模式状态
const isPrivacyOn = ref(false);
const activityFeedRef = ref(null);

onReachBottom(() => {
  console.log('首页触底，准备加载更多...');
  if (activityFeedRef.value) {
    activityFeedRef.value.handleLoadMore();
  }
});
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: $spacing-lg;
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