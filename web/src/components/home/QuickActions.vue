<template>
  <view class="quick-actions-grid">
    <!-- 记一笔 -->
    <view class="action-btn" hover-class="btn-hover" @click="handleAction('record')">
      <view class="ab-icon icon-income">
        <image src="/static/assets/actions/plus.png" class="iconfont" />
      </view>
      <text class="action-label">记一笔</text>
    </view>

    <!-- 账户调拨 -->
    <view class="action-btn" hover-class="btn-hover" @click="handleAction('transfer')">
      <view class="ab-icon icon-transfer">
        <image src="/static/assets/actions/transfer.png" class="iconfont" />
      </view>
      <text class="action-label">账户调拨</text>
    </view>

    <!-- 资产对账 (原再平衡) -->
    <view class="action-btn" hover-class="btn-hover" @click="handleAction('reconcile')">
      <view class="ab-icon icon-liquid">
        <image src="/static/assets/actions/scale.png" class="iconfont" />
      </view>
      <text class="action-label">资产对账</text>
    </view>

    <!-- 月度复盘 -->
    <view class="action-btn" hover-class="btn-hover" @click="handleAction('review')">
      <view class="ab-icon icon-profit">
        <image src="/static/assets/actions/chart-trending.png" class="iconfont" />
      </view>
      <text class="action-label">收支月报</text>
    </view>
  </view>
</template>

<script setup>
const handleAction = (action) => {
  switch (action) {
    case 'record':
      // 记一笔 - 跳转到记账页面
      uni.navigateTo({ url: '/pages/transactions/record' })
      break
    case 'transfer':
      // 账户调拨 - 跳转到调拨页面
      uni.navigateTo({ url: '/pages/assets/transfer' })
      break
    case 'reconcile':
      // 资产对账 - 跳转到对账页面
      uni.navigateTo({ url: '/pages/assets/reconcile' })
      break
    case 'review':
      // 收支月报 - 跳转到月度复盘页面
      uni.navigateTo({ url: '/pages/report/monthly' })
      break
    default:
      console.log('未知操作:', action)
  }
}
</script>

<style lang="scss" scoped>
.quick-actions-grid {
  margin: 0 $spacing-md; 
  display: grid; 
  grid-template-columns: repeat(4, 1fr); 
  gap: $spacing-base;
  background: $bg-white; 
  padding: $spacing-md $spacing-sm; 
  border-radius: $radius-lg; 
  box-shadow: $shadow-card; 
  margin-bottom: $spacing-lg;
}

.action-btn { 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  gap: $spacing-sm; 
}

.ab-icon {
  width: 96rpx; 
  height: 96rpx; 
  border-radius: 32rpx; 
  @include flex-center;
  transition: transform 0.1s;
  
  .iconfont {
    width: 64rpx;
    height: 64rpx;
  }
}

/* 关联到你定义的语义色背景和主色 */
.icon-income { 
  background-color: $bg-income; 
  color: $color-income; 
}
.icon-transfer { 
  background-color: $bg-transfer; 
  color: $color-transfer; 
}
.icon-profit { 
  background-color: $bg-profit; 
  color: $color-profit; 
}
.icon-liquid { 
  background-color: $bg-liquid; 
  color: $color-liquid; 
}

/* 点击反馈 */
.btn-hover {
  .ab-icon {
    transform: scale(0.92);
    filter: brightness(0.95);
  }
  .action-label {
    color: $text-main;
  }
}

.action-label { 
  font-size: 24rpx; 
  color: $text-sub; 
  font-weight: 500; 
}
</style>