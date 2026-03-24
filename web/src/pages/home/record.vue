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
        <!-- 遍历每一行 (每行5个) -->
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

          <!-- 二级分类抽屉：仅当选中的一级分类属于当前行时展开 -->
          <view 
            class="sub-drawer" 
            v-if="isParentInRow(row)"
          >
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

    <!-- 3. 输入面板 (备注/金额) -->
    <view class="input-panel card-warm">
      <view class="info-row">
        <view class="remark-box">
          <input 
            type="text" 
            v-model="form.remark" 
            placeholder="添加备注..." 
            placeholder-class="placeholder-style"
          />
        </view>
        <view class="amount-display num-font" :class="activeTab === 'EXPENSE' ? 'text-expense' : 'text-income'">
          {{ form.amount || '0.00' }}
        </view>
      </view>

      <view class="meta-row">
        <picker mode="date" :value="form.date" @change="e => form.date = e.detail.value">
          <view class="meta-btn">
            <image src="/static/assets/actions/calendar.png" class="meta-icon" />
            <text>{{ isToday ? '今天' : form.date }}</text>
          </view>
        </picker>

        <view class="meta-btn" @click="showAccountPicker">
          <image src="/static/assets/actions/wallet.png" class="meta-icon" />
          <text>{{ selectedAccount?.name || '选择账户' }}</text>
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
            <image src="/static/assets/actions/backspace.png" class="backspace-icon" />
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
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();

const form = ref({
  amount: '',
  remark: '',
  date: new Date().toISOString().split('T')[0],
  categoryId: null,
  accountId: null,
  type: 'EXPENSE'
});

const activeTab = ref('EXPENSE');
const parentCat = ref(null);
const subCat = ref(null);
const selectedAccount = ref(null);

// 获取分类并切分成 5 个一组
const chunkedCategories = computed(() => {
  const categories = configStore.getTransferCategoriesByType(activeTab.value) || [];
  const chunks = [];
  for (let i = 0; i < categories.length; i += 5) {
    chunks.push(categories.slice(i, i + 5));
  }
  return chunks;
});

// 判断当前选中的一级分类是否在这一行中
const isParentInRow = (row) => {
  if (!parentCat.value) return false;
  return row.some(item => item.id === parentCat.value.id);
};

const isToday = computed(() => form.value.date === new Date().toISOString().split('T')[0]);

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
  const categories = configStore.getTransferCategoriesByType(activeTab.value);
  if (categories.length > 0) {
    selectParent(categories[0]);
  }
  if (options.accountId) {
    selectedAccount.value = configStore.institutionMap[options.accountId];
  }
});

const onKeyPress = (k) => {
  let nextAmount = form.value.amount + k;
  if (k === '.' && (form.value.amount.includes('.') || !form.value.amount)) return;
  if (form.value.amount.includes('.') && form.value.amount.split('.')[1].length >= 2) return;
  if (parseFloat(nextAmount) >= 100000000) {
    uni.showToast({ title: '单笔金额上限为1亿', icon: 'none' });
    return;
  }
  form.value.amount += k;
};

const onDelete = () => form.value.amount = form.value.amount.slice(0, -1);

const showAccountPicker = () => {
  uni.showToast({ title: '账户选择开发中', icon: 'none' });
};

const saveTransaction = async (keepGoing = false) => {
  if (!form.value.amount || parseFloat(form.value.amount) === 0) {
    return uni.showToast({ title: '请输入金额', icon: 'none' });
  }
  const payload = {
    amount: parseFloat(form.value.amount),
    remark: form.value.remark,
    occuredAt: form.value.date + 'T00:00:00',
    categoryId: subCat.value?.id || parentCat.value?.id,
    accountId: selectedAccount.value?.id || null,
    type: form.value.type
  };
  console.log('提交 Payload:', payload);
  uni.showToast({ title: '保存成功', icon: 'success' });
  if (keepGoing) {
    form.value.amount = '';
    form.value.remark = '';
  } else {
    setTimeout(() => uni.navigateBack(), 800);
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

/* Tabs */
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

/* 分类展示区 */
.category-main-wrapper {
  flex: 1;
  margin: 20rpx $spacing-md;
  overflow: hidden;
  padding: 0;
  
  .category-scroll {
    height: 100%;
    padding: 24rpx;
  }
}

.category-row-group {
  margin-bottom: 24rpx;
}

/* 一级网格：确保一行始终5列 */
.parent-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16rpx;
}

.parent-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  
  .p-icon-wrapper {
    width: 80rpx;
    height: 80rpx;
    background-color: $bg-page;
    border-radius: 24rpx;
    @include flex-center;
    transition: all 0.2s;
    border: 2rpx solid transparent;
    .p-cat-icon { width: 50rpx; height: 50rpx; }
  }
  
  .p-cat-label { font-size: 20rpx; color: $text-sub; }

  &.parent-active {
    .p-icon-wrapper {
      background-color: $primary-subtle;
      border-color: $primary;
    }
    .p-cat-label { color: $primary; font-weight: $fw-bold; }
  }
}

/* 二级抽屉样式 */
.sub-drawer {
  width: 100%;
  margin-top: 20rpx;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.sub-section {
  padding: 24rpx;
  background-color: $bg-subtle;
  border-radius: $radius-base;
  
  .sub-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20rpx 16rpx;
  }
}

.sub-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  
  .s-icon-wrapper {
    width: 64rpx;
    height: 64rpx;
    background-color: $bg-white;
    border-radius: 16rpx;
    @include flex-center;
    .s-cat-icon { width: 40rpx; height: 40rpx; }
  }
  
  .s-cat-label { font-size: 18rpx; color: $text-regular; }

  &.sub-active {
    .s-icon-wrapper {
      background-color: $primary;
      .s-cat-icon { filter: brightness(100); }
    }
    .s-cat-label { color: $primary; font-weight: $fw-bold; }
  }
}

/* 输入面板及键盘（保持一致） */
.input-panel {
  margin: 0 $spacing-md 20rpx;
  padding: 24rpx 32rpx;
  .info-row {
    display: flex; justify-content: space-between; align-items: center;
    .remark-box { flex: 1; input { font-size: 30rpx; color: $text-main; } }
    .amount-display { font-size: 56rpx; font-weight: $fw-bold; }
  }
  .meta-row {
    display: flex; gap: 20rpx; margin-top: 20rpx;
    .meta-btn {
      background-color: $bg-page; padding: 8rpx 20rpx; border-radius: $radius-base;
      display: flex; align-items: center; gap: 10rpx;
      .meta-icon { width: 28rpx; height: 28rpx; }
      text { font-size: 22rpx; color: $text-sub; }
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
    height: 100rpx; background-color: $bg-white; border: 1rpx solid $gray-100; border-radius: 20rpx;
    @include flex-center; font-size: 40rpx; font-family: $font-family-money;
  }
  .backspace-icon { width: 44rpx; height: 44rpx; }
  .key-hover { background-color: $gray-50; }
  .action-btn { font-size: 28rpx; &.finish { background-color: $primary; color: $white; flex: 1; font-weight: $fw-bold; } }
}

.text-expense { color: $color-expense; }
.text-income { color: $color-income; }
.placeholder-style { color: $text-placeholder; }
</style>