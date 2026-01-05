<!-- components/assets/CategoryListView.vue -->
<template>
  <view class="category-view">
    <view 
      v-for="cat in list" 
      :key="cat.categoryCode" 
      class="cat-card" 
      :class="{ 'is-expanded': isExpanded(cat.categoryCode) }"
    >
      <!-- 1. 第一层：大分类头部 -->
      <view class="card-header" @tap="toggle(cat.categoryCode)">
        <view class="icon-box" :style="{ backgroundColor: cat.color }">
          <image :src="cat.iconUrl" mode="aspectFit" class="cat-icon" />
        </view>
        
        <view class="cat-info">
          <text class="cat-name">{{ cat.name }}</text>
          <text class="cat-count">{{ cat.items.length }}项资产</text>
        </view>

        <view class="cat-right">
          <text class="cat-amount">¥ {{ formatAmount(cat.totalBalance) }}</text>
          <view class="arrow-icon" :class="{ 'rotate': isExpanded(cat.categoryCode) }"></view>
        </view>
      </view>

      <!-- 2. 展开区域 -->
      <view 
        class="details-container" 
        :style="{ maxHeight: isExpanded(cat.categoryCode) ? '3000rpx' : '0' }"
      >
        <view class="details-inner">
          <view 
            v-for="(group, subCode) in groupItemsBySub(cat.items)" 
            :key="subCode" 
            class="sub-section"
          >
            <!-- 第二层：二级分类标题 -->
            <view class="sub-header-bar">
              <view class="sub-left">
                <view class="sub-indicator"></view>
                <text class="sub-title">{{ getSubCatName(cat.categoryCode, subCode) }}</text>
              </view>
              <text class="sub-total">¥ {{ formatAmount(calculateSubTotal(group)) }}</text>
            </view>

            <!-- 第三层：具体明细 -->
            <view class="l3-group" v-if="mode === 'detailed'">
              <view 
                v-for="item in group" 
                :key="item.id" 
                class="detail-item" 
                @tap="$emit('item-click', item)"
              >
                <view class="item-logo-box">
                  <image 
                    :src="item.instInfo?.logoUrl || '/static/icons/default-bank.png'" 
                    class="item-logo" 
                    mode="aspectFit" 
                  />
                </view>
                
                <view class="item-info">
                  <text class="item-main-row">
                    {{ item.instInfo?.shortName || '未知机构' }}
                    <text class="item-id-tag" v-if="item.institutionIdentifier">({{item.institutionIdentifier}})</text>
                  </text>
                  <text class="item-sub-row">{{ item.accountName || "默认账户" }}</text>
                </view>
                
                <view class="item-right">
                  <text class="item-amt">¥ {{ formatAmount(item.totalBalance) }}</text>
                  <uni-icons type="right" size="10" color="#D1D5DB" />
                </view>
              </view>
            </view>
          </view>
          
          <view v-if="mode === 'detailed'" class="add-guide-row" @tap.stop="$emit('add-click', cat.categoryCode)">
            <view class="add-guide-inner">
              <uni-icons type="plus" size="14" color="#9CA3AF" />
              <text>添加{{ cat.name }}明细</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();

// 接收父组件传入的参数
const props = defineProps({
  list: { type: Array, default: () => [] },
  mode: { type: String, default: 'detailed' },
  // 新增：接收父组件当前的“一键展开/收起”状态
  defaultExpandAll: { type: Boolean, default: true }
});

const emit = defineEmits(['item-click', 'add-click', 'sync-expand-status']);

// 1. 初始化展开数组为空
const expandedCodes = ref([]);

// 判断是否展开
const isExpanded = (code) => expandedCodes.value.includes(code);

/**
 * 进阶优化：监听 list 的变化
 * 当切换视图（v-if 切换）或数据重载时，根据父组件的意图初始化展开状态
 */
watch(() => props.list, (newList) => {
  if (newList && newList.length > 0) {
    applyExpandStrategy(props.defaultExpandAll);
  }
}, { immediate: true });

/**
 * 切换逻辑处理
 */
const toggle = (code) => {
  const index = expandedCodes.value.indexOf(code);
  if (index > -1) {
    expandedCodes.value.splice(index, 1);
  } else {
    expandedCodes.value.push(code);
  }
  
  // 进阶优化：回传状态同步。如果全部手动收起了，通知父组件修改按钮文字
  if (expandedCodes.value.length === 0) {
    emit('sync-expand-status', false);
  } else if (expandedCodes.value.length === props.list.length) {
    emit('sync-expand-status', true);
  }
};

/**
 * 执行展开/收起策略
 */
const applyExpandStrategy = (isExpand) => {
  if (isExpand) {
    expandedCodes.value = props.list.map(cat => cat.categoryCode);
  } else {
    expandedCodes.value = [];
  }
};

