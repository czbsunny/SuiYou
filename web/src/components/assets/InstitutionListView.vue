<!-- components/assets/InstitutionListView.vue -->
<template>
  <view class="institution-view">
    <view 
      v-for="inst in list" 
      :key="inst.instCode" 
      class="inst-card" 
      :class="{ 'is-expanded': isExpanded(inst.instCode) }"
    >
      <!-- 1. 第一层：机构头部 -->
      <view class="card-header" @tap="toggle(inst.instCode)">
        <view class="logo-wrapper">
          <image :src="inst.logoUrl || '/static/icons/default.png'" mode="aspectFit" class="inst-logo" />
        </view>
        <view class="inst-info">
          <text class="inst-name">{{ inst.instName }}</text>
          <text class="inst-count">共 {{ inst.accounts?.length || 0 }} 个账户，{{ getAssetCount(inst) }} 项资产</text>
        </view>
        <view class="inst-amount-box">
            <text class="amt">¥ {{ formatAmount(inst.total) }}</text>
            <view class="arrow-icon" :class="{ 'rotate': isExpanded(inst.instCode) }"></view>
        </view>
      </view>

      <!-- 2. 展开的账户及资产列表 -->
      <view class="details-container" :style="{ maxHeight: isExpanded(inst.instCode) ? '3000rpx' : '0' }">
        <view class="details-inner">
          
          <view v-for="acc in inst.accounts" :key="acc.name" class="account-group">
            <!-- 第二层：账户行 (始终展示) -->
            <view class="acc-row" @tap="$emit('item-click', acc.items[0])">
              <view class="acc-left">
                <view class="acc-badge">卡</view>
                <view class="acc-title-area">
                  <text class="acc-name">{{ acc.name }}</text>
                </view>
              </view>
              <view class="acc-right">
                <text class="acc-amt">¥ {{ formatAmount(acc.totalBalance) }}</text>
              </view>
            </view>

            <!-- 第三层：具体资产项 - 仅在明细模式下展示 -->
            <view class="asset-list" v-if="mode === 'detailed'">
              <view 
                v-for="asset in acc.items" 
                :key="asset.id" 
                class="asset-item"
                @tap.stop="$emit('item-click', asset)"
              >
                <view class="asset-icon-box" :style="{ backgroundColor: getCatColor(asset.category) }">
                  <image :src="getSubCatIcon(asset.category, asset.subCategory)" class="sub-icon" mode="aspectFit" />
                </view>

                <view class="asset-info">
                  <text class="asset-type-name">{{ getSubCatName(asset.category, asset.subCategory) }}</text>
                  <text class="asset-remark">{{ asset.name }}</text>
                </view>

                <view class="asset-right">
                  <text class="asset-amt">¥ {{ formatAmount(asset.totalBalance) }}</text>
                  <uni-icons type="right" size="10" color="#E5E7EB" />
                </view>
              </view>
            </view>
          </view>
          
          <!-- 添加引导按钮仅在明细模式下展示 -->
          <view v-if="mode === 'detailed'" class="add-guide-row" @tap.stop="$emit('add-click', '', inst.instCode)">
            <view class="add-guide-inner">
              <uni-icons type="plus" size="14" color="#9CA3AF" />
              <text>在 {{ inst.instName }} 添加资产</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 无数据兜底 -->
    <view v-if="list.length === 0" class="empty-tip">
      <image src="/static/images/empty-asset.png" class="empty-img" />
      <text>暂无持仓机构</text>
    </view>
  </view>
</template>

<script setup>
import { useConfigStore } from '@/stores/config.js';

const props = defineProps({ 
  list: { type: Array, default: () => [] },
  mode: { type: String, default: 'detailed' },
  expandedCodes: { type: Array, default: () => [] }
});

const emit = defineEmits(['item-click', 'add-click', 'update:expandedCodes']);
const configStore = useConfigStore();

const toggle = (code) => {
  // 复制一份数组进行操作（遵循单向数据流）
  let newCodes = [...props.expandedCodes];
  const index = newCodes.indexOf(code);
  
  if (index > -1) newCodes.splice(index, 1);
  else newCodes.push(code);
  
  // 发送给父组件更新
  emit('update:expandedCodes', newCodes);
};

// 判断是否展开
const isExpanded = (code) => props.expandedCodes.includes(code);

// --- 工具函数 ---
const getSubCatIcon = (catCode, subCode) => {
  const sub = configStore.getSubCategoriesByCode(catCode).find(s => s.categoryCode === subCode);
  return sub ? sub.iconUrl : '/static/icons/default-asset.png';
};

