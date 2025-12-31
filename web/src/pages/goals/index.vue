<template>
  <!-- 移除 page-wrapper 的 100vh，靠内容撑开 -->
  <view class="page-container">
    <view class="header-dashboard">
      <view class="dashboard-top">
        <view class="summary-stats">
          <view class="stat-item">
            <text class="stat-label">累计储备</text>
            <text class="stat-value">¥684,500</text>
          </view>
          <view class="v-divider"></view>
          <view class="stat-item">
            <text class="stat-label">每月定存</text>
            <text class="stat-value text-primary">+¥5,500</text>
          </view>
          <view class="v-divider"></view>
          <view class="stat-item">
            <text class="stat-label">平均进度</text>
            <text class="stat-value text-gold">24%</text>
          </view>
        </view>
        
        <view class="add-btn-mini" @click="handleAddGoal">
          <image 
            src="/static/images/plus-gray.png" 
            class="add-icon-img" 
            mode="aspectFit"
          ></image>
        </view>
      </view>
    </view>

    <view class="main-content">
      <!-- 模块 1：当前主线 -->
      <section class="section">
        <view class="section-header">
          <text class="section-title">当前主线</text>
        </view>
        
        <view class="hero-card" @click="goDetail(primaryGoal)">
          <image :src="primaryGoal.image" mode="aspectFill" class="hero-bg" />
          <view class="hero-overlay"></view>

          <view class="hero-info">
            <view class="hero-top">
              <view class="hero-title-box">
                <view class="title-row">
                  <text class="goal-name">{{ primaryGoal.title }}</text>
                  <view class="status-pill" :class="getStatusConfig(primaryGoal.status).class">
                    <text :class="['fa-solid', getStatusConfig(primaryGoal.status).icon]"></text>
                    <text class="pill-text">{{ getStatusConfig(primaryGoal.status).text }}</text>
                  </view>
                </view>
                <text class="goal-subtitle">{{ primaryGoal.subtitle }}</text>
              </view>
              <text class="percent-text">{{ primaryGoal.progress }}%</text>
            </view>

            <view class="progress-bar-container">
              <view class="progress-fill" :style="{ width: primaryGoal.progress + '%' }"></view>
            </view>

            <view class="auto-save-info">
              <view class="auto-save-icon">
                <text class="fa-solid fa-rotate"></text>
              </view>
              <view class="auto-save-text">
                <view class="info-row">
                  <text>每月自动存入</text>
                  <text class="amount-highlight">+¥{{ formatMoney(primaryGoal.monthlyAuto) }}</text>
                </view>
                <text class="sub-info">下次执行: {{ primaryGoal.nextDeduction }}</text>
              </view>
            </view>
          </view>
        </view>
      </section>

      <!-- 模块 2：愿望清单 -->
      <section class="section">
        <view class="section-header">
          <text class="section-title">愿望清单</text>
          <text class="count-badge">进行中 {{ wishlist.length }}</text>
        </view>

        <view class="wish-grid">
          <view v-for="item in wishlist" :key="item.id" class="wish-card" @click="goDetail(item)">
            <!-- 顶部：图标 + 名称 -->
            <view class="wish-top">
              <view class="wish-icon-wrapper">
                <view class="wish-icon-box">{{ item.icon }}</view>
                <text class="wish-title">{{ item.title }}</text>
              </view>
              <view v-if="item.status === 'AT_RISK'" class="risk-dot"></view>
            </view>

            <!-- 底部：金额 + 进度条 -->
            <view class="wish-bottom">
              <view class="amount-group">
                <text class="amount-current">¥{{ formatMoney(item.current) }}</text>
                <text class="amount-target">/ {{ formatMoney(item.target) }}</text>
              </view>
              <view class="mini-progress-bar">
                <view 
                  class="mini-progress-fill" 
                  :class="{ 'bg-orange': item.status === 'AT_RISK' }" 
                  :style="{ width: getPercent(item.current, item.target) + '%' }"
                ></view>
              </view>
            </view>
          </view>

          <!-- 新增愿望占位符 -->
          <view class="wish-card add-wish-card" @click="handleAddGoal">
            <image src="/static/images/plus-gray.png" class="plus-icon-img" mode="aspectFit"></image>
            <text class="add-text">新愿望</text>
          </view>
        </view>
      </section>

      <!-- 模块 3：成就展馆 -->
      <section class="section">
        <view class="section-header">
          <text class="section-title">成就展馆</text>
          <text class="count-badge">已达成 {{ achievements.length }}</text>
        </view>

        <view class="achievement-list">
          <view v-for="item in achievements" :key="item.id" class="achievement-item">
            <view class="achievement-left">
              <view class="medal-box">{{ item.icon }}</view>
              <view class="achievement-info">
                <text class="achievement-title">{{ item.title }}</text>
                <text class="achievement-date">达成于 {{ item.date }}</text>
              </view>
            </view>
            <view class="achievement-right">
              <text class="achievement-amount">¥{{ formatMoney(item.amount) }}</text>
              <view class="done-tag">
                <text class="fa-solid fa-medal"></text>
                <text>已达成</text>
              </view>
            </view>
          </view>
        </view>
      </section>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

