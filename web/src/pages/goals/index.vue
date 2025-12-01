<template>
  <view class="goal-page">
    <view class="content-view">
        <!-- 总资产概览卡片 -->
        <view class="overview-card">
            <view class="overview-header">
                 <view>
                    <view class="overview-label">
                        <image src="/static/icons/wallet.png" class="icon-sm mr-1" />
                        <text>目标总资产 (元)</text>
                    </view>
                    <text class="overview-amount">
                        ¥{{ totalGoalAssets }}
                    </text>
                 </view>
                 <view class="gain-badge">
                     <text>+0.00% 今日</text>
                 </view>
            </view>

            <view class="overview-footer">
                <view class="progress-row">
                    <text class="progress-label">总完成度</text>
                    <text class="progress-percent">{{ overallProgress.toFixed(1) }}%</text>
                </view>
                
                <view class="progress-bar-bg">
                    <view 
                        class="progress-bar-fill" 
                        :style="{ width: `${overallProgress}%` }"
                    ></view>
                </view>
                
                <view class="progress-stats">
                    <text class="stat-text">目标 ¥{{ totalGoalTarget.toLocaleString() }}</text>
                    <text class="stat-text">还需 ¥{{ (totalGoalTarget - totalGoalAssets).toLocaleString() }}</text>
                </view>
            </view>
        </view>

        <!-- 目标列表区域 -->
        <view class="goal-section">
            <view class="section-header">
                <text class="section-title">我的目标</text>
                <view class="manage-btn">
                    <text>管理</text> 
                    <image src="/static/images/chevron-right-blue.png" class="icon-xs" />
                </view>
            </view>

            <view class="goal-list">
                <view 
                    v-for="g in goals"
                    :key="g.id" 
                    @click="handleOpenDetail(g)"
                    class="goal-item"
                >
                    <view class="item-header">
                        <view class="item-left">
                            <view class="icon-circle">
                                <text class="emoji-icon">{{ g.icon || '🎯' }}</text>
                            </view>
                            <view class="item-info">
                                <text class="item-name">{{ g.name }}</text>
                                <view class="item-date">
                                    <image src="/static/images/clock-gray.png" class="icon-xs mr-1" />
                                    <text>{{ g.targetDate.substring(0, 7) }} 截止</text>
                                </view>
                            </view>
                        </view>
                        <view class="item-right">
                             <text :class="['status-badge', getPercent(g) >= 100 ? 'complete' : 'ongoing']">
                                {{ getPercent(g).toFixed(0) }}%
                            </text>
                        </view>
                    </view>
                    
                    <view class="item-progress-section">
                        <view class="amount-row">
                             <text class="label-text">已存</text>
                             <text class="amount-text">¥{{ g.currentAmount.toLocaleString() }}</text>
                        </view>
                        <view class="progress-bar-bg small">
                             <view :class="['progress-bar-fill', getPercent(g) >= 100 ? 'bg-green' : 'bg-primary']" :style="{ width: `${getPercent(g)}%` }"></view>
                        </view>
                    </view>
                </view>
            </view>
        </view>

        <!-- 理财小贴士 -->
        <view class="tips-card">
            <view class="tips-header">
                <view class="tips-title-group">
                    <image src="/static/images/sparkles.png" class="icon-md text-primary" />
                    <text class="tips-title">理财小贴士</text>
                </view>
                <view class="tips-actions">
                    <view class="action-icon hover-red"><image src="/static/images/heart.png" class="icon-md" /></view>
                    <view class="action-icon hover-gray ml-3"><image src="/static/images/thumbs-down.png" class="icon-md" /></view>
                </view>
            </view>
            
            <view>
                <text class="tips-content">
                    定期回顾您的投资组合，确保它符合您的风险承受能力和财务目标。不要因为短期波动而轻易改变长期计划。
                </text>
                <view class="tags-row">
                    <text class="tag">#长期主义</text>
                    <text class="tag ml-2">#风险控制</text>
                </view>
            </view>
        </view>
    </view>

    <!-- 添加按钮 -->
    <view @click="handleCreateGoal" class="fab-btn">
        <image src="/static/images/plus.png" class="fab-icon" />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useRouter } from 'vue-router';
