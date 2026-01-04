<!-- components/assets/InstitutionListView.vue -->
<template>
  <view class="institution-view">
    <view 
      v-for="inst in list" 
      :key="inst.instCode" 
      class="inst-card" 
      :class="{ expanded: expandedInstCode === inst.instCode }"
    >
      <!-- 1. 机构卡片头部：展示汇总信息 -->
      <view class="card-header" @click="toggle(inst.instCode)">
        <view class="logo-wrapper">
          <image :src="inst.logoUrl || '/static/icons/default.png'" mode="aspectFit" class="inst-logo" />
        </view>
        <view class="inst-info">
          <text class="inst-name">{{ inst.instName }}</text>
          <text class="inst-count">{{ 2 }}个账户，{{ inst.accounts.length }} 项资产</text>
        </view>
        <view class="inst-amount">
            <text class="amt">¥{{ formatAmount(inst.total) }}</text>
            <view class="arrow" :class="{ rotate: expandedInstCode === inst.instCode }"></view>
        </view>
      </view>

      <!-- 2. 展开的账户明细列表 -->
      <view 
        class="details-list" 
        :style="{ maxHeight: expandedInstCode === inst.instCode ? (inst.accounts.length + 1) * 120 + 'rpx' : '0' }"
      >
        <view 
          v-for="acc in inst.accounts" 
          :key="acc.id" 
          class="acc-item" 
          @click="$emit('item-click', acc)"
        >
          <view class="acc-left">
            <view class="acc-name-group">
              <text class="acc-name">{{ acc.name }}</text>
              <!-- 核心：显示该账户属于哪个资产类别 -->
              <text class="cat-tag">{{ getCatName(acc.category) }}</text>
            </view>
          </view>
          <view class="acc-right">
            <text class="acc-amt">¥{{ formatAmount(acc.totalBalance) }}</text>
            <uni-icons type="chevron-right" size="12" color="#D1D5DB" />
          </view>
        </view>
        
        <!-- 3. 上下文添加按钮：点击直接带入该机构编码 -->
        <view class="add-guide" @click="$emit('add-click', '', inst.instCode)">
          <image src="/static/images/plus-gray.png" class="plus-mini" />
          <text>在{{ inst.instName }}添加资产</text>
        </view>
      </view>
    </view>

    <!-- 无数据兜底 -->
    <view v-if="list.length === 0" class="empty-tip">
      暂无关联机构
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const props = defineProps({
  list: { type: Array, default: () => [] }
});

const emit = defineEmits(['item-click', 'add-click']);
const configStore = useConfigStore();

// 状态：当前展开的机构编码
const expandedInstCode = ref('');

const toggle = (code) => {
  expandedInstCode.value = expandedInstCode.value === code ? '' : code;
};

// 工具函数
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const getCatName = (code) => {
  return configStore.assetCategories.find(c => c.categoryCode === code)?.name || '其他';
};
</script>

<style lang="scss" scoped>
.inst-card {
  background: #ffffff;
  border-radius: 40rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  
  &.expanded {
    box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.04);
  }
}

.card-header {
  padding: 32rpx;
  display: flex;
  align-items: center;
  
  .logo-wrapper {
    width: 80rpx;
    height: 80rpx;
    border-radius: 24rpx;
    background-color: #F9FAFB;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;
    border: 1rpx solid rgba(0,0,0,0.03);
    
    .inst-logo {
      width: 52rpx;
      height: 52rpx;
    }
  }

  .inst-info {
    flex: 1;
    .inst-name {
      font-size: 30rpx;
      font-weight: 800;
      color: #1F2937;
      display: block;
      margin-bottom: 4rpx;
    }
    .inst-count {
      font-size: 22rpx;
      color: #9CA3AF;
      font-weight: 500;
    }
  }

  .inst-amount {
    display: flex;
    flex-direction: column; // 改为水平排列
    align-items: center; // 垂直居中
    .amt {
      font-size: 32rpx;
      font-weight: 700;
      font-family: 'DIN Alternate', sans-serif;
      display: block;
      color: #1F2937;
    }
    .arrow {
      width: 12rpx;
      height: 12rpx;
      border-top: 4rpx solid #D1D5DB;
      border-right: 4rpx solid #D1D5DB;
      transform: rotate(135deg);
      margin: 12rpx auto 0;
      transition: all 0.3s ease;
      &.rotate {
        transform: rotate(-45deg);
        margin-top: 20rpx;
      }
    }
  }
}

.details-list {
  background: #FAFAFA;
  overflow: hidden;
  transition: max-height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.acc-item {
  padding: 28rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
  
  &:active {
    background-color: #F3F4F6;
  }

  .acc-left {
    .acc-name-group {
      display: flex;
      align-items: center;
      gap: 16rpx;
    }
    .acc-name {
      font-size: 26rpx;
      color: #4B5563;
      font-weight: 600;
    }
    .cat-tag {
      font-size: 18rpx;
      background: #E5E7EB;
      color: #6B7280;
      padding: 2rpx 10rpx;
      border-radius: 6rpx;
      font-weight: 700;
      text-transform: uppercase;
    }
  }

  .acc-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
    .acc-amt {
      font-size: 26rpx;
      font-weight: 700;
      font-family: 'Monaco', monospace;
      color: #1F2937;
    }
  }
}

.add-guide {
  padding: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  
  .plus-mini {
    width: 24rpx;
    height: 24rpx;
    opacity: 0.4;
  }
  
  text {
    font-size: 24rpx;
    color: #9CA3AF;
    font-weight: 600;
  }

  &:active {
    background-color: #ffffff;
    text { color: #2A806C; }
  }
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #9CA3AF;
  font-size: 26rpx;
}
</style>