type StatusType = 'ON_TRACK' | 'AT_RISK' | 'COMPLETED';

const primaryGoal = ref({
  title: '快乐老家',
  subtitle: '首要目标 · 婚房基金',
  image: 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?q=80&w=1000&auto=format&fit=crop',
  progress: 32,
  status: 'ON_TRACK' as StatusType,
  monthlyAuto: 2000,
  nextDeduction: '11月15日'
});

const wishlist = ref([
  { id: '1', icon: '🏔️', title: '川西徒步', current: 8000, target: 15000, status: 'ON_TRACK' },
  { id: '2', icon: '📷', title: '索尼 A7M4', current: 5000, target: 18000, status: 'AT_RISK' },
  { id: '3', icon: '💻', title: 'MacBook Pro', current: 2000, target: 16000, status: 'ON_TRACK' },
]);

const achievements = ref([
  { id: '1', icon: '🎓', title: '助学贷款结清', date: '2023.05', amount: 24000 },
  { id: '2', icon: '💰', title: '人生第一桶金', date: '2022.12', amount: 100000 },
]);

const formatMoney = (val: number) => val.toLocaleString();
const getPercent = (current: number, target: number) => Math.round((current / target) * 100);

const getStatusConfig = (status: StatusType) => {
  const config = {
    'ON_TRACK': { class: 'status-normal', icon: 'fa-check-circle', text: '进度正常' },
    'AT_RISK': { class: 'status-warning', icon: 'fa-triangle-exclamation', text: '进度滞后' },
    'COMPLETED': { class: 'status-done', icon: 'fa-trophy', text: '已达成' },
  };
  return config[status] || config['ON_TRACK'];
};

const handleAddGoal = () => uni.navigateTo({ url: '/pages/goals/add' });
const goDetail = (item: any) => console.log('查看详情', item);
</script>

<style lang="scss" scoped>
$primary-color: #2a806c;
$gold-color: #D4AF37;
$bg-color: #F5F7F9;
$text-main: #2C3E50;
$text-sub: #95A5A6;

.page-container {
  min-height: 100vh;
  background-color: $bg-color;
}

/* 头部面板：适配导航栏下方 */
.header-dashboard {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  background-color: rgba($bg-color, 0.96);
  backdrop-filter: blur(10px);
  padding: 30rpx 48rpx;
  z-index: 99;
  border-bottom: 1rpx solid rgba(0,0,0,0.03);

  .dashboard-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.summary-stats {
  display: flex;
  gap: 32rpx;
  align-items: center;

  .stat-item {
    .stat-label {
      display: block;
      font-size: 20rpx;
      color: $text-sub;
      font-weight: bold;
      margin-bottom: 6rpx;
    }
    .stat-value {
      font-size: 36rpx;
      font-weight: bold;
      color: $text-main;
    }
    .text-primary { color: $primary-color; }
    .text-gold { color: $gold-color; }
  }

  .v-divider {
    width: 2rpx;
    height: 50rpx;
    background-color: #e2e8f0;
  }
}

.add-btn-mini {
  width: 74rpx; // 稍微加宽一点点，更有点击感
  height: 74rpx;
  background: #fff;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 15rpx rgba(0,0,0,0.06);
  
  &:active { 
    transform: scale(0.92); 
    background-color: #f8fafc;
  }

  .add-icon-img {
    /* 建议图标大小在 30rpx-36rpx 之间，视觉最协调 */
    width: 32rpx;
    height: 32rpx;
    /* 如果图片是深灰色的，我们可以通过透明度微调它与整体设计的融合度 */
    opacity: 0.8;
  }
}

/* 主内容区 */
.main-content {
  padding: 30rpx 48rpx;
}

.section {
  margin-bottom: 50rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 28rpx;
      font-weight: bold;
      color: $text-main;
    }
    .count-badge {
      font-size: 22rpx;
      color: $text-sub;
    }
  }
}

/* 核心资产卡片 */
.hero-card {
  position: relative;
  width: 100%;
  aspect-ratio: 16/11;
  border-radius: 50rpx;
  overflow: hidden;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.08);

  .hero-bg { width: 100%; height: 100%; }
  .hero-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.2) 60%);
  }

  .hero-info {
    position: absolute;
    bottom: 0; left: 0; right: 0;
    padding: 40rpx;
  }

  .hero-top {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    .goal-name { font-size: 44rpx; color: #fff; font-weight: bold; display: block; margin-bottom: 6rpx; }
    .goal-subtitle { font-size: 24rpx; color: rgba(255,255,255,0.7); }
    .percent-text { font-size: 44rpx; font-weight: bold; color: $gold-color; }
  }
}

