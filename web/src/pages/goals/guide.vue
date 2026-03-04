<template>
  <view class="guide-container">
    <GoalGuide 
      :categories="configStore.goalCategories"
      :templates="configStore.goalTemplates"
      @select="handleGoalSelect"
    />
  </view>
</template>

<script setup>
import { useConfigStore } from '@/stores/config.js';
import GoalGuide from '@/components/goals/GoalGuide.vue';

const configStore = useConfigStore();

const handleGoalSelect = (template) => {
  uni.navigateTo({
    url: `/pages/goals/add?tpl=${template.code}`
  });
};
</script>

<style lang="scss" scoped>
.guide-container {
  min-height: 100vh;
  background-color: #F8F7F2;
}
</style>
