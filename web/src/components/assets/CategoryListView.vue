<!-- components/assets/CategoryListView.vue -->
<template>
  <view class="category-view-container">
    <!-- 1. 顶部标题区 (保持与机构视图一致) -->
    <view class="view-header">
      <text class="view-title">我的资产</text>
      <!-- 🟢 占位对齐组：保持结构与 InstitutionListView 一致 -->
      <view class="action-group-placeholder">
        <!-- 保持空的，或者只显示一个简单的全局加号 -->
        <view class="icon-btn-ghost"></view>
      </view>
    </view>

    <!-- 2. 分类卡片列表 -->
    <view class="category-list">
      <view 
        v-for="cat in list" 
        :key="cat.categoryCode" 
        class="cat-card" 
        :class="{ 'is-expanded': expandedCode === cat.categoryCode }"
      >
        <!-- 第一层：大分类头部 -->
        <view class="card-header" @tap="handleToggle(cat.categoryCode)">
          <view class="icon-box" :style="{ backgroundColor: cat.color }">
            <image :src="cat.iconUrl" mode="aspectFit" class="cat-icon" />
          </view>
          
          <view class="cat-info">
            <text class="cat-name">{{ cat.name }}</text>
            <text class="cat-count">{{ cat.items.length }}项资产</text>
          </view>

          <view class="cat-right">
            <text class="cat-amount">¥ {{ formatAmount(cat.totalBalance) }}</text>
            <view class="arrow-icon" :class="{ 'rotate': expandedCode === cat.categoryCode }"></view>
          </view>
        </view>

        <!-- 第二层：展开区域 (手风琴效果) -->
        <view 
          class="details-container" 
          :style="{ maxHeight: expandedCode === cat.categoryCode ? calculateHeight(cat.items) : '0' }"
        >
          <view class="details-inner">
            <view 
              v-for="(group, subCode) in groupItemsBySub(cat.items)" 
              :key="subCode" 
              class="sub-section"
            >
              <!-- 细分分组标题 -->
              <view class="sub-header-bar">
                <view class="sub-left">
                  <view class="sub-indicator" :style="{ background: cat.color }"></view>
                  <text class="sub-title">{{ getSubCatName(cat.categoryCode, subCode) }}</text>
                </view>
                <text class="sub-total">¥ {{ formatAmount(calculateSubTotal(group)) }}</text>
              </view>

              <!-- 第三层：具体资产明细 -->
              <view class="item-group">
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
                    <view class="item-main-row">
                      <text class="item-name">{{ item.name || item.instInfo?.instName }}</text>
                      <text class="item-id-tag" v-if="item.institutionIdentifier">({{item.institutionIdentifier}})</text>
                    </view>
                    <text class="item-sub-row">{{ item.instInfo?.instName || "外部账户" }}</text>
                  </view>
                  
                  <view class="item-right">
                    <text class="item-amt">¥ {{ formatAmount(item.totalBalance) }}</text>
                    <uni-icons type="right" size="10" color="#D1D5DB" />
                  </view>
                </view>
              </view>
            </view>
            
            <!-- 添加引导按钮 -->
            <view class="add-guide-row" @tap.stop="$emit('add-click', cat.categoryCode)">
              <view class="add-guide-inner">
                <image src="/static/images/plus-gray.png" class="plus-mini" mode="aspectFit" />
                <text>添加{{ cat.name }}项</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <view class="safe-bottom-pad"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();

const props = defineProps({
  list: { type: Array, default: () => [] }
});

const emit = defineEmits(['item-click', 'add-click']);

// 🟢 核心逻辑：改为单个 String 记录当前展开的 Code
const expandedCode = ref(''); // 默认展开流动资产

const handleToggle = (code) => {
  // 手风琴逻辑：点击已展开的则关闭，点击其他的则切换
  expandedCode.value = expandedCode.value === code ? '' : code;
};

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

// 预估高度计算（确保动画顺滑）
const calculateHeight = (items) => {
  if (!items) return '0';
  // 估算逻辑：子标题高度 + (项目数 * 项目高度) + 底部按钮高度
  return (items.length * 150 + 300) + 'rpx';
};

const calculateSubTotal = (group) => group.reduce((sum, item) => sum + Number(item.totalBalance || 0), 0);
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const getSubCatName = (catCode, subCode) => {
  const sub = configStore.getSubCategoriesByCode(catCode).find(s => s.categoryCode === subCode);
  return sub ? sub.name : '其他明细';
};
</script>