import { getUserGoals, createGoal } from '../../services/goalService.js';

const router = useRouter();
const goals = ref([]);

const refresh = async () => {
    const g = await getUserGoals();
    
    // 确保goals是数组格式
    goals.value = Array.isArray(g) ? g : [];
};

onShow(refresh);

const handleOpenDetail = (goal) => {
    uni.navigateTo({
        url: '/components/goals/detail?id=' + goal.id
    });
};

const handleCreateGoal = async () => {
    uni.showModal({
        title: '新建目标',
        editable: true,
        placeholderText: '请输入目标名称',
        content: '例如：买车基金',
        success: async (res) => {
            if (res.confirm && res.content) {
                await createGoal({
                    name: res.content,
                    targetAmount: 100000,
                    currentAmount: 0,
                    riskLevel: 'MODERATE' // 使用字符串代替枚举，避免依赖
                });
                refresh();
            }
        }
    });
};

const getPercent = (g) => Math.min(100, (g.currentAmount / g.targetAmount) * 100);

const totalGoalAssets = computed(() => goals.value.reduce((sum, g) => sum + g.currentAmount, 0));
const totalGoalTarget = computed(() => goals.value.reduce((sum, g) => sum + g.targetAmount, 0));
const overallProgress = computed(() => totalGoalTarget.value > 0 ? Math.min(100, (totalGoalAssets.value / totalGoalTarget.value) * 100) : 0);
</script>

<style lang="scss" scoped>

.goal-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 128px; /* pb-32 */
  padding-top: 12px; /* pt-6 */
  position: relative;
  width: 100%;
}

.content-view {
  padding: 0 16px; /* px-4 */
  padding-bottom: 144px; /* pb-36 */
  box-sizing: border-box;
}

/* 总资产卡片 */
.overview-card {
  background-color: $bg-white;
  border-radius: 24px; /* rounded-[24px] */
  padding: 24px; /* p-6 */
  box-shadow: 0 10px 15px -3px rgba(243, 244, 246, 0.5); /* shadow-gray-100/50 */
}

.overview-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px; /* mb-4 */
}

.overview-label {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 12px; /* text-xs */
  color: $text-muted; /* text-gray-400 */
  font-weight: 500;
  margin-bottom: 4px; /* mb-1 */
}

.overview-amount {
  font-size: 36px; /* text-2xl */
  font-weight: bold;
  color: $text-main;
  font-family: monospace;
  letter-spacing: -0.05em; /* tracking-tighter */
}

.gain-badge {
  background-color: $bg-success;
  color: $primary;
  padding: 4px 8px; /* px-2 py-1 */
  border-radius: 8px;
  font-size: 12px; /* text-xs */
  font-weight: 500;
}

.overview-footer {
  border-top: 1px solid $bg-subtle;
  padding-top: 16px; /* pt-4 */
}

.progress-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px; /* mb-2 */
}

.progress-label {
  font-size: 12px; /* text-xs */
  font-weight: bold;
  color: #4b5563; /* text-gray-600 */
}

.progress-percent {
  font-size: 12px; /* text-xs */
  color: $primary;
  font-weight: bold;
  font-family: monospace;
}

.progress-bar-bg {
  height: 8px; /* h-2 */
  background-color: $bg-subtle-hover;
  border-radius: 9999px;
  overflow: hidden;
  
  &.small {
    height: 6px; /* h-1.5 */
    margin-top: 8px; /* mt-2 */
  }
}

.progress-bar-fill {
  height: 100%;
  background-color: $primary;
  border-radius: 9999px;
  transition: width 1s ease;
}
.bg-green { background-color: #22c55e; }
.bg-primary { background-color: $primary; }

.progress-stats {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-top: 4px; /* mt-1 */
}

.stat-text {
  font-size: 10px; /* text-[10px] */
  color: $text-muted;
}

/* 目标列表 */
.section-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  margin-bottom: 12px; /* mb-3 */
  padding: 0 4px; /* px-1 */
}

