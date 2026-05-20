<template>
  <view class="main-container">
    <scroll-view 
      class="main-scroll" 
      scroll-y 
      enable-back-to-top
      :refresher-enabled="true" 
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onPullDownRefresh"
    >
      <view class="content-wrapper">
        
        <!-- 1. 资产汇总卡片 -->
        <view class="card-warm summary-card animate-fade-in">
          <view class="asset-main">
            <view class="label-row" @tap="toggleAssetHidden">
              <text class="label">基金总资产 (元)</text>
              <uni-icons :type="isAssetHidden ? 'eye-slash' : 'eye'" size="16" color="#94a3b8"></uni-icons>
            </view>
            <text class="value-large num-font" :class="{ 'is-blur': isAssetHidden }">
              {{ isAssetHidden ? '******' : formatMoney(portfolio.currentValue) }}
            </text>
          </view>

          <view class="indicator-grid">
            <view class="indicator-item">
              <text class="label">持有收益</text>
              <text class="value-mid num-font" :class="getReturnClass(portfolio.totalReturn)">
                {{ isAssetHidden ? '******' : (portfolio.totalReturn >= 0 ? '+' : '') + formatMoney(portfolio.totalReturn) }}
              </text>
            </view>
            <view class="indicator-item text-right">
              <view class="label-row justify-end" @tap="toggleReturnMode">
                <text class="label">今日{{ showReturnRate ? '收益率' : '收益' }}</text>
                <uni-icons type="loop" size="12" color="#94a3b8" class="ml-1"></uni-icons>
              </view>
              <text class="value-mid num-font" :class="getReturnClass(portfolio.dailyReturn)">
                {{ isAssetHidden ? '******' : (portfolio.dailyReturn >= 0 ? '+' : '') + (showReturnRate ? formatRate(portfolio.dailyReturnRate) : formatMoney(portfolio.dailyReturn)) }}
              </text>
            </view>
          </view>
        </view>

        <!-- 2. 列表标题栏 -->
        <view class="section-header">
          <view class="header-left">
            <text class="title">持有明细</text>
            <text class="count-tag">{{ portfolio.holdings.length }}只</text>
          </view>
          <view class="header-right" @tap="navigateToAdd">
            <uni-icons type="plusempty" size="20" color="#64748b"></uni-icons>
          </view>
        </view>

        <!-- 3. 持仓列表 -->
        <view class="card-list">
          <view 
            v-for="(item, index) in portfolio.holdings" 
            :key="item.id"
            :class="['card-warm', 'holding-card', 'item-ref-' + index]"
            @touchstart="handleTouchStart($event, item, index)"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
            @tap="onItemTap(item)"
          >
            <view class="card-row">
              <view class="name-box">
                <text class="f-name truncate">{{ item.name }}</text>
                <view class="status-box">
                  <text :class="['status-tag', { 'is-final': isUpdated(item.navUpdatedAt) }]">
                    {{ isUpdated(item.navUpdatedAt) ? '净值已出' : '盘中估值' }}
                  </text>
                  <text class="update-time">{{ formatNavDate(item.navUpdatedAt) }}</text>
                </view>
              </view>
              <view class="profit-box">
                <text class="main-val num-font" :class="getReturnClass(item.dailyReturnRate)">
                  {{ item.dailyReturnRate >= 0 ? '+' : '' }}{{ formatRate(item.dailyReturnRate) }}
                </text>
              </view>
            </view>

            <view class="card-row mt-20">
              <view class="data-item">
                <text class="d-label">市值</text>
                <text class="d-val num-font" :class="{ 'is-blur': isAssetHidden }">{{ isAssetHidden ? '****' : formatMoney(item.amount) }}</text>
              </view>
              <view class="data-item text-center">
                <text class="d-label">今日收益</text>
                <text class="d-val num-font" :class="getReturnClass(item.dailyReturn)">{{ item.dailyReturn >= 0 ? '+' : '' }}{{ formatMoney(item.dailyReturn) }}</text>
              </view>
              <view class="data-item text-right">
                <text class="d-label">总收益</text>
                <text class="d-val num-font" :class="getReturnClass(item.returnValue)">{{ item.returnValue >= 0 ? '+' : '' }}{{ formatMoney(item.returnValue) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="safe-area-bottom"></view>
      </view>
    </scroll-view>

    <!-- 4. 操作菜单 -->
    <view v-if="menuVisible" class="menu-mask" @tap="closeMenu" @touchmove.stop.prevent>
      <view class="menu-card" :style="menuPosStyle" @tap.stop>
        <view class="menu-header">
          <text class="m-title truncate" v-if="selectedHolding">{{ selectedHolding.name }}</text>
        </view>
        <view class="menu-body">
          <view class="menu-option" @tap="handleEdit">
            <uni-icons type="compose" size="20" color="#64748b"></uni-icons>
            <text class="m-text">修改持仓</text>
          </view>
          <view class="menu-option is-delete" @tap="handleDelete">
            <uni-icons type="trash" size="20" color="#ef4444"></uni-icons>
            <text class="m-text text-red">删除持仓</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';

// 响应式数据
const isRefreshing = ref(false);
const isAssetHidden = ref(false);
const showReturnRate = ref(false);
const menuVisible = ref(false);
const selectedHolding = ref(null);
const menuPosStyle = ref({});

const portfolio = reactive({
  currentValue: 504285.00,
  totalReturn: 24800.50,
  dailyReturn: 1562.20,
  dailyReturnRate: 2.22,
  holdings: [
    { id: 1, name: '招商中证500指数增强', amount: 35189.0, dailyReturn: 250.4, dailyReturnRate: 0.71, returnValue: -1260.75, navUpdatedAt: '2023-10-27 14:45' },
    { id: 2, name: '纳斯达克100ETF(QDII)', amount: 130849.0, dailyReturn: 1102.32, dailyReturnRate: 3.58, returnValue: 1202.32, navUpdatedAt: '2023-10-27 10:30' }
  ]
});

// 手势相关
let touchTimer = null;
let startX = 0;
let startY = 0;
let hasMoved = false;

// 方法定义
const formatMoney = (v) => v ? v.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00';
const formatRate = (v) => v ? v.toFixed(2) + '%' : '0.00%';
const getReturnClass = (v) => v > 0 ? 'text-up' : (v < 0 ? 'text-down' : '');
const toggleAssetHidden = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const toggleReturnMode = () => { showReturnRate.value = !showReturnRate.value; uni.vibrateShort(); };

const isUpdated = (dateStr) => {
  if (!dateStr) return false;
  // 简单逻辑：如果包含 19:xx 以后或者日期是今天且当前时间已过 19 点
  return dateStr.includes('19:') || dateStr.includes('20:'); 
};

const formatNavDate = (date) => date ? date.substring(5, 10) : '--';

const onPullDownRefresh = () => {
  isRefreshing.value = true;
  setTimeout(() => {
    isRefreshing.value = false;
    uni.showToast({ title: '已更新', icon: 'none' });
  }, 1000);
};

// 长按菜单逻辑
const handleTouchStart = (e, item, index) => {
  hasMoved = false;
  startX = e.touches[0].clientX;
  startY = e.touches[0].clientY;
  touchTimer = setTimeout(() => {
    if (!hasMoved) {
      selectedHolding.value = item;
      uni.vibrateShort();
      const query = uni.createSelectorQuery();
      query.select('.item-ref-' + index).boundingClientRect(rect => {
        if (rect) {
          let topPos = rect.bottom + 10;
          if (topPos + 120 > uni.getSystemInfoSync().windowHeight) topPos = rect.top - 130;
          menuPosStyle.value = `top: ${topPos}px; left: 50%; transform: translateX(-50%);`;
          menuVisible.value = true;
        }
      }).exec();
    }
  }, 500);
};

const handleTouchMove = (e) => {
  if (Math.abs(e.touches[0].clientX - startX) > 10 || Math.abs(e.touches[0].clientY - startY) > 10) {
    hasMoved = true;
    if (touchTimer) clearTimeout(touchTimer);
  }
};

const handleTouchEnd = () => { if (touchTimer) clearTimeout(touchTimer); };
const closeMenu = () => { menuVisible.value = false; };
const navigateToAdd = () => uni.showToast({ title: '去添加', icon: 'none' });
const onItemTap = (item) => console.log('详情', item.name);
const handleEdit = () => { uni.showToast({ title: '编辑' }); closeMenu(); };
const handleDelete = () => { uni.showToast({ title: '删除' }); closeMenu(); };

</script>

<style lang="scss" scoped>
/* 变量定义 */
$text-main: #1e293b;
$text-sub: #64748b;
$up: #ef4444;
$down: #22c55e;
$bg-page: #f8fafc;

.main-container { height: 100vh; background-color: $bg-page; }
.content-wrapper { padding: 20rpx; }

/* 通用卡片 */
.card-warm {
  background-color: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 看板 */
.summary-card {
  .asset-main {
    margin-bottom: 30rpx;
    .label-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 8rpx; }
    .label { font-size: 24rpx; color: $text-sub; }
    .value-large { font-size: 56rpx; font-weight: 700; color: $text-main; }
  }
  .indicator-grid {
    display: flex; justify-content: space-between;
    .indicator-item {
      display: flex; flex-direction: column;
      .label { font-size: 22rpx; color: $text-sub; margin-bottom: 6rpx; }
      .value-mid { font-size: 32rpx; font-weight: 600; }
    }
  }
}

/* 标题 */
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10rpx 10rpx 20rpx;
  .header-left { display: flex; align-items: center; gap: 10rpx; 
    .title { font-size: 30rpx; font-weight: 700; color: $text-main; }
    .count-tag { font-size: 22rpx; color: $text-sub; background-color: $bg-subtle; padding: 4rpx 16rpx; border-radius: 20rpx; }
  }
}

/* 列表卡片 */
.holding-card {
  .card-row {
    display: flex; justify-content: space-between; align-items: center;
    .name-box { 
      flex: 1; min-width: 0; 
      .f-name { font-size: 28rpx; font-weight: 600; margin-bottom: 6rpx; display: block; }
    }
    .status-box { 
      display: flex; align-items: center; gap: 10rpx; 
      .status-tag { font-size: 18rpx; padding: 2rpx 8rpx; border-radius: 4rpx; background: #f1f5f9; color: #94a3b8; }
      .status-tag.is-final { background: #f0fdf4; color: $down; }
      .update-time { font-size: 18rpx; color: #cbd5e1; }
    }
    .main-val { font-size: 32rpx; font-weight: 700; }
    .data-item { flex: 1; display: flex; flex-direction: column; .d-label { font-size: 20rpx; color: #94a3b8; } .d-val { font-size: 26rpx; font-weight: 600; } }
  }
}

/* 菜单 */
.menu-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.15); z-index: 999;
  .menu-card {
    position: absolute; width: 440rpx; background: #fff; border-radius: 24rpx; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.1);
    .menu-header { padding: 20rpx; background: #f8fafc; text-align: center; border-bottom: 1rpx solid #f1f5f9; }
    .m-title { font-size: 22rpx; color: #64748b; }
    .menu-body { display: flex; .menu-option { flex: 1; padding: 30rpx 0; display: flex; flex-direction: column; align-items: center; .m-text { font-size: 22rpx; margin-top: 10rpx; } } }
  }
}

.num-font { font-family: "Helvetica Neue", Arial, sans-serif; }
.text-up { color: $up; }
.text-down { color: $down; }
.is-blur { filter: blur(10rpx); opacity: 0.4; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.justify-end { justify-content: flex-end; }
.mt-20 { margin-top: 20rpx; }
.ml-1 { margin-left: 4rpx; }
.text-red { color: $up; }
</style>