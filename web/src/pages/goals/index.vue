<template>
  <view class="goals-tab-container">
    <!-- 1. 全局加载状态 -->
    <view v-if="isLoading" class="loading-box">
      <uni-load-more status="loading" />
    </view>

    <block v-else>
      <!-- 2. 空状态：显示引导组件 -->
      <GoalGuide 
        v-if="goalList.length === 0" 
        :categories="configStore.goalCategories"
        :templates="configStore.goalTemplates"
      />

      <!-- 3. 有数据：显示仪表盘组件 -->
      <GoalDashboard 
        v-else 
        :primaryGoal="primaryGoal"
        :wishlist="wishlist"
        :achievements="achievements"
        @add="onAddClick"
      />
    </block>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
// 引入组件
import GoalGuide from '@/components/goals/GoalGuide.vue';
import GoalDashboard from '@/components/goals/GoalDashboard.vue';

const configStore = useConfigStore();
const isLoading = ref(true);
const goalList = ref([]); // 这里存储后端返回的真实目标列表

// 从列表中提取“当前主线”（比如取第一个或者标记为主线的）
const primaryGoal = computed(() => goalList.value.find(g => g.isPrimary) || goalList.value[0]);
// 提取愿望清单
const wishlist = computed(() => goalList.value.filter(g => g.status !== 'COMPLETED' && !g.isPrimary));
// 提取成就馆
const achievements = computed(() => goalList.value.filter(g => g.status === 'COMPLETED'));

onShow(async () => {
  isLoading.value = true;
  try {
    // 调用后端接口获取目标列表
    // const res = await getGoalsApi();
    // goalList.value = res;
    
    // 模拟测试：如果没有目标，显示引导；如果有，显示列表
    // goalList.value = []; 
  } finally {
    isLoading.value = false;
  }
});

// 处理在 Dashboard 里的点击新增（跳转到专门的选择页）
const onAddClick = () => {
  uni.navigateTo({ url: '/pages/goals/select-goal' });
};
</script>

<style scoped>
.goals-tab-container { min-height: 100vh; background-color: #F8F7F2; }
.loading-box { padding-top: 200rpx; }
</style>