const getCatColor = (catCode) => {
  return configStore.assetCategories.find(c => c.categoryCode === catCode)?.color || '#E5E7EB';
};

const getAssetCount = (inst) => inst.accounts?.reduce((sum, acc) => sum + (acc.items?.length || 0), 0);
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const getSubCatName = (catCode, subCode) => {
  const sub = configStore.getSubCategoriesByCode(catCode).find(s => s.categoryCode === subCode);
  return sub ? sub.name : '普通资产';
};
</script>

<style lang="scss" scoped>
/* 样式部分保持一致 */
.inst-card {
  background: #ffffff; border-radius: 40rpx; margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02); transition: all 0.3s ease;
  &.is-expanded { box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.04); }
}

.card-header {
  padding: 36rpx 32rpx; display: flex; align-items: center;
  .logo-wrapper { 
    width: 88rpx; height: 88rpx; border-radius: 24rpx; background: #F9FAFB;
    display: flex; align-items: center; justify-content: center; margin-right: 24rpx; flex-shrink: 0;
    .inst-logo { width: 56rpx; height: 56rpx; }
  }
  
  .inst-info { 
    flex: 1; 
    display: flex;
    flex-direction: column; 
    justify-content: center;
    .inst-name { font-size: 32rpx; font-weight: 800; color: #1F2937; line-height: 1.2; } 
    .inst-count { font-size: 22rpx; color: #9CA3AF; margin-top: 6rpx; font-weight: 500; } 
  }

  .inst-amount-box {
    display: flex; align-items: center; gap: 16rpx; flex-shrink: 0;
    .amt { font-size: 34rpx; font-weight: 700; font-family: 'DIN Alternate'; color: #1F2937; }
    .arrow-icon { width: 12rpx; height: 12rpx; border-bottom: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB; transform: rotate(45deg); transition: 0.3s; &.rotate { transform: rotate(-135deg); margin-top: 8rpx; } }
  }
}

.details-container { background-color: #FAFBFC; overflow: hidden; transition: max-height 0.4s ease; }

.account-group {
  margin-bottom: 10rpx;
  .acc-row {
    padding: 24rpx 32rpx; display: flex; justify-content: space-between; align-items: center;
    background-color: rgba(0,0,0,0.02);
    .acc-left { 
      display: flex; align-items: center;
      .acc-badge { font-size: 18rpx; color: #2A806C; background: rgba(42, 128, 108, 0.1); width: 32rpx; height: 32rpx; border-radius: 8rpx; display: flex; align-items: center; justify-content: center; margin-right: 16rpx; font-weight: bold; }
      .acc-title-area { 
        display: flex; align-items: center;
        .acc-name { font-size: 28rpx; font-weight: 800; color: #374151; } 
      }
    }
    .acc-amt { font-size: 28rpx; font-weight: 700; font-family: 'DIN Alternate'; color: #4B5563; }
  }
}

.asset-item {
  padding: 24rpx 32rpx 24rpx 48rpx; 
  display: flex; align-items: center;
  position: relative;

  .asset-icon-box {
    width: 56rpx; height: 56rpx; border-radius: 8rpx;
    margin-right: 20rpx; flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
    position: relative;
    &::after { content: ''; position: absolute; inset: 0; background: rgba(255,255,255,0.8); border-radius: inherit; }
    .sub-icon { width: 56rpx; height: 56rpx; z-index: 1; }
  }

  .asset-info {
    flex: 1; min-width: 0;
    .asset-type-name { font-size: 26rpx; color: #4B5563; font-weight: 700; display: block; margin-bottom: 4rpx; }
    .asset-remark { font-size: 20rpx; color: #9CA3AF; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }

  .asset-right {
    display: flex; align-items: center; gap: 8rpx;
    .asset-amt { font-size: 28rpx; font-weight: 700; color: #4B5563; font-family: 'DIN Alternate'; }
  }

  &::before {
    content: ''; position: absolute; left: 32rpx; top: -24rpx; bottom: 50%;
    width: 2rpx; border-left: 2rpx dashed #E5E7EB;
  }
}

.add-guide-row { padding: 32rpx; .add-guide-inner { height: 80rpx; border: 2rpx dashed #E5E7EB; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; gap: 12rpx; text { font-size: 24rpx; color: #9CA3AF; font-weight: 600; } } }

.empty-tip {
  padding: 120rpx 0; text-align: center;
  .empty-img { width: 200rpx; height: 200rpx; opacity: 0.5; margin-bottom: 20rpx; }
  text { color: #9CA3AF; font-size: 26rpx; display: block; }
}
</style>