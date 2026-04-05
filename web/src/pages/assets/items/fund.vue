<template>
  <view class="main-container">
    <!-- 全局滚动区：确保下拉时所有卡片一起动 -->
    <scroll-view 
      class="main-scroll" 
      scroll-y 
      enable-back-to-top
      refresher-enabled 
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onPullDownRefresh"
    >
      <view class="content-wrapper">
        
        <!-- 1. 资产汇总卡片 (与列表同级，同样使用 card-warm) -->
        <view class="card-warm summary-card">
          <view class="stats-row">
            <!-- 左侧：资产 -->
            <view class="stats-item">
              <view class="label-box" @tap="toggleAssetHidden">
                <text class="label">资产总额(元)</text>
                <image :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" class="icon-sm" />
              </view>
              <text class="value num-font" :class="{ 'is-blur': isAssetHidden }">
                {{ isAssetHidden ? '******' : formatMoney(portfolio.totalAsset) }}
              </text>
            </view>
            
            <!-- 右侧：收益 -->
            <view class="stats-item align-right">
              <view class="label-box" @tap="toggleReturnMode">
                <image src="/static/images/switch.png" class="icon-sm" />
                <text class="label">今日{{ showReturnRate ? '收益率' : '收益' }}</text>
              </view>
              <text class="value num-font" :class="getReturnClass(portfolio.dailyReturn)">
                {{ portfolio.dailyReturn >= 0 ? '+' : '' }}{{ showReturnRate ? formatRate(portfolio.dailyReturnRate) : formatMoney(portfolio.dailyReturn) }}
              </text>
            </view>
          </view>
        </view>

        <!-- 2. 持仓列表卡片 (同样使用 card-warm) -->
        <view class="card-warm list-card">
          <view class="list-header">
            <view class="title-group">
              <text class="title">持有基金</text>
              <text class="count">({{ portfolio.holdings.length }})</text>
            </view>
            <view class="add-btn" @tap="navigateToAdd">
              <image src="/static/images/add.png" />
            </view>
          </view>

          <!-- 列表内容滚动容器 -->
          <view class="list-content">
            <!-- 列表内容 -->
            <view class="table-head">
              <text class="col-name">基金名称</text>
              <text class="col-rate">今日涨跌</text>
              <text class="col-profit">今日收益</text>
              <text class="col-total">持有收益</text>
            </view>

            <view 
              v-for="(item, index) in portfolio.holdings" 
              :key="item.id"
              :class="['holding-item', 'item-ref-' + index]"
              @touchstart="handleTouchStart($event, item, index)"
              @touchmove="handleTouchMove"
              @touchend="handleTouchEnd"
              @tap="onItemTap(item)"
              hover-class="item-hover-bg"
            >
              <view class="col-name">
                <text class="f-name truncate">{{ item.name }}</text>
                <view class="f-tags">
                  <text :class="['tag', { 'updated': isUpdated(item.navDate) }]">
                    {{ isUpdated(item.navDate) ? '已更新' : formatNavDate(item.navDate) }}
                  </text>
                  <text class="f-amount num-font">{{ isAssetHidden ? '****' : formatMoney(item.amount) }}</text>
                </view>
              </view>
              <view :class="['col-rate', 'num-font', getReturnClass(item.dailyReturnRate)]">
                {{ item.dailyReturnRate >= 0 ? '+' : '' }}{{ formatRate(item.dailyReturnRate) }}
              </view>
              <view :class="['col-profit', 'num-font', getReturnClass(item.dailyReturn)]">
                {{ item.dailyReturn >= 0 ? '+' : '' }}{{ formatMoney(item.dailyReturn) }}
              </view>
              <view :class="['col-total', 'num-font', getReturnClass(item.totalReturn)]">
                {{ item.totalReturn >= 0 ? '+' : '' }}{{ formatMoney(item.totalReturn) }}
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 3. 长按操作菜单 -->
    <view v-if="menuVisible" class="menu-mask" @tap="closeMenu" @touchmove.stop.prevent>
      <view class="menu-card" :style="menuPosStyle" @tap.stop>
        <view class="menu-header">
          <text class="m-title truncate">{{ selectedHolding?.name }}</text>
        </view>
        <view class="menu-body">
          <view class="menu-option" @tap="handleEdit" hover-class="item-hover-bg">
            <image src="/static/images/edit_fund.png" class="m-icon" />
            <text>修改持仓</text>
          </view>
          <view class="menu-option is-delete" @tap="handleDelete" hover-class="item-hover-bg">
            <image src="/static/images/del-fund.png" class="m-icon" />
            <text>删除持仓</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
