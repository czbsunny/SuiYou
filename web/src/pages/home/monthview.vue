<template>
  <view class="report-page">
    <!-- 1. 顶部月报标题区 -->
    <view class="report-header animate-fade-in">
      <text class="month-title">2024年10月</text>
      <text class="sub-title">家庭财务复盘报告</text>
      <view class="status-tag">数据已结算</view>
    </view>

    <!-- 2. 核心概览：资产灵魂三问 -->
    <view class="overview-section animate-fade-in">
      <view class="overview-card card-warm">
        <view class="main-metric">
          <text class="label">月末净资产</text>
          <view class="val-group">
            <text class="num-font amount">1,258,600.45</text>
            <view class="trend" :class="netWorthChange >= 0 ? 'text-up' : 'text-down'">
              <uni-icons :type="netWorthChange >= 0 ? 'top' : 'bottom'" size="12" :color="netWorthChange >= 0 ? '#ef4444' : '#22c55e'"></uni-icons>
              <text class="num-font">{{ formatMoney(Math.abs(netWorthChange)) }}</text>
            </view>
          </view>
        </view>

        <view class="stats-grid">
          <view class="stat-item">
            <text class="s-label">本月储蓄率</text>
            <text class="s-val num-font">32.5%</text>
            <view class="s-progress"><view class="p-inner" style="width: 32.5%"></view></view>
          </view>
          <view class="stat-item">
            <text class="s-label">本月投资损益</text>
            <text class="s-val num-font text-up">+12,450.00</text>
            <text class="s-sub-label">资产自然增长</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 3. 收支流水复盘 -->
    <view class="section-title">收支复盘</view>
    <view class="flow-card card-warm">
      <view class="flow-chart-sim">
        <view class="flow-bar income">
          <text class="f-label">总收入</text>
          <text class="f-val num-font">45,000.00</text>
          <view class="f-rect" style="height: 120rpx"></view>
        </view>
        <view class="flow-bar expense">
          <text class="f-label">总支出</text>
          <text class="f-val num-font">30,375.00</text>
          <view class="f-rect" style="height: 80rpx"></view>
        </view>
      </view>
      
      <!-- 支出分类 Top3 -->
      <view class="top-list">
        <view class="list-item" v-for="(item, index) in topExpenses" :key="index">
          <text class="rank">{{ index + 1 }}</text>
          <text class="cat-name">{{ item.name }}</text>
          <view class="cat-bar-bg"><view class="cat-bar-inner" :style="{ width: item.percent + '%' }"></view></view>
          <text class="cat-amount num-font">{{ item.amount }}</text>
        </view>
      </view>
    </view>

    <!-- 4. 资产结构复盘（安全性分析） -->
    <view class="section-title">资产健康度</view>
    <view class="health-card card-warm">
      <view class="structure-list">
        <view class="struct-item" v-for="item in assetStructure" :key="item.name">
          <view class="dot" :style="{ background: item.color }"></view>
          <text class="s-name">{{ item.name }}</text>
          <text class="s-percent num-font">{{ item.percent }}%</text>
          <text class="s-desc">{{ item.status }}</text>
        </view>
      </view>
      <view class="safety-tip">
        <uni-icons type="info" size="14" color="#a3b0ad"></uni-icons>
        <text>家庭紧急预备金已覆盖未来 6.2 个月的支出</text>
      </view>
    </view>

    <!-- 5. 结语：非建议性总结 -->
    <view class="report-footer">
      <text class="footer-text">
        本月家庭储蓄目标已达成。收支比例维持在健康区间，风险资产配置比例在本月因市值波动略有上升。下月请关注大额保险费续期。
      </text>
    </view>

    <view class="safe-area-bottom" style="height: 60rpx;"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const netWorthChange = ref(24500.85); // 本月净资产变动

const topExpenses = ref([
  { name: '居家物业', amount: '12,000', percent: 40 },
  { name: '餐饮美食', amount: '6,500', percent: 25 },
  { name: '交通出行', amount: '3,200', percent: 15 }
]);

const assetStructure = ref([
  { name: '现金/活期', percent: 25, color: '#3b82f6', status: '流动性充足' },
  { name: '稳健理财', percent: 45, color: '#10B981', status: '核心资产' },
  { name: '风险投资', percent: 20, color: '#F59E0B', status: '波动中' },
  { name: '保险/其他', percent: 10, color: '#8B5CF6', status: '基础保障' }
]);

const formatMoney = (val) => {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding: 0 $spacing-md;
}

