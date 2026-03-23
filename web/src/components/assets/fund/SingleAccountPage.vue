<template>
  <view class="single-account-section">
    <!-- 1. 持仓卡片 -->
    <view class="account-assets-section card-warm">
      <view class="account-assets-header">
        <text class="section-title">持有基金</text>
        <view class="header-actions">
          <view class="add-fund-btn-circle" @click="navigateToAddFundPage" hover-class="hover-opacity">
            <image class="add-icon" src="/static/images/add.png" mode="aspectFit"></image>
          </view>
        </view>
      </view>

      <view class="holdings-list" v-if="holdings.length > 0">
        <!-- 简洁表头 -->
        <view class="holdings-header">
          <view class="header-item name-col"><text>基金名称</text></view>
          <view class="header-item rate-col">
            <text>今日涨幅</text>
          </view>
          <view class="header-item profit-col"><text>今日收益</text></view>
          <view class="header-item profit-col"><text>持有收益</text></view>
        </view>

        <!-- 内容列表 -->
        <view class="holdings-content">
          <view class="holding-item" :class="'item-idx-' + index" 
            v-for="(item, index) in holdings" :key="index" 
            @touchstart="handleTouchStart($event, item, index)"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
            hover-class="item-hover-bg">
            
            <view class="fund-info-col name-col">
              <text class="fund-name truncate">{{ item.name || item.symbol }}</text>
              <view class="fund-status-row">
                <text class="status-tag" :class="{ 'is-updated': isNavUpdatedToday(item.navUpdatedAt) }">
                  {{ formatNavDate(item.navUpdatedAt) }}
                </text>
                <text class="fund-amount num-font" :class="{ 'amount-hidden': isAssetHidden }">
                  {{ isAssetHidden ? '******' : formatCurrency(item.amount) }}
                </text>
              </view>
            </view>
            
            <view class="rate-col">
              <text class="rate-text num-font" :class="getReturnClass(item.dailyReturnRate)">
                {{ item.dailyReturnRate >= 0 ? '+' : '' }}{{ formatPercent(item.dailyReturnRate || 0) }}
              </text>
            </view>
            
            <view class="profit-col num-font" :class="getReturnClass(item.dailyReturn)">
              {{ item.dailyReturn >= 0 ? '+' : '' }}{{ formatCurrency(item.dailyReturn || 0) }}
            </view>
            
            <view class="profit-col num-font" :class="getReturnClass(item.returnValue)">
              <view class="val-main">{{ item.returnValue >= 0 ? '+' : '' }}{{ formatCurrency(item.returnValue || 0) }}</view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 列表底部呼吸感留白 -->
      <view class="section-footer-spacer"></view>
    </view>
  </view>
</template>

<script>
import { formatCurrency, formatPercentage } from '@/utils/formatUtil';

