<template>
  <view class="plugin-root">
    <!-- 1. Apple Card 理财身份卡片 (暖金渐变) -->
    <view class="card-container">
      <view class="asset-card" :style="{ background: cardGradient }">
        <view class="hero-gloss"></view>
        <view class="hero-content">
          <!-- 头部：图标与设置 -->
          <view class="hero-header">
            <view class="inst-info">
              <view class="icon-wrapper">
                <uni-icons type="wallet-filled" size="24" color="#D97706"></uni-icons>
              </view>
              <view class="name-box">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="inst-identifier">活期理财 | 手动维护</text>
              </view>
            </view>
            <!-- 设置按钮 -->
            <view class="settings-btn" @tap.stop="handleSettings">
              <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
            </view>
          </view>
          
          <!-- 中间：市值/余额 -->
          <view class="balance-section">
            <text class="label">当前总余额 ({{ asset.currency }})</text>
            <view class="amount-row">
              <text class="symbol">¥</text>
              <text class="num money">{{ formatNumber(asset.totalBalance) }}</text>
            </view>
          </view>

          <!-- 底部：收益简报 -->
          <view class="hero-footer">
            <view class="profit-summary">
              <view class="p-item">
                <text class="p-label">昨日收益</text>
                <text class="p-value">+{{ formatNumber(attr.lastDayProfit || 0) }}</text>
              </view>
              <view class="p-divider"></view>
              <view class="p-item">
                <text class="p-label">累计收益</text>
                <text class="p-value">{{ formatNumber(attr.totalProfit || 0) }}</text>
              </view>
            </view>
            <view class="status-tag">计入净值</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 核心操作区 (三大按钮) -->
    <view class="action-section">
      <!-- 更新收益：全宽度核心位 -->
      <view class="action-card main-action btn-active" @tap="handleAction('UPDATE_PROFIT')">
        <view class="icon-box bg-amber">
          <img src="@/static/assets/actions/hand-coins.png" class="icon-img" alt="hand-coins">
        </view>
        <view class="action-info">
          <text class="title">更新收益</text>
          <text class="desc">手动录入最新产生的收益金额</text>
        </view>
        <uni-icons type="right" size="16" color="#D1D5DB"></uni-icons>
      </view>

      <view class="sub-actions">
        <!-- 存入资金 -->
        <view class="action-card sub-item btn-active" @tap="handleAction('TRANSFER_IN')">
          <view class="icon-box bg-import">
            <img src="@/static/assets/actions/import.png" class="icon-img" alt="download">
          </view>
          <text class="sub-text">存入资金</text>
        </view>
        <!-- 取出资金 -->
        <view class="action-card sub-item btn-active" @tap="handleAction('TRANSFER_OUT')">
          <view class="icon-box bg-export">
            <img src="@/static/assets/actions/export.png" class="icon-img" alt="upload">
          </view>
          <text class="sub-text">取出资金</text>
        </view>
      </view>
    </view>

    <!-- 3. 预设理财参数 (解析 attributes) -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">预设理财参数</text>
          <uni-icons type="info-filled" size="14" color="#E5E7EB"></uni-icons>
        </view>
        <view class="attr-list">
          <view class="attr-item">
            <text class="label">预期年化利率</text>
            <text class="value money text-orange">{{ attr.expectedRate || '--' }}%</text>
          </view>
          <view class="attr-item">
            <text class="label">关联银行卡</text>
            <text class="value">{{ attr.linkedCardName || '未关联' }}</text>
          </view>
          <view class="attr-item">
            <text class="label">首次购入日期</text>
            <text class="value">{{ attr.startDate || '--' }}</text>
          </view>
          <view class="attr-item">
            <text class="label">风险评级</text>
            <text class="value text-emerald">{{ attr.riskLevel || 'R1 低风险' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 4. 最近操作日志 -->
    <view class="section-container">
      <view class="log-card">
        <text class="log-title">最近操作记录</text>
        <view class="log-list">
          <view v-for="(log, index) in mockLogs" :key="index" class="log-item">
            <text class="log-time">{{ log.time }}</text>
            <text class="log-content">{{ log.content }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  asset: Object,
  account: Object
});

const emit = defineEmits(['action', 'refresh']);

// --- 逻辑处理 ---

// 解析 JSON 属性，用于展示预设参数
const attr = computed(() => {
  try {
    return typeof props.asset?.attributes === 'string' 
      ? JSON.parse(props.asset.attributes) 
      : (props.asset?.attributes || {});
  } catch (e) {
    return {};
  }
});

// 理财类特有的橙金渐变
const cardGradient = computed(() => {
  return 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)';
});

// 模拟最近的 3 条操作日志 (实际可以根据 Transaction 接口获取)
const mockLogs = [
  { time: '昨天 09:30', content: '更新收益 +4.32' },
  { time: '12-30 14:00', content: '存入资金 +10,000.00' }
];

// --- 工具函数 ---
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

// --- 事件发送 ---
const handleAction = (type) => {
  emit('action', type, props.asset);
};

