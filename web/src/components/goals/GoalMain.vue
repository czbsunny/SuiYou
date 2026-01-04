<template>
  <section class="section">
    <view class="section-header">
      <text class="section-title">当前主线</text>
    </view>
    
    <!-- 情况 A：有主线目标 -->
    <view v-if="data && data.id" class="focus-card" @click="$emit('click', data)">
      <view class="focus-top">
        <view class="goal-info">
          <text class="goal-icon">{{ data.icon }}</text>
          <view class="goal-texts">
            <text class="goal-name">{{ data.title }}</text>
            <text class="goal-sub">目标日期: {{ data.deadline || '未设置' }}</text>
          </view>
        </view>
        <view class="goal-status on-track">进行中</view>
      </view>
      
      <view class="progress-box">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: data.progress + '%' }"></view>
        </view>
        <view class="progress-data">
          <text class="percent">{{ data.progress }}%</text>
          <text class="amount">已筹 ¥{{ data.current?.toLocaleString() }} / ¥{{ data.target?.toLocaleString() }}</text>
        </view>
      </view>
    </view>

    <!-- 情况 B：无主线目标 (空白状态页) -->
    <view v-else class="empty-focus-card" @click="$emit('add')">
      <view class="empty-content">
        <image src="/static/images/plus-gray.png" class="empty-icon" />
        <text class="empty-title">未设置主线目标</text>
        <text class="empty-desc">将最重要的目标设为主线，在此置顶追踪</text>
      </view>
    </view>
  </section>
</template>

<script setup>
defineProps({ data: Object });
defineEmits(['click', 'add']);
</script>

<style lang="scss" scoped>
.section { margin-bottom: 40rpx; }
.section-title { font-size: 28rpx; font-weight: bold; color: #2C3E50; }

.focus-card {
  background: #fff; padding: 32rpx; border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
  border: 1rpx solid #edf2f7;
  .focus-top {
    display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 30rpx;
    .goal-info { display: flex; align-items: center; gap: 20rpx;
      .goal-icon { font-size: 44rpx; }
      .goal-name { font-size: 32rpx; font-weight: bold; color: #2C3E50; display: block; }
      .goal-sub { font-size: 22rpx; color: #95A5A6; }
    }
    .goal-status { font-size: 20rpx; padding: 4rpx 16rpx; border-radius: 8rpx; background: #eefdf5; color: #27ae60; }
  }
}

.progress-bar {
  height: 12rpx; background: #f0f4f8; border-radius: 10rpx; overflow: hidden;
  .progress-fill { height: 100%; background: #2a806c; border-radius: 10rpx; }
}
.progress-data {
  display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx;
  .percent { font-size: 36rpx; font-weight: bold; color: #2a806c; font-family: 'DIN Alternate', sans-serif; }
  .amount { font-size: 22rpx; color: #95A5A6; }
}

/* 空白态样式 */
.empty-focus-card {
  width: 100%; height: 260rpx; background: #fff; border-radius: 32rpx;
  border: 2rpx dashed #d1d5db; display: flex; align-items: center; justify-content: center;
  &:active { background: #f8fafc; }
  .empty-content {
    display: flex; flex-direction: column; align-items: center;
    .empty-icon { width: 40rpx; height: 40rpx; margin-bottom: 16rpx; opacity: 0.3; }
    .empty-title { font-size: 28rpx; font-weight: bold; color: #7f8c8d; }
    .empty-desc { font-size: 22rpx; color: #bdc3c7; margin-top: 4rpx; }
  }
}
</style>