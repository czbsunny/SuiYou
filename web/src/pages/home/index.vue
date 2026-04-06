<template>
  <view class="page-container">
    <!-- 1. 核心净资产 (墨绿渐变大卡片) -->
    <NetWorthCard ref="netWorthRef" v-model:isPrivacyOn="isPrivacyOn" />

    <!-- 2. 快捷操作 (记一笔/对账/月报) -->
    <!-- <QuickActions /> -->
    <RecordActions />
    
    <!-- 3. 最新动态 (记账流/动态) -->
    <ActivityFeed ref="activityFeedRef" :is-privacy-on="isPrivacyOn" />
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onReachBottom, onPullDownRefresh  } from '@dcloudio/uni-app'; 
import ActivityFeed from '@/components/home/ActivityFeed.vue';
import QuickActions from '@/components/home/QuickActions.vue';
import NetWorthCard from '@/components/home/NetWorthCard.vue';
import RecordActions from '@/components/home/RecordActions.vue';

// 全局隐私模式状态
const isPrivacyOn = ref(false);
const activityFeedRef = ref(null);
const netWorthRef = ref(null);

const isRefreshing = ref(false);

onReachBottom(() => {
  console.log('首页触底，准备加载更多...');
  if (activityFeedRef.value) {
    activityFeedRef.value.handleLoadMore();
  }
});

onPullDownRefresh(async () => {
  console.log('开始刷新...');
  try {
    const p1 = netWorthRef.value?.fetchNetWorth();
    const p2 = activityFeedRef.value?.fetchTransactions(false);
    
    await Promise.all([p1, p2]);
  } catch (err) {
    console.error('刷新失败', err);
  } finally {
    uni.stopPullDownRefresh();
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