<template>
  <view class="guide-container">
    <GoalGuide 
      :categories="goalStore.categories"
      :templates="goalStore.templates"
      @select="handleGoalSelect"
    />
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app';
import { useGoalStore } from '@/stores/goal.js';
import GoalGuide from '@/components/goals/GoalGuide.vue';

const goalStore = useGoalStore();

onLoad(async () => {
  await goalStore.fetchCategories();
  await goalStore.fetchTemplates();
});

const handleGoalSelect = (template) => {
  uni.navigateTo({
    url: `/pages/goal/add?tpl=${template.code || template.templateCode}`
  });
};
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.guide-container {
  min-height: 100vh;
  background-color: $background;
}
</style>