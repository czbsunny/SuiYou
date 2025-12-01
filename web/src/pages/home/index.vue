<template>
  <view class="home-page">
    <!-- 顶部背景装饰 -->
    <view class="header-decoration">
        <view class="blob-white"></view>
        <view class="blob-black"></view>
    </view>

    <!-- 欢迎区域 -->
    <view class="welcome-section">
      <view class="welcome-content">
        <text class="welcome-greeting">你好，欢迎回来</text>
        <text class="welcome-subtitle">让我们一起管理好家庭财务</text>
      </view>
    </view>

    <!-- 财务总览卡片 -->
    <view class="overview-section">
      <view class="financial-overview-card">
        <!-- 背景装饰 -->
        <view class="bg-decoration-1"></view>
        <view class="bg-decoration-2"></view>

        <!-- 总资产信息 -->
        <view class="total-assets-box">
          <view class="label-row">
            <text class="label-text">家庭总资产 (元)</text>
            <image src="/static/images/eye.png" class="eye-icon" @click="toggleBalanceVisibility" />
          </view>
          <text class="amount-text">
            {{ showBalance ? formatCurrency(financialSummary.totalAssets) : '***,***' }}
          </text>
        </view>

        <!-- 收益信息 -->
        <view class="income-row">
          <view class="income-item">
            <text class="income-label">今日收益</text>
            <text :class="['income-val', financialSummary.dailyReturn >= 0 ? 'text-gain-light' : 'text-loss-light']">
              {{ financialSummary.dailyReturn > 0 ? '+' : '' }}{{ showBalance ? formatCurrency(financialSummary.dailyReturn) : '***' }}
            </text>
          </view>
          <view class="income-item border-left">
            <text class="income-label">本月收益</text>
            <text :class="['income-val', financialSummary.monthlyReturn >= 0 ? 'text-gain-light' : 'text-loss-light']">
              {{ financialSummary.monthlyReturn > 0 ? '+' : '' }}{{ showBalance ? formatCurrency(financialSummary.monthlyReturn) : '***' }}
            </text>
          </view>
          <view class="income-item border-left">
            <text class="income-label">累计收益</text>
            <text :class="['income-val', financialSummary.totalReturn >= 0 ? 'text-gain-light' : 'text-loss-light']">
              {{ financialSummary.totalReturn > 0 ? '+' : '' }}{{ showBalance ? formatCurrency(financialSummary.totalReturn) : '***' }}
            </text>
          </view>
        </view>

        <!-- 资产配置 -->
        <view class="allocation-section">
          <view class="allocation-header">
            <text class="allocation-title">资产配置</text>
            <view class="view-detail" @click="navigateToPortfolio">
              <text>查看详情</text>
              <image src="/static/images/chevron-right.png" class="arrow-icon" />
            </view>
          </view>
          
          <view class="allocation-chart">
            <view class="chart-item" v-for="(item, index) in assetAllocation" :key="index">
              <view class="chart-dot" :style="{ backgroundColor: item.color }"></view>
              <text class="chart-label">{{ item.label }}</text>
              <text class="chart-value">{{ showBalance ? item.value + '%' : '**%' }}</text>
            </view>
          </view>
          
          <view class="allocation-bar">
            <view 
              v-for="(item, index) in assetAllocation" 
              :key="index"
              class="allocation-segment"
              :style="{ backgroundColor: item.color, width: item.value + '%' }"
            ></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 目标总览区域 -->
    <view class="goals-section">
      <view class="section-header">
        <text class="section-title">财务目标总览</text>
        <view class="view-all" @click="navigateToGoals">
          <text>查看全部</text>
          <image src="/static/images/chevron-right-blue.png" class="arrow-icon" />
        </view>
      </view>

      <!-- 目标总进度卡片 -->
      <view class="goal-overview-card">
        <view class="goal-summary">
          <text class="goal-label">总完成度</text>
          <text class="goal-percent">{{ goalsSummary.overallProgress.toFixed(1) }}%</text>
        </view>
        
        <view class="goal-progress-bar-bg">
          <view 
            class="goal-progress-bar-fill" 
            :style="{ width: `${goalsSummary.overallProgress}%` }"
          ></view>
        </view>
        
        <view class="goal-stats">
          <text class="stat-text">目标总数：{{ goalsSummary.totalGoals }}</text>
          <text class="stat-text">已完成：{{ goalsSummary.completedGoals }}</text>
          <text class="stat-text">进行中：{{ goalsSummary.activeGoals }}</text>
        </view>
      </view>

      <!-- 目标列表 -->
      <view class="goals-list">
        <view 
          v-for="goal in recentGoals" 
          :key="goal.id"
          class="goal-item"
          @click="navigateToGoalDetail(goal.id)"
        >
          <view class="goal-item-left">
            <view class="goal-icon-circle">
              <text class="goal-emoji">{{ goal.icon || '🎯' }}</text>
            </view>
            <view class="goal-item-info">
              <text class="goal-item-name">{{ goal.name }}</text>
              <text class="goal-item-target">目标：{{ showBalance ? formatCurrency(goal.targetAmount) : '***' }}</text>
            </view>
          </view>
          <view class="goal-item-right">
            <text class="goal-item-progress">{{ (goal.currentAmount / goal.targetAmount * 100).toFixed(1) }}%</text>
            <view class="goal-item-progress-bar-bg">
              <view 
                class="goal-item-progress-bar-fill" 
                :style="{ width: `${Math.min(100, goal.currentAmount / goal.targetAmount * 100)}%` }"
              ></view>
            </view>
          </view>
        </view>

        <!-- 添加目标按钮 -->
        <view class="add-goal-btn" @click="navigateToCreateGoal">
          <image src="/static/images/plus.png" class="add-icon" />
          <text>添加新目标</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作区域 -->
    <view class="quick-actions-section">
      <view class="section-header">
        <text class="section-title">快捷操作</text>
      </view>

      <view class="quick-actions-grid">
        <view class="action-item" @click="navigateToAddAsset">
          <view class="action-icon-box bg-blue">
            <image src="/static/images/plus-circle.png" class="action-icon" />
          </view>
          <text class="action-label">添加资产</text>
        </view>
        <view class="action-item" @click="navigateToAddExpense">
          <view class="action-icon-box bg-red">
            <image src="/static/images/minus-circle.png" class="action-icon" />
          </view>
          <text class="action-label">记录支出</text>
        </view>
        <view class="action-item" @click="navigateToPortfolio">
          <view class="action-icon-box bg-green">
            <image src="/static/images/chart-pie.png" class="action-icon" />
          </view>
          <text class="action-label">投资组合</text>
        </view>
        <view class="action-item" @click="navigateToReport">
          <view class="action-icon-box bg-purple">
            <image src="/static/images/file-text.png" class="action-icon" />
          </view>
          <text class="action-label">财务报表</text>
        </view>
      </view>
    </view>

    <!-- 底部卡片区域 -->
    <view class="bottom-cards-section">
      <view class="card-row">
        <!-- 本月支出卡片 -->
        <view class="small-card expense-card">
          <text class="card-title">本月支出</text>
          <text class="card-amount">{{ showBalance ? formatCurrency(currentMonthExpense) : '***' }}</text>
          <text class="card-change">+12.5% 较上月</text>
        </view>

        <!-- 本月收入卡片 -->
        <view class="small-card income-card">
          <text class="card-title">本月收入</text>
          <text class="card-amount">{{ showBalance ? formatCurrency(currentMonthIncome) : '***' }}</text>
          <text class="card-change">+5.2% 较上月</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';

