<template>
  <view class="plugin-root">
    <!-- 1. Apple Card 现金身份卡片 (石墨深灰质感) -->
    <view class="card-container">
      <view class="asset-card card-cash">
        <view class="hero-gloss"></view>
        <view class="hero-content">
          <!-- 头部：图标与设置 -->
          <view class="hero-header">
            <view class="inst-info">
              <view class="icon-wrapper">
                <i class="fa-solid fa-money-bill-1-wave text-slate-700 text-xl"></i>
              </view>
              <view class="name-box">
                <text class="asset-name">{{ asset.name }}</text>
                <text class="inst-identifier">实物持有 | 存放于: {{ attr.location || '个人钱包' }}</text>
              </view>
            </view>
            <!-- 设置按钮 -->
            <view class="settings-btn" @tap.stop="handleSettings">
              <image src="@/static/assets/actions/settings.png" class="icon-img" alt="settings"></image>
            </view>
          </view>
          
          <!-- 中间：现金余额展示 -->
          <view class="balance-section">
            <text class="label">当前持有总额</text>
            <view class="amount-row">
              <text class="symbol">¥</text>
              <text class="num money">{{ formatNumber(asset.totalBalance) }}</text>
            </view>
          </view>

          <!-- 底部：最后清点时间 -->
          <view class="hero-footer">
            <view class="audit-info">
              <text class="p-label">最后清点</text>
              <text class="p-value">{{ formatDate(asset.updatedAt) }}</text>
            </view>
            <view class="status-tag">计入净值</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 中部核心操作区 (2x2 针对现金优化) -->
    <view class="action-grid">
      <!-- 余额校准：现金最核心的动作，高亮显示 -->
      <view class="action-card btn-active calibrate-highlight" @tap="handleAction('CALIBRATE')">
        <view class="icon-box bg-emerald-solid">
          <img src="@/static/assets/actions/scale-light.png" class="icon-img" alt="scale">
        </view>
        <view class="action-info">
          <text class="action-text">清点校准</text>
          <text class="action-desc">数完钱点这里</text>
        </view>
      </view>

      <!-- 存入银行：对应 TRANSFER 逻辑 -->
      <view class="action-card btn-active" @tap="handleAction('TRANSFER')">
        <view class="icon-box bg-blue">
          <img src="@/static/assets/actions/export.png" class="icon-img" alt="export">
        </view>
        <text class="action-text">存入银行</text>
      </view>

      <!-- 记录支出 -->
      <view class="action-card btn-active" @tap="handleAction('EXPENSE')">
        <view class="icon-box bg-orange">
          <img src="@/static/assets/actions/minus.png" class="icon-img" alt="minus">
        </view>
        <text class="action-text">记录支出</text>
      </view>

      <!-- 记录收入 -->
      <view class="action-card btn-active" @tap="handleAction('INCOME')">
        <view class="icon-box bg-purple">
          <img src="@/static/assets/actions/plus.png" class="icon-img" alt="plus">
        </view>
        <text class="action-text">记录收入</text>
      </view>
    </view>

    <!-- 3. 保管详情 (解析 attributes JSON) -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">资产保管详情</text>
        </view>
        <view class="grid-list">
          <view class="grid-item">
            <text class="label">存放位置</text>
            <text class="value">{{ attr.location || '随身携带' }}</text>
          </view>
          <view class="grid-item">
            <text class="label">主要用途</text>
            <text class="value">{{ attr.purpose || '日常备用' }}</text>
          </view>
          <view class="grid-item">
            <text class="label">资产币种</text>
            <text class="value">{{ asset.currency === 'CNY' ? '人民币' : asset.currency }}</text>
          </view>
          <view class="grid-item">
            <text class="label">可见范围</text>
            <text class="value">{{ account?.visibleScope === 'PRIVATE' ? '个人私有' : '全家可见' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 4. 最近变动预览 -->
    <view class="section-container">
      <view class="data-card">
        <view class="card-header">
          <text class="title">最近清点与流水</text>
          <text class="more-link" @tap="handleSeeMore">全部明细</text>
        </view>
        <view class="history-list">
          <view v-for="(item, index) in mockHistory" :key="index" class="history-item">
            <view class="h-left">
              <view class="h-icon" :class="item.type">
                <i :class="item.icon"></i>
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

// 解析 JSON 属性
const attr = computed(() => {
  try {
    return typeof props.asset?.attributes === 'string' 
      ? JSON.parse(props.asset.attributes) 
      : (props.asset?.attributes || {});
  } catch (e) {
    return {};
  }
});

// 模拟最近的流水
const mockHistory = [
  { name: '清点校准 (自动平账)', date: '今日 14:20', amount: 5.50, type: 'in', icon: 'fa-solid fa-check' },
  { name: '日常打车支出', date: '昨日 09:10', amount: 24.00, type: 'out', icon: 'fa-solid fa-taxi' }
];

// --- 工具函数 ---
const formatNumber = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '未记录';
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// --- 事件处理 ---
const handleAction = (type) => emit('action', type, props.asset);

const handleSettings = () => {
  uni.showActionSheet({
    itemList: ['修改现金账户资料', '清点并锁定', '删除此资产'],
    success: (res) => { /* 逻辑实现 */ }
  });
};

const handleSeeMore = () => uni.navigateTo({ url: `/pages/assets/history?id=${props.asset.id}` });
</script>

<style lang="scss" scoped>
.plugin-root { padding-bottom: 60rpx; }

/* 1. Apple Card - 深色实物感 */
.card-container { padding: 24rpx 32rpx; }
.asset-card {
  height: 420rpx; border-radius: 48rpx; position: relative; overflow: hidden;
  &.card-cash {
    background: linear-gradient(135deg, #4B5563 0%, #1F2937 100%);
    box-shadow: 0 20rpx 40rpx rgba(31, 41, 55, 0.2);
  }
  
  .hero-gloss {
    position: absolute; inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(0,0,0,0) 100%);
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
      .inst-identifier { font-size: 18rpx; opacity: 0.6; }
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
  border-top: 1rpx solid rgba(255,255,255,0.1); padding-top: 24rpx;
  .audit-info {
    display: flex; flex-direction: column;
    .p-label { font-size: 16rpx; opacity: 0.5; text-transform: uppercase; margin-bottom: 2rpx; }
    .p-value { font-size: 22rpx; font-weight: 600; color: rgba(255,255,255,0.9); }
  }
  .status-tag { font-size: 18rpx; background: rgba(255,255,255,0.2); padding: 6rpx 16rpx; border-radius: 10rpx; font-weight: bold; }
}

/* 2. 操作金刚区 */
.action-grid {
  padding: 16rpx 32rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx;
  
  .action-card {
    background: #fff; border-radius: 32rpx; padding: 28rpx 32rpx;
    display: flex; align-items: center; gap: 24rpx;
    border: 1rpx solid rgba(0,0,0,0.02);
    
    .icon-box {
      width: 80rpx; height: 80rpx; border-radius: 20rpx;
      display: flex; align-items: center; justify-content: center;
      &.bg-orange { background: #fff7ed; }
      &.bg-blue { background: #eff6ff; }
      &.bg-purple { background: #ECFDF5; }
      &.bg-emerald-solid { background: #10b981; } // 校准按钮特殊处理
      .icon-img {
        max-width: 60%;
        max-height: 60%;
        object-fit: contain;
      }
    }
    .action-text { font-size: 28rpx; font-weight: 700; color: #4B5563; }
  }

  .calibrate-highlight {
    background: #f0fdf4; border: 1rpx solid #dcfce7;
    .action-info {
      display: flex; flex-direction: column;
      .action-desc { font-size: 18rpx; color: #10b981; margin-top: 2rpx; font-weight: 500; }
    }
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
        width: 60rpx; height: 60rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx;
        &.in { background: #ecfdf5; color: #10b981; }
        &.out { background: #F3F4F6; color: #9CA3AF; }
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