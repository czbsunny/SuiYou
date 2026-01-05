<!-- components/assets/CategoryListView.vue -->
<template>
  <view class="category-view">
    <view 
      v-for="cat in list" 
      :key="cat.categoryCode" 
      class="cat-card" 
      :class="{ expanded: expandedCode === cat.categoryCode }"
    >
      <!-- 1. 分类头部 -->
      <view class="card-header" @click="toggle(cat.categoryCode)">
        <view class="icon-box" :style="{ backgroundColor: cat.color }">
          <image :src="cat.iconUrl" mode="aspectFit" class="cat-icon" />
        </view>
        <view class="cat-info">
          <text class="cat-name">{{ cat.name }}</text>
          <text class="cat-count">{{ cat.items.length }}项资产</text>
        </view>
        <view class="cat-right">
          <text class="cat-amount">¥{{ formatAmount(cat.totalBalance) }}</text>
          <view class="arrow" :class="{ rotate: expandedCode === cat.categoryCode }"></view>
        </view>
      </view>

      <!-- 2. 展开的明细列表（优化重点） -->
      <view 
        class="details-container" 
        :style="{ maxHeight: expandedCode === cat.categoryCode ? (cat.items.length * 140 + 120) + 'rpx' : '0' }"
      >
        <view class="details-inner">
          <view 
            v-for="item in cat.items" 
            :key="item.id" 
            class="detail-item" 
            @click="$emit('item-click', item)"
          >
            <!-- 左侧：机构Logo -->
            <view class="item-logo-box">
              <image 
                :src="item.instInfo?.logoUrl || '/static/icons/default-bank.png'" 
                class="item-logo" 
                mode="aspectFit" 
              />
            </view>
            
            <!-- 中间：具体名称 + 账户描述 -->
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-sub-type">{{ getSubCatName(cat.categoryCode, item.subCategory) }}</text>
            </view>
            
            <!-- 右侧：金额 -->
            <view class="item-right">
              <text class="item-amt">¥{{ formatAmount(item.totalBalance) }}</text>
              <uni-icons type="chevron-right" size="12" color="#D1D5DB" />
            </view>
          </view>
          
          <!-- 3. 上下文添加按钮（更精致的虚线设计） -->
          <view class="add-guide-row" @click="$emit('add-click', cat.categoryCode)">
            <view class="add-guide-inner">
              <image src="/static/images/plus-gray.png" class="plus-mini" />
              <text>添加{{ cat.name }}项</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const props = defineProps(['list']);
const expandedCode = ref('LIQUID');

const toggle = (code) => {
  expandedCode.value = expandedCode.value === code ? '' : code;
};

const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

// 获取子类名称的逻辑
const getSubCatName = (catCode, subCode) => {
  const sub = configStore.getSubCategoriesByCode(catCode).find(s => s.categoryCode === subCode);
  return sub ? sub.name : '普通资产';
};
</script>

<style lang="scss" scoped>
.cat-card {
  background: #ffffff;
  border-radius: 40rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.card-header {
  padding: 32rpx;
  display: flex;
  align-items: center;
  
  .icon-box {
    width: 88rpx; height: 88rpx; border-radius: 28rpx;
    display: flex; align-items: center; justify-content: center;
    margin-right: 24rpx; position: relative;
    &::after { content: ''; position: absolute; inset: 0; opacity: 0.15; border-radius: inherit; background: inherit; }
    .cat-icon { width: 52rpx; height: 52rpx; z-index: 1; }
  }

  .cat-info {
    flex: 1;
    .cat-name { font-size: 32rpx; font-weight: 800; color: #1F2937; display: block; margin-bottom: 4rpx; }
    .cat-count { font-size: 22rpx; color: #9CA3AF; font-weight: 500; }
  }

  .cat-right {
    display: flex; align-items: center; gap: 20rpx;
    .cat-amount { font-size: 32rpx; font-weight: 700; font-family: 'DIN Alternate'; color: #1F2937; }
    .arrow {
      width: 10rpx; height: 10rpx; border-bottom: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB;
      transform: rotate(45deg); transition: 0.3s;
      &.rotate { transform: rotate(-135deg); margin-top: 8rpx; }
    }
  }
}

/* 🟢 明细列表容器优化 */
.details-container {
  background-color: #F9FAFB; // 使用浅灰色背景，营造内嵌感
  overflow: hidden;
  transition: max-height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.details-inner {
  padding: 10rpx 0 20rpx;
}

.detail-item {
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
  
  &:active { background-color: #F3F4F6; }

  .item-logo-box {
    width: 64rpx; height: 64rpx; border-radius: 16rpx;
    background: #fff; margin-right: 20rpx;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.02);
    .item-logo { width: 44rpx; height: 44rpx; }
  }

  .item-info {
    flex: 1;
    .item-name { font-size: 28rpx; font-weight: 600; color: #4B5563; display: block; }
    .item-sub-type { font-size: 20rpx; color: #9CA3AF; }
  }

  .item-right {
    display: flex; align-items: center; gap: 12rpx;
    .item-amt { font-size: 28rpx; font-weight: 700; font-family: 'Monaco', monospace; color: #1F2937; }
  }
}

/* 🟢 添加按钮优化 */
.add-guide-row {
  padding: 30rpx 40rpx;
  .add-guide-inner {
    height: 80rpx;
    border: 2rpx dashed #E5E7EB;
    border-radius: 20rpx;
    display: flex; align-items: center; justify-content: center; gap: 12rpx;
    background: rgba(255,255,255,0.5);
    
    image { width: 24rpx; height: 24rpx; opacity: 0.4; }
    text { font-size: 24rpx; color: #9CA3AF; font-weight: 600; }
    
    &:active { background: #fff; border-color: #2A806C; text { color: #2A806C; } }
  }
}
</style>