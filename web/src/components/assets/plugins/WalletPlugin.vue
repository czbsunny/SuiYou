<template>
  <view class="plugin-root">
    <!-- 1. Apple Card 钱包身份卡片 -->
    <view class="card-container">
      <!-- 根据机构代码动态切换背景类 -->
      <view class="asset-card" :class="walletThemeClass">
        <view class="hero-gloss"></view>
        <view class="hero-content">
          <!-- 头部：平台标识与设置 -->
          <view class="hero-header">
            <view class="inst-info">
              <view class="platform-logo-box">
                <image v-if="platformIconUrl" :src="platformIconUrl" class="p-logo" mode="aspectFit" />
                <uni-icons v-else :type="platformIconName" size="24" :color="platformIconColor" />
              </view>
              <view class="name-box">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="inst-identifier">{{ maskIdentifier(account?.institutionIdentifier) }}</text>
              </view>
            </view>
            <!-- 设置按钮：卡片内避让胶囊 -->
            <view class="settings-btn" @tap.stop="handleSettings">
              <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
            </view>
          </view>
          
          <!-- 中间：钱包余额展示 -->
          <view class="balance-section">
            <text class="label">当前可用余额</text>
            <view class="amount-row">
              <text class="symbol">¥</text>
              <text class="num money">{{ formatNumber(asset.totalBalance) }}</text>
            </view>
          </view>

          <!-- 底部：状态标签 -->
          <view class="hero-footer">
            <view class="tags">
              <text class="tag-item">{{ asset.visibleScope === 'PRIVATE' ? '个人私有' : '家庭共享' }}</text>
              <text class="tag-item">不计利息</text>
            </view>
            <text class="update-time">刚刚更新</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 中部核心操作区 (针对钱包场景优化语义) -->
    <view class="action-grid">
      <!-- 支出是钱包最高频动作 -->
      <view class="action-card btn-active" @tap="handleAction('EXPENSE')">
        <view class="icon-box bg-expense">
          <img src="@/static/assets/actions/minus.png" class="icon-img" alt="minus">
        </view>
        <text class="action-text">记录支出</text>
      </view>
      <!-- 充值提现对应 TRANSFER 逻辑 -->
      <view class="action-card btn-active" @tap="handleAction('TRANSFER')">
        <view class="icon-box bg-transfer">
          <img src="@/static/assets/actions/repeat.png" class="icon-img" alt="repeat">
        </view>
        <text class="action-text">充值提现</text>
      </view>
      <!-- 校准解决漏记账痛点 -->
      <view class="action-card btn-active" @tap="handleAction('CALIBRATE')">
        <view class="icon-box bg-scale">
          <img src="@/static/assets/actions/scale.png" class="icon-img" alt="scale">
        </view>
        <text class="action-text">余额校准</text>
      </view>
      <!-- 收入(红包/转账) -->
      <view class="action-card btn-active" @tap="handleAction('INCOME')">
        <view class="icon-box bg-income">
          <img src="@/static/assets/actions/plus.png" class="icon-img" alt="plus">
        </view>
        <text class="action-text">记录收入</text>
      </view>
    </view>

    <!-- 3. 钱包账户状态 (解析 attributes JSON) -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">钱包账户状态</text>
        </view>
        <view class="grid-list">
          <view class="grid-item">
            <text class="label">可用金额</text>
            <text class="value money">¥ {{ formatNumber(asset.availableBalance) }}</text>
          </view>
          <view class="grid-item">
            <text class="label">提现冻结</text>
            <text class="value money" :class="{'text-red': asset.frozenBalance > 0}">¥ {{ formatNumber(asset.frozenBalance) }}</text>
          </view>
          <view class="grid-item">
            <text class="label">实名状态</text>
            <text class="value">{{ attr.verified ? '已实名' : '未实名' }}</text>
          </view>
          <view class="grid-item">
            <text class="label">计入净值</text>
            <text class="value" :class="asset.includeInNetWorth ? 'text-emerald' : 'text-gray'">{{ asset.includeInNetWorth ? '开启中' : '已关闭' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 4. 最近流水预览 -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">最近流水记录</text>
          <text class="more-link" @tap="handleSeeMore">查看全部</text>
        </view>
        <view class="history-list">
          <view v-for="(item, index) in mockHistory" :key="index" class="history-item">
            <view class="h-left">
              <view class="h-icon" :class="item.type">
                <uni-icons :type="item.icon" size="16" :color="item.type === 'in' ? '#10b981' : '#9CA3AF'"></uni-icons>
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

const props = defineProps({
  asset: Object,
  account: Object
});

const emit = defineEmits(['action', 'refresh']);

// --- 业务逻辑处理 ---

// 解析 JSON 属性
const attr = computed(() => {
  try {
    return JSON.parse(props.asset?.attributes || '{}');
  } catch (e) {
    return {};
  }
});

// 动态主题配色逻辑
const walletThemeClass = computed(() => {
  const inst = props.account?.institution?.toUpperCase();
  if (inst === 'ALIPAY') return 'card-alipay';
  if (inst === 'WECHAT') return 'card-wechat';
  return 'card-default';
});

// 动态图标处理
const platformIconUrl = computed(() => {
  // 如果后端返回了特定图标路径，可以优先展示
  return null; 
});

const platformIconName = computed(() => {
  const inst = props.account?.institution?.toUpperCase();
  if (inst === 'ALIPAY') return 'weixin'; // uni-icons没有alipay，可用weixin作为参考或自定义
  return 'wallet';
});

const platformIconColor = computed(() => {
  const inst = props.account?.institution?.toUpperCase();
  if (inst === 'ALIPAY') return '#1677FF';
  if (inst === 'WECHAT') return '#07C160';
  return '#1F2937';
});

// --- 模拟流水数据 ---
const mockHistory = [
  { name: '瑞幸咖啡 (扫码支付)', date: '今日 08:30', amount: 9.90, type: 'out', icon: 'cart-filled' },
  { name: '微信红包 (存入)', date: '昨日 22:15', amount: 8.88, type: 'in', icon: 'gift-filled' }
];

// --- 工具函数 ---
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const maskIdentifier = (str) => {
  if (!str) return '未绑定账号';
  if (str.length > 7) {
    return str.substring(0, 3) + '****' + str.substring(str.length - 4);
  }
  return str;
};

// --- 事件处理 ---
const handleAction = (type) => emit('action', type, props.asset);
const handleSettings = () => {
  uni.showActionSheet({
    itemList: ['修改钱包别名', '可见范围设置', '删除钱包'],
    success: (res) => { /* 逻辑实现 */ }
  });
};
const handleSeeMore = () => uni.navigateTo({ url: `/pages/assets/history?id=${props.asset.id}` });
</script>

<style lang="scss" scoped>
.plugin-root { padding-bottom: 60rpx; }

/* Apple Card 动态背景 */
.card-container { padding: 24rpx 32rpx; }
.asset-card {
  height: 420rpx; border-radius: 48rpx; position: relative; overflow: hidden;
  transition: all 0.3s ease;
  
  &.card-alipay { 
    background: linear-gradient(135deg, #1677FF 0%, #0050B3 100%);
    box-shadow: 0 20rpx 40rpx rgba(22, 119, 255, 0.2);
  }
  &.card-wechat { 
    background: linear-gradient(135deg, #07C160 0%, #05944A 100%);
    box-shadow: 0 20rpx 40rpx rgba(7, 193, 96, 0.2);
  }
  &.card-default { 
    background: linear-gradient(135deg, #1F2937 0%, #111827 100%);
  }

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
    .platform-logo-box { 
      width: 60rpx; height: 60rpx; border-radius: 16rpx; background: rgba(255,255,255,0.95);
      display: flex; align-items: center; justify-content: center;
      .p-logo { width: 44rpx; height: 44rpx; }
    }
    .name-box {
      .asset-name { font-size: 30rpx; font-weight: 700; display: block; }
      .inst-identifier { font-size: 18rpx; opacity: 0.6; }
    }
  }
  .settings-btn { 
    width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; 
    .icon-img {
      width: 40rpx;
      height: 40rpx;
      object-fit: contain;
    }
  }
}

.balance-section {
  .label { font-size: 20rpx; opacity: 0.7; margin-bottom: 4rpx; display: block; text-transform: uppercase; }
  .amount-row {
    display: flex; align-items: baseline; gap: 6rpx;
    .symbol { font-size: 32rpx; font-weight: 600; opacity: 0.8; }
    .num { font-size: 64rpx; font-weight: 800; font-family: 'DIN Alternate', sans-serif; }
  }
}

.hero-footer {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1rpx solid rgba(255,255,255,0.1); padding-top: 24rpx;
  .tags {
    display: flex; gap: 12rpx;
    .tag-item { font-size: 18rpx; background: rgba(0,0,0,0.15); padding: 4rpx 12rpx; border-radius: 8rpx; font-weight: 600; }
  }
  .update-time { font-size: 18rpx; opacity: 0.5; }
}

/* 2. 操作金刚区 */
.action-grid {
  padding: 16rpx 32rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx;
  .action-card {
    background: #fff; border-radius: 32rpx; padding: 28rpx;
    display: flex; align-items: center; gap: 24rpx;
    border: 1rpx solid rgba(0,0,0,0.02);
    .icon-box {
      width: 84rpx; height: 84rpx; border-radius: 22rpx;
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
    .action-text { font-size: 28rpx; font-weight: 700; color: #4B5563; }
  }
}

/* 3. 通用明细卡片 */
.section-container { padding: 12rpx 32rpx; }
.data-card {
  background: #fff; border-radius: 40rpx; padding: 40rpx;
  border: 1rpx solid rgba(0,0,0,0.02);
  .card-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx;
    .title { font-size: 20rpx; font-weight: 800; color: #9CA3AF; text-transform: uppercase; letter-spacing: 2rpx; }
    .more-link { font-size: 22rpx; color: #3b82f6; font-weight: 700; }
  }
}

.grid-list {
  display: grid; grid-template-columns: 1fr 1fr; gap: 40rpx 24rpx;
  .grid-item {
    .label { font-size: 22rpx; color: #9CA3AF; margin-bottom: 8rpx; display: block; }
    .value { font-size: 28rpx; font-weight: 700; color: #1F2937; }
    .text-emerald { color: #10b981; }
    .text-red { color: #ef4444; }
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
        width: 60rpx; height: 60rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center;
        &.in { background: #ecfdf5; }
        &.out { background: #F3F4F6; }
      }
      .h-info {
        .h-name { font-size: 26rpx; font-weight: 700; color: #374151; display: block; }
        .h-date { font-size: 18rpx; color: #9CA3AF; }
      }
    }
    .h-amt { font-size: 28rpx; font-weight: 800; &.in { color: #10b981; } &.out { color: #374151; } }
  }
}

.money { font-family: 'JetBrains Mono', monospace; }
.btn-active:active { transform: scale(0.96); opacity: 0.8; transition: 0.1s; }
</style>