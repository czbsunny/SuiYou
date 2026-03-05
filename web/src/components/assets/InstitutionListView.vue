<!-- components/assets/InstitutionListView.vue -->
<template>
  <view class="account-list-container">
    <!-- 头部操作区 -->
    <view class="account-header">
      <text class="account-title">我的账户</text>
      <view class="header-actions">
        <view class="icon-btn" @tap="$emit('manage-click')" hover-class="btn-hover">
          <image src="/static/icons/sliders.png" mode="aspectFit" class="btn-img" />
        </view>
        <view class="icon-btn" @tap="$emit('add-account-click')" hover-class="btn-hover">
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="icon-img" />
        </view>
      </view>
    </view>

    <!-- 账户卡片流 -->
    <view class="account-list">
      <view 
        v-for="acc in list" 
        :key="acc.id" 
        class="account-card"
        :style="{ background: acc.bgColor || '#1F2937' }"
        @tap="$emit('account-click', acc)"
      >
        <view class="glass-texture"></view>
        
        <view class="card-content">
          <!-- 第一部分：账户头部信息 -->
          <view class="card-header">
            <view class="account-row-top">
              <view class="brand-group">
                <view class="logo-white-box">
                  <image :src="acc.logoUrl || '/static/icons/default-bank.png'" mode="aspectFit" class="logo" />
                </view>
                <view class="name-area">
                  <text class="inst-name">{{ acc.instName }}</text>
                  <text v-if="acc.accountName" class="acc-alias"> - {{ acc.accountName }}</text>
                </view>
              </view>
              <!-- 添加资产快捷键 -->
              <view class="add-asset-trigger" @tap.stop="$emit('add-asset-click', acc)">
                <image src="/static/images/plus.png" mode="aspectFit" class="plus-mini-img" />
              </view>
            </view>

            <!-- 第二行：识别码 (稍微远离第一行) -->
            <view class="account-row-id">
              <text class="id-text">**** **** {{ acc.identifier || '0000' }}</text>
            </view>
          </view>

          <!-- 第二部分：财务数据与页脚 (强制推到底部) -->
          <view class="card-footer">
            <!-- 第三行：金额与收益 -->
            <view class="account-row-balance">
              <view class="balance-left">
                <text class="symbol">¥</text>
                <text class="num">{{ formatAmount(acc.totalBalance) }}</text>
              </view>
              <view v-if="acc.yesterdayProfit" class="profit-right">
                <text class="profit-label">昨日收益</text>
                <text class="profit-val">+{{ formatAmount(acc.yesterdayProfit) }}</text>
              </view>
            </view>

            <!-- 第四行：页脚状态 -->
            <view class="account-row-footer">
              <text class="footer-info">{{ getFooterText(acc) }}</text>
              <view class="item-tag">{{ acc.itemCount || 0 }} 项资产</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="list.length === 0" class="empty-placeholder" @tap="$emit('add-account-click')">
        <image src="/static/images/plus-gray.png" class="empty-plus" />
        <text>点击创建一个账户</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  list: { type: Array, default: () => [] }
});

const emit = defineEmits(['account-click', 'add-asset-click', 'add-account-click', 'manage-click']);
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
const getFooterText = (acc) => {
  if (acc.accountType === 'CREDIT_CARD') return `还款日: 每月${acc.repaymentDate}号`;
  return acc.subText || '账户状态正常';
};
</script>

<style lang="scss" scoped>
.account-list-container { 
  margin-bottom: 40rpx;
}

/* 🟢 视图头部样式 */
.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.account-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-main;
  letter-spacing: 1rpx;
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

/* 极简图标按钮 */
.icon-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-white;
  color: $text-sub;
  border: 1rpx solid $border-color;
  transition: all 0.2s;

  .btn-img, .icon-img {
    width: 28rpx;
    height: 28rpx;
  }
}

.btn-hover {
  opacity: 0.8;
  transform: scale(0.94);
}

/* 🟢 卡片核心样式 */
.account-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.account-card {
  width: 100%; height: 390rpx;
  border-radius: 32rpx;
  position: relative; overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  &:active { transform: scale(0.98); }
}

.glass-texture {
  position: absolute; top: -50%; right: -20%; width: 120%; height: 120%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0) 50%);
  border-radius: 50%; pointer-events: none;
}

.card-content {
  padding: 44rpx; height: 100%; display: flex; flex-direction: column;
  justify-content: flex-start;
  color: #fff; position: relative; z-index: 2; box-sizing: border-box;
}

/* 第一部分：账户头 */
.card-header { margin-bottom: auto; }

.account-row-top {
  display: flex; justify-content: space-between; align-items: center;
  .brand-group {
    display: flex; align-items: center; gap: 20rpx; flex: 1; min-width: 0;
    .logo-white-box {
      width: 64rpx; height: 64rpx; background: #fff; border-radius: 18rpx;
      display: flex; align-items: center; justify-content: center; flex-shrink: 0;
      .logo { width: 64rpx; height: 64rpx; border-radius: 18rpx; }
    }
    .name-area {
      display: flex; flex-direction: row; align-items: center; flex: 1; min-width: 0;
      .inst-name { font-size: 30rpx; font-weight: 800; line-height: 1.2; white-space: nowrap; flex-shrink: 0; }
      .acc-alias { font-size: 30rpx; opacity: 0.7; font-weight: 500; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    }
  }
  .add-asset-trigger {
    width: 60rpx; height: 60rpx; border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex; align-items: center; justify-content: center;
    border: 1rpx solid rgba(255, 255, 255, 0.3);
    .plus-mini-img { width: 28rpx; height: 28rpx; }
    &:active { background: rgba(255, 255, 255, 0.4); transform: scale(0.9); }
  }
}

.account-row-id {
  margin-top: 12rpx;
  .id-text {
    font-size: 24rpx; font-family: 'Courier New', Courier, monospace;
    opacity: 0.6; letter-spacing: 4rpx; font-weight: 600;
  }
}

/* 第二部分：底部财务区 */
.card-footer {
  margin-top: 36rpx;
}

.account-row-balance {
  display: flex; justify-content: space-between; align-items: flex-end;
  .balance-left {
    display: flex; align-items: baseline;
    .symbol { font-size: 40rpx; font-weight: 700; margin-right: 12rpx; }
    .num { font-size: 60rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; letter-spacing: -1rpx; }
  }
  .profit-right {
    text-align: right; margin-bottom: 12rpx;
    .profit-label { font-size: 18rpx; opacity: 0.7; display: block; margin-bottom: 4rpx; font-weight: 600; }
    .profit-val { font-size: 26rpx; font-weight: 700; color: #FFF7E6; }
  }
}

.account-row-footer {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 22rpx; font-weight: 600; 
  border-top: 1rpx solid rgba(255,255,255,0.15);
  padding-top: 24rpx;
  .footer-info { opacity: 0.8; }
  .item-tag { background: rgba(0,0,0,0.1); padding: 6rpx 16rpx; border-radius: 10rpx; }
}

.empty-placeholder {
  height: 240rpx; border: 3rpx dashed $border-color; border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.4); display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12rpx;
  .empty-plus { width: 40rpx; height: 40rpx; opacity: 0.2; }
  text { font-size: 26rpx; color: $text-muted; font-weight: 500; }
}
</style>