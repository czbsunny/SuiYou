<template>
  <view class="account-template-bank">
    <!-- 1. 核心账户卡片 (Premium 尊享版皮肤) -->
    <view class="skin-bank-premium">
      <view class="hero-gloss"></view>
      <view class="card-content">
        <view class="card-header">
          <view class="inst-info">
            <view class="logo-box">
              <image :src="instInfo.logoUrl" mode="aspectFit" class="logo" />
            </view>
            <view class="name-area">
              <text class="inst-name">{{ instInfo.instName }}</text>
              <text class="acc-desc">金葵花客户 | {{ account.institutionIdentifier }}</text>
            </view>
          </view>
          <view class="settings-btn" @tap="$emit('settings')">
            <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
          </view>
        </view>
        
        <view class="balance-area">
          <text class="label">该行净资产 (CNY)</text>
          <view class="amount">
            <text class="symbol">¥</text>
            <text class="num money">{{ formatAmount(netWorth) }}</text>
          </view>
        </view>
        
        <view class="card-footer">
          <text class="footer-stat">总资产 ¥{{ formatAmount(totalAssets) }}</text>
          <text class="footer-stat">总负债 ¥{{ formatAmount(totalLiabilities) }}</text>
        </view>
      </view>
    </view>

    <!-- 2. 操作区 (2x1 宽展布局) -->
    <view class="action-grid">
      <view class="action-item btn-active" @tap="$emit('action', 'TRANSFER')">
        <view class="icon-box bg-transfer">
          <img src="@/static/assets/actions/repeat.png" class="icon-img" alt="repeat">
        </view>
        <text>转账调拨</text>
      </view>
      <view class="action-item btn-active" @tap="$emit('action', 'CALIBRATE')">
        <view class="icon-box bg-scale">
          <img src="@/static/assets/actions/scale.png" class="icon-img" alt="scale">
        </view>
        <text>账户对账</text>
      </view>
    </view>

    <!-- 3. 资产负债列表 (分组展示) -->
    <view class="asset-groups">
      <!-- 动态渲染每个分组 -->
      <view v-for="group in assetGroups" :key="group.type" class="group-card">
        <!-- 分组头部小计 -->
        <view class="group-header">
          <text class="group-title">{{ group.title }}</text>
          <text class="group-sum money" :class="{ 'liability': group.isLiability }">
            {{ group.isLiability ? '-' : '' }}¥{{ formatAmount(group.total) }}
          </text>
        </view>

        <!-- 分组内的资产项 -->
        <view 
          v-for="asset in group.items" 
          :key="asset.id" 
          class="asset-row btn-active"
          @tap="$emit('asset-click', asset)"
        >
          <view class="row-left">
            <view class="indicator" :style="{ backgroundColor: asset.color || group.defaultColor }"></view>
            <text class="asset-name">{{ asset.name }}</text>
          </view>
          <text class="asset-val money" :class="{ 'liability': group.isLiability }">
            {{ formatAmount(asset.totalBalance) }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const props = defineProps({
  account: Object,
  assets: Array // 该账户下的所有资产
});

const configStore = useConfigStore();

// --- 核心逻辑：资产分组 ---
const assetGroups = computed(() => {
  const groups = [
    { type: 'LIQUID', title: '流动资产', items: [], total: 0, defaultColor: '#2A806C', isLiability: false },
    { type: 'INVEST', title: '定期与理财', items: [], total: 0, defaultColor: '#D97706', isLiability: false },
    { type: 'DEBT', title: '账户负债', items: [], total: 0, defaultColor: '#F87171', isLiability: true }
  ];

  props.assets.forEach(asset => {
    // 逻辑：根据 Asset.groupType 分组
    const group = groups.find(g => g.type === asset.groupType) || groups[0];
    group.items.push(asset);
    group.total += Number(asset.totalBalance);
  });

  return groups.filter(g => g.items.length > 0);
});

// --- 数据计算 ---
const totalAssets = computed(() => {
  return assetGroups.value
    .filter(g => !g.isLiability)
    .reduce((sum, g) => sum + g.total, 0);
});

const totalLiabilities = computed(() => {
  const debtGroup = assetGroups.value.find(g => g.isLiability);
  return debtGroup ? debtGroup.total : 0;
});

const netWorth = computed(() => totalAssets.value - totalLiabilities.value);

const instInfo = computed(() => configStore.institutionMap[props.account.institution] || {});

const formatAmount = (val) => Number(Math.abs(val) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
</script>

<style lang="scss" scoped>
.account-template-bank {
  padding: 24rpx 32rpx;
}

/* 1. Card Skin */
.skin-bank-premium {
  height: 440rpx; border-radius: 54rpx; position: relative; overflow: hidden;
  background: linear-gradient(135deg, #1a1a1a 0%, #374151 100%);
  box-shadow: 0 30rpx 60rpx rgba(0,0,0,0.25); margin-bottom: 40rpx;
  
  .hero-gloss {
    position: absolute; inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.12) 0%, rgba(0,0,0,0) 100%);
  }
  
  .card-content {
    position: relative; z-index: 1; padding: 54rpx; height: 100%;
    display: flex; flex-direction: column; justify-content: space-between; color: #fff;
  }
}

.card-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  .inst-info {
    display: flex; align-items: center; gap: 24rpx;
    .logo-box { 
      width: 72rpx; height: 72rpx; border-radius: 20rpx; background: rgba(255,255,255,0.1); 
      backdrop-filter: blur(10px); display: flex; align-items: center; justify-content: center;
      .logo { width: 48rpx; height: 48rpx; }
    }
    .inst-name { font-size: 34rpx; font-weight: 800; }
    .acc-desc { font-size: 20rpx; opacity: 0.5; margin-top: 4rpx; display: block; }
  }

  .settings-btn { 
    width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; 
    .icon-img {
      width: 40rpx;
      height: 40rpx;
      object-fit: contain;
    }
  }
}

