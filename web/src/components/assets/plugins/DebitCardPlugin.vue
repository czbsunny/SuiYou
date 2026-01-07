<template>
  <view class="plugin-root">
    <!-- 1. Apple Card 资产身份卡片 -->
    <view class="card-container">
      <view class="asset-card" :style="{ background: cardGradient }">
        <view class="hero-gloss"></view>
        <view class="hero-content">
          <!-- 头部：机构与设置 -->
          <view class="hero-header">
            <view class="inst-info">
              <image :src="instInfo?.logoUrl || '/static/icons/default-bank.png'" class="inst-logo" mode="aspectFit" />
              <view class="name-box">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="inst-identifier">{{ instInfo?.instName }} | {{ account?.institutionIdentifier || '****' }}</text>
              </view>
            </view>
            <!-- 设置按钮：放在卡片内避开胶囊 -->
            <view class="settings-btn" @tap.stop="handleSettings">
              <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
            </view>
          </view>
          
          <!-- 中间：大额余额展示 -->
          <view class="balance-section">
            <text class="label">当前总余额</text>
            <view class="amount-row">
              <text class="symbol">¥</text>
              <text class="num money">{{ formatNumber(asset.totalBalance) }}</text>
            </view>
          </view>

          <!-- 底部：状态标签 -->
          <view class="hero-footer">
            <view class="tags">
              <text class="tag-item">{{ asset.visibleScope === 'PRIVATE' ? '个人私有' : '家庭共享' }}</text>
              <text class="tag-item" v-if="asset.includeInNetWorth">计入净值</text>
            </view>
            <text class="update-time">{{ lastUpdateText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 中部核心操作区 (2x2 金刚区) -->
    <view class="action-grid">
      <view class="action-card btn-active" @tap="handleAction('EXPENSE')">
        <view class="icon-box bg-expense">
          <img src="@/static/assets/actions/minus.png" class="icon-img" alt="minus">
        </view>
        <text class="action-text">记录支出</text>
      </view>
      <view class="action-card btn-active" @tap="handleAction('INCOME')">
        <view class="icon-box bg-income">
          <img src="@/static/assets/actions/plus.png" class="icon-img" alt="plus">
        </view>
        <text class="action-text">记录收入</text>
      </view>
      <view class="action-card btn-active" @tap="handleAction('CALIBRATE')">
        <view class="icon-box bg-scale">
          <img src="@/static/assets/actions/scale.png" class="icon-img" alt="scale">
        </view>
        <text class="action-text">余额校准</text>
      </view>
      <view class="action-card btn-active" @tap="handleAction('TRANSFER')">
        <view class="icon-box bg-transfer">
          <img src="@/static/assets/actions/repeat.png" class="icon-img" alt="repeat">
        </view>
        <text class="action-text">转账调拨</text>
      </view>
    </view>

    <!-- 3. 数据属性明细面板 -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">资产数据明细</text>
        </view>
        <view class="grid-list">
          <view class="grid-item">
            <text class="label">可用余额</text>
            <text class="value money">¥ {{ formatNumber(asset.availableBalance) }}</text>
          </view>
          <view class="grid-item">
            <text class="label">冻结金额</text>
            <text class="value money">¥ {{ formatNumber(asset.frozenBalance) }}</text>
          </view>
          <view class="grid-item">
            <text class="label">账户币种</text>
            <text class="value">{{ asset.currency === 'CNY' ? '人民币' : asset.currency }}</text>
          </view>
          <view class="grid-item">
            <text class="label">资产状态</text>
            <text class="value text-emerald">正常持有</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 4. 最近变动历史 (平铺展示) -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">最近变动记录</text>
          <text class="more-link" @tap="handleSeeMore">全部流水</text>
        </view>
        <view class="history-list">
          <view v-for="(item, index) in mockHistory" :key="index" class="history-item">
            <view class="h-left">
              <view class="h-icon" :class="item.type">
                <text>{{ item.type === 'in' ? '收' : '支' }}</text>
              </view>
              <view class="h-info">
                <text class="h-name">{{ item.name }}</text>
                <text class="h-date">{{ item.date }}</text>
              </view>
            </view>
            <text class="h-amt money" :class="item.type">{{ item.type === 'in' ? '+' : '-' }}{{ formatNumber(item.amount) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const props = defineProps({
  asset: Object,
  account: Object
});

const emit = defineEmits(['action', 'refresh']);

// --- 数据计算 ---
const instInfo = computed(() => {
  return configStore.institutionMap[props.asset?.institution] || {};
});

const cardGradient = computed(() => {
  const cat = configStore.assetCategories.find(c => c.categoryCode === props.asset?.category);
  return cat?.color || 'linear-gradient(135deg, #2A806C 0%, #1e5e4e 100%)';
});

const lastUpdateText = computed(() => '今日 14:20 更新');

// --- 模拟数据 (实际应由 API 或 Prop 传入) ---
const mockHistory = [
  { name: '日常消费', date: '12-28 18:30', amount: 52.00, type: 'out' },
  { name: '余额校准 (系统)', date: '12-25 10:00', amount: 500.00, type: 'in' }
];

// --- 工具函数 ---
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

// --- 事件处理 ---
const handleAction = (type) => {
  emit('action', type, props.asset);
};

const handleSettings = () => {
  uni.showActionSheet({
    itemList: ['修改账户资料', '校准余额', '删除资产'],
    success: (res) => {
      console.log('点击了', res.tapIndex);
    }
  });
};

const handleSeeMore = () => {
  uni.navigateTo({ url: `/pages/assets/history?id=${props.asset.id}` });
};
</script>

<style lang="scss" scoped>
.plugin-root {
  padding-bottom: 60rpx;
}

/* 1. Apple Card */
.card-container {
  padding: 24rpx 32rpx;
}
.asset-card {
  height: 420rpx; border-radius: 48rpx; position: relative; overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
  
  .hero-gloss {
    position: absolute; inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.18) 0%, rgba(0,0,0,0) 100%);
  }
  .hero-content {
    position: relative; z-index: 1; padding: 48rpx; height: 100%;
    display: flex; flex-direction: column; justify-content: space-between; color: #fff;
  }
}

.hero-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  .inst-info {
    display: flex; align-items: center; gap: 16rpx;
    .inst-logo { width: 50rpx; height: 50rpx; border-radius: 12rpx; background: rgba(255,255,255,0.9); }
    .name-box {
      .asset-name { font-size: 30rpx; font-weight: 700; display: block; }
      .inst-identifier { font-size: 20rpx; opacity: 0.6; }
    }
  }
  .settings-btn {
    width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center;
    background: rgba(255,255,255,0.1); border-radius: 50%;
    .icon-img {
      width: 40rpx;
      height: 40rpx;
      object-fit: contain;
    }
  }
}

.balance-section {
  .label { font-size: 22rpx; opacity: 0.7; margin-bottom: 8rpx; display: block; }
  .amount-row {
    display: flex; align-items: baseline; gap: 8rpx;
    .symbol { font-size: 36rpx; font-weight: 600; opacity: 0.8; }
    .num { font-size: 68rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; }
  }
}

.hero-footer {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1rpx solid rgba(255,255,255,0.1); padding-top: 24rpx;
  .tags {
    display: flex; gap: 12rpx;
    .tag-item { font-size: 18rpx; background: rgba(255,255,255,0.15); padding: 4rpx 12rpx; border-radius: 8rpx; font-weight: 600; }
  }
  .update-time { font-size: 18rpx; opacity: 0.5; }
}

/* 2. 金刚区 */
.action-grid {
  padding: 20rpx 32rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx;
  .action-card {
    background: #fff; border-radius: 32rpx; padding: 28rpx 32rpx;
    display: flex; align-items: center; gap: 24rpx;
    border: 1rpx solid rgba(0,0,0,0.02);
    .icon-box {
      width: 80rpx; height: 80rpx; border-radius: 20rpx;
      display: flex; align-items: center; justify-content: center;
      &.bg-expense { background: #fff7ed; }
      &.bg-income { background: #ecfdf5; }
      &.bg-scale { background: #eff6ff; }
      &.bg-transfer { background: #f5f3ff; }
      .icon-img {
        max-width: 60%;
        max-height: 60%;
        object-fit: contain;
      }
    }
    .action-text { font-size: 28rpx; font-weight: 700; color: #374151; }
  }
}

/* 3. 通用卡片样式 */
.section-container {
  padding: 12rpx 32rpx;
}
.data-card {
  background: #fff; border-radius: 40rpx; padding: 40rpx;
  border: 1rpx solid rgba(0,0,0,0.02);
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx;
    .title { font-size: 22rpx; font-weight: 800; color: #9CA3AF; text-transform: uppercase; letter-spacing: 2rpx; }
    .more-link { font-size: 22rpx; color: #3b82f6; font-weight: 700; }
  }
}

.grid-list {
  display: grid; grid-template-columns: 1fr 1fr; gap: 40rpx 24rpx;
  .grid-item {
    .label { font-size: 22rpx; color: #9CA3AF; margin-bottom: 8rpx; display: block; }
    .value { font-size: 28rpx; font-weight: 700; color: #1F2937; }
    .text-emerald { color: #10b981; }
  }
}

/* 4. 流水列表 */
.history-list {
  .history-item {
    display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0;
    border-bottom: 1rpx solid #F9FAFB;
    &:last-child { border-bottom: none; }
    .h-left {
      display: flex; align-items: center; gap: 20rpx;
      .h-icon {
        width: 64rpx; height: 64rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center;
        font-size: 20rpx; font-weight: 800;
        &.in { background: #ecfdf5; color: #10b981; }
        &.out { background: #F3F4F6; color: #9CA3AF; }
      }
      .h-info {
        .h-name { font-size: 26rpx; font-weight: 700; color: #374151; display: block; }
        .h-date { font-size: 18rpx; color: #9CA3AF; }
      }
    }
    .h-amt {
      font-size: 28rpx; font-weight: 700;
      &.in { color: #10b981; }
      &.out { color: #374151; }
    }
  }
}

.money { font-family: 'JetBrains Mono', monospace; }
.btn-active:active { transform: scale(0.96); opacity: 0.8; transition: 0.1s; }
</style>