<template>
  <view class="asset-page">
    <!-- 顶部背景装饰 -->
    <view class="header-decoration">
        <view class="blob-white"></view>
        <view class="blob-black"></view>
    </view>

    <!-- 顶部 Tabs -->
    <view class="tabs-wrapper">
       <!-- 注意：这里使用了修正后的组件名 -->
       <AccountTabs 
          :accounts="accounts" 
          :selectedId="selectedAccountId" 
          @select="setSelectedAccountId"
          @add="openCreateModal"
          @manage="goToAccountManager"
       />
    </view>
    
    <!-- 资产总览卡片 -->
    <view class="overview-wrapper">
      <AssetOverview 
          :totalAssets="displayTotalAssets"
          :totalReturn="displayTotalReturn"
          :dailyReturn="displayDailyReturn"
          :showBalance="showBalance"
          @toggleBalance="showBalance = !showBalance"
      />
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 汇总视图 -->
      <view v-if="selectedAccountId === 'SUMMARY'" class="fade-in-content">
          <view class="section-header">
              <text class="section-title">账户列表</text>
              <text class="count-badge">{{ accounts.length }}个账户</text>
          </view>
          
          <view class="account-list">
              <AccountCard 
                  v-for="acc in accounts"
                  :key="acc.id" 
                  :account="acc" 
                  :showBalance="showBalance"
                  @edit="openEditModal"
                  @click="setSelectedAccountId(acc.id)"
              />
              
              <!-- 添加新账户按钮 -->
              <view @click="openCreateModal" class="add-account-btn">
                <image src="/static/images/plus-gray.png" class="add-icon" /> 
                <text>添加新资产账户</text>
              </view>
          </view>
      </view>
      
      <!-- 单个账户详情视图 -->
      <view v-else-if="selectedAccount" class="fade-in-content">
         <HoldingList 
            :account="selectedAccount" 
            :holdings="selectedAccount.holdings || []"
            @import="triggerFileUpload" 
            @analyze="handleAnalyzeClick"
         />
      </view>
    </view>

    <!-- 弹窗组件 (Manager已移除，改为跳转) -->
    <AccountModal 
      v-if="isModalOpen"
      :isOpen="isModalOpen"
      :initialData="editingAccount"
      @close="isModalOpen = false"
      @save="handleSaveAccount"
      @delete="handleDeleteAccount"
    />
    
    <PortfolioSyncModal 
      v-if="isSyncModalOpen"
      :isOpen="isSyncModalOpen"
      :accountName="selectedAccount?.name || ''"
      @close="isSyncModalOpen = false"
      @confirm="handleSyncConfirm"
    />
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { 
    getAssetAccounts, createAssetAccount, updateAssetAccount, deleteAssetAccount, 
    getAccountHoldings, syncAssetAccountToPortfolio
} from '../../services/accountService';

// 引入修正后的同目录组件
import AssetOverview from './overview.vue';
import AccountCard from './card.vue';
import AccountModal from './modal.vue';
import AccountTabs from './tabs.vue';
import HoldingList from './holdingList.vue';
import PortfolioSyncModal from './syncModal.vue';

const accounts = ref([]);
const selectedAccountId = ref('SUMMARY');
const showBalance = ref(true);
const loading = ref(false);
const isModalOpen = ref(false);
const editingAccount = ref(null);
const isSyncModalOpen = ref(false);

const loadData = async () => {
  loading.value = true;
  try {
      const data = await getAssetAccounts();
      accounts.value = data;
  } catch (e) {
      console.error(e);
  } finally {
      loading.value = false;
  }
};

const loadHoldings = async (id) => {
    try {
        const holdings = await getAccountHoldings(id);
        accounts.value = accounts.value.map(a => a.id === id ? { ...a, holdings } : a);
    } catch (e) {
        console.error("Failed to load holdings", e);
    }
};

onShow(() => {
    loadData();
});

watch(selectedAccountId, (newId) => {
    if (newId !== 'SUMMARY') loadHoldings(newId);
});

const totalAsset = computed(() => accounts.value.reduce((acc, curr) => acc + curr.totalAsset, 0));
const dailyReturn = computed(() => accounts.value.reduce((acc, curr) => acc + curr.dailyReturn, 0));
const totalReturn = computed(() => accounts.value.reduce((acc, curr) => acc + curr.totalReturn, 0));

const selectedAccount = computed(() => accounts.value.find(a => a.id === selectedAccountId.value));
const displayTotalAssets = computed(() => selectedAccountId.value === 'SUMMARY' ? totalAsset.value : selectedAccount.value?.totalAsset ?? 0);
const displayTotalReturn = computed(() => selectedAccountId.value === 'SUMMARY' ? totalReturn.value : selectedAccount.value?.totalReturn ?? 0);
const displayDailyReturn = computed(() => selectedAccountId.value === 'SUMMARY' ? dailyReturn.value : selectedAccount.value?.dailyReturn ?? 0);

