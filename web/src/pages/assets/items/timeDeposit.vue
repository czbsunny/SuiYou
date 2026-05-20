<template>
  <view class="main-container">
    <scroll-view class="main-scroll" scroll-y>
      <view class="content-wrapper">
        
        <!-- 1. 定存汇总看板 -->
        <view class="card-warm summary-card animate-fade-in">
          <view class="asset-main">
            <view class="label-row" @tap="toggleAssetHidden">
              <text class="label">定存总额 (元)</text>
              <uni-icons :type="isAssetHidden ? 'eye-slash' : 'eye'" size="16" color="#94a3b8"></uni-icons>
            </view>
            <text class="value-large num-font" :class="{ 'is-blur': isAssetHidden }">
              {{ isAssetHidden ? '******' : formatMoney(totalPrincipal) }}
            </text>
          </view>

          <view class="indicator-grid">
            <view class="indicator-item">
              <text class="label">预计总利息 (待收)</text>
              <text class="value-mid num-font text-gold" :class="{ 'is-blur': isAssetHidden }">
                {{ isAssetHidden ? '******' : '+' + formatMoney(totalInterest) }}
              </text>
            </view>
            <view class="indicator-item text-right">
              <text class="label">加权平均利率</text>
              <text class="value-mid num-font text-main">3.45%</text>
            </view>
          </view>
        </view>

        <!-- 2. 列表标题栏 -->
        <view class="section-header">
          <view class="header-left">
            <text class="title">存款明细</text>
            <text class="count-tag">{{ depositList.length }}单</text>
          </view>
          <view class="header-right" @tap="onAdd">
            <uni-icons type="plusempty" size="20" color="#64748b"></uni-icons>
          </view>
        </view>

        <!-- 3. 定存卡片列表 -->
        <view class="card-list">
          <view 
            class="card-warm deposit-card" 
            v-for="(item, index) in depositList" 
            :key="item.id"
            @tap="onItemTap(item)"
          >
            <!-- 第一行：银行与利率 -->
            <view class="card-row">
              <view class="bank-info">
                <text class="bank-name truncate">{{ item.bankName }}</text>
                <text class="deposit-type">{{ item.type }}</text>
              </view>
              <view class="rate-tag">
                <text class="num-font">{{ item.rate }}%</text>
              </view>
            </view>

            <!-- 第二行：本金与预计利息 -->
            <view class="card-row mt-24">
              <view class="data-group">
                <text class="d-label">存款本金</text>
                <text class="d-val num-font" :class="{ 'is-blur': isAssetHidden }">{{ isAssetHidden ? '****' : formatMoney(item.principal) }}</text>
              </view>
              <view class="data-group text-right">
                <text class="d-label">到期利息</text>
                <text class="d-val num-font text-gold">{{ formatMoney(item.expectedInterest) }}</text>
              </view>
            </view>

            <!-- 第三行：时间进度条 -->
            <view class="progress-section mt-24">
              <view class="progress-bar">
                <view class="progress-inner" :style="{ width: getProgress(item) + '%' }"></view>
              </view>
              <view class="progress-labels">
                <text class="p-date">{{ item.startDate }} 存入</text>
                <text class="p-days">还剩 {{ getRemainingDays(item.endDate) }} 天</text>
                <text class="p-date">{{ item.endDate }} 到期</text>
              </view>
            </view>

            <!-- 临近到期提醒 -->
            <view v-if="getRemainingDays(item.endDate) < 30" class="due-notice">
              <uni-icons type="info" size="12" color="#f59e0b"></uni-icons>
              <text class="ml-1">该存单即将到期</text>
            </view>
          </view>
        </view>

        <view class="safe-area-bottom"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

const isAssetHidden = ref(false);

