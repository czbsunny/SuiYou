<template>
  <section class="section">
    <view class="section-header">
      <text class="section-title">成就展馆</text>
      <text class="count-badge">已达成 {{ list.length }}</text>
    </view>

    <view class="achievement-list">
      <view v-for="item in list" :key="item.id" class="achievement-item">
        <view class="achievement-left">
          <view class="medal-box">{{ item.icon }}</view>
          <view class="achievement-info">
            <text class="achievement-title">{{ item.title }}</text>
            <text class="achievement-date">达成于 {{ item.date }}</text>
          </view>
        </view>
        <view class="achievement-right">
          <text class="achievement-amount">¥{{ item.amount.toLocaleString() }}</text>
          <view class="done-tag">
            <text class="fa-solid fa-medal"></text>
            <text>已达成</text>
          </view>
        </view>
      </view>
    </view>
  </section>
</template>

<script setup>
defineProps({
  list: { type: Array, default: () => [] }
});
</script>

<style lang="scss" scoped>
/* 此处粘贴原代码中 .achievement-item 相关的全部样式 */
.section { margin-bottom: 50rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 28rpx; font-weight: bold; color: #2C3E50; }
.count-badge { font-size: 22rpx; color: #95A5A6; }
.achievement-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24rpx 30rpx; background: #fff; border-radius: 30rpx; margin-bottom: 20rpx;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.02);
  .achievement-left {
    display: flex; align-items: center; gap: 20rpx;
    .medal-box { width: 70rpx; height: 70rpx; background: #fffdf0; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32rpx; border: 1rpx solid #fef3c7; }
    .achievement-title { font-size: 26rpx; font-weight: bold; color: #2C3E50; display: block; }
    .achievement-date { font-size: 18rpx; color: #95A5A6; }
  }
  .achievement-right {
    text-align: right;
    .achievement-amount { font-size: 24rpx; font-weight: bold; color: #2C3E50; display: block; }
    .done-tag { font-size: 18rpx; color: #D4AF37; display: flex; align-items: center; gap: 4rpx; }
  }
}
</style>