const triggerFileUpload = () => {
    uni.showToast({ title: '图片识别导入功能暂未开放', icon: 'none' });
};

const openCreateModal = () => {
    editingAccount.value = null;
    isModalOpen.value = true;
};
const openEditModal = (acc) => {
    editingAccount.value = acc;
    isModalOpen.value = true;
};

const setSelectedAccountId = (id) => selectedAccountId.value = id;

// 跳转到账户管理页面
const goToAccountManager = () => {
    uni.navigateTo({
        url: '/components/asset/manager'
    });
};

const handleSaveAccount = async (data) => {
    if (editingAccount.value) {
        const updated = await updateAssetAccount(editingAccount.value.id, data);
        accounts.value = accounts.value.map(acc => acc.id === updated.id ? { ...acc, ...updated } : acc);
        loadData();
    } else {
        const created = await createAssetAccount(data);
        accounts.value = [...accounts.value, created];
    }
};

const handleDeleteAccount = async (id) => {
    await deleteAssetAccount(id);
    accounts.value = accounts.value.filter(a => a.id !== id);
    if(selectedAccountId.value === id) selectedAccountId.value = 'SUMMARY';
};

const handleAnalyzeClick = () => isSyncModalOpen.value = true;

const handleSyncConfirm = async () => {
    if (!selectedAccount.value) return;
    loading.value = true;
    try {
        await syncAssetAccountToPortfolio(selectedAccount.value.id);
        isSyncModalOpen.value = false;
        uni.showToast({ title: '同步成功', icon: 'success' });
    } catch(e) {
        console.error(e);
        uni.showToast({ title: '同步失败', icon: 'none' });
    } finally {
        loading.value = false;
    }
};
</script>

<style lang="scss" scoped>
.asset-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 96px; /* pb-24 */
  position: relative;
  width: 100%;
}

/* 顶部背景装饰 */
.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 220px;
  background: linear-gradient(to bottom, $primary, $primary-dark);
  border-bottom-left-radius: 48px;
  border-bottom-right-radius: 48px;
  z-index: 0;
  overflow: hidden;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);

  .blob-white {
    position: absolute;
    top: -20%;
    right: -10%;
    width: 300px;
    height: 300px;
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    filter: blur(64px); /* blur-3xl */
    pointer-events: none;
  }

  .blob-black {
    position: absolute;
    bottom: 10%;
    left: -10%;
    width: 200px;
    height: 200px;
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 50%;
    filter: blur(40px); /* blur-2xl */
    pointer-events: none;
  }
}

/* 布局容器 */
.tabs-wrapper {
  position: relative;
  z-index: 10;
  padding: 0x 8px 0 8px; /* pt-4 px-2 */
}

.overview-wrapper {
  padding: 0 20px; /* px-5 */
  position: relative;
  z-index: 10;
  margin-bottom: 12px; /* mb-6 */
}

.main-content {
  padding: 0 16px; /* px-4 */
  position: relative;
  z-index: 10;
  margin-top: 24px;
}

/* 汇总视图样式 */
.fade-in-content {
  animation: fadeIn 0.3s ease-out;
  padding-bottom: 24px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 16px; /* mb-4 */
  margin-bottom: 16px; /* mb-4 */
  padding: 0 4px; /* px-1 */
}

.section-title {
  font-size: 16px; /* text-base */
  font-weight: bold;
  color: $text-main;
  margin-right: 12px;
}

.count-badge {
  padding: 2px 8px; /* px-2 py-0.5 */
  background-color: $bg-subtle-hover; /* bg-gray-light */
  color: $text-regular;
  font-size: 12px; /* text-[12px] */
  border-radius: 9999px;
  font-weight: 500;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 12px; /* space-y-3 */
}

/* 添加账户按钮 */
.add-account-btn {
  width: 100%;
  padding: 16px 0; /* py-4 */
  border: 2px dashed $border-color;
  border-radius: 16px; /* rounded-2xl */
  color: $text-muted; /* text-gray-400 */
  font-size: 14px; /* text-sm */
  font-weight: 500;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.5); /* bg-white/50 */
  transition: all 0.3s;
  box-sizing: border-box;

  &:active {
    background-color: #ffffff;
    border-color: $primary;
    color: $primary;
  }
}

.add-icon {
  width: 18px; /* size 18 */
  height: 18px;
  margin-right: 8px; /* mr-2 */
}
</style>