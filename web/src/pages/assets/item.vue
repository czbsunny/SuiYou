<!-- pages/assets/detail/Item.vue -->
<template>
  <view class="detail-shell" v-if="asset">
    <scroll-view scroll-y class="main-scroll">
      <component 
        :is="currentPlugin" 
        :asset="asset" 
        :account="account"
        @refresh="fetchDetail"
      />
    </scroll-view>
  </view>

  <view v-else class="loading-state">
    <uni-load-more status="loading" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useConfigStore } from '@/stores/config.js';
import { getAssets } from '@/services/assetService.js';

// 导入所有业务插件
import DebitCardPlugin from '@/components/assets/plugins/DebitCardPlugin.vue';
import WalletPlugin from '@/components/assets/plugins/WalletPlugin.vue';
import CashPlusPlugin from '@/components/assets/plugins/CashPlusPlugin.vue';
import CashPlugin from '@/components/assets/plugins/CashPlugin.vue';
import { onLoad } from '@dcloudio/uni-app';

const configStore = useConfigStore();
const asset = ref(null);
const account = ref(null);
const assetId = ref(null);

onLoad(async (options) => {
  assetId.value = options.id;

  fetchDetail();
});

const fetchDetail = async () => {
  try {
    const res = await getAssets();
    console.log(res);
    const foundAsset = res.assets.find(a => String(a.id) === String(assetId.value));
    if (foundAsset) {
      foundAsset.instInfo = configStore.getInstitutionByCode(foundAsset.institution);
      asset.value = foundAsset;
    } else {
      uni.showToast({ title: '资产不存在', icon: 'none' });
    }
  } catch (e) {
    console.error(e);
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
  return map[asset.value.subCategory] || null;
});

</script>

<style lang="scss" scoped>
.detail-shell {
  min-height: 100vh; background: #F9F8F4; display: flex; flex-direction: column;
}
.main-scroll { flex: 1; overflow: hidden; }
.loading-state { height: 100vh; display: flex; align-items: center; justify-content: center; }
</style>