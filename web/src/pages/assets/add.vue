<template>
  <view class="add-asset-page">
    <view class="page-header">
      <view class="back-button" @click="goBack">
        <image src="/static/images/back.png" class="back-icon" />
      </view>
      <text class="page-title">添加资产</text>
      <view class="save-button" @click="saveAsset">
        <text class="save-text">保存</text>
      </view>
    </view>

    <view class="content-container">
      <!-- 1. 资产大类选择 -->
      <view class="section">
        <text class="section-title">资产类别</text>
        <view class="asset-type-grid">
          <view 
            v-for="type in assetTypes" 
            :key="type.id"
            class="asset-type-item"
            :class="{ active: selectedAssetType === type.id }"
            @click="selectAssetType(type.id)"
          >
            <image :src="type.icon" class="type-icon" />
            <text class="type-name">{{ type.name }}</text>
          </view>
        </view>
      </view>

      <!-- 2. 子分类选择 -->
      <view class="section">
        <text class="section-title">子分类</text>
        <view class="subcategory-scroll">
          <view 
            v-for="sub in currentSubcategories" 
            :key="sub.id"
            class="subcategory-item"
            :class="{ active: selectedSubcategory === sub.id }"
            @click="selectSubcategory(sub.id)"
          >
            <text class="subcategory-name">{{ sub.name }}</text>
          </view>
        </view>
      </view>

      <!-- 3. 机构选择 -->
      <view class="section">
        <text class="section-title">机构</text>
        <view class="input-wrapper">
          <input 
            v-model="assetForm.institution" 
            type="text" 
            placeholder="请输入机构名称"
            class="input-field"
          />
        </view>
      </view>

      <!-- 4. 账户名称 -->
      <view class="section">
        <text class="section-title">账户名称</text>
        <view class="input-wrapper">
          <input 
            v-model="assetForm.accountName" 
            type="text" 
            placeholder="请输入账户名称"
            class="input-field"
          />
        </view>
      </view>

      <!-- 5. 金额 -->
      <view class="section">
        <text class="section-title">金额</text>
        <view class="input-wrapper">
          <text class="currency-symbol">¥</text>
          <input 
            v-model="assetForm.amount" 
            type="number" 
            placeholder="0.00"
            class="input-field amount-input"
          />
        </view>
      </view>

      <!-- 6. 动态参数区域 -->
      <view class="section" v-if="selectedSubcategory">
        <text class="section-title">详细信息</text>
        <view class="dynamic-params">
          <!-- 根据子分类显示不同的参数 -->
          <!-- 定期存款参数 -->
          <template v-if="selectedSubcategory === 'fixed_deposit'">
            <view class="param-item">
              <text class="param-label">定存开始时间</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.startDate" 
                  type="date" 
                  placeholder="选择开始日期"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">定存利率(%)</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.interestRate" 
                  type="number" 
                  placeholder="0.00"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">定存期限(年)</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.termYears" 
                  type="number" 
                  placeholder="0"
                  class="input-field"
                />
              </view>
            </view>
          </template>

          <!-- 房贷参数 -->
          <template v-else-if="selectedSubcategory === 'mortgage'">
            <view class="param-item">
              <text class="param-label">贷款利率(%)</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.interestRate" 
                  type="number" 
                  placeholder="0.00"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">还款日</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.repaymentDate" 
                  type="number" 
                  placeholder="1-31"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">截止日期</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.endDate" 
                  type="date" 
                  placeholder="选择截止日期"
                  class="input-field"
                />
              </view>
            </view>
          </template>

          <!-- 股票参数 -->
          <template v-else-if="selectedSubcategory === 'stock'">
            <view class="param-item">
              <text class="param-label">股票代码</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.stockCode" 
                  type="text" 
                  placeholder="请输入股票代码"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">持有数量</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.quantity" 
                  type="number" 
                  placeholder="0"
                  class="input-field"
                />
              </view>
            </view>
          </template>

          <!-- 基金参数 -->
          <template v-else-if="selectedSubcategory === 'fund'">
            <view class="param-item">
              <text class="param-label">基金代码</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.fundCode" 
                  type="text" 
                  placeholder="请输入基金代码"
                  class="input-field"
                />
              </view>
            </view>
            <view class="param-item">
              <text class="param-label">持有份额</text>
              <view class="input-wrapper">
                <input 
                  v-model="assetForm.shares" 
                  type="number" 
                  placeholder="0.00"
                  class="input-field"
                />
              </view>
            </view>
          </template>

          <!-- 其他资产参数 -->
          <template v-else>
            <view class="param-item">
              <text class="param-label">备注</text>
              <view class="input-wrapper">
                <textarea 
                  v-model="assetForm.remark" 
                  placeholder="请输入备注信息"
                  class="textarea-field"
                  rows="3"
                ></textarea>
              </view>
            </view>
          </template>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

// 页面导航
const goBack = () => {
  uni.navigateBack();
};

// 资产大类
const assetTypes = ref([
  { id: 'cash', name: '流动资产', icon: '/static/images/wallet.png' },
  { id: 'investment', name: '投资理财', icon: '/static/images/chart-pie.png' },
  { id: 'fixed', name: '固定资产', icon: '/static/images/home.png' },
  { id: 'other', name: '其他资产', icon: '/static/images/cubes.png' },
  { id: 'debt', name: '负债贷款', icon: '/static/images/file-invoice-dollar.png' }
]);

