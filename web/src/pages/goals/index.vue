<template>
  <view class="page-container">
    <!-- 1. 统计面板 -->
    <GoalStats :stats="summaryStats" @add="handleAddGoal" />

    <view class="main-content">
      <!-- 2. 当前主线 -->
      <GoalMain :data="primaryGoal" @click="goDetail" />

      <!-- 3. 愿望清单 -->
      <GoalList :list="goallist" @add="handleAddGoal" @clickItem="goDetail" />

      <!-- 4. 成就展馆 -->
      <GoalAchievements :list="achievements" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import GoalStats from '@/components/goals/GoalStats.vue';
import GoalMain from '@/components/goals/GoalMain.vue';
import GoalList from '@/components/goals/GoalList.vue';
import GoalAchievements from '@/components/goals/GoalAchievements.vue';

// 页面状态数据 (同你之前的定义)
const primaryGoal = ref({ });
const goallist = ref([]);
const achievements = ref([ ]);

// 格式化后的统计数据给 Stats 组件
const summaryStats = computed(() => [
  { label: '累计储备', value: '¥684,500' },
  { label: '每月定存', value: '+¥5,500', valueClass: 'text-primary' },
  { label: '平均进度', value: '24%', valueClass: 'text-gold' }
]);

const handleAddGoal = () => uni.navigateTo({ url: '/pages/goals/add' });
const goDetail = (item) => console.log('详情', item);
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #F5F7F9;
}
.main-content {
  padding: 30rpx 48rpx;
}
</style>