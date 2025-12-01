<template>
  <view class="goal-page">
    <!-- 内容容器：普通块级元素，由页面自然滚动 -->
    <view class="content-container">
        
        <!-- 1. 顶部核心卡片：资产概览与进度 -->
        <view class="core-card">
            <view class="header-section">
                <view>
                    <text class="label-text">当前累计 (元)</text>
                    <text class="amount-large">¥{{ totalAssets.toLocaleString() }}</text>
                </view>
                <view class="header-right">
                    <text class="label-text">目标金额</text>
                    <text class="amount-small">¥{{ goal.targetAmount.toLocaleString() }}</text>
                </view>
            </view>

            <!-- 进度条区域 -->
            <view class="progress-section">
                <view class="progress-header">
                    <text class="progress-title">进度对比</text>
                    <text class="deadline-text">{{ goal.targetDate.substring(0, 7) }} 截止</text>
                </view>
                
                <view class="progress-bar-bg">
                    <!-- 时间进度 (绿色) -->
                    <view 
                         class="progress-bar-time"
                         :style="{ width: `${timePercent}%` }"
                    ></view>
                    
                    <!-- 金额进度 (主色) -->
                    <view 
                         class="progress-bar-amount"
                         :style="{ width: `${amountPercent}%` }"
                    ></view>
                </view>
                
                <view class="legend-row">
                    <view class="legend-group">
                        <view class="legend-item">
                            <view class="dot dot-amount"></view>
                            <text class="legend-text">金额进度 {{ amountPercent.toFixed(1) }}%</text>
                        </view>
                        <view class="legend-item">
                            <view class="dot dot-time"></view>
                            <text class="legend-text">时间进度 {{ timePercent.toFixed(1) }}%</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>

        <!-- 2. 统计小卡片网格 -->
        <view class="stats-grid">
            <view class="stat-card">
                <view class="stat-header text-primary">
                     <image src="/static/images/target.png" class="stat-icon" />
                     <text class="stat-title">剩余金额</text>
                </view>
                <text class="stat-value">¥{{ remainingAmount.toLocaleString() }}</text>
            </view>
            
            <view class="stat-card">
                <view class="stat-header text-orange">
                     <image src="/static/images/clock.png" class="stat-icon" />
                     <text class="stat-title">剩余时间</text>
                </view>
                <text class="stat-value">
                    {{ Math.ceil((end - now) / (1000 * 60 * 60 * 24)) }} <text class="unit-text">天</text>
                </text>
            </view>
        </view>

        <!-- 3. 关联组合卡片 -->
        <view class="portfolio-card">
            <view class="card-header" @click="handleLinkPortfolio">
                <view class="card-title-group">
                    <image src="/static/images/layers.png" class="title-icon text-primary" /> 
                    <text class="title-text">关联组合</text>
                </view>
                <view class="card-action">
                    <text>管理</text> 
                    <image src="/static/images/chevron-right.png" class="arrow-icon" />
                </view>
            </view>
            
            <view class="portfolio-list">
                <view v-if="!portfolios || portfolios.length === 0" class="empty-state">
                     <text class="empty-text">暂无关联资产</text>
                     <text class="link-text" @click="handleLinkPortfolio">立即关联</text>
                </view>
                <view v-else>
                    <view v-for="p in portfolios" :key="p.id" class="portfolio-item">
                        <view class="item-left">
                             <view class="item-icon-box">
                                 <image src="/static/images/pie-chart.png" class="item-icon" />
                             </view>
                             <view>
                                 <text class="item-name">{{ p.name }}</text>
                                 <text class="item-desc">默认账户</text>
                             </view>
                        </view>
                        <view class="item-right">
                            <text class="item-amount">¥{{ p.totalValue.toLocaleString() }}</text>
                            <text class="item-contribution">贡献 {{ totalAssets > 0 ? ((p.totalValue / totalAssets) * 100).toFixed(0) : 0 }}%</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>

        <!-- 4. 计算器入口按钮 -->
        <view @click="handleOpenCalculator" class="calc-entry-card">
            <view class="calc-left">
                <view class="calc-icon-box">
                    <image src="/static/images/calculator.png" class="calc-icon" />
                </view>
                <view>
                    <text class="calc-title">定投测算</text>
                    <text class="calc-desc">计算如何达成目标</text>
                </view>
            </view>
            <image src="/static/images/chevron-right.png" class="calc-arrow" />
        </view>
        
        <!-- 5. 底部操作区 (跟随内容流，不固定) -->
        <view class="footer-actions">
            <button class="edit-btn" @click="handleEditGoal">
                编辑目标设置
            </button>
        </view>

    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

