<!-- components/assets/CategoryListView.vue -->
<template>
  <view class="category-view">
    <view 
      v-for="cat in list" 
      :key="cat.categoryCode" 
      class="cat-card" 
      :class="{ expanded: expandedCode === cat.categoryCode }"
    >
      <view class="card-header" @click="toggle(cat.categoryCode)">
        <view class="icon-box" :style="{ backgroundColor: cat.color }">
          <image :src="cat.iconUrl" mode="aspectFit" class="cat-icon" />
        </view>
        <view class="cat-info">
          <text class="cat-name">{{ cat.name }}</text>
          <text class="cat-count">{{ cat.items.length }}项资产</text>
        </view>
        <view class="cat-amount">
          <text class="amt">¥{{ formatAmount(cat.totalBalance) }}</text>
          <view class="arrow" :class="{ rotate: expandedCode === cat.categoryCode }"></view>
        </view>
      </view>

      <view class="details-list" :style="{ maxHeight: expandedCode === cat.categoryCode ? (cat.items.length + 1) * 120 + 'rpx' : '0' }">
        <view v-for="item in cat.items" :key="item.id" class="detail-item" @click="$emit('item-click', item)">
          <view class="item-left">
            <image :src="item.instInfo?.logoUrl || '/static/icons/default.png'" class="item-logo" mode="aspectFit" />
            <text class="item-name">{{ item.name }}</text>
          </view>
          <text class="item-amt">¥{{ formatAmount(item.totalBalance) }}</text>
        </view>
        
        <!-- 上下文添加按钮 -->
        <view class="add-guide" @click="$emit('add-click', cat.categoryCode)">
          <image src="/static/images/plus-gray.png" class="plus-mini" />
          <text>添加{{ cat.name }}项</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
const props = defineProps(['list']);
const expandedCode = ref(''); // 默认展开流动资产

const toggle = (code) => {
  expandedCode.value = expandedCode.value === code ? '' : code;
};
const formatAmount = (val) => Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
</script>

<style lang="scss" scoped>
.cat-card {
  background: #fff; border-radius: 40rpx; margin-bottom: 24rpx;
  overflow: hidden; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
  transition: all 0.3s ease;
}
.card-header {
  padding: 32rpx; display: flex; align-items: center;
  .icon-box {
    width: 80rpx; height: 80rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center;
    margin-right: 24rpx; position: relative;
    &::after { content: ''; position: absolute; inset: 0; opacity: 0.15; border-radius: inherit; background: inherit; }
    .cat-icon { width: 48rpx; height: 48rpx; z-index: 1; }
  }
  .cat-info { flex: 1; .cat-name { font-size: 30rpx; font-weight: 800; color: #1F2937; display: block; } .cat-count { font-size: 22rpx; color: #9CA3AF; } }
  .cat-amount { text-align: right; .amt { font-size: 30rpx; font-weight: 700; font-family: 'DIN Alternate'; display: block; } }
  .arrow { width: 12rpx; height: 12rpx; border-top: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB; transform: rotate(135deg); margin: 10rpx auto 0; transition: 0.3s; &.rotate { transform: rotate(-45deg); margin-top: 20rpx; } }
}
.details-list { background: #FAFAFA; overflow: hidden; transition: max-height 0.4s ease; }
.detail-item {
  padding: 24rpx 32rpx; display: flex; justify-content: space-between; align-items: center; border-bottom: 1rpx solid rgba(0,0,0,0.03);
  .item-left { display: flex; align-items: center; gap: 20rpx; .item-logo { width: 44rpx; height: 44rpx; border-radius: 12rpx; } .item-name { font-size: 26rpx; color: #4B5563; } }
  .item-amt { font-size: 26rpx; font-weight: 700; font-family: monospace; }
}
.add-guide {
  padding: 30rpx; display: flex; align-items: center; justify-content: center; gap: 10rpx;
  color: #9CA3AF; font-size: 24rpx; font-weight: 600;
  .plus-mini { width: 24rpx; height: 24rpx; opacity: 0.4; }
}
</style>