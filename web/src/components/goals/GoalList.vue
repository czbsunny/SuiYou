<template>
  <section class="section">
    <view class="section-header">
      <text class="section-title">愿望清单</text>
      <text class="count-badge">进行中 {{ list.length }}</text>
    </view>

    <view class="goal-grid">
      <view v-for="item in list" :key="item.id" class="goal-card" @click="$emit('clickItem', item)">
        <view class="goal-top">
          <view class="goal-icon-wrapper">
            <!-- 逻辑：如果是图片路径则显示 image，否则显示 view(emoji) -->
            <image 
              v-if="isIconUrl(item.iconUrl)" 
              :src="item.iconUrl" 
              class="goal-icon-img" 
              mode="aspectFit"
            />
            <view v-else class="goal-icon-box">
              {{ item.iconUrl || '💰' }}
            </view>
            
            <text class="goal-title">{{ item.name }}</text>
          </view>
          <!-- 风险状态点 -->
          <view v-if="item.status === 'AT_RISK'" class="risk-dot"></view>
        </view>

        <view class="goal-bottom">
          <view class="amount-group">
            <text class="amount-current">¥{{ item.currentAmount }}</text>
            <text class="amount-target">/ {{ item.targetAmount }}</text>
          </view>
          <view class="mini-progress-bar">
            <view 
              class="mini-progress-fill" 
              :class="{ 'bg-orange': item.status === 'AT_RISK' }" 
              :style="{ width: calculateProgress(item) + '%' }"
            ></view>
          </view>
        </view>
      </view>

      <!-- 新增入口卡片 -->
      <view class="goal-card add-goal-card" @click="$emit('add')">
        <image src="/static/images/plus-gray.png" class="plus-icon-img" mode="aspectFit"></image>
        <text class="add-text">新愿望</text>
      </view>
    </view>
  </section>
</template>

<script setup>
// 注意：在模板循环中判断类型，建议使用普通函数而非 computed
const props = defineProps({
  list: { type: Array, default: () => [] }
});

defineEmits(['add', 'clickItem']);

// 判断是否为图片链接
const isIconUrl = (url) => {
  if (!url) return false;
  return url.startsWith('http') || url.startsWith('/static') || url.startsWith('/');
};

// 计算百分比
const calculateProgress = (item) => {
  if (!item.targetAmount || item.targetAmount === 0) return 0;
  const percent = Math.round((item.currentAmount / item.targetAmount) * 100);
  return percent > 100 ? 100 : percent;
};

</script>

<style lang="scss" scoped>
.section { margin-bottom: 50rpx; }
.section-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 24rpx; 
}
.section-title { font-size: 28rpx; font-weight: bold; color: #2C3E50; }
.count-badge { font-size: 22rpx; color: #95A5A6; }

.goal-grid { 
  display: grid; 
  grid-template-columns: repeat(2, 1fr); 
  gap: 24rpx; 
}

.goal-card {
  background: #fff; 
  padding: 28rpx; 
  border-radius: 40rpx; 
  height: 260rpx;
  display: flex; 
  flex-direction: column; 
  justify-content: space-between;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.03); 
  transition: all 0.2s;
  box-sizing: border-box;
  
  &:active { transform: scale(0.96); }

  .goal-top {
    display: flex; 
    justify-content: space-between; 
    align-items: flex-start;

    .goal-icon-wrapper { 
      display: flex; 
      align-items: center; 
      gap: 16rpx; 
      flex: 1; 
      overflow: hidden; 
    }

    /* 统一图片和Emoji的外框尺寸 */
    .goal-icon-img, .goal-icon-box { 
      width: 64rpx; 
      height: 64rpx; 
      flex-shrink: 0;
    }

    .goal-icon-img {
      border-radius: 18rpx;
      /* 建议图片背景加个极浅灰色，防止透明图看不见 */
      background: #f8fafc;
    }

    .goal-icon-box { 
      background: #f8fafc; 
      border-radius: 18rpx; 
      display: flex; 
      align-items: center; 
      justify-content: center; 
      font-size: 32rpx; 
    }

    .goal-title { 
      font-size: 26rpx; 
      font-weight: bold; 
      color: #2C3E50; 
      white-space: nowrap; 
      overflow: hidden; 
      text-overflow: ellipsis; 
    }

    .risk-dot { 
      width: 12rpx; 
      height: 12rpx; 
      background: #f6ad55; 
      border-radius: 50%; 
      margin-top: 26rpx; 
    }
  }

  .goal-bottom {
    .amount-group { 
      display: flex; 
      align-items: baseline; 
      margin-bottom: 12rpx;
      
      .amount-current { 
        font-size: 28rpx; 
        font-weight: bold; 
        color: #2C3E50; 
        font-family: 'DIN Alternate', sans-serif; 
      }
      .amount-target { 
        font-size: 20rpx; 
        color: #95A5A6; 
        margin-left: 8rpx; 
      }
    }
    
    .mini-progress-bar { 
      width: 100%; 
      height: 8rpx; 
      background: #edf2f7; 
      border-radius: 100rpx; 
      overflow: hidden;
      
      .mini-progress-fill { 
        height: 100%; 
        background: #2a806c; 
        border-radius: 100rpx; 
        transition: width 0.3s ease;
        &.bg-orange { background: #f6ad55; } 
      }
    }
  }
}

/* 新增愿望占位卡片 */
.add-goal-card {
  border: 3rpx dashed #cbd5e0; 
  background: rgba(255, 255, 255, 0.4); 
  justify-content: center; 
  align-items: center;
  box-shadow: none;
  
  .plus-icon-img { 
    width: 48rpx; 
    height: 48rpx; 
    margin-bottom: 12rpx; 
    opacity: 0.3; 
  }
  .add-text { font-size: 22rpx; color: #a0aec0; font-weight: 500;}
}
</style>