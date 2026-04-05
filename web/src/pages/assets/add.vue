<template>
  <view class="add-asset-page">
    <scroll-view scroll-y class="content-scroll">
      <view class="content-container">
        <!-- 1. 顶部上下文：展示已选分类和机构，允许返回重选 -->
        <AssetContextCard 
          :category-name="categoryName"
          :institution="institution"
          @reselect="goBackToReselect"
        />

        <!-- 2. 账户容器信息 (处理标识码和复用逻辑) -->
        <AccountFormCard 
          v-model:identifier="form.identifier"
          v-model:accountName="form.accountName"
          :inst-code="instCode"
          :category-code="subCategoryCode"
          :available-accounts="availableAccounts"
        />

        <!-- 3. 资产基础信息 -->
        <AssetFormCard 
          v-model:assetName="form.assetName"
          v-model:amount="form.amount"
          v-model:currency="form.currency"
          v-model:includeInNetWorth="form.includeInNetWorth"
          :default-asset-name="defaultAssetName"
        />

      </view>
    </scroll-view>
    
    <view class="fixed-bottom">
      <view class="save-btn" @click="saveAsset">确认添加</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { createAsset } from '@/services/assetService.js';
import { getAccounts } from '@/services/accountService.js';

// 引入拆分后的子组件
import AssetContextCard from '@/components/assets/add/AssetContextCard.vue';
import AccountFormCard from '@/components/assets/add/AccountFormCard.vue';
import AssetFormCard from '@/components/assets/add/AssetFormCard.vue';
import DynamicFormCard from '@/components/assets/add/DynamicFormCard.vue';

const configStore = useConfigStore();

// --- 路由参数接收 ---
const categoryCode = ref('');
const subCategoryCode = ref('');
const instCode = ref('');

// --- 可选账户列表 ---
const availableAccounts = ref([]);

// --- 表单统一状态 ---
const form = ref({
  identifier: '',     // 账户标识码 (尾号/账号)
  accountName: '',    // 账户名称 (容器名)
  assetName: '',      // 资产名称
  amount: '0.00',         // 余额
  currency: 'CNY',
  includeInNetWorth: true
});

// --- 生命周期 ---
onLoad((options) => {
  // 从路由接收上两步选择的结果
  categoryCode.value = options.categoryCode || '';
  subCategoryCode.value = options.subCategoryCode || '';
  instCode.value = options.instCode || '';
  
  // 加载该机构下的可选账户
  loadAvailableAccounts();
});

// 加载可选账户列表
const loadAvailableAccounts = async () => {
  if (!instCode.value) return;
  
  try {
    const res = await getAccounts({ institution: instCode.value });
    availableAccounts.value = res.accounts.filter(acc => {
      acc.assetTypes = [];
      if (acc.assets && acc.assets.length > 0) {
        acc.assets.forEach(asset => { acc.assetTypes.push(asset.subCategory);});
      }
      return !acc.assetTypes || !acc.assetTypes.includes(subCategoryCode.value);
    });
  } catch (error) {
    console.error('加载可选账户失败:', error);
    availableAccounts.value = [];
  }
};

// --- 计算属性 ---
// 获取机构对象以展示 Logo
const institution = computed(() => {
  return configStore.getInstitutionByCode(instCode.value) || {};
});

// 获取分类名称用于展示
const categoryName = computed(() => {
  console.log('路由参数:', categoryCode.value, subCategoryCode.value);
  const subCategories = configStore.getSubCategoriesByCode(categoryCode.value);
  const sub = subCategories.find(item => item.categoryCode === subCategoryCode.value);
  return sub ? sub.name : '';
});

// 智能生成默认资产名 (例如: 活期余额、信用卡欠款)
const defaultAssetName = computed(() => {
  return categoryName.value || '默认资产';
});

// --- 交互逻辑 ---
const goBackToReselect = () => {
  // 返回上一页 (机构选择页)
  uni.navigateBack();
};

const saveAsset = async () => {
  if (!form.value.amount) {
    uni.showToast({ title: '请输入资产金额', icon: 'none' });
    return;
  }
  
  if (!form.value.identifier) {
    uni.showToast({ title: '请输入账户标识码', icon: 'none' });
    return;
  }
  
  const amount = parseFloat(form.value.amount);
  if (isNaN(amount) || amount < 0) {
    uni.showToast({ title: '请输入有效的资产金额', icon: 'none' });
    return;
  }

  const category = configStore.assetCategories.find(c => c.categoryCode === categoryCode.value);
  const subCategories = configStore.getSubCategoriesByCode(categoryCode.value);
  const subCategory = subCategories.find(s => s.categoryCode === subCategoryCode.value);

  if (!category || !subCategory) {
    uni.showToast({ title: '分类信息异常', icon: 'none' });
    return;
  }

  const payload = {
    name: form.value.assetName || defaultAssetName.value,
    groupType: category.groupType || 'ASSET',
    category: categoryCode.value,
    subCategory: subCategoryCode.value,
    balance: parseFloat(form.value.amount),
    currency: form.value.currency,
    includeInNetWorth: form.value.includeInNetWorth,
    accountDTO: {
      institution: instCode.value,
      institutionIdentifier: form.value.identifier,
      accountName: form.value.accountName,
      themeColor: institution.value.themeColor
    }
  };

  console.log('提交的数据:', payload);

  try {
    uni.showLoading({ title: '保存中...' });
    await createAsset(payload);
    uni.hideLoading();
    uni.showToast({ title: '添加成功', icon: 'success' });
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/assets/index' });
    }, 1500);
  } catch (error) {
    uni.hideLoading();
    uni.showToast({ title: error.message || '添加失败', icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
.add-asset-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}
.content-scroll {
  flex: 1;
  height: 0;
}
.content-container {
  padding: $spacing-sm $spacing-base;
}

.fixed-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-md $spacing-base;
  z-index: 100;
  background: linear-gradient(to top, rgba($bg-page, 1) 70%, rgba($bg-page, 0) 100%);
}
.save-btn {
  background-color: $primary;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-inverse;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba($primary, 0.3);
}
</style>