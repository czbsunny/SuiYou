<template>
  <view class="main-container">
    <!-- 1. 资产汇总区域 (原本的 AssetSummary) -->
    <view class="summary-card-wrapper">
      <view class="summary-card card-warm">
        <view class="asset-info">
          <view class="left-pane">
            <view class="label-row" @tap="toggleAssetHidden">
              <text class="label">当前组合总资产 (元)</text>
              <image :src="isAssetHidden ? '/static/images/eye-close.png' : '/static/images/eye-open.png'" class="eye-icon" />
            </view>
            <text class="value main-num" :class="{ 'is-blur': isAssetHidden }">
              {{ isAssetHidden ? '******' : formatMoney(portfolio.totalAsset) }}
            </text>
          </view>
          <view class="right-pane" @tap="toggleReturnMode">
            <view class="label-row">
              <image src="/static/images/switch.png" class="switch-icon" />
              <text class="label">今日{{ showReturnRate ? '收益率' : '收益' }}</text>
            </view>
            <text class="value sub-num" :class="getReturnClass(portfolio.dailyReturn)">
              {{ portfolio.dailyReturn >= 0 ? '+' : '' }}{{ showReturnRate ? formatRate(portfolio.dailyReturnRate) : formatMoney(portfolio.dailyReturn) }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 持仓列表区域 (原本的 SingleAccountPage) -->
    <view class="list-section">
      <view class="list-header">
        <text class="title">持有基金 ({{ portfolio.holdings.length }})</text>
        <view class="add-btn" @tap="navigateToAdd">
          <image src="/static/images/add.png" />
        </view>
      </view>

      <!-- 滚动区 -->
      <scroll-view 
        class="scroll-body" 
        scroll-y 
        enable-back-to-top
        refresher-enabled 
        :refresher-triggered="isRefreshing"
        @refresherrefresh="onPullDownRefresh"
      >
        <view class="table-head">
          <text class="col-name">基金名称</text>
          <text class="col-rate">今日涨幅</text>
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
          hover-class="item-hover"
        >
          <view class="col-name">
            <text class="f-name">{{ item.name }}</text>
            <view class="f-tags">
              <text :class="['tag', { 'is-new': isToday(item.navDate) }]">{{ formatNavDate(item.navDate) }}</text>
              <text class="f-amount" v-if="!isAssetHidden">¥{{ formatMoney(item.amount) }}</text>
              <text class="f-amount" v-else>****</text>
            </view>
          </view>
          <view :class="['col-rate', getReturnClass(item.dailyReturnRate)]">
            {{ item.dailyReturnRate >= 0 ? '+' : '' }}{{ formatRate(item.dailyReturnRate) }}
          </view>
          <view :class="['col-profit', getReturnClass(item.dailyReturn)]">
            {{ item.dailyReturn >= 0 ? '+' : '' }}{{ formatMoney(item.dailyReturn) }}
          </view>
          <view :class="['col-total', getReturnClass(item.totalReturn)]">
            {{ item.totalReturn >= 0 ? '+' : '' }}{{ formatMoney(item.totalReturn) }}
          </view>
        </view>
        
        <!-- 底部留白，防止被遮挡 -->
        <view class="safe-bottom"></view>
      </scroll-view>
    </view>

    <!-- 3. 操作菜单 (原本的 ActionMenu) -->
    <view v-if="menuVisible" class="menu-mask" @tap="closeMenu" @touchmove.stop.prevent>
      <view class="menu-content" :style="menuPosStyle" @tap.stop>
        <view class="menu-title">{{ selectedHolding?.name }}</view>
        <view class="menu-btns">
          <view class="btn" @tap="handleEdit">修改持仓</view>
          <view class="btn delete" @tap="handleDelete">删除基金</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { formatCurrency, formatPercentage } from '@/utils/formatUtil';
import { deletePortfolioHolding } from '@/services/portfolioHoldService.js';
import { getPortfolioByAssetId } from '@/services/assetService.js';

export default {
  data() {
    return {
      portfolio: {
        totalAsset: 0,
        dailyReturn: 0,
        dailyReturnRate: 0,
        holdings: []
      },
      isAssetHidden: false,
      showReturnRate: false,
      isRefreshing: false,
      
      // 长按冲突处理相关
      menuVisible: false,
      selectedHolding: null,
      menuPosStyle: {},
      touchTimer: null,
      startPosX: 0,
      startPosY: 0,
      isMoveTriggered: false
    }
  },
  onShow() {
    this.loadData();
  },
  methods: {
    // --- 数据加载 ---
    async loadData() {
      try {
        const res = await getPortfolioByAssetId(); // 直接获取组合详情
        this.portfolio = res;
      } catch (e) {
        console.error(e);
      }
    },

    async onPullDownRefresh() {
      this.isRefreshing = true;
      await this.loadData();
      this.isRefreshing = false;
      uni.vibrateShort();
    },

    // --- 长按冲突逻辑控制 ---
    handleTouchStart(e, item, index) {
      this.isMoveTriggered = false;
      this.startPosX = e.touches[0].clientX;
      this.startPosY = e.touches[0].clientY;
      
      // 开启长按定时器（350ms判定为长按）
      this.touchTimer = setTimeout(() => {
        if (!this.isMoveTriggered) {
          this.showActionMenu(item, index);
        }
      }, 400);
    },

    handleTouchMove(e) {
      const moveX = Math.abs(e.touches[0].clientX - this.startPosX);
      const moveY = Math.abs(e.touches[0].clientY - this.startPosY);
      
      // 只要手指移动超过 10px，就判定为“正在滚动”，取消长按
      if (moveX > 10 || moveY > 10) {
        this.isMoveTriggered = true;
        this.clearTimer();
      }
    },

    handleTouchEnd() {
      this.clearTimer();
    },

    clearTimer() {
      if (this.touchTimer) {
        clearTimeout(this.touchTimer);
        this.touchTimer = null;
      }
    },

    // --- 菜单定位与交互 ---
    showActionMenu(item, index) {
      this.selectedHolding = item;
      uni.vibrateShort();

      // 获取点击项的位置，计算菜单弹出位置
      const query = uni.createSelectorQuery().in(this);
      query.select('.item-ref-' + index).boundingClientRect(rect => {
        let top = rect.bottom + 10;
        // 如果太靠底部，则向上弹出
        if (top > uni.getWindowInfo().windowHeight - 150) {
          top = rect.top - 120;
        }
        this.menuPosStyle = {
          top: top + 'px',
          left: '50%',
          transform: 'translateX(-50%)'
        };
        this.menuVisible = true;
      }).exec();
    },

    closeMenu() {
      this.menuVisible = false;
    },

    // --- 业务操作 ---
    onItemTap(item) {
      // 这里的普通点击可以跳转到基金详情页
      console.log('点击详情:', item.name);
    },

    handleEdit() {
      uni.navigateTo({
        url: `/pages/assets/fund/edit?id=${this.selectedHolding.id}`
      });
      this.closeMenu();
    },

    async handleDelete() {
      const res = await uni.showModal({ title: '确认', content: '确定删除该持仓吗？' });
      if (res.confirm) {
        await deletePortfolioHolding(this.selectedHolding.id);
        this.loadData();
      }
      this.closeMenu();
    },

    // --- 工具类 ---
    formatMoney(v) { return formatCurrency(v, ''); },
    formatRate(v) { return formatPercentage(v); },
    getReturnClass(v) { return v > 0 ? 'text-up' : (v < 0 ? 'text-down' : 'text-main'); },
    toggleAssetHidden() { this.isAssetHidden = !this.isAssetHidden; },
    toggleReturnMode() { this.showReturnRate = !this.showReturnRate; },
    isToday(date) { /* 逻辑实现... */ return true; },
    formatNavDate(date) { return '12-25'; }, // 简化
    navigateToAdd() { uni.navigateTo({ url: '/pages/assets/fund/add' }); }
  }
}
</script>

<style lang="scss" scoped>
.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f9fa;
  overflow: hidden;
}

