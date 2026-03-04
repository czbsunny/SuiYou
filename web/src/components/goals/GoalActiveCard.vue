<template>
  <view class="active-card-container">
    <!-- 标题和操作按钮 -->
    <view class="section-header">
      <text class="section-title">正在努力</text>
      <view class="header-actions">
        <!-- 仅图标化按钮：排序 & 添加 -->
        <view class="icon-btn" @click="$emit('sort')" hover-class="btn-hover">
          <image src="/static/icons/sliders.png" mode="aspectFit" class="btn-img" />
        </view>
        <view class="icon-btn" @click="$emit('add')" hover-class="btn-hover">
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="icon-img" />
        </view>
      </view>
    </view>

    <!-- 卡片列表 -->
    <view class="card-list">
      <view 
        v-for="goal in data" 
        :key="goal.id" 
        class="active-card" 
        @click="$emit('click', goal)"
      >
        <!-- 第一行：Icon、名称、剩余时间 (紧凑排布) -->
        <view class="row-header">
          <view class="left-group">
            <view class="icon-box">
              <image v-if="isIconUrl(goal.iconUrl)" :src="goal.iconUrl" mode="aspectFit" class="img-icon" />
              <text v-else class="emoji-icon">{{ goal.icon || '🎯' }}</text>
            </view>
            <text class="goal-name">{{ goal.name }}</text>
          </view>
          <view class="remain-tag">
            <text class="label">剩</text>
            <text class="num num-font">{{ getRemainDays(goal.deadline) }}</text>
            <text class="label">天</text>
          </view>
        </view>

        <!-- 第二行：数据明细 (百分比与金额并列) -->
        <view class="row-data">
          <view class="progress-info">
            <text class="status-desc">已达成</text>
            <text class="percent num-font">{{ getProgressPercent(goal) }}%</text>
          </view>
          <view class="amount-info">
            <text class="label">已攒</text>
            <text class="current num-font">¥{{ formatMoney(goal.currentAmount) }}</text>
            <text class="target">/ ¥{{ formatMoney(goal.targetAmount) }}</text>
          </view>
        </view>

        <!-- 第三行：进度条 (纤细化处理) -->
        <view class="row-progress">
          <view class="bar-track">
            <view class="bar-fill" :style="{ width: getProgressPercent(goal) + '%' }"></view>
          </view>
        </view>

        <!-- 第四行：指令提示 (极致扁平化) -->
        <view class="row-tips" v-if="goal.nextAction">
          <text class="tip-tag">建议</text>
          <text class="tip-text">{{ goal.nextAction }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
});

defineEmits(['click', 'sort', 'add']);

const isIconUrl = (url) => url && (url.startsWith('http') || url.startsWith('/static') || url.startsWith('/'));

const getProgressPercent = (goal) => {
  if (!goal.targetAmount) return 0;
  const p = Math.round((goal.currentAmount / goal.targetAmount) * 100);
  return Math.min(100, p);
};

const getRemainDays = (deadline) => {
  if (!deadline) return 0;
  const diff = new Date(deadline) - new Date();
  const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
  return days > 0 ? days : 0;
};

const formatMoney = (val) => Number(val).toLocaleString('zh-CN');
</script>

<style lang="scss" scoped>
.active-card-container {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-main;
  letter-spacing: 1rpx;
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

  .btn-img, .icon-img {
    width: 28rpx;
    height: 28rpx;
  }
}

.btn-hover {
  opacity: 0.8;
  transform: scale(0.94);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.active-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 30rpx 30rpx 24rpx;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(50, 46, 43, 0.02);
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.985);
    background-color: #FAFAFA;
  }
}

/* Header: 标题与天数 */
.row-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.left-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.icon-box {
  width: 52rpx;
  height: 52rpx;
  background-color: $bg-subtle;
  border-radius: 12rpx;
  @include flex-center;
  flex-shrink: 0;
  
  .img-icon { width: 36rpx; height: 36rpx; }
  .emoji-icon { font-size: 32rpx; }
}

.goal-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-main;
  @include text-ellipsis;
}

.remain-tag {
  background-color: #F1F4F2;
  padding: 4rpx 16rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  color: $text-sub;
  
  .num {
    color: $primary;
    font-weight: 700;
    margin: 0 4rpx;
    font-size: 24rpx;
  }
}

/* Progress Bar: 纤细化 */
.row-progress {
  margin-bottom: 16rpx;
}

.bar-track {
  height: 12rpx;
  background-color: $bg-subtle;
  border-radius: 10rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(to right, $primary, $primary-dark);
  border-radius: 10rpx;
  transition: width 0.8s cubic-bezier(0.25, 1, 0.5, 1);
}

/* Data Detail: 布局对齐 */
.row-data {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.progress-info {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  
  .percent {
    font-size: 24rpx;
    font-weight: 700;
    color: $primary;
  }
  .status-desc {
    font-size: 20rpx;
    color: $text-muted;
  }
}

.amount-info {
  text-align: right;
  font-size: 22rpx;
  color: $text-muted;
  
  .current {
    color: $text-main;
    font-weight: 600;
    margin-left: 6rpx;
  }
  .target {
    margin-left: 4rpx;
  }
}

/* Tips: 极致简约 */
.row-tips {
  border-top: 1rpx solid rgba(0,0,0,0.03);
  padding-top: 16rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .tip-tag {
    font-size: 18rpx;
    color: $primary;
    background-color: $bg-subtle;
    padding: 2rpx 8rpx;
    border-radius: 6rpx;
    font-weight: 600;
  }
  
  .tip-text {
    font-size: 22rpx;
    color: $text-sub;
    flex: 1;
    @include text-ellipsis;
  }
}

/* 数字字体通用类 */
.num-font {
  font-family: $font-family-money;
  @include tabular-nums;
}
</style>