// 接收父组件传来的真实数据，不再使用假数据
const props = defineProps({
    goal: {
        type: Object,
        required: true,
        default: () => ({
            targetAmount: 0,
            targetDate: '2099-12-31',
            startTime: new Date().toISOString(),
            deadline: new Date().toISOString()
        })
    },
    portfolios: {
        type: Array,
        default: () => []
    }
});

// 核心计算逻辑
const totalAssets = computed(() => {
    if (!props.portfolios) return 0;
    return props.portfolios.reduce((sum, p) => sum + (p.totalValue || 0), 0);
});

const amountPercent = computed(() => {
    if (!props.goal.targetAmount) return 0;
    return Math.min(100, (totalAssets.value / props.goal.targetAmount) * 100);
});

const start = computed(() => new Date(props.goal.startTime).getTime());
const end = computed(() => new Date(props.goal.deadline).getTime());
const now = new Date().getTime();

const timePercent = computed(() => {
    const totalTime = end.value - start.value;
    const elapsedTime = now - start.value;
    return totalTime > 0 ? Math.min(100, Math.max(0, (elapsedTime / totalTime) * 100)) : 0;
});

const remainingAmount = computed(() => Math.max(0, props.goal.targetAmount - totalAssets.value));

// 页面跳转逻辑 (使用 UniApp 标准 API)
const handleOpenCalculator = () => {
  uni.navigateTo({
    url: `/components/goals/calculator?goalId=${props.goal.id}`
  });
};

const handleLinkPortfolio = () => {
  uni.navigateTo({
    url: `/components/goals/linkPortfolio?goalId=${props.goal.id}`
  });
};

const handleEditGoal = () => {
  uni.navigateTo({
    url: `/components/goals/edit?goalId=${props.goal.id}`
  });
};
</script>

