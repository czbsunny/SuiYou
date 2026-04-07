<template>
  <view class="batch-container">
    <!-- 1. 录入区：卡片式展示 -->
    <view class="input-card card-warm">
      <view class="card-title-row">
        <text class="card-title">{{ isEditing ? '编辑待入资产' : '新资产录入' }}</text>
        <text v-if="isEditing" class="edit-mode-tag">编辑模式</text>
      </view>

      <view class="input-fields">
        <view class="field-item" :class="{ active: activeField === 'symbol' }">
          <text class="field-label">基金信息</text>
          <view class="value-row">
            <view class="search-trigger" @tap="navigateToFundSearch">
              <text class="value" :class="{ placeholder: !form.symbol }">
                {{ form.symbol ? `${form.symbol} ${form.name ? '-' + form.name : ''}` : '点击搜索或拍照识别' }}
              </text>
            </view>

            <view class="import-trigger" @tap="handleImageImport" hover-class="hover-opacity">
              <view class="import-line"></view>
              <image src="/static/images/import.png" class="import-icon" mode="aspectFit" />
              <text class="import-text">图片录入</text>
            </view>
          </view>
        </view>

        <view class="row-flex">
          <view class="field-item flex-1" :class="{ active: activeField === 'amount' }" @tap="activeField = 'amount'; showKeyboard = true">
            <text class="field-label">金额</text>
            <view class="value-row">
              <text class="unit">¥</text>
              <text class="value num-font" :class="{ placeholder: !form.amount }">{{ form.amount || '0.00' }}</text>
              <view class="custom-cursor" v-if="activeField === 'amount' && showKeyboard"></view>
            </view>
          </view>
          
          <view class="field-item flex-1" :class="{ active: activeField === 'returnValue' }" @tap="activeField = 'returnValue'; showKeyboard = true">
            <text class="field-label">收益</text>
            <view class="value-row" :class="profitColorClass">
              <text class="unit" :class="profitColorClass">¥</text>
              <text class="value num-font" :class="{ placeholder: !form.returnValue, [profitColorClass]: form.returnValue }">{{ form.returnValue || '0.00' }}</text>
              <view class="custom-cursor" v-if="activeField === 'returnValue' && showKeyboard"></view>
            </view>
          </view>
        </view>
      </view>

      <view class="add-action-bar">
        <button class="stage-push-btn" @tap="pushToStaging" hover-class="hover-grey">
          {{ isEditing ? '保存修改' : '暂存至下方列表' }}
        </button>
      </view>
    </view>

    <!-- 2. 缓冲区：待提交列表 -->
    <view class="staging-area">
      <view class="area-header">
        <text class="title">待提交列表</text>
        <view class="count-tag num-font">{{ stagingList.length }} 笔</view>
      </view>

      <scroll-view scroll-y class="list-scroll">
        <view v-if="stagingList.length === 0" class="empty-hint">
          <image src="/static/images/no-data.png" mode="aspectFit" class="empty-icon"></image>
          <text>暂无数据，请添加</text>
        </view>
        
        <view 
          v-for="(item, index) in stagingList" :key="index" 
          class="staging-card animate-slide-in"
        >
          <view class="s-info">
            <view class="s-fund-row">
              <text v-if="item.name" class="s-name truncate">{{ item.name }}</text>
              <text class="s-code num-font">{{ item.symbol }}</text>
            </view>
            <view class="s-data-row">
              <view class="data-item">
                <text class="s-label">金额</text>
                <text class="s-amt num-font">¥{{ formatNumber(item.amount) }}</text>
              </view>
              <view class="data-item">
                <text class="s-label">收益</text>
                <text class="s-pnl num-font" :class="Number(item.returnValue) >= 0 ? 'text-up' : 'text-down'">
                  {{ Number(item.returnValue) >= 0 ? '+' : '' }}{{ formatNumber(item.returnValue) }}
                </text>
              </view>
            </view>
          </view>
          
          <view class="s-actions">
            <view class="action-icon-btn edit" @tap.stop="handleEdit(index)" hover-class="hover-opacity">
              <image src="/static/images/edit_fund.png" mode="aspectFit" class="icon" />
            </view>
            <view class="action-icon-btn del" @tap.stop="handleRemove(index)" hover-class="hover-opacity">
              <image src="/static/images/del-fund.png" mode="aspectFit" class="icon" />
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 3. 底部固定保存按钮 -->
    <view class="bottom-bar" v-if="stagingList.length > 0">
      <button class="btn-primary" @tap="finalSubmit">
        确认存入账户 (¥{{ totalAmount }})
      </button>
    </view>

    <!-- 4. 自定义键盘 -->
    <Keyboard 
      v-if="showKeyboard"
      :activeField="activeField"
      :disabledKeys="getDisabledKeys()"
      :showSubmitBar="false"
      @keyPress="onKeyPress"
      @nextField="nextField"
      @done="() => { if (pushToStaging()) showKeyboard = false }"
      @close="showKeyboard = false"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createPortfolioHoldings } from '@/services/portfolioService';