/* JS 部分保持您的逻辑和接口调用不变 */
import { formatCurrency, formatPercentage } from '@/utils/formatUtil';
import { deletePortfolioHolding } from '@/services/portfolioHoldService.js';
import { getPortfolioByAssetId } from '@/services/assetService.js';

export default {
  data() {
    return {
      portfolio: { totalAsset: 0, dailyReturn: 0, dailyReturnRate: 0, holdings: [] },
      isAssetHidden: false,
      showReturnRate: false,
      isRefreshing: false,
      menuVisible: false,
      selectedHolding: null,
      menuPosStyle: {},
      touchTimer: null,
      startX: 0, startY: 0, hasMoved: false,
      assetId: ''
    }
  },
  onLoad(options) {
    if (options.data) {
      const item = JSON.parse(decodeURIComponent(options.data));
      if (item) {
        this.assetId = item.id;
      }
    }
  },
  onShow() { this.loadData(); },
  methods: {
    async loadData() {
      try {
        const res = await getPortfolioByAssetId(this.assetId);
        this.portfolio = res || { holdings: [] };
      } catch (e) { console.error(e); }
    },
    async onPullDownRefresh() {
      this.isRefreshing = true;
      await this.loadData();
      this.isRefreshing = false;
      uni.vibrateShort();
    },
    handleTouchStart(e, item, index) {
      this.hasMoved = false;
      this.startX = e.touches[0].clientX;
      this.startY = e.touches[0].clientY;
      this.touchTimer = setTimeout(() => {
        if (!this.hasMoved) this.showActionMenu(item, index);
      }, 450);
    },
    handleTouchMove(e) {
      if (Math.abs(e.touches[0].clientX - this.startX) > 10 || Math.abs(e.touches[0].clientY - this.startY) > 10) {
        this.hasMoved = true;
        this.clearTimer();
      }
    },
    handleTouchEnd() { this.clearTimer(); },
    clearTimer() { if (this.touchTimer) clearTimeout(this.touchTimer); },
    showActionMenu(item, index) {
      this.selectedHolding = item;
      uni.vibrateShort();
      uni.createSelectorQuery().in(this).select('.item-ref-' + index).boundingClientRect(rect => {
        if (!rect) return;
        let topPos = rect.bottom + 10;
        if (topPos + 120 > uni.getWindowInfo().windowHeight) topPos = rect.top - 130;
        this.menuPosStyle = { top: topPos + 'px', left: '50%', transform: 'translateX(-50%)' };
        this.menuVisible = true;
      }).exec();
    },
    closeMenu() { this.menuVisible = false; },
    handleEdit() {
      uni.navigateTo({ url: `/pages/assets/fund/edit?id=${this.selectedHolding.id}` });
      this.closeMenu();
    },
    async handleDelete() {
      const res = await uni.showModal({ title: '确认删除', content: '确定要移除这项基金持仓吗？', confirmColor: '#ef4444' });
      if (res.confirm) {
        await deletePortfolioHolding(this.selectedHolding.id);
        this.loadData();
      }
      this.closeMenu();
    },
    onItemTap(item) { console.log('detail:', item.id); },
    formatMoney(v) { return formatCurrency(v, ''); },
    formatRate(v) { return formatPercentage(v); },
    getReturnClass(v) { return v > 0 ? 'text-gain' : (v < 0 ? 'text-loss' : 'text-main'); },
    toggleAssetHidden() { this.isAssetHidden = !this.isAssetHidden; uni.vibrateShort(); },
    toggleReturnMode() { this.showReturnRate = !this.showReturnRate; uni.vibrateShort(); },
    isUpdated(date) { return false; },
    formatNavDate(date) { return date ? date.substring(5, 10) : '--'; },
    navigateToAdd() { 
      uni.navigateTo({ 
        url: `/pages/assets/fund/add?portfolioId=${this.portfolio.id}`
      }); 
    }
  }
}
</script>