.section-title {
  font-weight: bold;
  color: $text-main;
  font-size: 16px; /* text-base */
}

.manage-btn {
  font-size: 12px; /* text-xs */
  color: $primary;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.goal-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* space-y-3 */
}

.goal-item {
  background-color: $bg-white;
  padding: 20px; /* p-5 */
  border-radius: 20px; /* rounded-[20px] */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05); /* shadow-sm */
  position: relative;
  overflow: hidden;
  border: 1px solid transparent;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.99);
  }
  
  /* hover effect not fully supported on mobile but good for structure */
}

.item-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px; /* mb-3 */
}

.item-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.icon-circle {
  width: 40px; /* w-10 */
  height: 40px;
  border-radius: 50%;
  background-color: rgba(42, 128, 108, 0.05); /* bg-primary/5 */
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-icon {
  font-size: 20px;
}

.item-info {
  margin-left: 12px; /* ml-3 */
}

.item-name {
  font-weight: bold;
  font-size: 16px; /* text-base */
  color: $text-main;
  display: block;
}

.item-date {
  font-size: 12px; /* text-xs */
  color: $text-muted;
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 2px; /* mt-0.5 */
}

.status-badge {
  font-size: 12px; /* text-xs */
  font-weight: bold;
  padding: 4px 8px; /* px-2 py-1 */
  border-radius: 6px; /* rounded-md */
  
  &.complete {
    background-color: $bg-loss-light;
    color: $green-600;
  }
  
  &.ongoing {
    background-color: $bg-warning;
    color: $orange-600;
  }
}

.item-progress-section {
  display: flex;
  flex-direction: column;
  gap: 8px; /* space-y-2 */
}

.amount-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
}

.label-text {
  font-size: 14px; /* text-sm */
  color: $text-regular;
}

.amount-text {
  font-size: 16px; /* text-base */
  font-weight: bold;
  color: $text-main;
  font-family: monospace;
}

/* 理财小贴士 */
.tips-card {
  background-color: $bg-white;
  padding: 20px; /* p-5 */
  border-radius: 20px; /* rounded-[20px] */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border-left: 4px solid $primary;
  margin-top: 32px; /* mt-8 */
  margin-bottom: 64px; /* mb-16 */
}

.tips-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px; /* mb-3 */
}

.tips-title-group {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.tips-title {
  font-weight: bold;
  color: $text-main;
  font-size: 14px; /* text-sm */
  margin-left: 8px; /* ml-2 */
}

.tips-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  color: $text-muted;
}

.tips-content {
  font-size: 12px; /* text-xs */
  color: #4b5563; /* text-gray-600 */
  line-height: 1.625; /* leading-relaxed */
  margin-bottom: 8px; /* mb-2 */
  display: block;
}

.tags-row {
  display: flex;
  flex-direction: row;
}

.tag {
  background-color: rgba(42, 128, 108, 0.1); /* bg-primary/10 */
  color: $primary;
  font-size: 10px; /* text-[10px] */
  padding: 2px 8px; /* px-2 py-0.5 */
  border-radius: 4px;
}

.ml-1 { margin-left: 4px; }
.ml-2 { margin-left: 8px; }
.ml-3 { margin-left: 12px; }
.mr-1 { margin-right: 4px; }

.icon-sm { width: 12px; height: 12px; }
.icon-xs { width: 12px; height: 12px; }
.icon-md { width: 16px; height: 16px; }

/* 悬浮按钮 */
.fab-btn {
  position: fixed;
  bottom: 128px; /* bottom-32 */
  right: 24px; /* right-6 */
  width: 56px; /* w-14 */
  height: 56px;
  background-color: $primary;
  border-radius: 50%;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.4); /* shadow-primary/40 */
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 50;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.90);
  }
}

.fab-icon {
  width: 28px;
  height: 28px;
}

/* 交互类 */
.hover-red:active { color: #ef4444; }
.hover-gray:active { color: #4b5563; }
</style>