import { recognize_fund } from '@/services/ocr';
import Keyboard from '@/components/common/Keyboard.vue';

const portfolioId = ref('');
const activeField = ref('symbol');
const stagingList = ref([]);
const isEditing = ref(false);
const editingIndex = ref(-1);
const showKeyboard = ref(false);

const form = ref({ symbol: '', name: '', amount: '', returnValue: '' });

onLoad((options) => { 
  portfolioId.value = options.portfolioId;
  console.log('portfolioId:', portfolioId.value);
});
onMounted(() => { uni.$on('fund-selected', handleFundSelected); });
onUnmounted(() => { uni.$off('fund-selected', handleFundSelected); });

const handleFundSelected = (fund) => {
  form.value.symbol = fund.fundCode;
  form.value.name = fund.fundName;
};

const handleImageImport = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      uploadFundImage(res.tempFilePaths[0]);
    }
  });
};

const uploadFundImage = async (filePath) => {
  uni.showLoading({ title: '图片识别中...', mask: true });
  try {
    const res = await recognize_fund(filePath);
    if (res.status === 'success') {
      const newList = res.fundInfoList || [];
      newList.forEach(item => {
        stagingList.value.unshift({
          symbol: item.symbol,
          name: item.name,
          amount: String(item.amount || '0.00'),
          returnValue: String(item.returnValue || '0.00')
        });
      });
      uni.showToast({ title: `识别成功，已添加${newList.length}笔`, icon: 'none' });
    } else {
      uni.showToast({ title: '识别失败，请尝试手动输入', icon: 'none', duration: 2000 });
    }
  } catch (error) {
    uni.showToast({ title: '识别异常', icon: 'none' });
  }
};

const navigateToFundSearch = () => { uni.navigateTo({ url: '/pages/assets/fund/search' }); };

const getDisabledKeys = () => {
  const disabled = [];
  if (activeField.value === 'symbol') disabled.push('.', '-');
  else if (activeField.value === 'amount') disabled.push('-');
  return disabled;
};

const profitColorClass = computed(() => {
  const val = parseFloat(form.value.returnValue);
  if (val > 0) return 'text-up';
  if (val < 0) return 'text-down';
  return '';
});

const totalAmount = computed(() => {
  const sum = stagingList.value.reduce((s, i) => s + Number(i.amount || 0), 0);
  return sum.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
});

const onKeyPress = (key) => {
  let val = form.value[activeField.value];
  if (key === 'delete') { form.value[activeField.value] = val.slice(0, -1); return; }
  if (key === '.') { if (val.includes('.') || val === '') return; }
  if (key === '-') {
    if (activeField.value !== 'returnValue') return;
    form.value.returnValue = val.startsWith('-') ? val.substring(1) : '-' + val;
    return;
  }
  if (!isNaN(key) && val.includes('.')) {
    if (val.split('.')[1].length >= 2) return;
  }
  if (activeField.value === 'symbol' && val.length >= 6) return;
  if (val.length >= 12) return;
  form.value[activeField.value] += key;
};

const nextField = () => {
  const order = ['amount', 'returnValue'];
  activeField.value = order[(order.indexOf(activeField.value) + 1) % 2];
};

const pushToStaging = () => {
  if (form.value.symbol.length < 6 || !form.value.amount) {
    uni.showToast({ title: '请完善基金信息及金额', icon: 'none' });
    return false;
  }
  if (isEditing.value) {
    stagingList.value[editingIndex.value] = { ...form.value };
    isEditing.value = false;
  } else {
    stagingList.value.unshift({ ...form.value });
  }
  form.value = { symbol: '', name: '', amount: '', returnValue: '' };
  activeField.value = 'symbol';
  return true;
};

const handleEdit = (index) => {
  form.value = { ...stagingList.value[index] };
  isEditing.value = true;
  editingIndex.value = index;
  activeField.value = 'amount';
  showKeyboard.value = true;
};

const handleRemove = (index) => { stagingList.value.splice(index, 1); };

const finalSubmit = async () => {
  uni.showLoading({ title: '存入中...', mask: true });
  try {
    const holdingsData = stagingList.value.map(item => ({
      symbol: item.symbol,
      name: item.name,
      amount: parseFloat(item.amount || '0.00') || 0,
      returnValue: parseFloat(item.returnValue || '0.00') || 0
    }));
    await createPortfolioHoldings(portfolioId.value, holdingsData);
    uni.showToast({ title: '存入成功' });
    uni.$emit('refreshHoldings', portfolioId.value);
    setTimeout(() => uni.navigateBack(), 1200);
  } catch (e) {
    uni.showToast({ title: e.message || '系统繁忙', icon: 'none' });
  }
};

const formatNumber = (val) => Number(val).toLocaleString();
</script>