export default {
  props: { selectedAccount: Number, accountData: Object, isHoldingsLoading: Boolean, isAssetHidden: Boolean },
  data() { 
    return { 
      holdings: [],
      touchStartX: 0,
      touchStartY: 0,
      touchStartTime: 0,
      isLongPress: false,
      currentItem: null,
      currentIndex: -1,
      longPressTimer: null
    }; 
  },
  watch: { 
    accountData: { handler(newData) { 
      if (newData) this.holdings = newData.holdings || []; 
    }, immediate: true } 
  },
  methods: {
    formatCurrency(v) { return formatCurrency(v, ''); },
    formatPercent(v) { return formatPercentage(v); },
    getReturnClass(v) { return v > 0 ? 'text-up' : (v < 0 ? 'text-down' : 'text-main'); },
    getRateBarCellClass(rate, index) {
      const absRate = Math.abs(rate);
      const isPos = rate >= 0;
      let cells = absRate >= 5 ? Math.min(Math.round(absRate - 4), 5) : (absRate >= 1 ? Math.min(Math.round(absRate), 5) : (absRate > 0 ? 1 : 0));
      if (index <= cells) return isPos ? (absRate >= 5 ? 'rate-bar-cell-up-full' : 'rate-bar-cell-up-light') : (absRate >= 5 ? 'rate-bar-cell-down-full' : 'rate-bar-cell-down-light');
      return 'rate-bar-cell-empty';
    },
    parseBackendTime(dateStr) {
      if (!dateStr) return null;

      const cleaned = dateStr
        .replace('T', ' ')
        .replace(/\.\d{3}\d+$/, '');

      const [datePart, timePart] = cleaned.split(' ');
      if (!datePart || !timePart) return null;

      const [y, m, d] = datePart.split('-').map(Number);
      const [hh, mm, ss] = timePart.split(':').map(Number);

      return new Date(y, m - 1, d, hh, mm, ss);
    },

    formatNavDate(dateStr) {
      if (!dateStr) return '待更新';
      if (this.isNavUpdatedToday(dateStr)) return '已更新';
      
      const navDate = this.parseBackendTime(dateStr);
      const month = String(navDate.getMonth() + 1).padStart(2, '0');
      const day = String(navDate.getDate()).padStart(2, '0');

      return `${month}-${day}`;
    },

    isNavUpdatedToday(dateStr) {
      if (!dateStr) return false;
      const navDate = this.parseBackendTime(dateStr);
      if (!navDate) return false;

      const now = new Date();
      const start = new Date(
        navDate.getFullYear(),
        navDate.getMonth(),
        navDate.getDate(),
        19, 0, 0, 0
      );
      const end = new Date(start);
      end.setDate(end.getDate() + 1);
      end.setHours(9, 0, 0, 0);

      if (navDate < start) return false;
      return now >= start && now < end;
    },
    
    navigateToAddFundPage() { 
      uni.navigateTo({ url: '/pages/assets/fund/add?accountId=' + this.selectedAccount }); 
    },
    
    // 触摸开始事件
    handleTouchStart(event, item, index) {
      this.touchStartX = event.touches[0].clientX;
      this.touchStartY = event.touches[0].clientY;
      this.touchStartTime = Date.now();
      this.isLongPress = false;
      this.currentItem = item;
      this.currentIndex = index;
      
      // 清除之前的定时器
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer);
      }
      
      // 设置长按定时器
      this.longPressTimer = setTimeout(() => {
        this.isLongPress = true;
        this.$emit('longpress', { item, index });
      }, 350); // 350ms是默认长按时间
    },
    
    // 触摸移动事件
    handleTouchMove(event) {
      const touchX = event.touches[0].clientX;
      const touchY = event.touches[0].clientY;
      const deltaX = Math.abs(touchX - this.touchStartX);
      const deltaY = Math.abs(touchY - this.touchStartY);
      
      // 如果滑动距离超过5px，则认为是滑动操作，取消长按定时器
      if (deltaX > 5 || deltaY > 5) {
        if (this.longPressTimer) {
          clearTimeout(this.longPressTimer);
          this.longPressTimer = null;
        }
      }
    },
    
    // 触摸结束事件
    handleTouchEnd() {
      // 清除定时器
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer);
        this.longPressTimer = null;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.single-account-section { padding: 0 10rpx; min-height: 100vh; }
.account-assets-section { padding: 24rpx 16rpx; }
.account-assets-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.section-title { font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; }
.add-fund-btn-circle {
  width: 56rpx; height: 56rpx; border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-pressed);
  box-shadow: $shadow-button; @include flex-center;
  .add-icon { width: 36rpx; height: 36rpx; }
}

/* 布局比例 */
.name-col { flex: 2; min-width: 0; }
.rate-col { flex: 0.8; text-align: right; }
.profit-col { flex: 1.1; text-align: right; }

.holdings-header {
  display: flex; padding: 16rpx 0; border-bottom: 1rpx solid $border-color;
  .header-item {
    font-size: 24rpx; color: $text-muted; font-weight: $fw-medium;
    &.name-col { padding-left: 8rpx; text-align: left; }
    &:not(.name-col) { padding-right: 8rpx; }
  }
}
.holding-item {
  display: flex; align-items: center; padding: 28rpx 0; border-bottom: 1rpx solid rgba($text-main, 0.05);
  .fund-info-col { padding-left: 8rpx; display: flex; flex-direction: column; gap: 8rpx; }
  .fund-name { font-size: 28rpx; color: $text-main; font-weight: $fw-semibold; }
  .fund-status-row { display: flex; align-items: center; gap: 10rpx; }
  .status-tag {
    font-size: 18rpx; padding: 2rpx 8rpx; border-radius: 4rpx; background: $bg-white; color: $text-placeholder;
    &.is-updated { background: $primary-subtle; color: $primary; }
  }
  .fund-amount { font-size: 22rpx; color: $text-placeholder; &.amount-hidden { letter-spacing: 4rpx; } }
}
.rate-col, .profit-col { padding-right: 8rpx; font-size: 28rpx; font-weight: $fw-medium; }
.rate-bar { display: flex; gap: 3rpx; justify-content: flex-end; align-items: flex-end; height: 20rpx; }
.rate-cell {
  width: 7rpx; border-radius: 2rpx; background: $text-inverse;
  @for $i from 1 through 5 { &:nth-child(#{$i}) { height: 6rpx + ($i - 1) * 4rpx; } }
  &.rate-bar-cell-up-full { background: $color-gain; }
  &.rate-bar-cell-down-full { background: $color-loss; }
}
.section-footer-spacer { height: 40rpx; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>