/* 1. Header */
.report-header {
  padding: 60rpx 0 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  .month-title { font-size: 48rpx; font-weight: $fw-bold; color: $text-main; }
  .sub-title { font-size: 24rpx; color: $text-muted; margin-top: 8rpx; letter-spacing: 4rpx; }
  .status-tag {
    margin-top: 24rpx; padding: 4rpx 20rpx; background: $primary-subtle;
    color: $primary; font-size: 20rpx; border-radius: $radius-full;
  }
}

/* 2. Overview */
.overview-card {
  padding: 40rpx;
  .main-metric {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 40rpx;
    .label { font-size: 24rpx; color: $text-muted; margin-bottom: 12rpx; }
    .amount { font-size: 56rpx; font-weight: $fw-bold; color: $text-main; }
    .trend {
      display: flex; align-items: center; gap: 4rpx; margin-top: 8rpx;
      text { font-size: 24rpx; font-weight: $fw-semibold; }
    }
  }
  .stats-grid {
    display: flex; border-top: 1rpx solid $gray-100; padding-top: 32rpx;
    .stat-item {
      flex: 1; display: flex; flex-direction: column; align-items: center;
      &:first-child { border-right: 1rpx solid $gray-100; }
      .s-label { font-size: 20rpx; color: $text-muted; margin-bottom: 8rpx; }
      .s-val { font-size: 32rpx; font-weight: $fw-bold; color: $text-main; }
      .s-progress {
        width: 120rpx; height: 6rpx; background: $gray-100; border-radius: 10rpx; margin-top: 12rpx;
        .p-inner { height: 100%; background: $primary; border-radius: 10rpx; }
      }
      .s-sub-label { font-size: 18rpx; color: $text-placeholder; margin-top: 8rpx; }
    }
  }
}

.section-title {
  padding: 40rpx 0 20rpx; font-size: 30rpx; font-weight: $fw-semibold; color: $text-main;
}

/* 3. Flow Card */
.flow-card {
  padding: 40rpx;
  .flow-chart-sim {
    display: flex; justify-content: space-around; align-items: flex-end;
    padding-bottom: 40rpx; border-bottom: 1rpx solid $gray-50;
    .flow-bar {
      display: flex; flex-direction: column; align-items: center; gap: 8rpx;
      .f-label { font-size: 20rpx; color: $text-muted; }
      .f-val { font-size: 24rpx; font-weight: $fw-bold; }
      .f-rect { width: 80rpx; border-radius: 12rpx 12rpx 4rpx 4rpx; }
      &.income .f-rect { background: $bg-income; border: 2rpx solid $color-income; }
      &.expense .f-rect { background: $bg-expense; border: 2rpx solid $color-expense; }
    }
  }
  .top-list {
    padding-top: 32rpx;
    .list-item {
      display: flex; align-items: center; gap: 20rpx; margin-bottom: 24rpx;
      .rank { font-size: 24rpx; font-weight: $fw-bold; color: $text-placeholder; font-style: italic; }
      .cat-name { font-size: 24rpx; color: $text-sub; width: 120rpx; }
      .cat-bar-bg { flex: 1; height: 12rpx; background: $gray-50; border-radius: 6rpx; }
      .cat-bar-inner { height: 100%; background: $orange-400; border-radius: 6rpx; }
      .cat-amount { font-size: 24rpx; font-weight: $fw-semibold; color: $text-main; }
    }
  }
}

/* 4. Health Card */
.health-card {
  padding: 32rpx;
  .struct-item {
    display: flex; align-items: center; gap: 16rpx; margin-bottom: 24rpx;
    .dot { width: 16rpx; height: 16rpx; border-radius: 50%; }
    .s-name { font-size: 24rpx; color: $text-sub; flex: 1; }
    .s-percent { font-size: 26rpx; font-weight: $fw-bold; width: 80rpx; }
    .s-desc { font-size: 22rpx; color: $text-muted; }
  }
  .safety-tip {
    margin-top: 32rpx; padding: 20rpx; background: $gray-50; border-radius: $radius-base;
    display: flex; align-items: center; gap: 12rpx;
    text { font-size: 22rpx; color: $text-regular; }
  }
}

/* 5. Footer */
.report-footer {
  padding: 40rpx 20rpx 100rpx;
  .footer-text {
    font-size: 24rpx; color: $text-muted; line-height: 1.8; text-align: justify;
  }
}

.animate-fade-in { animation: fadeIn 0.8s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>