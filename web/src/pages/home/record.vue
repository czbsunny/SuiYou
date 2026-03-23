<template>
  <view class="record-page">
    <!-- 1. 顶部切换 Tab (仅支出、收入) -->
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

    <!-- 2. 分类选择区 -->
    <view class="category-section">
      <!-- 一级分类网格 -->
      <scroll-view class="parent-scroll" scroll-y>
        <view class="category-grid">
          <view 
            v-for="item in currentCategories" 
            :key="item.id"
            class="category-item"
            @click="selectParent(item)"
          >
            <view class="icon-box" :class="{ active: parentCat?.id === item.id }">
              <text class="emoji">{{ item.emoji }}</text>
            </view>
            <text class="cat-label">{{ item.name }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 二级细分分类 (胶囊样式) -->
      <view class="sub-category-bar" v-if="parentCat?.children?.length">
        <scroll-view scroll-x class="sub-scroll" enable-flex>
          <view 
            v-for="sub in parentCat.children" 
            :key="sub.id"
            class="sub-chip"
            :class="{ active: subCat?.id === sub.id }"
            @click="subCat = sub"
          >
            {{ sub.name }}
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 3. 信息输入展示区 -->
    <view class="input-panel card-warm">
      <view class="info-row">
        <!-- 备注与分类显示 -->
        <view class="remark-box">
          <text class="selected-path" v-if="parentCat">
            {{ parentCat.name }} <text class="sep">></text> {{ subCat?.name }}
          </text>
          <input 
            type="text" 
            v-model="remark" 
            placeholder="添加备注..." 
            placeholder-class="placeholder-style"
          />
        </view>
        <!-- 金额显示 -->
        <view class="amount-display num-font" :class="activeTab === 'EXPENSE' ? 'text-expense' : 'text-income'">
          {{ amount || '0.00' }}
        </view>
      </view>

      <view class="meta-row">
        <!-- 日期选择 -->
        <picker mode="date" :value="date" @change="onDateChange">
          <view class="meta-btn">
            <uni-icons type="calendar" size="14" color="#7a8582"></uni-icons>
            <text>{{ isToday ? '今天' : date }}</text>
          </view>
        </picker>

        <!-- 账户选择 -->
        <view class="meta-btn" @click="showAccountPicker">
          <uni-icons type="wallet" size="14" color="#7a8582"></uni-icons>
          <text>{{ selectedAccount?.name || '选择账户' }}</text>
        </view>
      </view>
    </view>

    <!-- 4. 自定义数字键盘 (保持简洁) -->
    <view class="keyboard-container">
      <view class="key-grid">
        <view class="num-keys">
          <view v-for="key in ['1','2','3','4','5','6','7','8','9','0','.']" 
            :key="key" class="key-btn" hover-class="key-hover" @click="onKeyPress(key)">
            {{ key }}
          </view>
          <view class="key-btn" hover-class="key-hover" @click="onDelete">
            <uni-icons type="backspace" size="24"></uni-icons>
          </view>
        </view>
        <view class="action-keys">
          <view class="action-btn next" hover-class="key-hover" @click="saveAndNext">再记</view>
          <view class="action-btn finish" hover-class="key-hover" @click="save">完成</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const activeTab = ref('EXPENSE');
const amount = ref('');
const remark = ref('');
const date = ref(new Date().toISOString().split('T')[0]);
const parentCat = ref(null); // 一级分类
const subCat = ref(null);    // 二级分类
const selectedAccount = ref(null);

// 模拟两级分类数据
const mockCategories = {
  EXPENSE: [
    { 
      id: 1, name: '食品餐饮', emoji: '🍱', 
      children: [
        { id: 101, name: '午餐/晚餐' }, { id: 102, name: '早餐' }, 
        { id: 103, name: '零食下午茶' }, { id: 104, name: '买菜开火' }
      ] 
    },
    { 
      id: 2, name: '购物消费', emoji: '🛍️', 
      children: [
        { id: 201, name: '日用品' }, { id: 202, name: '服饰鞋包' }, 
        { id: 203, name: '数码电器' }, { id: 204, name: '护肤美妆' }
      ] 
    },
    { id: 3, name: '出行交通', emoji: '🚗', children: [{ id: 301, name: '打车租车' }, { id: 302, name: '公共交通' }, { id: 303, name: '加油停车' }] },
    { id: 4, name: '居家生活', emoji: '🏠', children: [{ id: 401, name: '房租房贷' }, { id: 402, name: '水电燃气' }, { id: 403, name: '物业维修' }] },
    { id: 9, name: '其他支出', emoji: '⚙️', children: [{ id: 901, name: '其他' }] }
  ],
  INCOME: [
    { id: 1001, name: '职业报酬', emoji: '💰', children: [{ id: 1101, name: '工资' }, { id: 1102, name: '奖金' }, { id: 1103, name: '兼职' }] },
    { id: 1002, name: '金融收益', emoji: '📈', children: [{ id: 1201, name: '理财分红' }, { id: 1202, name: '利息收入' }] }
  ]
};

const currentCategories = computed(() => mockCategories[activeTab.value]);

const isToday = computed(() => date.value === new Date().toISOString().split('T')[0]);

// 逻辑：切换 Tab 时初始化分类
const handleTabChange = (tab) => {
  activeTab.value = tab;
  selectParent(currentCategories.value[0]);
};

// 逻辑：选择一级分类
const selectParent = (item) => {
  parentCat.value = item;
  // 默认选中第一个二级分类
  if (item.children && item.children.length > 0) {
    subCat.value = item.children[0];
  }
};

onLoad((options) => {
  // 默认选中第一个分类
  selectParent(currentCategories.value[0]);
});

/* 键盘与业务逻辑保持之前版本一致 */
const onKeyPress = (k) => {
  if (k === '.' && (amount.value.includes('.') || !amount.value)) return;
  if (amount.value.includes('.') && amount.value.split('.')[1].length >= 2) return;
  amount.value += k;
};
const onDelete = () => amount.value = amount.value.slice(0, -1);
const onDateChange = (e) => date.value = e.detail.value;

const save = () => {
  if (!amount.value) return uni.showToast({ title: '请输入金额', icon: 'none' });
  uni.showLoading({ title: '保存中' });
  setTimeout(() => { uni.hideLoading(); uni.navigateBack(); }, 600);
};

const saveAndNext = () => {
  uni.showToast({ title: '已保存', icon: 'success' });
  amount.value = '';
  remark.value = '';
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
  padding: 24rpx 0;
  background-color: $bg-white;
  .tab-item {
    position: relative;
    font-size: 32rpx;
    color: $text-muted;
    transition: all 0.3s;
    &.active {
      color: $primary;
      font-weight: $fw-bold;
      .active-line {
        position: absolute;
        bottom: -12rpx;
        left: 0;
        width: 100%;
        height: 6rpx;
        background-color: $primary;
        border-radius: 10rpx;
      }
    }
  }
}

/* Category Section */
.category-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: $bg-white;
  overflow: hidden;

  .parent-scroll {
    flex: 1;
    padding: 30rpx 30rpx 10rpx;
    .category-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 32rpx 20rpx;
    }
  }

  /* 二级分类条 */
  .sub-category-bar {
    padding: 20rpx 0 30rpx;
    border-bottom: 1rpx solid $gray-50;
    .sub-scroll {
      display: flex;
      padding: 0 30rpx;
      white-space: nowrap;
      height: 64rpx;
      align-items: center;
    }
    .sub-chip {
      display: inline-block;
      padding: 10rpx 32rpx;
      margin-right: 16rpx;
      background-color: $bg-page;
      color: $text-regular;
      font-size: 24rpx;
      border-radius: $radius-full;
      transition: all 0.2s;
      &.active {
        background-color: $primary-subtle;
        color: $primary;
        font-weight: $fw-medium;
      }
    }
  }
}

