<template>
  <view class="add-non-account-page">
    <scroll-view scroll-y class="content-scroll">
      <view class="content-container">
        <!-- 1. 分类选择 -->
        <CategorySelector 
          v-model="selectedAssetCategory"
          v-model:subValue="selectedSubcategory"
          :categories="assetCategories"
          :subcategories="currentSubcategories"
        />

        <!-- 2. 基础信息组件 -->
        <AssetBasicForm 
          v-model:name="assetForm.assetName"
          v-model:amount="assetForm.amount"
          :placeholder="assetNamePlaceholder"
          :currency="assetForm.currency"
          v-model:includeInNetWorth="assetForm.includeInNetWorth"
        />

        <!-- 3. 补充信息 -->
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
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { ASSET_CATEGORY_DISPLAY } from '@/configs/assets';
import { createAsset } from '@/services/assetService.js';

// 导入子组件
import CategorySelector from '@/components/assets/add/CategorySelector.vue';
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
  vehicle: [
    { key: 'brand', label: '品牌', type: 'text' },
    { key: 'model', label: '型号', type: 'text' },
    { key: 'purchaseDate', label: '购买日期', type: 'text' },
    { key: 'purchasePrice', label: '购买价格', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  gold: [
    { key: 'weight', label: '重量(g)', type: 'digit' },
    { key: 'purchasePrice', label: '购买价格', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  electronics: [
    { key: 'brand', label: '品牌', type: 'text' },
    { key: 'model', label: '型号', type: 'text' },
    { key: 'purchaseDate', label: '购买日期', type: 'text' },
    { key: 'purchasePrice', label: '购买价格', type: 'digit' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ],
  other: [
    { key: 'description', label: '描述', type: 'textarea' },
    { key: 'remark', label: '备注', type: 'textarea' }
  ]
});

// 5. 表单响应式数据
// 默认选中第一个资产类别
const selectedAssetCategory = ref(assetCategories.value[0]?.code || 'LIQUID');
const selectedSubcategory = ref('');
const assetForm = ref({
  assetName: '',
  amount: '',
  currency: 'CNY',
  includeInNetWorth: true,
  remark: '',
  startDate: '',
  endDate: '',
  interestRate: '',
  stockCode: '',
  quantity: '',
  purchasePrice: '',
  address: '',
  area: '',
  brand: '',
  model: '',
  weight: '',
  description: ''
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
});

// 计算属性：当前显示的大类下的子类列表
const currentSubcategories = computed(() => {
  return configStore.getSubCategoriesByCode(selectedAssetCategory.value);
});

// 计算属性：当前子类需要填写的动态字段
const currentFields = computed(() => {
  if (!selectedSubcategory.value) return [];
  // 如果该子类没有配置特有字段，默认只显示备注
  return subcategoryFields.value[selectedSubcategory.value] || [{ key: 'remark', label: '备注', type: 'textarea' }];
});

// 计算属性：智能生成输入框占位符
const assetNamePlaceholder = computed(() => {
  if (!selectedSubcategory.value) return '请输入资产名称';
  const subName = currentSubcategories.value.find(s => s.categoryCode === selectedSubcategory.value)?.name;
  return subName;
});

// 方法：保存
const saveAsset = async () => {
  if (!assetForm.value.assetName) {
    // 智能填入默认名
    assetForm.value.assetName = assetNamePlaceholder.value;
  }
  
  if (!assetForm.value.amount) {
    uni.showToast({ title: '请输入金额', icon: 'none' });
    return;
  }

  const payload = {
    groupType: 'ASSET',
    category: selectedAssetCategory.value,
    subCategory: selectedSubcategory.value,
    name: assetForm.value.assetName,
    currency: assetForm.value.currency,
    includeInNetWorth: assetForm.value.includeInNetWorth,
    balance: parseFloat(assetForm.value.amount),
    // 添加非账户资产特有的字段
    attributes: {
      address: assetForm.value.address,
      area: assetForm.value.area,
      brand: assetForm.value.brand,
      model: assetForm.value.model,
      purchaseDate: assetForm.value.purchaseDate,
      purchasePrice: assetForm.value.purchasePrice,
      weight: assetForm.value.weight,
      description: assetForm.value.description,
      remark: assetForm.value.remark
    }
  };

  console.log('提交的非账户资产数据:', payload);

  try {
    await createAsset(payload);
    uni.showToast({ title: '添加成功', icon: 'success' });
    setTimeout(() => {
      // 触发资产列表刷新
      uni.$emit('refreshNonAccountAssets');
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    uni.showToast({ title: error.message || '添加失败', icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
/* 变量定义 */
$primary: #2a806c;
$primary-light: rgba(42, 128, 108, 0.08);
$bg-page: #f5f7fa;
$bg-white: #ffffff;
$text-main: #1F2937;
$text-sub: #6B7280;
$text-placeholder: #9CA3AF;
$border-light: rgba(0, 0, 0, 0.04);
$tag-inactive: #F3F4F6;

.add-non-account-page {
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
  padding: 16px; // 页面边距
}

/* --- 卡片通用样式 --- */
.section-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 20px 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03); // 轻柔阴影
  
  .card-title {
    font-size: 14px;
    font-weight: 700;
    color: $text-main;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 3px solid $primary; // 金色装饰条
    line-height: 1;
  }
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
  // 底部按钮背景渐变
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