// 模拟数据
const showBalance = ref(true);

// 财务总览数据
const financialSummary = ref({
  totalAssets: 1285643.50,
  dailyReturn: 1562.30,
  monthlyReturn: 48500.20,
  totalReturn: 125600.80
});

// 资产配置数据
const assetAllocation = ref([
  { label: '股票', value: 45, color: '#60a5fa' },
  { label: '债券', value: 25, color: '#fbbf24' },
  { label: '现金', value: 20, color: '#34d399' },
  { label: '其他', value: 10, color: '#e879f9' }
]);

// 目标总览数据
const goalsSummary = ref({
  overallProgress: 65.8,
  totalGoals: 5,
  completedGoals: 2,
  activeGoals: 3
});

// 最近目标数据
const recentGoals = ref([
  { id: 1, name: '购房基金', targetAmount: 500000, currentAmount: 350000, icon: '🏠', color: '#60a5fa' },
  { id: 2, name: '子女教育', targetAmount: 200000, currentAmount: 120000, icon: '🎓', color: '#fbbf24' },
  { id: 3, name: '退休储蓄', targetAmount: 1000000, currentAmount: 450000, icon: '👴', color: '#34d399' }
]);

// 本月收支数据
const currentMonthExpense = ref(15680.50);
const currentMonthIncome = ref(32500.00);

