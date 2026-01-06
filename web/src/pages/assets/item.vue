<!-- pages/assets/detail/Item.vue -->
<template>
  <!-- 关键：增加 v-if="asset" 保护，确保数据加载完再渲染，否则会报错 -->
  <view class="detail-shell" v-if="asset">
    <scroll-view scroll-y="true" class="scroll-content">
      <!-- 1 核心概览卡片 -->
      <view class="basic-info-card">
        <view class="asset-main">
          <image :src="asset.instInfo?.logoUrl || '/static/icons/default-bank.png'" class="type-icon" mode="aspectFit" />
          <view class="name-box">
            <text class="name">{{ asset.name }}</text>
            <text class="institution">{{ asset.instInfo?.instName || '未知机构' }}</text>
          </view>
          <view class="status-tag">流动资产</view>
        </view>
        
        <view class="value-row">
          <text class="label">当前余额 (元)</text>
          <text class="amount money">{{ formatAmount(asset.totalBalance) }}</text>
        </view>
      </view>

      <!-- 2. 动态插件区 -->
      <component 
        :is="currentPlugin" 
        :extData="asset" 
      />

      <!-- 3. 辅助信息区 -->
      <view class="info-group">
        <view class="group-header">
          <view class="line"></view>
          <text class="group-title">备注信息</text>
        </view>
        <view class="remark-content">
          {{ asset.remark || '暂无备注说明' }}
        </view>
      </view>
      
      <!-- 4. 链接项 -->
      <view class="link-group">
        <view class="list-link-item" @tap="toHistory">
          <text class="link-label">最近流水</text>
          <view class="right-info">
            <text>查看全部</text>
            <uni-icons type="right" size="14" color="#9CA3AF" />
          </view>
        </view>
      </view>
      
      <!-- 占位防止底部遮挡 -->
      <view class="safe-area-inset"></view>
    </scroll-view>

    <!-- 5. 底部悬浮操作栏 -->
    <view class="footer-actions">
      <view class="action-btn secondary" @tap="onEdit">
        <uni-icons type="compose" size="18" color="#2A806C" />
        <text>修改信息</text>
      </view>
      <view class="action-btn primary" @tap="onTransfer">
        <text>余额调拨</text>
      </view>
    </view>
  </view>

  <!-- 加载状态 -->
  <view v-else class="loading-state">
    <uni-load-more status="loading" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

// 导入插件
import DebitCardPlugin from '@/components/assets/plugins/DebitCardPlugin.vue';
import WalletPlugin from '@/components/assets/plugins/WalletPlugin.vue';
import CashPlusPlugin from '@/components/assets/plugins/CashPlusPlugin.vue';
import CashPlugin from '@/components/assets/plugins/CashPlugin.vue';

const configStore = useConfigStore();
const asset = ref(null);
const assetId = ref(null);

onLoad(async (options) => {
  assetId.value = options.id;
  await fetchDetail();
});

const fetchDetail = async () => {
  try {
    const res = await getAssets();
    const found = res.assets.find(a => String(a.id) === String(assetId.value));
    if (found) {
      // 补充机构信息
      found.instInfo = configStore.getInstitutionByCode(found.institution);
      console.log(found);
      asset.value = found;
    } else {
      uni.showToast({ title: '资产不存在', icon: 'none' });
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

const currentPlugin = computed(() => {
  if (!asset.value) return null;
  const map = {
    'DEBIT_CARD': DebitCardPlugin,
    'E_WALLET': WalletPlugin,
    'CASH_PLUS': CashPlusPlugin,
    'CASH': CashPlugin
  };
  return map[asset.value.categoryCode] || null;
});

const goBack = () => uni.navigateBack();
const formatAmount = (val) => Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });

const onEdit = () => uni.showToast({title: '去编辑'});
const onTransfer = () => uni.showToast({title: '去调拨'});
const toHistory = () => uni.showToast({title: '去流水'});
</script>

<style lang="scss" scoped>
.detail-shell {
  min-height: 100vh; background: #F9F8F4; display: flex; flex-direction: column;
}

.scroll-content { flex: 1; }

/* 2. 基础卡片 */
.basic-info-card {
  background: #fff; margin: 24rpx 32rpx; border-radius: 32rpx; padding: 48rpx 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  .asset-main {
    display: flex; align-items: center; margin-bottom: 48rpx;
    .type-icon { width: 96rpx; height: 96rpx; border-radius: 24rpx; margin-right: 24rpx; }
    .name-box {
      flex: 1;
      .name { font-size: 36rpx; font-weight: 800; color: #1F2937; display: block; }
      .institution { font-size: 24rpx; color: #9CA3AF; margin-top: 6rpx; }
    }
    .status-tag { font-size: 20rpx; background: rgba(42, 128, 108, 0.1); color: #2A806C; padding: 4rpx 16rpx; border-radius: 8rpx; font-weight: bold; }
  }
  .value-row {
    .label { font-size: 24rpx; color: #9CA3AF; margin-bottom: 12rpx; display: block; }
    .amount { font-size: 60rpx; font-weight: 800; color: #1F2937; font-family: 'DIN Alternate', sans-serif; }
  }
}

/* 4. 备注区 */
.info-group {
  background: #fff; margin: 0 32rpx 24rpx; border-radius: 32rpx; padding: 32rpx;
  .group-header {
    display: flex; align-items: center; margin-bottom: 20rpx;
    .line { width: 6rpx; height: 24rpx; background: #2A806C; border-radius: 4rpx; margin-right: 16rpx; }
    .group-title { font-size: 26rpx; font-weight: 800; color: #4B5563; }
  }
  .remark-content { font-size: 26rpx; color: #6B7280; line-height: 1.6; }
}

/* 5. 链接项 */
.link-group {
  margin: 0 32rpx;
  .list-link-item {
    background: #fff; border-radius: 24rpx; padding: 32rpx; display: flex; justify-content: space-between; align-items: center;
    .link-label { font-size: 28rpx; font-weight: 700; color: #374151; }
    .right-info { display: flex; align-items: center; gap: 8rpx; text { font-size: 24rpx; color: #9CA3AF; } }
  }
}

/* 6. 底部按钮 */
.footer-actions {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
  display: flex; gap: 24rpx; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.03);
  .action-btn {
    flex: 1; height: 96rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; gap: 12rpx;
    font-size: 30rpx; font-weight: 800;
    &.secondary { background: #F1F5F9; color: #2A806C; }
    &.primary { background: #2A806C; color: #FFFFFF; box-shadow: 0 8rpx 16rpx rgba(42, 128, 108, 0.2); }
    &:active { transform: scale(0.98); opacity: 0.9; }
  }
}

.safe-area-inset { height: 180rpx; }
.loading-state { height: 100vh; display: flex; align-items: center; justify-content: center; background: #F9F8F4; }
</style>