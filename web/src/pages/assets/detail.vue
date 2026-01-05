<template>
  <view class="detail-container" v-if="asset">
    <!-- 1. 顶部资产卡片 (还原 Apple Card 质感) -->
    <view class="asset-hero" :style="{ backgroundColor: categoryColor }">
      <view class="hero-gloss"></view>
      <view class="hero-content">
        <view class="hero-header">
          <view class="inst-info">
            <image :src="instInfo?.logoUrl" class="inst-logo" mode="aspectFit" />
            <text class="inst-name">{{ instInfo?.instName || '其他机构' }}</text>
          </view>
          <view class="status-badge">正常</view>
        </view>
        
        <view class="balance-section">
          <text class="label">当前余额 ({{ asset.currency }})</text>
          <view class="amount-row">
            <text class="symbol">¥</text>
            <text class="num">{{ formatNumber(asset.totalBalance) }}</text>
          </view>
        </view>

        <view class="hero-footer">
          <text class="account-name">{{ asset.name }}</text>
          <text class="account-type">{{ subCategoryName }}</text>
        </view>
      </view>
    </view>

    <!-- 2. 数据明细面板 -->
    <view class="info-group">
      <view class="info-card">
        <view class="info-item">
          <text class="info-label">冻结金额</text>
          <text class="info-value">¥ {{ formatNumber(asset.frozenBalance) }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <text class="info-label">可见范围</text>
          <text class="info-value">{{ asset.visibleScope === 'PRIVATE' ? '仅自己可见' : '家庭共享' }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <text class="info-label">计入净值</text>
          <text class="info-value">{{ asset.includeInNetWorth ? '是' : '否' }}</text>
        </view>
      </view>
    </view>

    <!-- 3. 时间属性 -->
    <view class="info-group">
      <view class="info-card">
        <view class="info-item">
          <text class="info-label">创建时间</text>
          <text class="info-value">{{ formatDate(asset.createdAt) }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-item">
          <text class="info-label">最后更新</text>
          <text class="info-value">{{ formatDate(asset.updatedAt) }}</text>
        </view>
      </view>
    </view>

    <!-- 4. 底部操作栏 -->
    <view class="action-footer">
      <view class="btn btn-edit" @click="handleEdit">
        <uni-icons type="compose" size="18" color="#fff"></uni-icons>
        <text>修改资料</text>
      </view>
      <view class="btn btn-delete" @click="confirmDelete">
        <uni-icons type="trash" size="18" color="#FF3B30"></uni-icons>
        <text>删除资产</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

const configStore = useConfigStore();
const asset = ref(null);
const assetId = ref(null);

onLoad(async (options) => {
  assetId.value = options.id;
  await fetchDetail();
});

// 获取详情（由于后端目前没提供单独详情接口，我们从列表里找）
const fetchDetail = async () => {
  try {
    const res = await getAssets();
    asset.value = res.assets.find(a => String(a.id) === String(assetId.value));
    if (!asset.value) {
      uni.showToast({ title: '资产不存在', icon: 'none' });
      return;
    }
    if (String(asset.value.ownerId) !== String(configStore.userId)) {
      uni.showToast({ title: '您没有权限查看此资产', icon: 'none' });
      return;
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

// --- 数据映射 ---
const instInfo = computed(() => {
  return configStore.institutionMap[asset.value?.institution] || {};
});

const categoryColor = computed(() => {
  const cat = configStore.assetCategories.find(c => c.categoryCode === asset.value?.category);
  return cat?.color || '#2A806C';
});

const subCategoryName = computed(() => {
  const sub = configStore.getSubCategoriesByCode(asset.value?.category)
    .find(s => String(s.id) === String(asset.value?.subCategory));
  return sub?.name || '普通资产';
});

// --- 工具函数 ---
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0.00';
  return Number(num).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// --- 动作处理 ---
const handleEdit = () => {
  uni.navigateTo({ url: `/pages/assets/add?id=${assetId.value}&mode=edit` });
};

const confirmDelete = () => {
  uni.showModal({
    title: '删除确认',
    content: '确定要删除该资产账户吗？此操作不可撤销。',
    confirmColor: '#FF3B30',
    success: (res) => {
      if (res.confirm) {
        // 调用删除接口逻辑...
        uni.showToast({ title: '已删除' });
        setTimeout(() => uni.navigateBack(), 1500);
      }
    }
  });
};
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #F9F8F4;
  padding: 30rpx;
  padding-bottom: 180rpx;
}

/* 顶部英雄卡片 */
.asset-hero {
  height: 420rpx;
  border-radius: 48rpx;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.1);
  margin-bottom: 40rpx;
  
  .hero-gloss {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(0,0,0,0) 100%);
  }

  .hero-content {
    position: relative;
    z-index: 1;
    padding: 50rpx;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    color: #fff;
  }
}

.hero-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .inst-info {
    display: flex;
    align-items: center;
    gap: 16rpx;
    .inst-logo { width: 50rpx; height: 50rpx; border-radius: 12rpx; background: rgba(255,255,255,0.9); }
    .inst-name { font-size: 30rpx; font-weight: 600; }
  }
  .status-badge {
    font-size: 20rpx;
    background: rgba(255,255,255,0.2);
    padding: 6rpx 16rpx;
    border-radius: 10rpx;
  }
}

.balance-section {
  .label { font-size: 24rpx; opacity: 0.8; margin-bottom: 10rpx; display: block; }
  .amount-row {
    display: flex;
    align-items: baseline;
    gap: 10rpx;
    .symbol { font-size: 40rpx; font-weight: 600; }
    .num { font-size: 72rpx; font-weight: 700; font-family: 'DIN Alternate', sans-serif; }
  }
}

.hero-footer {
  display: flex;
  justify-content: space-between;
  border-top: 1rpx solid rgba(255,255,255,0.1);
  padding-top: 30rpx;
  .account-name { font-size: 28rpx; font-weight: 500; }
  .account-type { font-size: 24rpx; opacity: 0.7; }
}

/* 信息卡片组 */
.info-group {
  margin-bottom: 30rpx;
  .info-card {
    background: #fff;
    border-radius: 32rpx;
    padding: 0 30rpx;
  }
  .info-item {
    height: 100rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .info-label { font-size: 28rpx; color: #6B7280; }
    .info-value { font-size: 28rpx; color: #1F2937; font-weight: 500; }
  }
  .divider { height: 1rpx; background: #F3F4F6; }
}

/* 底部按钮 */
.action-footer {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  padding: 30rpx 50rpx 60rpx;
  background: rgba(249, 248, 244, 0.95);
  display: flex;
  gap: 30rpx;

  .btn {
    flex: 1;
    height: 100rpx;
    border-radius: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    font-size: 28rpx;
    font-weight: 600;
    transition: all 0.2s;
    
    &:active { transform: scale(0.96); }
  }
  .btn-edit { background: #1F2937; color: #fff; }
  .btn-delete { background: #fff; color: #FF3B30; border: 1rpx solid #FF3B30; }
}
</style>