/* 进度条与状态 */
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  font-size: 18rpx;
  font-weight: bold;
  background: rgba(255,255,255,0.15);
  color: #fff;
  border: 1rpx solid rgba(255,255,255,0.2);
  margin-left: 15rpx;
  vertical-align: middle;

  &.status-normal { color: #68d391; }
  &.status-warning { color: #f6ad55; }
  &.status-done { color: #f6e05e; }
}

.progress-bar-container {
  width: 100%; height: 10rpx; background: rgba(255,255,255,0.2);
  border-radius: 100rpx; margin-bottom: 25rpx; overflow: hidden;
  .progress-fill { height: 100%; background: $gold-color; }
}

.auto-save-info {
  display: flex; align-items: center; gap: 20rpx;
  background: rgba(255,255,255,0.1); backdrop-filter: blur(10px);
  padding: 20rpx; border-radius: 20rpx;
  .auto-save-icon {
    width: 50rpx; height: 50rpx; border-radius: 50%;
    background: rgba(104, 211, 145, 0.2); display: flex; align-items: center; justify-content: center; color: #68d391; font-size: 24rpx;
  }
  .info-row { font-size: 22rpx; color: #fff; }
  .amount-highlight { color: #68d391; font-weight: bold; margin-left: 10rpx; }
  .sub-info { font-size: 18rpx; color: rgba(255,255,255,0.4); display: block; }
}

/* 愿望卡片 */
.wish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25rpx;
}

/* 愿望卡片重构样式 */
.wish-card {
  background: #fff;
  padding: 28rpx; // 稍微缩小内边距，更精致
  border-radius: 40rpx;
  height: 260rpx; // 高度稍微压缩，视觉更紧凑
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.03);
  transition: all 0.2s;

  &:active { transform: scale(0.96); }

  .wish-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .wish-icon-wrapper {
      display: flex;
      align-items: center; // 图标和文字水平对齐
      gap: 16rpx;
      flex: 1;
      overflow: hidden; // 防止文字过长撑开
    }

    .wish-icon-box {
      width: 64rpx; // 缩小图标尺寸
      height: 64rpx;
      background: #f8fafc;
      border-radius: 18rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      flex-shrink: 0; // 防止图标被压缩
    }

    .wish-title {
      font-size: 26rpx;
      font-weight: bold;
      color: $text-main;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .risk-dot {
      width: 12rpx;
      height: 12rpx;
      background: #f6ad55;
      border-radius: 50%;
      margin-top: 26rpx; // 配合标题居中
    }
  }

  .wish-bottom {
    .amount-group {
      display: flex;
      align-items: baseline; // 金额对齐基准线
      margin-bottom: 12rpx;
      
      .amount-current {
        font-size: 28rpx;
        font-weight: bold;
        color: $text-main;
        font-family: 'DIN Alternate', sans-serif;
      }
      
      .amount-target {
        font-size: 20rpx;
        color: $text-sub;
        margin-left: 8rpx;
      }
    }

    .mini-progress-bar {
      width: 100%;
      height: 8rpx;
      background: #edf2f7;
      border-radius: 100rpx;
      overflow: hidden;

      .mini-progress-fill {
        height: 100%;
        background: $primary-color;
        border-radius: 100rpx;
        &.bg-orange { background: #f6ad55; }
      }
    }
  }
}

.add-wish-card {
  border: 3rpx dashed #cbd5e0;
  background: rgba(255, 255, 255, 0.4);
  justify-content: center; // 覆盖之前的 space-between
  align-items: center;
  
  .plus-icon-img {
    width: 48rpx;
    height: 48rpx;
    margin-bottom: 12rpx;
    opacity: 0.3;
  }
  .add-text {
    font-size: 22rpx;
    color: #a0aec0;
  }
}

/* 成就列表 */
.achievement-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24rpx 30rpx; background: #fff; border-radius: 30rpx; margin-bottom: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.02);

  .achievement-left {
    display: flex; align-items: center; gap: 20rpx;
    .medal-box {
      width: 70rpx; height: 70rpx; background: #fffdf0; border-radius: 50%;
      display: flex; align-items: center; justify-content: center; font-size: 32rpx; border: 1rpx solid #fef3c7;
    }
    .achievement-title { font-size: 26rpx; font-weight: bold; color: $text-main; display: block; }
    .achievement-date { font-size: 18rpx; color: $text-sub; }
  }
  .achievement-right {
    text-align: right;
    .achievement-amount { font-size: 24rpx; font-weight: bold; color: $text-main; display: block; }
    .done-tag { font-size: 18rpx; color: $gold-color; display: flex; align-items: center; gap: 4rpx; }
  }
}
</style>