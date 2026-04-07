<template>
  <view class="edit-container">
    <!-- 1. 编辑卡片：应用 card-warm 基础类 -->
    <view class="input-card card-warm">
      <view class="card-title-row">
        <text class="card-title">修改基金信息</text>
      </view>

      <view class="input-fields">
        <view class="field-item readonly">
          <text class="field-label">基金信息</text>
          <view class="value-row">
            <text class="value-text main-info">
              {{ form.symbol }} {{ form.name ? '-' + form.name : '' }}
            </text>
          </view>
        </view>

        <view class="row-flex">
          <!-- 金额输入 -->
          <view class="field-item flex-1" :class="{ active: activeField === 'amount' }" @tap="activeField = 'amount'; showKeyboard = true">
            <text class="field-label">金额</text>
            <view class="value-row">
              <text class="unit">¥</text>
              <text class="value num-font" :class="{ placeholder: !form.amount }">
                {{ form.amount || '0.00' }}
              </text>
              <view class="custom-cursor" v-if="activeField === 'amount' && showKeyboard"></view>
            </view>
          </view>

          <!-- 收益输入 -->
          <view class="field-item flex-1" :class="{ active: activeField === 'returnValue' }" @tap="activeField = 'returnValue'; showKeyboard = true">
            <text class="field-label">收益</text>
            <view class="value-row" :class="profitColorClass">
              <text class="unit" :class="profitColorClass">¥</text>
              <text class="value num-font" :class="{ placeholder: !form.returnValue, [profitColorClass]: form.returnValue }">
                {{ form.returnValue || '0.00' }}
              </text>
              <view class="custom-cursor" v-if="activeField === 'returnValue' && showKeyboard"></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 底部操作区 -->
    <view class="bottom-action">
      <button class="btn-primary" @tap="finalSubmit">
        确认修改
      </button>
      <!-- 安全区适配 -->
      <view class="safe-area-bottom"></view>
    </view>

    <!-- 3. 数字键盘 -->
    <Keyboard 
      v-if="showKeyboard"
      :activeField="activeField"
      :disabledKeys="getDisabledKeys()"
      :showSubmitBar="false"
      @keyPress="onKeyPress"
      @nextField="nextField"
      @close="showKeyboard = false"
    />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { updatePortfolioHolding } from '@/services/portfolioHolding';
import Keyboard from '@/components/common/Keyboard.vue';

const activeField = ref('amount');
const showKeyboard = ref(false);
const portfolioId = ref('');

const form = ref({ id: '', symbol: '', name: '', amount: '', returnValue: '' });

onLoad((options) => {
  portfolioId.value = options.portfolioId;
  if (options.holdingInfo) {
    const holdingInfo = JSON.parse(decodeURIComponent(options.holdingInfo));
    console.log(holdingInfo);
    form.value = holdingInfo;
  }
});

const getDisabledKeys = () => {
  const disabled = [];
  if (activeField.value === 'amount') disabled.push('-');
  return disabled;
};

const profitColorClass = computed(() => {
  const val = parseFloat(form.value.returnValue);
  if (val > 0) return 'text-up';
  if (val < 0) return 'text-down';
  return '';
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
  if (val.length >= 12) return;
  form.value[activeField.value] += key;
};

const nextField = () => {
  activeField.value = activeField.value === 'amount' ? 'returnValue' : 'amount';
};

const finalSubmit = async () => {
  if (!form.value.amount) return uni.showToast({ title: '请填写金额', icon: 'none' });
  uni.showLoading({ title: '正在保存...', mask: true });
  try {
    payload = {
      id: form.value.id,
      amount: form.value.amount,
      returnValue: form.value.returnValue,
    }
    await updatePortfolioHolding(payload);
    uni.showToast({ title: '修改成功' });
    uni.$emit('refreshHoldings', portfolioId.value);
    setTimeout(() => uni.navigateBack(), 1200);
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
.edit-container {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

/* 录入卡片重构 */
.input-card {
  margin: $spacing-md;
  padding: 40rpx;
  
  .card-title-row {
    margin-bottom: 40rpx;
  }
  
  .card-title {
    font-size: 34rpx;
    font-weight: $fw-semibold;
    color: $text-main;
  }
}

.field-item {
  padding: 24rpx 0;
  border-bottom: 2rpx solid $border-color;
  transition: all 0.3s ease;
  
  &.active {
    border-bottom-color: $primary; // 激活时柿子橙底线
    // 当字段激活且键盘显示时，增强可见性
    .field-label {
      color: $primary;
    }
    .value {
      color: $text-main;
      font-weight: $fw-bold;
    }
  }
  
  &.readonly {
    border-bottom-style: dashed; // 只读字段使用虚线，视觉降权
  }

  .field-label {
    font-size: 22rpx;
    color: $text-placeholder;
    font-weight: $fw-medium;
    margin-bottom: 12rpx;
    display: block;
  }
  
  .value-row {
      display: flex;
      align-items: center;
      min-height: 60rpx;
      justify-content: flex-start;
      
      .unit {
        font-size: 24rpx;
        font-weight: $fw-semibold;
        color: $text-main;
        margin-right: 8rpx;
      }
      
      .value {
        font-size: 36rpx;
        font-weight: $fw-semibold;
        color: $text-main;
        
        &.main-info {
          font-size: 30rpx;
          color: $text-muted;
        }
        
        &.placeholder {
          color: $text-inverse;
          font-size: 28rpx;
          font-weight: $fw-regular;
        }
      }
    }
}

.row-flex {
  display: flex;
  gap: 30rpx;
  margin-top: 10rpx;
}

.flex-1 {
  flex: 1;
  min-width: 0;
}

/* 模拟光标变色 */
.custom-cursor {
  width: 4rpx;
  height: 38rpx;
  background: $primary;
  margin-left: 6rpx;
  animation: blink 1s infinite;
}

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* 底部操作区 */
.bottom-action {
  margin-top: auto;
  padding: $spacing-md;
  background-color: $bg-white;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.02);
}

.btn-primary {
  /* 已经在 App.vue 定义，这里仅做间距微调 */
  width: 100%;
}
</style>