// 子分类数据
const subcategories = ref([
  // 流动资产子分类
  { id: 'cash_account', name: '现金账户', parentType: 'cash' },
  { id: 'alipay', name: '支付宝', parentType: 'cash' },
  { id: 'wechat', name: '微信钱包', parentType: 'cash' },
  { id: 'fixed_deposit', name: '定期存款', parentType: 'cash' },
  // 投资理财子分类
  { id: 'stock', name: '股票', parentType: 'investment' },
  { id: 'fund', name: '基金', parentType: 'investment' },
  { id: 'bond', name: '债券', parentType: 'investment' },
  { id: 'insurance', name: '保险', parentType: 'investment' },
  // 固定资产子分类
  { id: 'house', name: '房产', parentType: 'fixed' },
  { id: 'car', name: '车辆', parentType: 'fixed' },
  // 其他资产子分类
  { id: 'gold', name: '黄金', parentType: 'other' },
  { id: 'collectibles', name: '收藏品', parentType: 'other' },
  // 负债贷款子分类
  { id: 'mortgage', name: '房贷', parentType: 'debt' },
  { id: 'car_loan', name: '车贷', parentType: 'debt' },
  { id: 'credit_card', name: '信用卡', parentType: 'debt' },
  { id: 'personal_loan', name: '个人贷款', parentType: 'debt' }
]);

// 选中的资产类别
const selectedAssetType = ref('cash'); // 默认选中流动资产
const selectedSubcategory = ref(null);

// 当前显示的子分类
const currentSubcategories = computed(() => {
  return subcategories.value.filter(sub => sub.parentType === selectedAssetType.value);
});

// 选择资产大类
const selectAssetType = (typeId) => {
  selectedAssetType.value = typeId;
  selectedSubcategory.value = null; // 重置子分类选择
};

// 选择子分类
const selectSubcategory = (subId) => {
  selectedSubcategory.value = subId;
};

// 资产表单数据
const assetForm = ref({
  institution: '',
  accountName: '',
  amount: '',
  // 动态参数
  startDate: '',
  interestRate: '',
  termYears: '',
  repaymentDate: '',
  endDate: '',
  stockCode: '',
  quantity: '',
  fundCode: '',
  shares: '',
  remark: ''
});

// 保存资产
const saveAsset = () => {
  // 表单验证
  if (!selectedSubcategory.value) {
    uni.showToast({ title: '请选择资产子分类', icon: 'none' });
    return;
  }
  if (!assetForm.value.institution) {
    uni.showToast({ title: '请输入机构名称', icon: 'none' });
    return;
  }
  if (!assetForm.value.accountName) {
    uni.showToast({ title: '请输入账户名称', icon: 'none' });
    return;
  }
  if (!assetForm.value.amount || parseFloat(assetForm.value.amount) <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' });
    return;
  }

  // 构建资产数据
  const assetData = {
    type: selectedAssetType.value,
    subcategory: selectedSubcategory.value,
    institution: assetForm.value.institution,
    accountName: assetForm.value.accountName,
    amount: parseFloat(assetForm.value.amount),
    ...assetForm.value
  };

  console.log('保存资产数据:', assetData);
  
  // 模拟保存成功
  uni.showToast({ title: '保存成功', icon: 'success' });
  setTimeout(() => {
    uni.navigateBack();
  }, 1500);
};
</script>

<style lang="scss" scoped>
.add-asset-page {
  min-height: 100vh;
  background-color: $bg-page;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: $white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.back-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20px;
  height: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: $text-main;
}

.save-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-text {
  font-size: 16px;
  color: $primary;
  font-weight: 600;
}

.content-container {
  padding: 20px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: $text-main;
  margin-bottom: 12px;
  display: block;
}

/* 资产大类选择 */
.asset-type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.asset-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background-color: $white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.asset-type-item.active {
  background-color: $primary;
  box-shadow: 0 4px 12px rgba(42, 128, 108, 0.2);
}

.type-icon {
  width: 32px;
  height: 32px;
  margin-bottom: 8px;
}

.type-name {
  font-size: 12px;
  color: $text-main;
}

.asset-type-item.active .type-name {
  color: $white;
}

/* 子分类选择 */
.subcategory-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;
}

.subcategory-item {
  padding: 8px 16px;
  background-color: $white;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  white-space: nowrap;
}

.subcategory-item.active {
  background-color: $primary;
}

.subcategory-name {
  font-size: 14px;
  color: $text-main;
}

.subcategory-item.active .subcategory-name {
  color: $white;
}

/* 输入框样式 */
.input-wrapper {
  position: relative;
  background-color: $white;
  border-radius: 12px;
  padding: 0 16px;
  height: 48px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 16px;
  color: $text-main;
  border: none;
  outline: none;
  background-color: transparent;
}

.input-field::placeholder {
  color: $text-placeholder;
}

.currency-symbol {
  font-size: 16px;
  color: $text-main;
  margin-right: 8px;
}

.amount-input {
  text-align: right;
}

/* 动态参数区域 */
.dynamic-params {
  background-color: $white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.param-item {
  margin-bottom: 20px;
}

.param-item:last-child {
  margin-bottom: 0;
}

.param-label {
  font-size: 14px;
  color: $text-main;
  margin-bottom: 8px;
  display: block;
}

.textarea-field {
  width: 100%;
  height: 80px;
  font-size: 16px;
  color: $text-main;
  border: none;
  outline: none;
  resize: none;
  background-color: transparent;
}

.textarea-field::placeholder {
  color: $text-placeholder;
}
</style>