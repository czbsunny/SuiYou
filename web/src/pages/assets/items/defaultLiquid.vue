<template>
  <view class="plugin-root">
    <!-- 1. 默认流动资产卡片 -->
    <view class="card-container">
      <view class="asset-card">
        <view class="hero-gloss"></view>
        <view class="hero-content">
          <!-- 头部：资产名称与设置 -->
          <view class="hero-header">
            <view class="inst-info">
              <view class="icon-wrapper">
                <i class="fa-solid fa-wallet text-slate-700 text-xl"></i>
              </view>
              <view class="name-box">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="inst-identifier">流动资产 | 手动维护</text>
              </view>
            </view>
            <!-- 设置按钮 -->
            <view class="settings-btn" @tap.stop="handleSettings">
              <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
            </view>
          </view>
          
          <!-- 中间：余额展示 -->
          <view class="balance-section">
            <text class="label">当前余额</text>
            <view class="amount-row">
              <text class="symbol">¥</text>
              <text class="num money">{{ formatNumber(asset.totalBalance) }}</text>
            </view>
          </view>

          <!-- 底部：状态信息 -->
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

    <!-- 2. 核心操作区 -->
    <view class="action-grid">
      <view class="action-card btn-active" @tap="handleAction('CALIBRATE')">
        <view class="icon-box bg-scale">
          <img src="@/static/assets/actions/scale.png" class="icon-img" alt="scale">
        </view>
        <text class="action-text">余额校准</text>
      </view>
      <view class="action-card btn-active" @tap="handleAction('TRANSFER')">
        <view class="icon-box bg-transfer">
          <img src="@/static/assets/actions/transfer.png" class="icon-img" alt="transfer">
        </view>
        <text class="action-text">转账调拨</text>
      </view>
    </view>

    <!-- 3. 资产详情 -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">资产详情</text>
        </view>
        <view class="grid-list">
          <view class="grid-item">
            <text class="label">资产类型</text>
            <text class="value">流动资产</text>
          </view>
          <view class="grid-item">
            <text class="label">账户币种</text>
            <text class="value">{{ asset.currency === 'CNY' ? '人民币' : asset.currency }}</text>
          </view>
          <view class="grid-item">
            <text class="label">创建时间</text>
            <text class="value">{{ formatDate(asset.createdAt) }}</text>
          </view>
          <view class="grid-item">
            <text class="label">最近更新</text>
            <text class="value">{{ formatDate(asset.updatedAt) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const assetId = route.params.id;

const asset = ref({});
const account = ref({});

onMounted(() => {
  // 这里应该从API获取资产信息和账户信息
  // 暂时使用模拟数据
  fetchAssetData();
  fetchAccountData();
});

const fetchAssetData = async () => {
  try {
    // 模拟API调用
    asset.value = {
      id: assetId,
      name: '默认流动资产',
      totalBalance: 10000.00,
      currency: 'CNY',
      visibleScope: 'PRIVATE',
      includeInNetWorth: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  } catch (error) {
    console.error('获取资产信息失败:', error);
  }
};

const fetchAccountData = async () => {
  try {
    // 模拟API调用
    account.value = {};
  } catch (error) {
    console.error('获取账户信息失败:', error);
  }
};

const lastUpdateText = computed(() => '今日 14:20 更新');

// --- 工具函数 ---
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '未记录';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// --- 事件处理 ---
const handleAction = (type) => {
  console.log('Action:', type, asset.value);
  // 这里可以根据不同的action类型执行不同的逻辑
};

const handleSettings = () => {
  uni.showActionSheet({
    itemList: ['修改资产信息', '校准余额', '删除资产'],
    success: (res) => {
      console.log('点击了', res.tapIndex);
    }
  });
};
</script>

<style lang="scss" scoped>
.plugin-root {
  padding-bottom: 60rpx;
}

/* 1. 资产卡片 */
.card-container {
  padding: 24rpx 32rpx;
}
.asset-card {
  height: 420rpx; border-radius: 48rpx; position: relative; overflow: hidden;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  box-shadow: 0 20rpx 40rpx rgba(99, 102, 241, 0.2);
  
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
    .icon-wrapper { 
      width: 64rpx; height: 64rpx; border-radius: 18rpx; background: rgba(255,255,255,0.95);
      display: flex; align-items: center; justify-content: center;
    }
    .name-box {
      .asset-name { font-size: 30rpx; font-weight: 700; display: block; }
      .inst-identifier { font-size: 18rpx; opacity: 0.7; }
    }
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

/* 2. 操作区 */
.action-grid {
  padding: 20rpx 32rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx;
  .action-card {
    background: #fff; border-radius: 32rpx; padding: 28rpx 32rpx;
    display: flex; align-items: center; gap: 24rpx;
    border: 1rpx solid rgba(0,0,0,0.02);
    .icon-box {
      width: 80rpx; height: 80rpx; border-radius: 20rpx;
      display: flex; align-items: center; justify-content: center;
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
  }
}

.grid-list {
  display: grid; grid-template-columns: 1fr 1fr; gap: 40rpx 24rpx;
  .grid-item {
    .label { font-size: 22rpx; color: #9CA3AF; margin-bottom: 8rpx; display: block; }
    .value { font-size: 28rpx; font-weight: 700; color: #1F2937; }
  }
}

.money { font-family: 'JetBrains Mono', monospace; }
.btn-active:active { transform: scale(0.96); opacity: 0.8; transition: 0.1s; }
</style>