<template>
  <view class="account-dispatcher" v-if="account">
    <scroll-view scroll-y class="main-scroll">
      
      <!-- 🟢 核心修改：使用静态分支判断代替动态组件 -->
      <view class="template-container">
        
        <!-- 1. 银行类模板 (涵盖：传统银行、互联网银行、外资银行及其他) -->
        <BankAccountTemplate 
          v-if="isBankSector"
          :account="account" 
          :assets="childAssets"
          @action="handleAccountAction"
          @asset-click="handleAssetClick"
          @settings="handleSettings"
        />

        <!-- 2. 支付钱包类模板 (微信、支付宝等) -->
        <WalletAccountTemplate 
          v-else-if="sector === 'PAYMENT'"
          :account="account" 
          :assets="childAssets"
          @action="handleAccountAction"
          @asset-click="handleAssetClick"
          @settings="handleSettings"
        />

        <!-- 3. 兜底模板 -->
        <BankAccountTemplate 
          v-else
          :account="account" 
          :assets="childAssets"
          @action="handleAccountAction"
          @asset-click="handleAssetClick"
          @settings="handleSettings"
        />
        
      </view>

      <!-- 底部安全区占位 -->
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>

  <!-- 全屏加载状态 -->
  <view v-else class="loading-state">
    <uni-load-more status="loading" iconType="snow" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

// 🟢 必须显式导入所有模板组件以支持小程序编译
import BankAccountTemplate from '@/components/assets/templates/BankAccountTemplate.vue';
import WalletAccountTemplate from '@/components/assets/templates/WalletAccountTemplate.vue';

const configStore = useConfigStore();
const account = ref(null);
const childAssets = ref([]);
const accountId = ref(null);

onLoad((options) => {
  if (options.id) {
    accountId.value = options.id;
  }
});

// 使用 onShow 确保从设置页返回时能刷新数据
onShow(() => {
  if (accountId.value) {
    fetchAccountData();
  }
});

/**
 * 获取账户及其关联资产
 */
const fetchAccountData = async () => {
  try {
    const res = await getAssets();
    // 1. 过滤属于该账户的所有资产项
    const filteredAssets = res.assets.filter(a => String(a.accountId) === String(accountId.value));
    childAssets.value = filteredAssets;

    if (filteredAssets.length > 0) {
      const firstAsset = filteredAssets[0];
      const instInfo = configStore.getInstitutionByCode(firstAsset.institution);
      
      // 2. 组装账户视图模型
      account.value = {
        id: accountId.value,
        // 优先显示用户起的别名，否则显示机构识别码
        name: firstAsset.accountName || firstAsset.institutionIdentifier || '我的账户',
        totalBalance: filteredAssets.reduce((sum, a) => sum + Number(a.totalBalance), 0),
        institution: firstAsset.institution,
        instInfo: instInfo,
        instType: instInfo?.instType || 'BANK'
      };
    } else {
      uni.showToast({ title: '账户不存在或已移除', icon: 'none' });
      setTimeout(() => uni.navigateBack(), 1500);
    }
  } catch (e) {
    console.error('加载账户数据失败:', e);
  }
};

/**
 * 🟢 业态判定逻辑
 */
const sector = computed(() => account.value?.instType || 'BANK');

// 判定是否属于银行系（用于合并 v-if 判断）
const isBankSector = computed(() => {
  const s = sector.value;
  return s === 'BANK' || s === 'INTERNET_BANK' || s === 'FOREIGN_BANK' || s === 'OTHER';
});

/**
 * 路由与交互处理
 */
const handleAssetClick = (asset) => {
  uni.navigateTo({
    url: `/pages/assets/item?id=${asset.id}`
  });
};

const handleAccountAction = (type) => {
  // 根据 type 跳转转账或修正页面
  console.log('触发账户操作:', type);
};

const handleSettings = () => {
  uni.navigateTo({
    url: `/pages/assets/add-account?id=${accountId.value}&mode=edit`
  });
};
</script>

<style lang="scss" scoped>
.account-dispatcher {
  min-height: 100vh;
  background-color: #F9F8F4;
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
  align-items: center;
  justify-content: center;
  background-color: #F9F8F4;
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}

.template-container {
  width: 100%;
}
</style>