const handleSettings = () => {
  uni.showActionSheet({
    itemList: ['修改理财信息', '校准当前总额', '删除资产'],
    success: (res) => {
      // 这里的逻辑通常由父组件统一处理，或者 emit 出去
    }
  });
};
</script>

<style lang="scss" scoped>
.plugin-root { padding-bottom: 60rpx; }

/* 1. Apple Card */
.card-container { padding: 24rpx 32rpx; }
.asset-card {
  height: 440rpx; border-radius: 48rpx; position: relative; overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(217, 119, 6, 0.15);
  
  .hero-gloss {
    position: absolute; inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(0,0,0,0) 100%);
  }
  .hero-content {
    position: relative; z-index: 1; padding: 54rpx 48rpx; height: 100%;
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
  .label { font-size: 20rpx; opacity: 0.7; margin-bottom: 6rpx; display: block; letter-spacing: 1rpx; }
  .amount-row {
    display: flex; align-items: baseline; gap: 8rpx;
    .symbol { font-size: 36rpx; font-weight: 600; opacity: 0.8; }
    .num { font-size: 72rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; }
  }
}

.hero-footer {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1rpx solid rgba(255,255,255,0.15); padding-top: 24rpx;
  
  .profit-summary {
    display: flex; align-items: center; gap: 24rpx;
    .p-item {
      display: flex; flex-direction: column;
      .p-label { font-size: 16rpx; opacity: 0.6; text-transform: uppercase; margin-bottom: 2rpx; }
      .p-value { font-size: 24rpx; font-weight: 700; color: #fff; }
    }
    .p-divider { width: 1rpx; height: 32rpx; background: rgba(255,255,255,0.2); }
  }
  
  .status-tag { 
    font-size: 18rpx; background: rgba(255,255,255,0.2); 
    padding: 6rpx 16rpx; border-radius: 10rpx; font-weight: bold;
  }
}

/* 2. 操作区 */
.action-section {
  padding: 16rpx 32rpx;
  
  .action-card {
    background: #fff; border-radius: 36rpx; border: 1rpx solid rgba(0,0,0,0.02);
    display: flex; align-items: center;
  }
  
  .main-action {
    padding: 32rpx; margin-bottom: 24rpx;
    background: #fff; // 也可以用极淡的暖色调：#fffcf5;
    .icon-box { 
      width: 96rpx; height: 96rpx; border-radius: 28rpx; margin-right: 28rpx;
      display: flex; align-items: center; justify-content: center;
      &.bg-amber { background: #fef3c7; }
      .icon-img {
        max-width: 60%;
        max-height: 60%;
        object-fit: contain;
      }
    }
    .action-info {
      flex: 1;
      .title { font-size: 30rpx; font-weight: 800; color: #1F2937; display: block; }
      .desc { font-size: 22rpx; color: #9CA3AF; margin-top: 4rpx; }
    }
  }
  
  .sub-actions {
    display: flex; gap: 24rpx;
    .sub-item {
      flex: 1; flex-direction: column; padding: 40rpx 0; gap: 16rpx;
      .icon-box {
        width: 80rpx; height: 80rpx; border-radius: 50%;
        display: flex; align-items: center; justify-content: center;
        &.bg-import { background: #ecfdf5; }
        &.bg-export { background: #eff6ff; }
      }
      .sub-text { font-size: 26rpx; font-weight: 700; color: #4B5563; }
    }
  }
}

/* 3. 预设参数卡片 */
.section-container { padding: 12rpx 32rpx; }
.data-card {
  background: #fff; border-radius: 40rpx; padding: 40rpx;
  border: 1rpx solid rgba(0,0,0,0.02);
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 40rpx;
    .title { font-size: 20rpx; font-weight: 800; color: #9CA3AF; text-transform: uppercase; letter-spacing: 2rpx; }
  }
}

.attr-list {
  .attr-item {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx;
    &:last-child { margin-bottom: 0; }
    .label { font-size: 24rpx; color: #9CA3AF; font-weight: 500; }
    .value { font-size: 26rpx; font-weight: 700; color: #374151; }
    .text-orange { color: #f59e0b; }
    .text-emerald { color: #10b981; }
  }
}

/* 4. 日志样式 */
.log-card {
  background: rgba(255,255,255,0.5); border-radius: 40rpx; padding: 40rpx;
  border: 1rpx dashed #E5E7EB;
  .log-title { font-size: 20rpx; font-weight: 800; color: #9CA3AF; text-transform: uppercase; margin-bottom: 24rpx; display: block; }
  .log-item {
    display: flex; justify-content: space-between; margin-bottom: 16rpx;
    .log-time { font-size: 20rpx; color: #9CA3AF; }
    .log-content { font-size: 20rpx; color: #4B5563; font-weight: 600; }
  }
}

.money { font-family: 'JetBrains Mono', monospace; }
.btn-active:active { transform: scale(0.96); opacity: 0.8; transition: 0.1s; }
</style>