/* Input Panel */
.input-panel {
  margin: 20rpx;
  padding: 32rpx;
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    .remark-box {
      flex: 1;
      .selected-path {
        display: block;
        font-size: 20rpx;
        color: $text-placeholder;
        margin-bottom: 8rpx;
        .sep { margin: 0 4rpx; }
      }
      input { font-size: 30rpx; color: $text-main; }
    }
    .amount-display {
      font-size: 56rpx;
      font-weight: $fw-bold;
      &.text-expense { color: $color-expense; }
      &.text-income { color: $color-income; }
    }
  }
  .meta-row {
    display: flex;
    gap: 16rpx;
    .meta-btn {
      background-color: $bg-page;
      padding: 8rpx 20rpx;
      border-radius: 8rpx;
      display: flex;
      align-items: center;
      gap: 8rpx;
      text { font-size: 22rpx; color: $text-sub; }
    }
  }
}

/* Keyboard */
.keyboard-container {
  background-color: $bg-white;
  padding: 12rpx 12rpx calc(constant(safe-area-inset-bottom) + 12rpx);
  padding: 12rpx 12rpx calc(env(safe-area-inset-bottom) + 12rpx);
  .key-grid { display: flex; gap: 12rpx; }
  .num-keys { flex: 3; display: grid; grid-template-columns: repeat(3, 1fr); gap: 12rpx; }
  .action-keys { flex: 1; display: flex; flex-direction: column; gap: 12rpx; }
  .key-btn, .action-btn {
    height: 104rpx;
    background-color: $bg-white;
    border: 1rpx solid $gray-100;
    border-radius: 16rpx;
    @include flex-center;
    font-size: 42rpx;
    font-family: $font-family-money;
  }
  .key-hover { background-color: $gray-50; }
  .action-btn {
    font-size: 30rpx;
    &.finish { background-color: $primary; color: $white; flex: 1; font-weight: $fw-bold; }
  }
}
</style>