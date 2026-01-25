<template>
  <!-- 外壳容器 -->
  <view class="detail-shell" v-if="asset">
    <scroll-view scroll-y class="main-scroll">
      
      <!-- 🟢 重点：使用静态分支判断代替动态组件 -->
      <view class="plugin-content">
        
        <!-- 1. 银行储蓄卡插件 -->
        <DebitCardPlugin
          v-if="asset.subCategory === 'DEBIT_CARD'"
          :asset="asset"
          :account="account"
          @refresh="fetchDetail"
        />

        <!-- 2. 电子钱包插件 -->
        <WalletPlugin
          v-else-if="asset.subCategory === 'E_WALLET'"
          :asset="asset"
          :account="account"
          @refresh="fetchDetail"
        />

        <!-- 3. 活期理财插件 -->
        <CashPlusPlugin
          v-else-if="asset.subCategory === 'CASH_PLUS'"
          :asset="asset"
          :account="account"
          @refresh="fetchDetail"
        />

        <!-- 4. 现金插件 -->
        <CashPlugin
          v-else-if="asset.subCategory === 'CASH'"
          :asset="asset"
          :account="account"
          @refresh="fetchDetail"
        />

        <!-- 5. 兜底处理：如果是不支持的子分类 -->
        <view v-else class="unsupported-type">
          <uni-icons type="info" size="30" color="#9CA3AF" />
          <text class="tip-text">该资产项暂无专用详情插件</text>
          <text class="sub-tip">基础信息请前往账户详情查看</text>
        </view>
        
      </view>
      
      <!-- 底部安全区占位 -->
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>

  <!-- 全屏加载状态 -->
  <view v-else class="loading-state">
    <uni-load-more status="loading" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

// 显式导入所有业务插件组件
import DebitCardPlugin from '@/components/assets/plugins/DebitCardPlugin.vue';
import WalletPlugin from '@/components/assets/plugins/WalletPlugin.vue';
import CashPlusPlugin from '@/components/assets/plugins/CashPlusPlugin.vue';
import CashPlugin from '@/components/assets/plugins/CashPlugin.vue';

const configStore = useConfigStore();
const asset = ref(null);
const account = ref(null); // 如果有账户联动数据可在此填充
const assetId = ref(null);

onLoad((options) => {
  if (options.id) {
    assetId.value = options.id;
    fetchDetail();
  }
});

/**
 * 获取资产详情
 */
const fetchDetail = async () => {
  try {
    const res = await getAssets();
    // 从全量资产中匹配当前 ID 的资产项
    const foundAsset = res.assets.find(a => String(a.id) === String(assetId.value));
    
    if (foundAsset) {
      // 自动关联机构信息 (Logo/名称等)
      foundAsset.instInfo = configStore.getInstitutionByCode(foundAsset.institution);
      asset.value = foundAsset;
    } else {
      uni.showToast({ title: '资产已移除或不存在', icon: 'none' });
      setTimeout(() => uni.navigateBack(), 1500);
    }
  } catch (e) {
    console.error('Fetch Asset Detail Error:', e);
  }
};
</script>

<style lang="scss" scoped>
.detail-shell {
  min-height: 100vh;
  background: #F9F8F4; // 奶油色背景
  display: flex;
  flex-direction: column;
}

.main-scroll {
  flex: 1;
  width: 100%;
}

.loading-state {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F9F8F4;
}

.unsupported-type {
  padding: 100rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .tip-text {
    font-size: 30rpx;
    font-weight: 700;
    color: #4B5563;
    margin-top: 20rpx;
  }
  
  .sub-tip {
    font-size: 24rpx;
    color: #9CA3AF;
    margin-top: 10rpx;
  }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>