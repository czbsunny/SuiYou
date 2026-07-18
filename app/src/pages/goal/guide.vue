<template>
  <view class="guide-container">
    <GoalGuide 
      :categories="categories"
      :templates="templates"
      @select="handleGoalSelect"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getGoalCategories, getGoalTemplates } from '@/api/modules/goal';
import GoalGuide from '@/components/goals/GoalGuide.vue';

const categories = ref([]);
const templates = ref([]);

const loadData = async () => {
  try {
    const categoryRes = await getGoalCategories();
    categories.value = categoryRes.data.categories || [];
    
    const templateRes = await getGoalTemplates();
    templates.value = templateRes.data.templates || [];
  } catch (error) {
    console.error('加载目标数据失败:', error);
  }
};

onLoad(loadData);

const handleGoalSelect = (template) => {
  const tplStr = encodeURIComponent(JSON.stringify(template));
  uni.navigateTo({
    url: `/pages/goal/add?tpl=${tplStr}`
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