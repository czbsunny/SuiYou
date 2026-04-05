<template>
  <view class="record-page">
    <!-- 1. 顶部切换 Tab -->
    <view class="tabs-container">
      <view 
        v-for="tab in ['EXPENSE', 'INCOME']" 
        :key="tab"
        class="tab-item"
        :class="{ active: activeTab === tab }"
        @click="handleTabChange(tab)"
      >
        <text>{{ tab === 'EXPENSE' ? '支出' : '收入' }}</text>
        <view class="active-line"></view>
      </view>
    </view>

    <!-- 2. 分类选择区 (抽屉式展开布局) -->
    <view class="category-main-wrapper card-warm">
      <scroll-view class="category-scroll" scroll-y>
        <view v-for="(row, rowIndex) in chunkedCategories" :key="rowIndex" class="category-row-group">
          <!-- 一级分类行 -->
          <view class="parent-grid">
            <view 
              v-for="item in row" 
              :key="item.id"
              class="parent-item"
              :class="{ 'parent-active': parentCat?.id === item.id }"
              @click="selectParent(item)"
            >
              <view class="p-icon-wrapper">
                <image :src="item.iconUrl" mode="aspectFit" class="p-cat-icon" />
              </view>
              <text class="p-cat-label">{{ item.name }}</text>
            </view>
          </view>

          <!-- 二级分类抽屉 -->
          <view class="sub-drawer" v-if="isParentInRow(row)">
            <view class="sub-section">
              <view class="sub-grid">
                <view 
                  v-for="sub in parentCat.children" 
                  :key="sub.id"
                  class="sub-item"
                  :class="{ 'sub-active': subCat?.id === sub.id }"
                  @click.stop="subCat = sub"
                >
                  <view class="s-icon-wrapper">
                    <image :src="sub.iconUrl" mode="aspectFit" class="s-cat-icon" />
                  </view>
                  <text class="s-cat-label">{{ sub.name }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 3. 输入面板 (金额/日期时间/账户/备注) -->
    <view class="input-panel card-warm">
      <!-- 第一行：左侧显示金额 -->
      <view class="info-row">
        <view class="amount-container" :class="activeTab === 'EXPENSE' ? 'text-expense' : 'text-income'">
          <text class="currency-symbol">￥</text>
          <text class="amount-display num-font">{{ form.amount || '0.00' }}</text>
        </view>
      </view>

      <!-- 第二行：操作栏（时间、账户、备注） -->
      <view class="meta-row">
        <uni-datetime-picker 
          v-model="form.datetime"
          type="datetime" 
          :border="false" 
          :clear-icon="false"
          @change="onDateTimeChange"
        >
          <view class="meta-btn">
            <image src="/static/images/calendar.png" class="meta-icon" />
            <text>{{ displayDateTime }}</text>
          </view>
        </uni-datetime-picker>

        <!-- 账户选择 -->
        <view class="meta-btn" @click="showAccountPicker">
          <image src="/static/images/account.png" class="meta-icon" />
          <text>{{ selectedAccount?.accountName || '选择账户' }}</text>
        </view>

        <!-- 备注 -->
        <view class="remark-box-inline">
          <input 
            type="text" 
            v-model="form.remark" 
            placeholder="点击添加备注..." 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>
    </view>

    <!-- 4. 自定义数字键盘 -->
    <view class="keyboard-container">
      <view class="key-grid">
        <view class="num-keys">
          <view v-for="key in ['1','2','3','4','5','6','7','8','9','0','.']" 
            :key="key" class="key-btn" hover-class="key-hover" @click="onKeyPress(key)">
            {{ key }}
          </view>
          <view class="key-btn" hover-class="key-hover" @click="onDelete">
            <image src="/static/images/del-key.png" class="backspace-icon" />
          </view>
        </view>
        <view class="action-keys">
          <view class="action-btn next" hover-class="key-hover" @click="saveTransaction(true)">再记</view>
          <view class="action-btn finish" hover-class="key-hover" @click="saveTransaction(false)">完成</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { createTransaction } from '@/services/transactionService.js';

const configStore = useConfigStore();

/**
 * 时间处理：组件回显最稳定的格式是 YYYY-MM-DD HH:mm:ss
 */
const formatNow = () => {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  const ss = String(now.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
};

const form = ref({
  amount: '',
  remark: '',
  datetime: formatNow(),
  categoryId: null,
  accountId: null,
  type: 'EXPENSE'
});

const activeTab = ref('EXPENSE');
const parentCat = ref(null);
const subCat = ref(null);
const selectedAccount = ref(null);

// 修改回显逻辑：兼容组件返回的不同格式
const displayDateTime = computed(() => {
  if (!form.value.datetime) return '选择日期';
  const dt = form.value.datetime.replace('T', ' '); // 统一成带空格的格式
  const now = new Date();
  const nowStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  const currentYear = now.getFullYear().toString();

  if (dt.startsWith(nowStr)) {
    return `今天 ${dt.split(' ')[1].substring(0, 5)}`;
  }

  if (dt.startsWith(currentYear)) {
    return dt.substring(5, 16);
  }
  
  return dt.substring(0, 16);
});

const chunkedCategories = computed(() => {
  const categories = configStore.getTransferCategoriesByType(activeTab.value) || [];
  const chunks = [];
  for (let i = 0; i < categories.length; i += 5) {
    chunks.push(categories.slice(i, i + 5));
  }
  return chunks;
});

const isParentInRow = (row) => {
  if (!parentCat.value) return false;
  return row.some(item => item.id === parentCat.value.id);
};

const selectParent = (item) => {
  parentCat.value = item;
  subCat.value = item.children?.length > 0 ? item.children[0] : null;
};

const handleTabChange = (tab) => {
  activeTab.value = tab;
  form.value.type = tab;
  const categories = configStore.getTransferCategoriesByType(tab);
  if (categories.length > 0) {
    selectParent(categories[0]);
  }
};

onLoad((options) => {
  uni.$on('acceptAccountFromSelector', (res) => {
    selectedAccount.value = {
      ...res.account,
      selectedAsset: res.asset
    };
    console.log(selectedAccount.value);
    form.value.accountId = res.account.id;
  });

  const categories = configStore.getTransferCategoriesByType(activeTab.value);
  if (categories.length > 0) {
    selectParent(categories[0]);
  }
  if (options.accountId) {
    selectedAccount.value = configStore.institutionMap[options.accountId];
  }
});

onUnmounted(() => {
  uni.$off('acceptAccountFromSelector');
});

// 处理时间选择器变化，确保响应式更新
const onDateTimeChange = (val) => {
  if (val) {
    form.value.datetime = val.replace('T', ' '); // 保持内部格式统一
  }
};

const onKeyPress = (k) => {
  if (k === '.' && (form.value.amount.includes('.') || !form.value.amount)) return;
  if (form.value.amount.includes('.')) {
    const parts = form.value.amount.split('.');
    if (parts[1].length >= 2) return; 
  }
  let nextAmount = form.value.amount + k;
  if (parseFloat(nextAmount) >= 100000000) {
    uni.showToast({ title: '单笔金额上限为1亿', icon: 'none' });
    return;
  }
  form.value.amount += k;
};

const onDelete = () => form.value.amount = form.value.amount.slice(0, -1);

const showAccountPicker = () => {
    uni.navigateTo({
        url: `/pages/assets/account-selector?canSetDefault=true&initAccountId=${selectedAccount.value?.id}&initAssetId=${selectedAccount.value?.selectedAsset?.id}`
    });
};

const saveTransaction = async (keepGoing = false) => {
  if (!form.value.amount || parseFloat(form.value.amount) === 0) {
    return uni.showToast({ title: '请输入金额', icon: 'none' });
  }
  
  if (!selectedAccount.value?.selectedAsset?.id) {
    return uni.showToast({ title: '请选择账户', icon: 'none' });
  }
  
  const formattedDt = form.value.datetime.replace(' ', 'T');
  const payload = {
    type: form.value.type,
    amount: parseFloat(form.value.amount),
    transTime: formattedDt.length === 16 ? formattedDt + ':00' : formattedDt,
    sourceAssetId: form.value.type === 'EXPENSE' ? selectedAccount.value?.selectedAsset?.id : null,
    targetAssetId: form.value.type === 'INCOME' ? selectedAccount.value?.selectedAsset?.id : null,
    categoryId: subCat.value?.id || parentCat.value?.id,
    description: form.value.remark,
    tags: []
  };
  
  try {
    uni.showLoading({ title: '保存中...' });
    await createTransaction(payload);
    uni.showToast({ title: '保存成功', icon: 'success' });
    
    if (keepGoing) {
      form.value.amount = '';
      form.value.remark = '';
    } else {
      setTimeout(() => uni.navigateBack(), 800);
    }
  } catch (error) {
    console.error('保存交易记录失败:', error);
    uni.showToast({ title: '保存失败，请重试', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};
</script>

<style lang="scss" scoped>
.record-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-page;
}

.tabs-container {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  padding: 30rpx 0;
  background-color: $bg-white;
  z-index: 10;
  .tab-item {
    position: relative;
    font-size: 32rpx;
    color: $text-muted;
    &.active {
      color: $primary;
      font-weight: $fw-bold;
      .active-line {
        position: absolute;
        bottom: -16rpx;
        left: 0; width: 100%; height: 6rpx;
        background-color: $primary; border-radius: 10rpx;
      }
    }
  }
}

.category-main-wrapper {
  flex: 1;
  margin: $spacing-sm $spacing-base;
  overflow: hidden;
  padding: 0;
  .category-scroll {
    height: 100%;
    padding: $spacing-md;
  }
}

.category-row-group { margin-bottom: $spacing-md; } 

.parent-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16rpx;
}

.parent-item {
  display: flex; flex-direction: column; align-items: center; gap: 8rpx;
  .p-icon-wrapper {
    width: 80rpx; height: 80rpx; background-color: $bg-page; border-radius: 24rpx;
    @include flex-center; transition: all 0.2s; border: 2rpx solid transparent;
    .p-cat-icon { width: 50rpx; height: 50rpx; }
  }
  .p-cat-label { font-size: 20rpx; color: $text-sub; }
  &.parent-active {
    .p-icon-wrapper { background-color: $primary-subtle; border-color: $primary; }
    .p-cat-label { color: $primary; font-weight: $fw-bold; }
  }
}

.sub-drawer { width: 100%; margin-top: 20rpx; animation: slideDown 0.3s ease-out; }

.sub-section {
  padding: 24rpx; background-color: $bg-subtle; border-radius: $radius-base;
  .sub-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20rpx 16rpx; }
}

.sub-item {
  display: flex; flex-direction: column; align-items: center; gap: 6rpx;
  .s-icon-wrapper {
    width: 64rpx; height: 64rpx; background-color: $bg-white; border-radius: 16rpx;
    @include flex-center; .s-cat-icon { width: 40rpx; height: 40rpx; }
  }
  .s-cat-label { font-size: 18rpx; color: $text-regular; }
  &.sub-active {
    .s-icon-wrapper { background-color: $primary; .s-cat-icon { filter: brightness(100); } }
    .s-cat-label { color: $primary; font-weight: $fw-bold; }
  }
}

/* 输入面板 */
.input-panel {
  margin: $spacing-sm $spacing-base $spacing-base;
  padding: 24rpx 32rpx;
  
  .info-row {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    .amount-container {
      display: flex;
      align-items: baseline;
      .currency-symbol { font-size: 40rpx; font-weight: $fw-bold; margin-right: 8rpx; }
      .amount-display { font-size: 64rpx; font-weight: $fw-bold; }
    }
  }
  
  .meta-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16rpx;
    margin-top: 24rpx;
    
    ::v-deep .uni-date {
      width: auto;
      .uni-date__x-input { display: none; }
      .uni-date-x { padding: 0; }
    }

    .meta-btn {
      background-color: $bg-page;
      padding: 10rpx 20rpx;
      border-radius: $radius-base;
      display: flex;
      align-items: center;
      gap: 8rpx;
      &:active { opacity: 0.7; }
      .meta-icon { width: 28rpx; height: 28rpx; }
      text { font-size: 22rpx; color: $text-sub; font-weight: $fw-medium; }
    }
    
    .remark-box-inline {
      flex: 1;
      min-width: 180rpx;
      background-color: $bg-page;
      padding: 10rpx 20rpx;
      border-radius: $radius-base;
      display: flex;
      align-items: center;
      gap: 8rpx;
      .meta-icon { width: 28rpx; height: 28rpx; opacity: 0.4; }
      input { flex: 1; font-size: 22rpx; color: $text-main; }
    }
  }
}

.keyboard-container {
  background-color: $bg-white;
  padding: 12rpx 12rpx calc(env(safe-area-inset-bottom) + 12rpx);
  .key-grid { display: flex; gap: 12rpx; }
  .num-keys { flex: 3; display: grid; grid-template-columns: repeat(3, 1fr); gap: 12rpx; }
  .action-keys { flex: 1; display: flex; flex-direction: column; gap: 12rpx; }
  .key-btn, .action-btn {
    height: 100rpx; background-color: $bg-white;
    border: 1rpx solid $gray-100; border-radius: 20rpx;
    @include flex-center; font-size: 40rpx; font-family: $font-family-money;
  }
  .backspace-icon { width: 44rpx; height: 44rpx; }
  .key-hover { background-color: $gray-50; }
  .action-btn {
    font-size: 28rpx;
    &.finish { background-color: $primary; color: $white; flex: 1; font-weight: $fw-bold; }
  }
}

.text-expense { color: $color-expense; }
.text-income { color: $color-income; }
.placeholder-style { color: $text-placeholder; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>