// 顶部卡片
.summary-card-wrapper {
  padding: 20rpx;
  .summary-card {
    padding: 40rpx;
    border-radius: 24rpx;
    background: linear-gradient(135deg, #2a806c, #34a88e);
    color: white;
    .asset-info {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }
    .label-row {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;
      opacity: 0.8;
      .label { font-size: 24rpx; }
      .eye-icon, .switch-icon { width: 30rpx; height: 30rpx; margin-left: 10rpx; }
    }
    .main-num { font-size: 52rpx; font-weight: bold; }
    .sub-num { font-size: 40rpx; font-weight: 500; }
    .is-blur { filter: blur(4px); }
  }
}

// 列表区域
.list-section {
  flex: 1; // 自动占满剩余空间
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 32rpx 32rpx 0 0;
  padding: 30rpx 20rpx;
  
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    .title { font-size: 32rpx; font-weight: bold; color: #333; }
    .add-btn {
      width: 60rpx; height: 60rpx; border-radius: 50%;
      background: #f0fdf4; display: flex; align-items: center; justify-content: center;
      image { width: 32rpx; height: 32rpx; }
    }
  }
}

.scroll-body {
  flex: 1;
  height: 0; // 必须设置，flex:1 才会生效
}

.table-head {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
  text { font-size: 22rpx; color: #999; }
}

.holding-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:active { background-color: #fafafa; }
  
  .f-name { font-size: 28rpx; color: #333; font-weight: 500; display: block; }
  .f-tags {
    display: flex; align-items: center; margin-top: 8rpx;
    .tag { font-size: 18rpx; padding: 2rpx 8rpx; background: #eee; border-radius: 4rpx; color: #999; margin-right: 10rpx; }
    .tag.is-new { background: #e6f7ff; color: #1890ff; }
    .f-amount { font-size: 22rpx; color: #bbb; }
  }
}

// 列宽分配
.col-name { flex: 2.5; }
.col-rate { flex: 1.2; text-align: right; font-weight: 500; }
.col-profit { flex: 1.5; text-align: right; font-weight: 500; }
.col-total { flex: 1.5; text-align: right; font-weight: 500; }

// 操作菜单
.menu-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3); z-index: 999;
  
  .menu-content {
    position: absolute; width: 500rpx; background: white; border-radius: 16rpx;
    box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.1); overflow: hidden;
    .menu-title { padding: 30rpx; font-size: 26rpx; background: #f8f9fa; text-align: center; color: #666; }
    .menu-btns {
      display: flex;
      .btn {
        flex: 1; padding: 30rpx; text-align: center; font-size: 28rpx; color: #333;
        border-right: 1rpx solid #eee;
        &:last-child { border-right: none; }
        &.delete { color: #ff4d4f; }
      }
    }
  }
}

.text-up { color: #f5222d; }
.text-down { color: #389e0d; }
.text-main { color: #333; }
.safe-bottom { height: 60rpx; }
</style>