<style lang="scss" scoped>
.category-view-container {
  padding: 0 8rpx;
}

/* 🟢 视图标题样式 (与 InstitutionListView 保持高度一致) */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100rpx;      /* 必须与上面一致 */
  padding: 0 8rpx;
  margin-bottom: 10rpx;
  
  .view-title {
    font-size: 36rpx;
    font-weight: 800;
    color: #1F2937;
    letter-spacing: 2rpx;
    line-height: 1;
  }

  /* 🟢 关键：右侧占位，高度与按钮组一致，宽度也一致(60*2 + 16gap = 136rpx) */
  .action-group-placeholder {
    width: 136rpx; 
    height: 60rpx;
    visibility: hidden; /* 不可见但占据空间 */
  }
}

.cat-card {
  background: #ffffff; border-radius: 44rpx; margin-bottom: 24rpx;
  overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.02);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  &.is-expanded { 
    box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.04); 
  }
}

.card-header {
  padding: 36rpx 32rpx; 
  display: flex; 
  align-items: center;

  .icon-box { 
    width: 88rpx; height: 88rpx; border-radius: 28rpx; 
    display: flex; align-items: center; justify-content: center; 
    margin-right: 24rpx; flex-shrink: 0;
    .cat-icon { 
      width: 48rpx; height: 48rpx; 
      filter: brightness(0) invert(1); // 这里的滤镜取决于你的图标素材是否需要反白
    }
  }

  .cat-info { 
    flex: 1; 
    .cat-name { font-size: 32rpx; font-weight: 800; color: #1F2937; display: block; } 
    .cat-count { font-size: 22rpx; color: #9CA3AF; font-weight: 500; margin-top: 4rpx; } 
  }

  .cat-right { 
    display: flex; align-items: center; gap: 20rpx;
    .cat-amount { font-size: 32rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; color: #111827; } 
    .arrow-icon { 
      width: 12rpx; height: 12rpx; border-bottom: 4rpx solid #D1D5DB; border-right: 4rpx solid #D1D5DB; 
      transform: rotate(45deg); transition: 0.3s; 
      &.rotate { transform: rotate(-135deg); margin-top: 8rpx; } 
    } 
  }
}

.details-container { background-color: #F9FAFB; overflow: hidden; transition: max-height 0.5s ease; }

.sub-section {
  padding-top: 10rpx;
  
  .sub-header-bar {
    margin: 10rpx 24rpx; padding: 12rpx 24rpx; 
    display: flex; align-items: center; justify-content: space-between;

    .sub-left {
      display: flex; align-items: center;
      .sub-indicator { width: 4rpx; height: 20rpx; border-radius: 4rpx; margin-right: 12rpx; }
      .sub-title { font-size: 22rpx; font-weight: 700; color: #9CA3AF; text-transform: uppercase; letter-spacing: 1rpx; }
    }
    .sub-total { font-size: 22rpx; color: #9CA3AF; font-weight: 700; font-family: 'DIN Alternate'; }
  }
}

.detail-item {
  padding: 24rpx 32rpx; display: flex; align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.02);
  &:active { background-color: #F3F4F6; }

  .item-logo-box {
    width: 64rpx; height: 64rpx; border-radius: 16rpx;
    background: #fff; margin-right: 20rpx; flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.02);
    .item-logo { width: 44rpx; height: 44rpx; }
  }

  .item-info {
    flex: 1;
    .item-main-row { 
      font-size: 28rpx; font-weight: 700; color: #374151; display: block; 
      .item-id-tag { font-size: 24rpx; color: #9CA3AF; margin-left: 8rpx; font-weight: 400; }
    }
    .item-sub-row { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; display: block;}
  }

  .item-right {
    .item-amt { font-size: 28rpx; font-weight: 700; font-family: 'DIN Alternate'; color: #1F2937; }
  }
}

.add-guide-row {
  padding: 32rpx 40rpx;
  .add-guide-inner {
    height: 84rpx; border: 2rpx dashed #E5E7EB; border-radius: 24rpx;
    display: flex; align-items: center; justify-content: center; gap: 12rpx;
    background: rgba(255,255,255,0.5);
    .plus-mini { width: 24rpx; height: 24rpx; opacity: 0.4; }
    text { font-size: 24rpx; color: #9CA3AF; font-weight: 600; }
    &:active { background: #fff; border-color: #2D7A68; text { color: #2D7A68; } }
  }
}

.safe-bottom-pad { height: 100rpx; }
</style>