// 切换余额可见性
const toggleBalanceVisibility = () => {
  showBalance.value = !showBalance.value;
};

// 格式化货币
const formatCurrency = (amount) => {
  if (amount === undefined || amount === null) return '0.00';
  return '¥' + amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

// 导航函数
const navigateToGoals = () => {
  uni.navigateTo({ url: '/pages/goals/index' });
};

const navigateToPortfolio = () => {
  uni.navigateTo({ url: '/pages/portfolio/index' });
};

const navigateToAddAsset = () => {
  uni.navigateTo({ url: '/pages/asset/index' });
};

const navigateToGoalDetail = (id) => {
  uni.navigateTo({ url: `/components/goals/detail?id=${id}` });
};

const navigateToCreateGoal = () => {
  uni.showModal({
    title: '新建目标',
    editable: true,
    placeholderText: '请输入目标名称',
    content: '例如：购房基金',
    success: (res) => {
      if (res.confirm && res.content) {
        // 这里可以添加创建目标的逻辑
        uni.showToast({ title: '目标创建功能开发中', icon: 'none' });
      }
    }
  });
};

const navigateToAddExpense = () => {
  uni.showToast({ title: '记录支出功能开发中', icon: 'none' });
};

const navigateToReport = () => {
  uni.showToast({ title: '财务报表功能开发中', icon: 'none' });
};

// 页面显示时刷新数据
onShow(() => {
  // 这里可以添加实际的数据获取逻辑
  console.log('首页数据已加载');
});
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 96px; /* pb-24 */
  position: relative;
  width: 100%;
}

/* 顶部背景装饰 */
.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 300px;
  background: linear-gradient(to bottom, $primary, $primary-dark);
  border-bottom-left-radius: 48px;
  border-bottom-right-radius: 48px;
  z-index: 0;
  overflow: hidden;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);

  .blob-white {
    position: absolute;
    top: -20%;
    right: -10%;
    width: 300px;
    height: 300px;
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    filter: blur(64px); /* blur-3xl */
    pointer-events: none;
  }

  .blob-black {
    position: absolute;
    bottom: 10%;
    left: -10%;
    width: 200px;
    height: 200px;
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 50%;
    filter: blur(40px); /* blur-2xl */
    pointer-events: none;
  }
}

/* 欢迎区域 */
.welcome-section {
  position: relative;
  z-index: 10;
  padding: 32px 20px 16px;
}

.welcome-content {
  color: white;
}

.welcome-greeting {
  font-size: 24px;
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 14px;
  opacity: 0.9;
  display: block;
}

/* 财务总览卡片 */
.overview-section {
  padding: 0 20px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;
}

.financial-overview-card {
  background-color: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration-1 {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  background-color: rgba(42, 128, 108, 0.05);
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
}

.bg-decoration-2 {
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 100px;
  height: 100px;
  background-color: rgba(96, 165, 250, 0.05);
  border-radius: 50%;
  filter: blur(30px);
  pointer-events: none;
}

/* 总资产信息 */
.total-assets-box {
  margin-bottom: 24px;
}

.label-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.label-text {
  font-size: 14px;
  font-weight: 500;
  color: $text-regular;
}

.eye-icon {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.amount-text {
  font-size: 36px;
  font-weight: bold;
  color: $text-main;
  font-family: monospace;
  letter-spacing: -0.05em;
}

/* 收益信息 */
.income-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid $bg-subtle;
}

.income-item {
  flex: 1;
  text-align: center;
}

.income-item.border-left {
  border-left: 1px solid $bg-subtle;
}

.income-label {
  font-size: 12px;
  color: $text-regular;
  display: block;
  margin-bottom: 4px;
}

.income-val {
  font-size: 18px;
  font-weight: 600;
  font-family: monospace;
}

.text-gain-light {
  color: $text-gain;
}

.text-loss-light {
  color: $text-loss;
}

/* 资产配置 */
.allocation-section {
  margin-top: 20px;
}

.allocation-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.allocation-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-main;
}

