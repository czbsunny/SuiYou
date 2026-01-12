<!-- components/assets/InstitutionListView.vue -->
<template>
  <view class="institution-view">
    <view 
      v-for="sector in list" 
      :key="sector.type" 
      class="sector-card" 
      :class="{ 'is-expanded': isExpanded(sector.type) }"
    >
      <!-- 1. 第一层：业态头部 (复刻资产大类样式) -->
      <view class="card-header" @tap="toggle(sector.type)">
        <view class="icon-box" :style="{ backgroundColor: sector.color }">
          <!-- 业态图标 -->
          <image :src="sector.iconUrl" mode="aspectFit" class="sector-icon" />
        </view>
        <view class="sector-info">
          <text class="sector-name">{{ sector.name }}</text>
          <text class="sector-count">{{ sector.accounts.length }}个账户</text>
        </view>
        <view class="sector-right">
          <text class="sector-amount">¥ {{ formatAmount(sector.totalBalance) }}</text>
          <view class="arrow-icon" :class="{ 'rotate': isExpanded(sector.type) }"></view>
        </view>
      </view>

      <!-- 2. 展开区域 -->
      <view 
        class="details-container" 
        :style="{ maxHeight: isExpanded(sector.type) ? '3000rpx' : '0' }"
      >
        <view class="details-inner">
          <!-- 第二层：具体账户循环 -->
          <view v-for="acc in sector.accounts" :key="acc.id" class="acc-section">
            <view class="acc-item" @tap="$emit('account-click', acc)">
              <view class="item-logo-box">
                <image :src="acc.logoUrl" class="item-logo" mode="aspectFit" />
              </view>
              <view class="item-info">
                <view class="item-main-row">
                  <text class="item-name">{{ acc.name }}</text>
                  <text class="item-id" v-if="acc.identifier">({{ acc.identifier }})</text>
                </view>
                <text class="item-sub-row">{{ acc.subText }}</text>
              </view>
              <view class="item-right">
                <text class="item-amt">¥ {{ formatAmount(acc.totalBalance) }}</text>
              </view>
            </view>

            <!-- 第三层：账户下的资产明细 (仅明细模式展示) -->
            <view class="l3-group" v-if="mode === 'detailed'">
              <view 
                v-for="asset in acc.items" 
                :key="asset.id" 
                class="asset-detail-row"
                @tap.stop="$emit('item-click', asset)"
              >
                <view class="asset-line"></view>
                <view class="asset-info">
                  <text class="asset-name">{{ getSubCatName(asset.category, asset.subCategory) }}</text>
                </view>
                <view class="asset-right">
                  <text class="asset-amt">{{ formatAmount(asset.totalBalance) }}</text>
                  <uni-icons type="right" size="10" color="#D1D5DB" />
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useConfigStore } from '@/stores/config.js';

const props = defineProps({
  list: Array,
  mode: String,
  expandedTypes: Array
});

const emit = defineEmits(['update:expandedTypes', 'item-click']);
const configStore = useConfigStore();

const toggle = (type) => {
  let newTypes = [...props.expandedTypes];
  const idx = newTypes.indexOf(type);
  if (idx > -1) newTypes.splice(idx, 1);
  else newTypes.push(type);
  emit('update:expandedTypes', newTypes);
};

const isExpanded = (type) => props.expandedTypes.includes(type);

const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const getSubCatName = (cat, sub) => configStore.getSubCategoriesByCode(cat).find(s => s.categoryCode === sub)?.name || '明细资产';
</script>

<style lang="scss" scoped>
/* 🟢 复刻资产类别的卡片样式 */
.sector-card {
  background: #ffffff; border-radius: 40rpx; margin-bottom: 24rpx;
  overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  &.is-expanded { box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.06); }
}

.card-header {
  padding: 36rpx 32rpx; display: flex; align-items: center;
  .icon-box { 
    width: 88rpx; height: 88rpx; border-radius: 24rpx; flex-shrink: 0;
    display: flex; align-items: center; justify-content: center; margin-right: 24rpx;
    .sector-icon { 
      width: 56rpx; 
      height: 56rpx; 
      filter: brightness(0) invert(1);
      opacity: 1;
      transform: scale(1.05);
    }
  }
  .sector-info { 
    flex: 1; display: flex; flex-direction: column;
    .sector-name { font-size: 32rpx; font-weight: 800; color: #1F2937; }
    .sector-count { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; }
  }
  .sector-right {
    display: flex; align-items: center; gap: 16rpx;
    .sector-amount { font-size: 36rpx; font-weight: 800; font-family: 'DIN Alternate'; color: #111827; }
    .arrow-icon { 
      width: 12rpx; height: 12rpx; border-bottom: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB; 
      transform: rotate(45deg); transition: 0.3s; &.rotate { transform: rotate(-135deg); margin-top: 8rpx; } 
    }
  }
}

.details-container { background-color: #FAFBFC; overflow: hidden; transition: max-height 0.4s ease; }

/* 🟢 L2 账户行样式 (复刻你图片中工商银行那行的质感) */
.acc-section { border-bottom: 1rpx solid rgba(0, 0, 0, 0.02); }
.acc-item {
  padding: 28rpx 32rpx; display: flex; align-items: center;
  &:active { background: #F3F4F6; }
  .item-logo-box {
    width: 64rpx; height: 64rpx; border-radius: 12rpx; background: #fff;
    margin-right: 20rpx; flex-shrink: 0; display: flex; align-items: center; justify-content: center;
    border: 1rpx solid #F3F4F6;
    .item-logo { width: 44rpx; height: 44rpx; }
  }
  .item-info {
    flex: 1; display: flex; flex-direction: column; min-width: 0;
    .item-main-row {
      display: flex; align-items: center; gap: 8rpx;
      .item-name { font-size: 28rpx; font-weight: 700; color: #374151; }
      .item-id { font-size: 24rpx; color: #9CA3AF; font-weight: 500; }
    }
    .item-sub-row { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; }
  }
  .item-right {
    .item-amt { font-size: 30rpx; font-weight: 700; font-family: 'DIN Alternate'; color: #1F2937; }
  }
}

/* 🟢 L3 资产明细样式 (缩进设计) */
.l3-group { padding-left: 116rpx; padding-bottom: 10rpx; }
.asset-detail-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16rpx 32rpx 16rpx 0; position: relative;
  .asset-line { 
    position: absolute; left: -32rpx; top: 0; bottom: 0; width: 2rpx; 
    border-left: 2rpx dashed #E5E7EB; 
  }
  .asset-info {
    .asset-name { font-size: 24rpx; color: #6B7280; font-weight: 500; }
  }
  .asset-right {
    display: flex; align-items: center; gap: 8rpx;
    .asset-amt { font-size: 24rpx; color: #6B7280; font-family: 'Monaco'; font-weight: 600; }
  }
}
</style>