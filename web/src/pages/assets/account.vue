<!-- pages/assets/account/Account.vue -->
<template>
  <view class="account-dispatcher" v-if="account">
    <scroll-view scroll-y class="main-scroll">
      <component 
        :is="currentTemplate" 
        :account="account" 
        :assets="childAssets"
        @action="handleAccountAction"
        @asset-click="handleAssetClick"
        @settings="handleSettings"
      />
    </scroll-view>
  </view>

  <view v-else class="loading-state">
    <uni-load-more status="loading" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

// 导入不同业态的模板
import BankAccountTemplate from '@/components/assets/templates/BankAccountTemplate.vue';
import WalletAccountTemplate from '@/components/assets/templates/WalletAccountTemplate.vue';
// 预留其他模板入口
// import SecurityAccountTemplate from '@/components/assets/templates/SecurityAccountTemplate.vue';

const configStore = useConfigStore();
const account = ref(null);
const childAssets = ref([]);
const accountId = ref(null);

onLoad((options) => {
  accountId.value = options.id;
});

onMounted(() => {
  fetchAccountData();
});

// 获取账户及其关联资产
const fetchAccountData = async () => {
  try {
    // 2. 过滤属于该账户的所有资产项
    const res = await getAssets();
    childAssets.value = res.assets.filter(a => String(a.accountId) === String(accountId.value));
    account.value = {
        id: accountId.value,
        name: childAssets.value[0].institutionIdentifier || childAssets.value[0].accountName,
        totalBalance: childAssets.value.reduce((sum, a) => sum + Number(a.totalBalance), 0),
        institution: childAssets.value[0].institution,
        instType: configStore.institutionMap[childAssets.value[0].institution]?.instType || 'BANK'
    }
  } catch (e) {
    console.error('加载账户数据失败', e);
  }
};

// 🟢 模板分发逻辑
const currentTemplate = computed(() => {
  if (!account.value) return null;
  
  // 从 institution 配置中获取该机构所属的业态
  const instInfo = configStore.institutionMap[account.value.institution];
  const sector = instInfo?.instType || 'BANK';

  const map = {
    'BANK': BankAccountTemplate,
    'INTERNET_BANK': BankAccountTemplate, // 互联网银行复用银行模板
    'FOREIGN_BANK': BankAccountTemplate,  // 外资银行复用银行模板
    'PAYMENT': WalletAccountTemplate,     // 支付平台用钱包模板
    'OTHER': BankAccountTemplate          // 兜底用银行模板
  };

  return map[sector] || BankAccountTemplate;
});

// 处理资产项点击（跳转到 AssetItemDetail）
const handleAssetClick = (asset) => {
  uni.navigateTo({
    url: `/pages/assets/item?id=${asset.id}`
  });
};

// 处理账户层级的操作（转账、对账等）
const handleAccountAction = (type) => {
  console.log('账户操作:', type);
  // 这里跳转到具体的交易录入页
};

// 处理账户设置
const handleSettings = () => {
  uni.navigateTo({
    url: `/pages/assets/account-edit?id=${accountId.value}`
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
  overflow: hidden;
}

.loading-state {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>