<style lang="scss" scoped>
/* 核心布局 */
.main-container {
  height: 100vh;
  background-color: $bg-page;
}

.main-scroll {
  height: 100%;
}

.content-wrapper {
  padding: $spacing-sm; 
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* 1. 资产汇总区域样式优化 */
.summary-card {
  margin-bottom: $spacing-sm;
  
  .stats-row {
    display: flex;
    justify-content: space-between;
    align-items: center; /* 垂直居中 */
  }
  
  .stats-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    &.align-right { align-items: flex-end; }
    
    .label-box {
      display: flex;
      align-items: center;
      margin-bottom: $spacing-base;
      
      .label { font-size: 32rpx; color: $text-muted; }
      .icon-sm { width: 32rpx; height: 32rpx; margin: 0 8rpx; opacity: 0.5; }
    }
    
    .value {
      font-size: 44rpx; /* 统一大小 */
      font-weight: $fw-bold;
      color: $text-main;
      
      &.is-blur { filter: blur(6px); opacity: 0.3; }
    }
  }
}

/* 2. 持仓列表区域样式优化 */
.list-card {
  /* 直接继承 card-warm 样式 */
  display: flex;
  flex-direction: column;
  flex: 1;
  
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-base;
    
    .title { font-size: 32rpx; font-weight: $fw-bold; color: $text-main; }
    .count { font-size: 28rpx; color: $text-muted; margin-left: 8rpx; }
    
    .add-btn {
      width: 48rpx; height: 48rpx; border-radius: 50%;
      background-color: $primary-dark; @include flex-center;
      image { width: 32rpx; height: 32rpx; }
    }
  }
  
  .table-head {
    flex-shrink: 0;
  }
  
  .holding-item {
    flex-shrink: 0;
  }
  
  .list-content {
    flex: 1;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch; /* 优化iOS滚动体验 */
  }
}

/* 表格对齐 */
.table-head {
  display: flex;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid $gray-100;
  text { font-size: 24rpx; color: $text-muted; }
}

.holding-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid rgba($gray-100, 0.5);
  
  .f-name { font-size: 26rpx; color: $text-main; font-weight: $fw-medium; margin-bottom: 4rpx; }
  .f-tags {
    display: flex; align-items: center;
    .tag { font-size: 16rpx; padding: 2rpx 8rpx; background: $gray-50; color: $text-muted; border-radius: 4rpx; margin-right: 10rpx; }
    .tag.updated { background: $green-50; color: $color-success; }
    .f-amount { font-size: 24rpx; color: $text-placeholder; }
  }
}

/* 列比例保持一致 */
.col-name { flex: 2.2; min-width: 0; }
.col-rate { flex: 1.2; text-align: right; }
.col-profit { flex: 1.4; text-align: right; }
.col-total { flex: 1.4; text-align: right; }

/* 菜单样式 (继承 card-warm 影子感) */
.menu-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.15); backdrop-filter: blur(1px); z-index: 999;
  .menu-card {
    position: absolute; width: 500rpx; background: $white; border-radius: $radius-base;
    box-shadow: $shadow-card; overflow: hidden;
    .menu-header { padding: 24rpx; background: $bg-subtle; text-align: center; border-bottom: 1rpx solid $gray-100; }
    .m-title { font-size: 24rpx; color: $text-main; font-weight: $fw-bold; }
    .menu-body { display: flex; .menu-option { flex: 1; @include flex-center; flex-direction: column; padding: 24rpx 0; } }
    .m-icon { width: 44rpx; height: 44rpx; margin-bottom: 8rpx; }
    text { font-size: 24rpx; color: $text-sub; }
    .is-delete text { color: $color-error; }
  }
}

/* 通用工具 */
.num-font { font-family: $font-family-money; @include tabular-nums; font-size: 26rpx; }
.text-gain { color: $text-gain; }
.text-loss { color: $text-loss; }
.truncate { @include text-ellipsis; }
.item-hover-bg { background-color: $bg-subtle; }
.safe-area-bottom { height: calc(env(safe-area-inset-bottom) + 30rpx); }
</style>