const depositList = ref([
  { 
    id: 1, 
    bankName: '招商银行', 
    type: '大额存单', 
    principal: 200000, 
    rate: 3.55, 
    expectedInterest: 21300, 
    startDate: '2023-01-10', 
    endDate: '2026-01-10' 
  },
  { 
    id: 2, 
    bankName: '工商银行', 
    type: '定期整存整取', 
    principal: 50000, 
    rate: 2.75, 
    expectedInterest: 1375, 
    startDate: '2024-05-20', 
    endDate: '2025-05-20' 
  },
  { 
    id: 3, 
    bankName: '建设银行', 
    type: '定期整存整取', 
    principal: 80000, 
    rate: 2.25, 
    expectedInterest: 1800, 
    startDate: '2024-02-01', 
    endDate: '2024-11-01' 
  }
]);

const totalPrincipal = computed(() => depositList.value.reduce((s, i) => s + i.principal, 0));
const totalInterest = computed(() => depositList.value.reduce((s, i) => s + i.expectedInterest, 0));

const formatMoney = (v) => v.toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const getRemainingDays = (endDate) => {
  const diff = new Date(endDate) - new Date();
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
};

const getProgress = (item) => {
  const start = new Date(item.startDate).getTime();
  const end = new Date(item.endDate).getTime();
  const now = new Date().getTime();
  const progress = ((now - start) / (end - start)) * 100;
  return Math.min(100, Math.max(0, progress.toFixed(1)));
};

const toggleAssetHidden = () => { isAssetHidden.value = !isAssetHidden.value; uni.vibrateShort(); };
const onAdd = () => uni.showToast({ title: '添加存单' });
const onItemTap = (item) => console.log('详情', item.bankName);
</script>

<style lang="scss" scoped>
$text-main: #1e293b;
$text-sub: #64748b;
$gold: #b45309;
$bg-page: #f8fafc;

.main-container { height: 100vh; background-color: $bg-page; }
.content-wrapper { padding: 20rpx; }

/* 通用卡片 */
.card-warm {
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 36rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 看板定制 */
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
      flex: 1;
      .label { font-size: 22rpx; color: $text-sub; margin-bottom: 6rpx; display: block; }
      .value-mid { font-size: 32rpx; font-weight: 600; }
    }
  }
}

/* 列表标题 */
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10rpx 10rpx 20rpx;
  .header-left { display: flex; align-items: center; gap: 10rpx; .title { font-size: 30rpx; font-weight: 700; } }
  .count-tag { font-size: 22rpx; color: $text-sub; }
}

/* 定存卡片定制 */
.deposit-card {
  .card-row {
    display: flex; justify-content: space-between; align-items: flex-start;
    .bank-info {
      .bank-name { font-size: 30rpx; font-weight: 700; color: $text-main; display: block; }
      .deposit-type { font-size: 20rpx; color: #94a3b8; margin-top: 4rpx; }
    }
    .rate-tag {
      background-color: #fffbeb; color: $gold; padding: 4rpx 16rpx; border-radius: 8rpx;
      font-size: 24rpx; font-weight: 700; border: 1rpx solid #fde68a;
    }
  }

  .data-group {
    .d-label { font-size: 20rpx; color: #94a3b8; margin-bottom: 6rpx; display: block; }
    .d-val { font-size: 32rpx; font-weight: 700; color: #334155; }
  }

  .progress-section {
    .progress-bar {
      height: 8rpx; background-color: #f1f5f9; border-radius: 4rpx; overflow: hidden; margin-bottom: 12rpx;
      .progress-inner { height: 100%; background: linear-gradient(to right, #fbbf24, #f59e0b); border-radius: 4rpx; }
    }
    .progress-labels {
      display: flex; justify-content: space-between; align-items: center;
      .p-date { font-size: 18rpx; color: #cbd5e1; }
      .p-days { font-size: 20rpx; color: $gold; font-weight: 600; }
    }
  }

  .due-notice {
    margin-top: 24rpx; padding-top: 20rpx; border-top: 1rpx dashed #f1f5f9;
    display: flex; align-items: center; color: #f59e0b; font-size: 20rpx; font-weight: 600;
  }
}

/* 工具类 */
.num-font { font-family: "DIN Alternate", sans-serif; }
.text-gold { color: $gold; }
.text-main { color: $text-main; }
.is-blur { filter: blur(10rpx); opacity: 0.4; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mt-24 { margin-top: 24rpx; }
.ml-1 { margin-left: 4rpx; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>