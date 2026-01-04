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
        @clickItem="onItemClick"
      />
    </block>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
// 引入组件
import GoalGuide from '@/components/goals/GoalGuide.vue';
import GoalDashboard from '@/components/goals/GoalDashboard.vue';
import { goalService } from '@/services/goalService.js';


const configStore = useConfigStore();
const isLoading = ref(true);
const goalList = ref([]); // 这里存储后端返回的真实目标列表

// 从列表中提取“当前主线”（比如取第一个或者标记为主线的）
const primaryGoal = computed(() => goalList.value.filter(g => g.isPrimary)[0]);
// 提取愿望清单
const wishlist = computed(() => goalList.value.filter(g => g.status !== 'COMPLETED' && !g.isPrimary));
// 提取成就馆
const achievements = computed(() => goalList.value.filter(g => g.status === 'COMPLETED'));

const fetchGoalListData = async () => {
  isLoading.value = true;
  try {
    const res = await goalService.fetchUserGoals();
    goalList.value = res.goals || []; // 这一步会让 index.vue 里的 v-if 判断生效
  } finally {
    isLoading.value = false;
  }
};

onLoad(() => {
  uni.$on('refreshGoalList', () => {
    fetchGoalListData();
  });
});

onShow(() => {
  fetchGoalListData();
});

const onAddClick = () => {
  uni.navigateTo({ url: '/pages/goals/add' });
};

const onItemClick = (item) => {
  uni.navigateTo({
    url: `/pages/goals/detail?id=${item.id}`
  });
};

</script>

<style scoped>
.goals-tab-container { min-height: 100vh; background-color: #F8F7F2; }
.loading-box { padding-top: 200rpx; }
</style>