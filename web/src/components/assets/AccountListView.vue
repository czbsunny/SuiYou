<template>
  <view class="account-list-container">
    <!-- 1. 头部操作区 -->
    <view class="view-header">
      <text class="view-title">我的账户</text>
      <view class="header-actions">
        <view class="icon-btn" @tap="$emit('manage-click')" hover-class="btn-hover">
          <image src="/static/icons/sliders.png" mode="aspectFit" class="btn-img" />
        </view>
        <view class="icon-btn primary-tint" @tap="$emit('add-asset-click')" hover-class="btn-hover">
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="icon-img" />
        </view>
      </view>
    </view>

    <!-- 2. 账户卡片流 -->
    <view class="account-stream">
      <view 
        v-for="acc in list" 
        :key="acc.id" 
        class="account-card"
        @tap="$emit('account-click', acc)"
      >
        <!-- 💡 优化后的左侧指示标记 -->
        <view class="type-gem" :style="{ 
          background: `linear-gradient(to bottom, ${acc.bgColor || '#2a806c'}, ${adjustColor(acc.bgColor || '#2a806c', 30)})`,
          boxShadow: `0 4rpx 12rpx ${hexToRgba(acc.bgColor || '#2a806c', 0.2)}`
        }"></view>

        <!-- 左侧内容 -->
        <view class="card-left">
          <view class="logo-wrapper">
            <image :src="acc.logoUrl || '/static/icons/default-bank.png'" mode="aspectFit" class="logo-img" />
          </view>
          <view class="info-content">
            <view class="name-row">
              <text class="inst-name">{{ acc.instName }}</text>
              <text v-if="acc.accountName" class="acc-alias">{{ acc.accountName }}</text>
            </view>
            <text class="id-text num-font">**** {{ acc.identifier || '0000' }}</text>
          </view>
        </view>

        <!-- 右侧金额 -->
        <view class="card-right">
          <view class="balance-box">
            <text class="currency">¥</text>
            <text class="amount num-font">{{ formatAccountBalance(acc.totalBalance) }}</text>
          </view>
        </view>
      </view>

      <!-- 3. 空状态 -->
      <view v-if="list.length === 0" class="empty-action" @tap="$emit('add-asset-click')" hover-class="action-active">
        <text class="plus">+</text>
        <text>添加第一个资产</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  list: { type: Array, default: () => [] }
});

console.log('[DEBUG AccountListView] list prop:', props.list);

const emit = defineEmits(['account-click', 'add-asset-click', 'manage-click']);

// 金额格式化逻辑
const formatAccountBalance = (val) => {
  const num = Number(val || 0);
  if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万';
  }
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

/**
 * 辅助函数：颜色减淡（用于生成渐变）
 * @param hex 颜色
 * @param percent 减淡百分比
 */
const adjustColor = (hex, percent) => {
  const num = parseInt(hex.replace("#",""),16),
  amt = Math.round(2.55 * percent),
  R = (num >> 16) + amt,
  G = (num >> 8 & 0x00FF) + amt,
  B = (num & 0x0000FF) + amt;
  return "#" + (0x1000000 + (R<255?R<1?0:R:255)*0x10000 + (G<255?G<1?0:G:255)*0x100 + (B<255?B<1?0:B:255)).toString(16).slice(1);
};

/**
 * 辅助函数：十六进制转RGBA
 */
const hexToRgba = (hex, opacity) => {
  let r = parseInt(hex.slice(1, 3), 16),
      g = parseInt(hex.slice(3, 5), 16),
      b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
};
</script>

<style lang="scss" scoped>
.account-list-container {
  margin-bottom: 48rpx;
}

/* 🟢 头部样式 - 对齐 GoalActiveCard */
.view-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20rpx; padding: 0 4rpx;
  .view-title { font-size: 30rpx; font-weight: 600; color: $text-main; letter-spacing: 1rpx; }
  .header-actions { display: flex; gap: 16rpx; }
}

.icon-btn {
  width: 56rpx; height: 56rpx; border-radius: 12rpx; @include flex-center;
  background-color: $bg-white; color: $text-sub; border: 1rpx solid $border-color;
  transition: all 0.2s;
  .btn-img { width: 28rpx; height: 28rpx; }
  &.primary-tint { background-color: $bg-subtle; color: $primary; border: none; }
}

/* 🟢 卡片样式 */
.account-stream { display: flex; flex-direction: column; gap: 16rpx; }

.account-card {
  position: relative;
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 36rpx 36rpx 36rpx 44rpx; // 留出左侧空间
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(50, 46, 43, 0.02);
  transition: all 0.2s ease;
  overflow: hidden;

  &:active {
    transform: scale(0.985);
    background-color: #FAFAFA;
  }
}

/* 💎 进化后的左侧指示器 (The Gem) */
.type-gem {
  position: absolute;
  left: 10rpx; // 稍微向右偏移，不紧贴边缘
  top: 50%;
  transform: translateY(-50%); // 垂直居中
  width: 8rpx; // 稍微加宽一点
  height: 60%; // 缩小高度，占卡片的 40%
  border-radius: 100rpx; // 彻底圆化
  transition: all 0.3s ease;
}

.card-left {
  display: flex; align-items: center; gap: 24rpx; flex: 1; min-width: 0;
}

.logo-wrapper {
  width: 72rpx; height: 72rpx;
  background-color: $bg-subtle;
  border-radius: 18rpx;
  @include flex-center;
  flex-shrink: 0;
  .logo-img { width: 72rpx; height: 72rpx; border-radius: 18rpx;}
}

.info-content {
  display: flex; flex-direction: column; gap: 6rpx; flex: 1; min-width: 0;
}

.name-row {
  display: flex; align-items: baseline; gap: 12rpx;
  .inst-name { font-size: 28rpx; font-weight: 600; color: $text-main; }
  .acc-alias { font-size: 22rpx; color: $text-sub; @include text-ellipsis; }
}

.id-text { font-size: 22rpx; color: $text-muted; letter-spacing: 1rpx; }

.card-right { text-align: right; padding-left: 20rpx; flex-shrink: 0; }

.balance-box {
  display: flex; align-items: baseline; justify-content: flex-end; color: $text-main;
  .currency { font-size: 28rpx; font-weight: 700; margin-right: 4rpx; color: $text-sub; }
  .amount { font-size: 34rpx; font-weight: 700; }
}

/* 🟢 空状态 */
.empty-action {
  width: 100%; height: 120rpx; @include flex-center;
  border-radius: $radius-lg; border: 2rpx dashed $border-color;
  color: $text-muted; font-size: 26rpx; gap: 12rpx;
  .plus { font-size: 32rpx; margin-top: -4rpx; }
}

.action-active { background-color: $bg-subtle; }

.num-font {
  font-family: $font-family-money;
  @include tabular-nums;
}
</style>