.view-detail {
  font-size: 14px;
  color: $primary;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.arrow-icon {
  width: 16px;
  height: 16px;
  margin-left: 4px;
}

.allocation-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.chart-item {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.chart-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.chart-label {
  flex: 1;
  font-size: 14px;
  color: $text-regular;
}

.chart-value {
  font-size: 14px;
  font-weight: 500;
  color: $text-main;
  font-family: monospace;
}

.allocation-bar {
  display: flex;
  flex-direction: row;
  height: 8px;
  background-color: $bg-subtle;
  border-radius: 4px;
  overflow: hidden;
}

.allocation-segment {
  height: 100%;
  transition: width 0.5s ease;
}

/* 目标总览区域 */
.goals-section {
  padding: 0 20px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-main;
}

.view-all {
  font-size: 14px;
  color: $primary;
  display: flex;
  flex-direction: row;
  align-items: center;
}

/* 目标总进度卡片 */
.goal-overview-card {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.goal-summary {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.goal-label {
  font-size: 14px;
  color: $text-regular;
}

.goal-percent {
  font-size: 24px;
  font-weight: bold;
  color: $primary;
  font-family: monospace;
}

.goal-progress-bar-bg {
  height: 10px;
  background-color: $bg-subtle;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 12px;
}

.goal-progress-bar-fill {
  height: 100%;
  background-color: $primary;
  border-radius: 5px;
  transition: width 0.5s ease;
}

.goal-stats {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.stat-text {
  font-size: 12px;
  color: $text-regular;
}

/* 目标列表 */
.goals-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goal-item {
  background-color: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.1s;
}

.goal-item:active {
  transform: scale(0.99);
}

.goal-item-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.goal-icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(42, 128, 108, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.goal-emoji {
  font-size: 20px;
}

.goal-item-info {
  display: flex;
  flex-direction: column;
}

.goal-item-name {
  font-size: 16px;
  font-weight: 500;
  color: $text-main;
  margin-bottom: 4px;
}

.goal-item-target {
  font-size: 14px;
  color: $text-regular;
}

.goal-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.goal-item-progress {
  font-size: 16px;
  font-weight: 600;
  color: $primary;
  font-family: monospace;
}

.goal-item-progress-bar-bg {
  width: 80px;
  height: 6px;
  background-color: $bg-subtle;
  border-radius: 3px;
  overflow: hidden;
}

.goal-item-progress-bar-fill {
  height: 100%;
  background-color: $primary;
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* 添加目标按钮 */
.add-goal-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  background-color: white;
  border: 2px dashed $border-color;
  border-radius: 16px;
  padding: 16px;
  color: $primary;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.add-goal-btn:active {
  background-color: $bg-subtle;
  border-color: $primary;
}

.add-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

/* 快捷操作区域 */
.quick-actions-section {
  padding: 0 20px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.action-icon-box {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.action-icon-box.bg-blue {
  background-color: $blue-light;
}

.action-icon-box.bg-red {
  background-color: $red-50;
}

.action-icon-box.bg-green {
  background-color: $green-50;
}

.action-icon-box.bg-purple {
  background-color: #f5f3ff;
}

.action-icon {
  width: 32px;
  height: 32px;
}

.action-label {
  font-size: 14px;
  color: $text-regular;
}

/* 底部卡片区域 */
.bottom-cards-section {
  padding: 0 20px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;
}

.card-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.small-card {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-title {
  font-size: 14px;
  color: $text-regular;
}

.card-amount {
  font-size: 24px;
  font-weight: bold;
  color: $text-main;
  font-family: monospace;
}

.card-change {
  font-size: 12px;
  color: $text-gain;
  margin-top: 4px;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in-content {
  animation: fadeIn 0.3s ease-out;
}
</style>