<template>
  <view class="page-container">
    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-box">
      <uni-load-more status="loading" />
    </view>
    
    <!-- 详情内容 -->
    <block v-else-if="goalDetail">
      <!-- 顶部目标信息卡片 -->
      <view class="goal-header" :style="{ backgroundImage: goalDetail.bgUrl ? `url(${goalDetail.bgUrl})` : '' }">
        <view class="header-overlay">
          <view class="goal-icon">{{ goalDetail.icon || '🎯' }}</view>
          <text class="goal-name">{{ goalDetail.name }}</text>
          <view class="goal-status" :class="goalDetail.status?.toLowerCase()">
            {{ formatStatus(goalDetail.status) }}
          </view>
        </view>
      </view>
      
      <!-- 金额信息 -->
      <view class="amount-section">
        <view class="amount-item">
          <text class="label">目标金额</text>
          <text class="value">¥{{ formatMoney(goalDetail.targetAmount) }}</text>
        </view>
        <view class="amount-item">
          <text class="label">当前已存</text>
          <text class="value highlight">¥{{ formatMoney(goalDetail.currentAmount) }}</text>
        </view>
        <view class="amount-item">
          <text class="label">剩余缺口</text>
          <text class="value remain">¥{{ formatMoney(remainAmount) }}</text>
        </view>
      </view>
      
      <!-- 进度条 -->
      <view class="progress-section">
        <view class="progress-header">
          <text class="progress-title">完成进度</text>
          <text class="progress-percent">{{ progressPercent }}%</text>
        </view>
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <view class="progress-info">
          <text>开始日期: {{ formatDate(goalDetail.startDate) }}</text>
          <text>目标日期: {{ formatDate(goalDetail.deadline) }}</text>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-section">
        <button class="action-btn primary" @click="onDeposit">存入资金</button>
        <button class="action-btn secondary" @click="onEdit">编辑目标</button>
        <button class="action-btn danger" @click="onDelete">删除目标</button>
      </view>
    </block>
    
    <!-- 空状态 -->
    <view v-else class="empty-box">
      <text class="empty-text">目标不存在或已删除</text>
      <button class="back-btn" @click="goBack">返回列表</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { goalService } from '@/services/goalService.js';

const isLoading = ref(true);
const goalDetail = ref(null);
const goalId = ref('');

// 计算剩余金额
const remainAmount = computed(() => {
  if (!goalDetail.value) return 0;
  return Math.max(0, (goalDetail.value.targetAmount || 0) - (goalDetail.value.currentAmount || 0));
});

// 计算进度百分比
const progressPercent = computed(() => {
  if (!goalDetail.value || !goalDetail.value.targetAmount) return 0;
  const percent = Math.round(((goalDetail.value.currentAmount || 0) / goalDetail.value.targetAmount) * 100);
  return Math.min(percent, 100);
});

// 格式化金额
const formatMoney = (val) => {
  return Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    'ON_GOING': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  };
  return statusMap[status] || status;
};

// 加载目标详情
const loadGoalDetail = async (id) => {
  isLoading.value = true;
  try {
    const res = await goalService.getGoalDetail(id);
    goalDetail.value = res;
  } catch (error) {
    console.error('加载目标详情失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    isLoading.value = false;
  }
};

onLoad((options) => {
  if (options.id) {
    goalId.value = options.id;
    loadGoalDetail(options.id);
  } else {
    isLoading.value = false;
  }
});

// 存入资金
const onDeposit = () => {
  uni.navigateTo({
    url: `/pages/goals/deposit?id=${goalId.value}`
  });
};

// 编辑目标
const onEdit = () => {
  uni.navigateTo({
    url: `/pages/goals/edit?id=${goalId.value}`
  });
};

// 删除目标
const onDelete = () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，是否继续？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await goalService.deleteGoal(goalId.value);
          uni.showToast({ title: '删除成功', icon: 'success' });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    }
  });
};

// 返回列表
const goBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #F8F7F2;
}

.loading-box {
  padding-top: 200rpx;
}

.goal-header {
  height: 320rpx;
  background-size: cover;
  background-position: center;
  position: relative;
  
  .header-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.6));
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    
    .goal-icon {
      font-size: 80rpx;
      margin-bottom: 16rpx;
    }
    
    .goal-name {
      font-size: 40rpx;
      font-weight: 700;
      margin-bottom: 16rpx;
    }
    
    .goal-status {
      padding: 8rpx 24rpx;
      border-radius: 24rpx;
      font-size: 24rpx;
      background: rgba(255,255,255,0.2);
      
      &.on_going {
        background: #2A806C;
      }
      &.completed {
        background: #52C41A;
      }
      &.paused {
        background: #FAAD14;
      }
      &.cancelled {
        background: #999;
      }
    }
  }
}

.amount-section {
  display: flex;
  padding: 32rpx;
  background: #fff;
  margin: 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
  
  .amount-item {
    flex: 1;
    text-align: center;
    
    .label {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 12rpx;
    }
    
    .value {
      display: block;
      font-size: 32rpx;
      font-weight: 700;
      color: #333;
      
      &.highlight {
        color: #2A806C;
      }
      
      &.remain {
        color: #FF6B6B;
      }
    }
  }
}

.progress-section {
  padding: 32rpx;
  background: #fff;
  margin: 0 24rpx 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
  
  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .progress-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
    
    .progress-percent {
      font-size: 32rpx;
      font-weight: 700;
      color: #2A806C;
    }
  }
  
  .progress-bar {
    height: 16rpx;
    background: #F0F0F0;
    border-radius: 8rpx;
    overflow: hidden;
    margin-bottom: 20rpx;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #2A806C, #4CAF50);
      border-radius: 8rpx;
      transition: width 0.6s ease;
    }
  }
  
  .progress-info {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: #999;
  }
}

.action-section {
  padding: 0 24rpx;
  
  .action-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: 44rpx;
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 24rpx;
    
    &.primary {
      background: #2A806C;
      color: #fff;
    }
    
    &.secondary {
      background: #fff;
      color: #2A806C;
      border: 2rpx solid #2A806C;
    }
    
    &.danger {
      background: #fff;
      color: #FF4D4F;
      border: 2rpx solid #FF4D4F;
    }
    
    &:active {
      opacity: 0.8;
    }
  }
}

.empty-box {
  padding-top: 200rpx;
  text-align: center;
  
  .empty-text {
    display: block;
    font-size: 28rpx;
    color: #999;
    margin-bottom: 40rpx;
  }
  
  .back-btn {
    width: 240rpx;
    height: 80rpx;
    line-height: 80rpx;
    background: #2A806C;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}
</style>

