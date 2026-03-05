<template>
  <view class="add-asset-page">
    <scroll-view scroll-y class="content-scroll">
      <view class="content-container">
        
        <!-- 1. 分类选择 -->
        <CategorySelector 
          v-model="selectedAssetCategory"
          v-model:subValue="selectedSubcategory"
          :categories="assetCategories"
          :subcategories="currentSubcategories"
        />

        <!-- 2. 机构选择与标识码 -->
        <InstitutionPicker 
          v-if="selectedSubcategory && availableInstitutions.length > 0"
          :selected="selectedInstitution"
          v-model:identifier="assetForm.identifier"
          @click="openInstitutionSelect"
        />

        <!-- 3. 基础信息组件 -->
        <AssetBasicForm 
          v-model:name="assetForm.accountName"
          v-model:amount="assetForm.amount"
          :placeholder="accountNamePlaceholder"
          :currency="assetForm.currency"
          v-model:includeInNetWorth="assetForm.includeInNetWorth"
        />

        <!-- 4. 补充信息 -->
        <DynamicFieldGroup 
          v-model="assetForm"
          :fields="currentFields"
        />

        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>
    
    <view class="fixed-bottom">
      <view class="save-btn" @click="saveAsset">确认添加</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { ASSET_CATEGORY_DISPLAY } from '@/configs/assets';
import { createAsset } from '@/services/assetService.js';

// 导入子组件
import CategorySelector from '@/components/assets/add/CategorySelector.vue';
import InstitutionPicker from '@/components/assets/add/InstitutionPicker.vue';
import AssetBasicForm from '@/components/assets/add/AssetBasicForm.vue';
import DynamicFieldGroup from '@/components/assets/add/DynamicFieldGroup.vue';

const configStore = useConfigStore();

// ---------------- 数据定义区 ----------------

// 1. 大类定义 - 从配置文件读取
const assetCategories = ref(Object.keys(ASSET_CATEGORY_DISPLAY).map(key => ({
  code: key,
  name: ASSET_CATEGORY_DISPLAY[key].name,
  icon: ASSET_CATEGORY_DISPLAY[key].icon,
  iconGray: ASSET_CATEGORY_DISPLAY[key].iconGray,
  color: ASSET_CATEGORY_DISPLAY[key].color
})));