/**
 * 暴露给父组件的主动调用方法
 */
const toggleAll = (isExpand) => {
  applyExpandStrategy(isExpand);
};

defineExpose({ toggleAll });

// --- 数据处理辅助方法 ---
const groupItemsBySub = (items) => {
  if (!items) return {};
  return items.reduce((groups, item) => {
    const sub = item.subCategory || 'OTHER';
    if (!groups[sub]) groups[sub] = [];
    groups[sub].push(item);
    return groups;
  }, {});
};

const calculateSubTotal = (group) => group.reduce((sum, item) => sum + Number(item.totalBalance || 0), 0);
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const getSubCatName = (catCode, subCode) => {
  const sub = configStore.getSubCategoriesByCode(catCode).find(s => s.categoryCode === subCode);
  return sub ? sub.name : '其他明细';
};
</script>

<style lang="scss" scoped>
/* 保持原样，无需修改 */
.cat-card {
  background: #ffffff; border-radius: 40rpx; margin-bottom: 24rpx;
  overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  &.is-expanded { box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.06); }
}

.card-header {
  padding: 36rpx 32rpx; 
  display: flex; 
  align-items: center;

  .icon-box { 
    width: 96rpx; height: 96rpx; border-radius: 24rpx; 
    display: flex; align-items: center; justify-content: center; 
    margin-right: 24rpx; flex-shrink: 0;
    box-shadow: inset 0 0 10rpx rgba(0,0,0,0.05);
    .cat-icon { width: 72rpx; height: 72rpx; }
  }

  .cat-info { 
    flex: 1; 
    display: flex;
    flex-direction: column; 
    justify-content: center;
    min-width: 0;

    .cat-name { 
      font-size: 32rpx; 
      font-weight: 800; 
      color: #1F2937; 
      margin-bottom: 4rpx;
    } 
    .cat-count { 
      font-size: 22rpx; 
      color: #9CA3AF; 
      font-weight: 500;
    } 
  }

  .cat-right { 
    display: flex; 
    align-items: center; 
    gap: 16rpx; 
    flex-shrink: 0;

    .cat-amount { 
      font-size: 36rpx; 
      font-weight: 800; 
      font-family: 'DIN Alternate', sans-serif; 
      color: #111827;
    } 
    .arrow-icon { 
      width: 12rpx; height: 12rpx; border-bottom: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB; 
      transform: rotate(45deg); transition: 0.3s; 
      &.rotate { transform: rotate(-135deg); margin-top: 8rpx; } 
    } 
  }
}

.details-container { background-color: #FAFBFC; overflow: hidden; transition: max-height 0.4s ease; }

.sub-section {
  margin-top: 10rpx;
  padding-bottom: 16rpx; 
  
  .sub-header-bar {
    background: rgba(0, 0, 0, 0.03);
    margin: 0 24rpx; padding: 16rpx 24rpx; border-radius: 12rpx;
    display: flex; align-items: center; justify-content: space-between;

    .sub-left {
      display: flex; align-items: center;
      .sub-indicator { width: 8rpx; height: 8rpx; background: #2A806C; border-radius: 50%; margin-right: 12rpx; }
      .sub-title { font-size: 28rpx; font-weight: 800; color: #374151; }
    }
    .sub-total { font-size: 28rpx; color: #64748B; font-weight: 800; font-family: 'DIN Alternate'; }
  }
}

.l3-group { padding-left: 32rpx; }

.detail-item {
  padding: 28rpx 32rpx 28rpx 16rpx; 
  display: flex; 
  align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.02);
  
  &:active { background-color: #F3F4F6; }

  .item-logo-box {
    width: 64rpx; height: 64rpx;
    background: #fff; margin-right: 24rpx; flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
    border: 1rpx solid #F3F4F6;
    .item-logo { width: 64rpx; height: 64rpx; border-radius: 8rpx }
  }

  .item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .item-main-row { 
      font-size: 28rpx; 
      font-weight: 700; 
      color: #374151; 
      margin-bottom: 6rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .item-id-tag {
        font-size: 24rpx;
        color: #9CA3AF;
        font-weight: 500;
        margin-left: 4rpx;
      }
    }
    .item-sub-row { 
      font-size: 22rpx; 
      color: #9CA3AF; 
      font-weight: 500;
    }
  }

  .item-right {
    display: flex; 
    align-items: center; 
    gap: 8rpx;
    flex-shrink: 0;

    .item-amt { 
      font-size: 30rpx; 
      font-weight: 700; 
      font-family: 'DIN Alternate'; 
      color: #1F2937; 
    }
  }
}

.add-guide-row { padding: 32rpx 40rpx; .add-guide-inner { height: 84rpx; border: 2rpx dashed #E5E7EB; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; gap: 12rpx; text { font-size: 26rpx; color: #9CA3AF; font-weight: 600; } } }
</style>