<style lang="scss" scoped>
.batch-container {
  height: 100vh; 
  background-color: $bg-page; 
  display: flex; 
  flex-direction: column; 
  /* 关键：防止整个页面滚动 */
  overflow: hidden; 
}
.input-card {
  flex-shrink: 0; /* 关键：防止在空间不足时被压缩 */
  margin: $spacing-sm $spacing-base;
  padding: $spacing-sm $spacing-base;
  background-color: #fff; /* 确保有背景色遮盖底层 */
  border-radius: $radius-lg;
  z-index: 10;
  .card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
  .card-title { font-size: 32rpx; font-weight: $fw-semibold; color: $text-main; }
  .edit-mode-tag { font-size: 20rpx; color: $primary; background: $primary-subtle; padding: 6rpx 20rpx; border-radius: 20rpx; font-weight: $fw-medium; }
}

.field-item {
  padding: $spacing-sm 0 0; border-bottom: 2rpx solid $border-color; transition: all 0.3s ease;
  &.active { 
    border-bottom-color: $primary; 
    .field-label { font-size: 24rpx; color: $primary; }
    .value { color: $text-main; font-weight: $fw-bold; }
  }
  .field-label { font-size: 24rpx; color: $text-regular; font-weight: $fw-medium; margin-bottom: 12rpx; display: block; }
}

.value-row {
  display: flex; align-items: center; min-height: 60rpx; justify-content: flex-start;
  .search-trigger, .manual-input { flex: 1; min-width: 0; }
  .unit { font-size: 24rpx; font-weight: $fw-semibold; color: $text-main; margin-right: 8rpx; }
  .value { font-size: 36rpx; font-weight: $fw-semibold; color: $text-main; &.placeholder { color: $text-inverse; font-size: 28rpx; font-weight: $fw-regular; } }
}

.import-trigger {
  display: flex; flex-direction: column; align-items: center; padding-left: 24rpx; position: relative;
  .import-line { position: absolute; left: 0; top: 10%; bottom: 10%; width: 1rpx; background-color: $border-color; }
  .import-icon { width: 44rpx; height: 44rpx; margin-bottom: 4rpx; }
  .import-text { font-size: 18rpx; color: $primary; font-weight: $fw-medium; }
}

.row-flex { display: flex; gap: 30rpx; margin-top: 10rpx; }
.flex-1 { flex: 1; min-width: 0; }

.custom-cursor { width: 4rpx; height: 38rpx; background: $primary; margin-left: 6rpx; animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.stage-push-btn {
  margin-top: 40rpx; height: 90rpx; @include flex-center; background: $primary-dark; border-radius: $radius-base;
  font-size: 28rpx; font-weight: $fw-semibold; color: $text-inverse; border: none; &::after { border: none; }
}

/* 列表区 */
.staging-area {
  flex: 1; /* 占据剩余的所有垂直空间 */
  display: flex;
  flex-direction: column;
  padding: 0 $spacing-base;
  overflow: hidden; /* 关键：容器本身不滚动 */

  .area-header { 
    flex-shrink: 0; /* 标题头也不滚动 */
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    margin-bottom: 24rpx; 
    padding: 0 10rpx;
    .title { font-size: 26rpx; color: $text-placeholder; font-weight: $fw-semibold; }
    .count-tag { font-size: 24rpx; color: $text-placeholder; background: $border-color; padding: 2rpx 16rpx; border-radius: 20rpx; }
  }
}

.list-scroll { 
  flex: 1; 
  height: 0;
  padding-bottom: 160rpx; 
}

.staging-card {
  background: $bg-white; border-radius: $radius-lg; padding: $spacing-base; margin-bottom: $spacing-sm;
  display: flex; justify-content: space-between; align-items: center; box-shadow: $shadow-card;
  .s-fund-row { display: flex; align-items: center; margin-bottom: 12rpx; }
  .s-name { font-size: 26rpx; color: $text-main; font-weight: $fw-semibold; margin-right: 12rpx; }
  .s-code { font-size: 22rpx; color: $text-muted; background: $bg-white; padding: 4rpx 12rpx; border-radius: 8rpx; }
  .s-data-row { display: flex; gap: 40rpx; .data-item { display: flex; flex-direction: column; } .s-label { font-size: 20rpx; color: $text-placeholder; margin-bottom: 4rpx; } .s-amt, .s-pnl { font-size: 28rpx; font-weight: $fw-semibold; color: $text-main; } }
}

.action-icon-btn {
  width: 64rpx; height: 64rpx; @include flex-center; border-radius: 50%; margin-left: 16rpx;
  &.edit { background-color: $primary-subtle; } &.del { background-color: $color-up-subtle; }
  .icon { width: 32rpx; height: 32rpx; }
}

/* 底部按钮 */
.bottom-bar { 
  flex-shrink: 0;
  position: fixed; /* 或者保持 fixed */
  bottom: 0; 
  left: 0; 
  right: 0; 
  padding: $spacing-md; 
  background-color: $bg-white; 
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05); 
  z-index: 100;
}

.empty-hint { 
  @include flex-center; 
  flex-direction: column; 
  color: $text-inverse; 
  padding-top: 100rpx; 
  gap: 20rpx; 
  
  .empty-icon {
    width: 128rpx; // 根据您提供的256px图片，转换为rpx单位
    height: 128rpx;
    opacity: 0.3; // 添加适当的透明度
  }
  
  text { 
    font-size: 26rpx; 
  } 
}

.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>