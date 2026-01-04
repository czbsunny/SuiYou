<template>
  <view class="page-container">
    <!-- 1. 统计面板：数据可以根据 props 动态计算，或者直接接收 -->
    <GoalStats :stats="summaryStats" @add="handleAddGoal" />

    <view class="main-content">
      <!-- 2. 当前主线：传入从 props 接收到的 primaryGoal -->
      <GoalMain :data="primaryGoal" @click="goDetail" />

      <!-- 3. 愿望清单：传入从 props 接收到的 wishlist -->
      <GoalList :list="wishlist" @add="handleAddGoal" @clickItem="goDetail" />

      <!-- 4. 成就展馆：传入从 props 接收到的 achievements -->
      <GoalAchievements :list="achievements" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GoalStats from '@/components/goals/GoalStats.vue';
import GoalMain from '@/components/goals/GoalMain.vue';
import GoalList from '@/components/goals/GoalList.vue';
import GoalAchievements from '@/components/goals/GoalAchievements.vue';

// 🟢 关键修改：使用 defineProps 接收父组件的数据
const props = defineProps({
  primaryGoal: {
    type: Object,
    default: () => null
  },
  wishlist: {
    type: Array,
    default: () => []
  },
  achievements: {
    type: Array,
    default: () => []
  }
});

// 定义向外发出的事件
const emit = defineEmits(['add', 'clickItem']);

// 🟢 逻辑建议：这里的统计数据最好是根据 props 实时计算
const summaryStats = computed(() => {
  return [
    { label: '累计储备', value: '¥684,500' },
    { label: '每月定存', value: '+¥5,500', valueClass: 'text-primary' },
    { label: '平均进度', value: '24%', valueClass: 'text-gold' }
  ];
});

const handleAddGoal = () => {
  emit('add');
};

const goDetail = (item: any) => {
  emit('clickItem', item);
};
</script>

<style lang="scss" scoped>
/* 颜色变量 */
.text-primary { color: #2a806c; }
.text-gold { color: #D4AF37; }

.page-container {
  min-height: 100vh;
  background-color: #F8F7F2; // 建议统一使用之前的米色背景
}
.main-content {
  padding: 30rpx 48rpx 100rpx;
}
</style>