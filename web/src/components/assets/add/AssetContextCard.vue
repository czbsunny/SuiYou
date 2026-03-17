<template>
  <view class="context-card">
    <view class="info-group">
      <image v-if="institution.logoUrl" :src="institution.logoUrl" class="inst-logo" />
      <view class="text-info">
        <text class="inst-name">{{ institution.instName }}</text>
        <text class="cate-name">· {{ categoryName }}</text>
      </view>
    </view>
    <view class="reselect-btn" @click="$emit('reselect')">
      重选机构 <text class="iconfont icon-right"></text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  institution: Object,
  categoryName: String
});
defineEmits(['reselect']);
</script>

<style lang="scss" scoped>
.context-card {
  background-color: $bg-white;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .info-group {
    display: flex;
    align-items: center;
  }
  .inst-logo {
    width: 56rpx;
    height: 56rpx;
    border-radius: 12rpx;
    margin-right: 16rpx;
  }
  .inst-name {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-main;
  }
  .cate-name {
    font-size: 26rpx;
    color: $text-muted;
    margin-left: 8rpx;
  }
  .reselect-btn {
    font-size: 26rpx;
    color: $primary;
    display: flex;
    align-items: center;
  }
}
</style>