// 4. 动态表单字段配置
const subcategoryFields = ref({
  cash_account: [
    { key: 'accountNumber', label: '账号', type: 'text' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  fixed_deposit: [
    { key: 'startDate', label: '起息日', type: 'text', placeholder: 'YYYY-MM-DD' },
    { key: 'endDate', label: '到期日', type: 'text', placeholder: 'YYYY-MM-DD' },
    { key: 'interestRate', label: '年利率 %', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  stock: [
    { key: 'stockCode', label: '股票代码', type: 'text' },
    { key: 'quantity', label: '持有数量', type: 'digit' },
    { key: 'purchasePrice', label: '持仓成本', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  house: [
    { key: 'address', label: '地址', type: 'text' },
    { key: 'area', label: '面积(㎡)', type: 'digit' },
    { key: 'purchaseDate', label: '购买日期', type: 'text' },
    { key: 'purchasePrice', label: '原价', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  credit_card: [
    { key: 'creditLimit', label: '总额度', type: 'digit' },
    { key: 'billDate', label: '账单日', type: 'number', placeholder: '每月几号' },
    { key: 'repaymentDate', label: '还款日', type: 'number', placeholder: '每月几号' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
});

// 5. 表单响应式数据
// 默认选中第一个资产类别
const selectedAssetCategory = ref(assetCategories.value[0]?.code || 'LIQUID');
const selectedSubcategory = ref('');
const selectedInstitution = ref(null);
const assetForm = ref({
  institution: '',
  accountName: '',
  amount: '',
  currency: 'CNY',
  includeInNetWorth: true,
  identifier: '',
  remark: '',
  startDate: '',
  endDate: '',
  interestRate: '',
  stockCode: '',
  quantity: '',
  purchasePrice: '',
  address: '',
  area: '',
  creditLimit: '',
  billDate: '',
  repaymentDate: ''
});

// ---------------- 逻辑处理区 ----------------

// 生命周期
onLoad((options) => {
  if (options.type) {
    // 检查传入的 type 是否存在于 assetCategories 中
    const isValidType = assetCategories.value.some(type => type.code === options.type);
    if (isValidType) {
      selectedAssetCategory.value = options.type;
    }
  }
  // 默认选中当前大类下的第一个子类
  const firstSub = currentSubcategories.value[0];
  if (firstSub) {
    selectedSubcategory.value = firstSub.categoryCode;
  }
  
  // 监听机构选择事件
  uni.$on('institutionSelected', (institution) => {
    selectedInstitution.value = institution;
    assetForm.value.institution = institution.instCode;
    console.log('selectedInstitution', selectedInstitution.value);
  });
});

// 页面卸载时移除事件监听
onUnmounted(() => {
  uni.$off('institutionSelected');
});

// 计算属性：当前显示的大类下的子类列表
const currentSubcategories = computed(() => {
  return configStore.getSubCategoriesByCode(selectedAssetCategory.value);
});

// 计算属性：当前子类可用的机构
const availableInstitutions = computed(() => {
  if (!selectedSubcategory.value) return [];
  console.log('selectedSubcategory.value', selectedSubcategory.value);
  return configStore.getInstitutionsBySubCategoryCode(selectedSubcategory.value);
});

// 计算属性：当前子类需要填写的动态字段
const currentFields = computed(() => {
  if (!selectedSubcategory.value) return [];
  // 如果该子类没有配置特有字段，默认只显示备注
  return subcategoryFields.value[selectedSubcategory.value] || [{ key: 'remark', label: '备注', type: 'textarea' }];
});

// 计算属性：智能生成输入框占位符
const accountNamePlaceholder = computed(() => {
  if (!selectedSubcategory.value) return '请输入名称';
  const subName = currentSubcategories.value.find(s => s.categoryCode === selectedSubcategory.value)?.name;
  const instName = availableInstitutions.value.find(i => i.id === assetForm.value.institution)?.name;
  
  if (instName) return `${instName}${subName}`;
  return `${subName}`;
});

// 打开机构选择页面
const openInstitutionSelect = () => {
  uni.navigateTo({
    url: `/pages/assets/institution-select?subCode=${selectedSubcategory.value}`
  });
};

// 方法：保存
const saveAsset = async () => {
  if (!assetForm.value.accountName) {
    // 智能填入默认名
    assetForm.value.accountName = accountNamePlaceholder.value;
  }
  
  if (!assetForm.value.amount) {
    uni.showToast({ title: '请输入金额', icon: 'none' });
    return;
  }

  if (assetForm.value.institution && !assetForm.value.identifier) {
    uni.showToast({ title: '请输入机构账号末4位', icon: 'none' });
    return;
  }

  const payload = {
    groupType: selectedAssetCategory.value === 'debt' ? 'LIABILITY' : 'ASSET',
    category: selectedAssetCategory.value,
    subCategory: selectedSubcategory.value,
    institution: assetForm.value.institution,
    institutionIdentifier: assetForm.value.identifier,
    name: assetForm.value.accountName,
    currency: assetForm.value.currency,
    includeInNetWorth: assetForm.value.includeInNetWorth,
    balance: parseFloat(assetForm.value.amount),
  };

  console.log('提交的数据:', payload);

  try {
    await createAsset(payload);
    uni.showToast({ title: '添加成功', icon: 'success' });
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
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
  padding: 24rpx 32rpx;
}

/* --- 卡片通用样式 --- */
.section-card {
  background-color: $bg-white;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  
  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-main;
    margin-bottom: 24rpx;
    padding-left: 10rpx;
    border-left: 3px solid $primary;
    line-height: 1;
  }
}

.divider {
  height: 1rpx;
  background-color: $border-light;
  margin: 12px 0 16px 0;
}

.inner-divider {
  height: 1rpx;
  background-color: $border-light;
  margin: 16px 0;
  border-top: 1px dashed rgba(0,0,0,0.05);
  background-color: transparent;
}

.tags-container {
  .tags-label {
    font-size: 24rpx;
    color: $text-sub;
    margin-bottom: 10px;
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 16px;
  border-radius: 8px;
  background-color: $bg-subtle;
  color: $text-sub;
  font-size: 13px;
  font-weight: 400;
  transition: all 0.2s;
  
  &.active {
    background-color: $primary;
    color: $bg-white;
    font-weight: 600;
    box-shadow: 0 4px 8px rgba($primary, 0.25);
  }
}

.picker-trigger {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.selected-inst-box {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  
  .mini-logo-wrapper {
    width: 44rpx;
    height: 44rpx;
    background-color: #f8f9fa;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12rpx;
    overflow: hidden;
    border: 1rpx solid rgba(0,0,0,0.03);
  }

  .mini-logo {
    width: 100%;
    height: 100%;
  }

  .inst-display-text {
    font-size: 30rpx;
    color: $text-main;
    margin-right: 4rpx;
    
    &.placeholder {
      color: $text-muted;
      font-size: 28rpx;
    }
  }
}

.form-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1rpx solid $border-light;

  &.last-row {
    border-bottom: none;
    padding-bottom: 0;
  }
  
  &.column-layout {
    flex-direction: column;
    align-items: flex-start;
  }
}

.row-label {
  font-size: 30rpx;
  color: $text-main;
  width: 90px;
  flex-shrink: 0;
  font-weight: 500;
}

.currency-label {
  font-size: 24rpx;
  color: $primary;
  margin-left: 4px;
}

.row-input {
  flex: 1;
  font-size: 30rpx;
  color: $text-main;
  text-align: right;
  height: 24px;
  line-height: 24px;
  
  &.amount-input {
    font-size: 40rpx; 
    font-weight: 600;
    color: $primary;
  }
}

.input-placeholder {
  color: $text-muted;
  font-size: 28rpx;
  font-weight: 400;
}

.row-textarea {
  width: 100%;
  font-size: 28rpx;
  color: $text-main;
  min-height: 80px;
  background-color: $bg-subtle;
  border-radius: 8px;
  padding: 12px;
  box-sizing: border-box;
}

.picker {
  flex: 1;
  font-size: 30rpx;
  color: $text-main;
  text-align: right;
}

.mb-8 {
  margin-bottom: 8px;
}

/* --- 底部按钮 --- */
.bottom-spacer {
  height: 120px;
}

.fixed-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
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
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba($primary, 0.3);
  letter-spacing: 2px;
}

.save-btn-hover {
  transform: scale(0.99);
  opacity: 0.95;
}
</style>