<template>
  <view class="completed-item-container" v-if="data.length > 0">
    <!-- 标题 -->
    <view class="section-header">
      <text class="section-title dimmed">已实现</text>
    </view>
    
    <!-- 已完成目标列表 -->
    <view class="card-list">
      <view class="completed-item" v-for="goal in data" :key="goal.id" @click="$emit('click', goal)">
        <!-- 左侧图标：进一步缩小并弱化 -->
        <view class="comp-icon-box">
          <image 
            v-if="isIconUrl(goal.iconUrl)" 
            :src="goal.iconUrl" 
            mode="aspectFit" 
            class="img-icon" 
          />
          <text v-else class="emoji-icon">{{ goal.icon || '🏆' }}</text>
        </view>

        <!-- 中间信息：紧凑排列 -->
        <view class="comp-info">
          <text class="comp-title">{{ goal.name }}</text>
          <text class="comp-date num-font">{{ formatDate(goal.completedDate) }} 达成</text>
        </view>

        <!-- 右侧状态：极简线条标记 -->
        <view class="status-mark">
          <view class="line"></view>
          <text class="mark-text">已圆梦</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
});

defineEmits(['click']);

const isIconUrl = (url) => url && (url.startsWith('http') || url.startsWith('/static') || url.startsWith('/'));

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};
</script>

<style lang="scss" scoped>
.completed-item-container {
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

  &.dimmed {
    font-size: 26rpx;
    color: $text-muted;
    font-weight: 400;
  }
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.completed-item {
  background-color: $bg-white;
  border-radius: $radius-base;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  position: relative;
  border: 1rpx solid $border-color;
  transition: all 0.2s;
  
  &:active {
    opacity: 0.8;
    background-color: $bg-subtle;
  }
}

.comp-icon-box {
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  margin-right: 24rpx;
  flex-shrink: 0;
  background-color: #F5F7F6;
  border-radius: 12rpx;
  
  .img-icon {
    width: 36rpx;
    height: 36rpx;
    filter: grayscale(1);
    opacity: 0.4;
  }
  
  .emoji-icon {
    font-size: 32rpx;
    filter: grayscale(1);
    opacity: 0.5;
  }
}

.comp-info {
  flex: 1;
  min-width: 0;
  
  .comp-title {
    display: block;
    font-size: 26rpx;
    color: $text-sub;
    font-weight: 500;
    margin-bottom: 4rpx;
    @include text-ellipsis;
  }
  
  .comp-date {
    display: block;
    font-size: 20rpx;
    color: $text-muted;
    letter-spacing: 1rpx;
  }
}

/* 极简状态标记 */
.status-mark {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .line {
    width: 1rpx;
    height: 32rpx;
    background-color: $border-color;
  }
  
  .mark-text {
    font-size: 20rpx;
    color: $text-muted;
    font-weight: 600;
    letter-spacing: 2rpx;
    border: 1rpx solid $text-placeholder;
    padding: 2rpx 8rpx;
    border-radius: 4rpx;
    transform: rotate(2deg);
  }
}

.num-font {
  font-family: $font-family-money;
  @include tabular-nums;
}
</style>