<style lang="scss" scoped>
/* 页面根容器：min-height 100vh，由内容撑开 */
.goal-page {
  min-height: 100vh;
  background-color: $bg-page;
  width: 100%;
  /* 底部留白，适配 iPhone 底部安全区 */
  padding-bottom: calc(40px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 内容容器 */
.content-container {
  width: 100%;
  padding: 16px; 
  box-sizing: border-box;
}

/* --- 核心卡片样式 --- */
.core-card {
  background-color: $bg-white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 10px 15px -3px rgba(243, 244, 246, 0.5);
}

.header-section {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.header-right {
  text-align: right;
}

.label-text {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 4px;
  display: block;
}

.amount-large {
  font-size: 36px;
  font-weight: bold;
  color: $text-main;
  font-family: monospace;
  letter-spacing: -0.05em;
  display: block;
  line-height: 1;
}

.amount-small {
  font-size: 14px;
  font-weight: bold;
  color: $text-regular;
  font-family: monospace;
  display: block;
}

/* 进度条样式 */
.progress-section {
  margin-top: 32px;
  margin-bottom: 8px;
}

.progress-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
}

.progress-title {
  color: $text-regular;
}

.deadline-text {
  color: $text-muted;
  font-family: monospace;
}

.progress-bar-bg {
  position: relative;
  height: 16px;
  background-color: $bg-subtle-hover; /* 或 $bg-gray-100 */
  border-radius: 9999px;
  overflow: hidden;
}

.progress-bar-time {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: $bg-progress; /* 建议在 uni.scss 定义浅绿色 */
  transition: width 0.5s ease;
}

.progress-bar-amount {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: $color-gain;
  opacity: 0.9;
  transition: width 1s ease;
  box-shadow: 2px 0 5px rgba(0,0,0,0.1);
}

.legend-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.legend-group {
  display: flex;
  flex-direction: row;
  gap: 16px;
  font-size: 10px;
}

.legend-item {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot-amount { background-color: $color-gain; }
.dot-time { background-color: $bg-progress; }

.legend-text { color: $text-regular; }

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
}

.stat-card {
  background-color: $bg-white;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 96px;
}

.stat-header {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.text-primary { color: $primary; }
.text-orange { color: $text-warning; } /* 建议确保变量存在 */

.stat-icon { width: 16px; height: 16px; margin-right: 6px; }

.stat-title {
  font-size: 12px;
  font-weight: bold;
}

.stat-value {
  font-family: monospace;
  font-weight: bold;
  font-size: 18px;
  color: $text-main;
  display: block;
  margin-top: 8px;
}

.unit-text {
  font-size: 12px;
  color: $text-muted;
  font-family: sans-serif;
  font-weight: normal;
}

/* 关联组合卡片 */
.portfolio-card {
  background-color: $bg-white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  margin-top: 16px;
}

.card-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title-group {
  font-weight: bold;
  color: $text-main;
  font-size: 14px;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.title-icon { width: 16px; height: 16px; margin-right: 8px; }
.title-text { margin-left: 4px; }

.card-action {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 12px;
  color: $text-muted;
}

.arrow-icon { width: 14px; height: 14px; }

.portfolio-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 24px 0;
  border: 1px dashed $bg-subtle-hover;
  border-radius: 12px;
  background-color: $bg-subtle;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-text {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 8px;
}

.link-text {
  font-size: 12px;
  color: $primary;
  font-weight: bold;
}

.portfolio-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  border-radius: 8px;
  margin: 0 -8px;
  transition: background-color 0.2s;

  &:active {
    background-color: $bg-subtle;
  }
}

.item-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.item-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background-color: $border-light; /* 或具体颜色 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.item-icon { width: 16px; height: 16px; }

.item-name {
  font-size: 14px;
  font-weight: bold;
  color: $text-sub;
  display: block;
}

.item-desc {
  font-size: 10px;
  color: $text-muted;
  display: block;
}

.item-right { text-align: right; }

.item-amount {
  font-size: 14px;
  font-family: monospace;
  font-weight: bold;
  color: $text-main;
  display: block;
}

.item-contribution {
  font-size: 10px;
  color: $primary;
  font-weight: 500;
  display: block;
}

/* 测算入口卡片 */
.calc-entry-card {
  width: 100%;
  background-color: $bg-white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  border: 1px solid transparent;
  margin-top: 16px;
  transition: transform 0.1s;
  box-sizing: border-box;

  &:active {
    transform: scale(0.98);
    border-color: $primary-alpha-30;
  }
}

.calc-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.calc-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, $primary, $primary-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  box-shadow: 0 4px 6px -1px rgba(42, 128, 108, 0.2);
}

.calc-icon { width: 20px; height: 20px; }

.calc-title {
  font-weight: bold;
  color: $text-main;
  font-size: 14px;
  display: block;
}

.calc-desc {
  font-size: 12px;
  color: $text-muted;
  margin-top: 2px;
  display: block;
}

.calc-arrow { width: 18px; height: 18px; opacity: 0.3; }

/* 底部操作区 */
.footer-actions {
  margin-top: 32px;
  margin-bottom: 16px;
}

.edit-btn {
  width: 100%;
  padding: 14px 0;
  background-color: $bg-white;
  border: 1px solid #e5e7eb;
  color: $text-regular;
  font-weight: bold;
  border-radius: 12px;
  transition: background-color 0.2s;

  &:active {
    background-color: $bg-subtle;
  }
}
</style>