<template>
  <view class="page-container">
    <!-- 全局加载 -->
    <view v-if="isLoading" class="loading-box">
      <uni-load-more status="loading" :color="primaryColor" />
    </view>

    <block v-else>
      <!-- 空状态引导 -->
      <GoalGuide 
        v-if="goalList.length === 0" 
        :categories="configStore.goalCategories"
        :templates="configStore.goalTemplates"
        @select="handleGoalSelect"
      />

      <!-- 目标内容区 -->
      <block v-else>
        <!-- 1. 年度看板：压缩体积，去除浮夸图标 -->
        <GoalHeader 
          :year="goalSummary.year"
          :remain-amount="goalSummary.remainAmount"
          :total-amount="goalSummary.totalAmount"
          :active-count="goalSummary.activeCount"
          :pending-count="goalSummary.pendingCount"
          :status-text="goalSummary.statusText"
        />

        <view class="main-content animate-fade-in">
          <!-- 2. 进行中目标 -->
          <GoalActiveCard 
            :data="activeGoals" 
            @click="onItemClick"
            @sort="onSortClick"
            @add="onAddClick"
          />

          <!-- 3. 已实现 (仅在有数据时显示，且大幅度淡化) -->
          <view class="section" v-if="completedGoals.length > 0">
            <view class="section-header">
              <text class="section-title dimmed">已实现</text>
            </view>
            <view class="card-list">
              <!-- 已实现 -->
              <GoalCompletedItem 
                :data="completedGoals" 
                @click="onItemClick"
              />
            </view>
          </view>
        </view>
      </block>
    </block>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { goalService } from '@/services/goalService.js';

// 组件保持引用，但需要确保内部样式已按变量重构
import GoalGuide from '@/components/goals/GoalGuide.vue';
import GoalHeader from '@/components/goals/GoalHeader.vue';
import GoalActiveCard from '@/components/goals/GoalActiveCard.vue';
import GoalCompletedItem from '@/components/goals/GoalCompletedItem.vue';

const primaryColor = '#2a806c';
const configStore = useConfigStore();
const isLoading = ref(true);
const goalList = ref([]);

// 简化的逻辑计算...
const goalSummary = computed(() => {
  // ... 计算逻辑保持
  return {
    year: new Date().getFullYear(),
    remainAmount: 12000, // 示例数据
    totalAmount: 50000,
    activeCount: activeGoals.value.length,
    statusText: '稳步积累中'
  };
});

const activeGoals = computed(() => goalList.value.filter(g => g.status === 'ON_GOING'));
const completedGoals = computed(() => goalList.value.filter(g => g.status === 'COMPLETED'));

const fetchGoalListData = async () => {
  isLoading.value = true;
  try {
    const res = await goalService.fetchUserGoals();
    goalList.value = res.goals || [];
  } finally {
    isLoading.value = false;
  }
};

onShow(fetchGoalListData);
const onAddClick = () => uni.navigateTo({ url: '/pages/goals/guide' });
const onSortClick = () => {}; 
const onItemClick = (item) => uni.navigateTo({ url: `/pages/goals/detail?id=${item.id}` });

const handleGoalSelect = (template) => {
  uni.navigateTo({
    url: `/pages/goals/add?tpl=${template.code}`
  });
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $bg-page; // #FAF9F6 暖米白
}

.main-content {
  padding: 24rpx 32rpx 80rpx; // 压缩顶部间距
}

.section {
  margin-bottom: 40rpx; // 压缩区域间距
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx; // 压缩标题与卡片距离
}

.section-title {
  font-size: 30rpx; // 缩小标题
  font-weight: 600;
  color: $text-main; // 深咖啡黑
  letter-spacing: 1rpx;

  &.dimmed {
    font-size: 26rpx;
    color: $text-muted;
    font-weight: 400;
  }
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

/* 极简图标按钮 */
.icon-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  @include flex-center;
  background-color: $bg-white;
  color: $text-sub;
  border: 1rpx solid $border-color;
  transition: all 0.2s;

  .iconfont { font-size: 28rpx; }
}

.btn-hover {
  opacity: 0.8;
  transform: scale(0.94);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx; // 紧凑的卡片间距
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>