.balance-area {
  .label { font-size: 22rpx; opacity: 0.5; letter-spacing: 2rpx; margin-bottom: 12rpx; display: block; text-transform: uppercase; }
  .amount {
    display: flex; align-items: baseline; gap: 12rpx;
    .symbol { font-size: 44rpx; font-weight: 600; opacity: 0.6; }
    .num { font-size: 72rpx; font-weight: 800; }
  }
}

.card-footer {
  display: flex; justify-content: space-between; border-top: 1rpx solid rgba(255,255,255,0.08); padding-top: 32rpx;
  .footer-stat { font-size: 20rpx; opacity: 0.5; font-weight: 600; }
}

/* 2. Action Grid */
.action-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 24rpx; margin-bottom: 48rpx;
  .action-item {
    background: #fff; border-radius: 36rpx; padding: 32rpx; display: flex; align-items: center; gap: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
    .icon-box {
      width: 72rpx; height: 72rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center;
      &.bg-scale { background: #eff6ff; }
      &.bg-transfer { background: #f5f3ff; }
      .icon-img {
        max-width: 60%;
        max-height: 60%;
        object-fit: contain;
      }
    }
    text { font-size: 28rpx; font-weight: 800; color: #374151; }
  }
}

/* 3. Grouped Assets */
.group-card {
  background: #fff; border-radius: 40rpx; padding: 12rpx 0; margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.group-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24rpx 40rpx; border-bottom: 1rpx solid #F8FAFC;
  .group-title { font-size: 24rpx; font-weight: 800; color: #9CA3AF; text-transform: uppercase; letter-spacing: 2rpx; }
  .group-sum { font-size: 22rpx; font-weight: 700; color: #4B5563; }
}

.asset-row {
  padding: 32rpx 40rpx; display: flex; justify-content: space-between; align-items: center;
  .row-left {
    display: flex; align-items: center; gap: 20rpx;
    .indicator { width: 8rpx; height: 32rpx; border-radius: 4rpx; }
    .asset-name { font-size: 28rpx; font-weight: 700; color: #374151; }
  }
  .asset-val { font-size: 28rpx; font-weight: 800; color: #1F2937; }
}

/* 特殊样式 */
.money { font-family: 'JetBrains Mono', 'DIN Alternate', monospace; }
.liability { color: #F87171 !important; }
.btn-active:active { opacity: 0.7; transform: scale(0.98); }
</style>