<template>
  <view class="page">
    <view class="icon-display">
      <image v-if="form.iconUrl && (form.iconUrl.startsWith('http') || form.iconUrl.startsWith('/static'))" 
             :src="form.iconUrl" 
             class="icon-img" 
             mode="aspectFit" />
      <text v-else class="icon-emoji">{{ form.iconUrl }}</text>
    </view>

    <view class="form-item">
      <text class="form-label">目标名称<text class="required-mark">*</text></text>
      <view class="input-wrap">
        <input 
          class="form-input" 
          v-model="form.title" 
          placeholder="例如：积攒一笔钱"
        />
      </view>
    </view>

    <view class="form-item">
      <text class="form-label">目标金额<text class="required-mark">*</text></text>
      <view class="input-wrap">
        <text class="amount-symbol">¥</text>
        <input 
          type="digit" 
          v-model="form.targetAmount" 
          placeholder="0.00" 
          class="form-input amount-input"
          placeholder-class="amount-placeholder"
        />
      </view>
    </view>

    <view class="form-item">
      <text class="form-label">达成日期</text>
      <uni-datetime-picker 
        type="month" 
        return-type="string" 
        :value="form.deadline" 
        :border="false"
        @change="onDateChange"
      >
        <view class="form-picker">
          <text :class="{ 'picker-placeholder': !form.deadline }">
            {{ form.deadline || '点击选择日期' }}
          </text>
          <image src="/static/images/chevron_right.png" class="picker-arrow" mode="aspectFit" />
        </view>
      </uni-datetime-picker>
    </view>

    <view class="bottom-bar">
      <view class="bottom-content">
        <button class="confirm-btn" @tap="handleSave">
          <text class="btn-text">创建</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createGoal } from '@/api/modules/goal.js';

const form = ref({
  title: '',
  targetAmount: '',
  iconUrl: '💰',
  deadline: '',
  categoryCode: '',
  templateCode: '',
  bgUrl: ''
});

onLoad((options) => {
  if (options.tpl) {
    try {
      const tplData = JSON.parse(decodeURIComponent(options.tpl));
      console.log(tplData);
      if (tplData) {
        form.value.templateCode = tplData.code;
        form.value.categoryCode = tplData.categoryCode;
        form.value.title = tplData.name;
        form.value.targetAmount = tplData.defaultAmount;
        form.value.iconUrl = tplData.iconUrl;

        if (tplData.defaultPeriodDays) {
          const date = new Date();
          date.setDate(date.getDate() + tplData.defaultPeriodDays);
          form.value.deadline = date.toISOString().split('T')[0];
        }
      }
    } catch (e) {
      console.error('解析模板数据失败:', e);
    }
  }
});

const onDateChange = (val) => {
  form.value.deadline = val;
};

const handleSave = async () => {
  if (!form.value.title || !form.value.targetAmount) {
    uni.showToast({ title: '请填写名称和金额', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '创建中...', mask: true });

  try {
    const createData = {
      name: form.value.title,
      targetAmount: parseFloat(form.value.targetAmount),
      deadline: form.value.deadline,
      iconUrl: form.value.iconUrl,
      bgUrl: form.value.bgUrl,
      categoryCode: form.value.categoryCode,
      templateCode: form.value.templateCode
    };
    
    await createGoal(createData);

    uni.hideLoading();
    uni.showToast({ title: '目标已开启！', icon: 'success' });
    
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/goal/index',
        success: () => {
          uni.$emit('refreshGoalList'); 
        }
      });
    }, 1500);
  } catch (error) {
    uni.hideLoading();
    uni.showModal({
      title: '提示',
      content: error.message || '系统繁忙，请稍后再试',
      showCancel: false
    });
  }
};
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/common.scss';

.page {
  min-height: 100vh;
  background-color: $background;
  padding: $spacing-4;
  padding-bottom: calc(#{$spacing-4} + 160rpx);
}

.icon-display {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: $spacing-6;
  padding-top: $spacing-4;
}

.icon-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: $rounded-xl;
}

.icon-emoji {
  font-size: 80rpx;
}

.form-item {
  margin-bottom: $spacing-6;
}

.form-label {
  display: block;
  font-size: $font-size-lg;
  font-weight: 900;
  color: $outline;
  margin-bottom: $spacing-3;
  padding-left: $spacing-1;
  letter-spacing: 2rpx;
}

.required-mark {
  color: $error;
  margin-left: 4rpx;
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 108rpx;
  padding-left: $spacing-4;
  background: #FFFFFF;
  border-radius: $rounded-xl;
  box-shadow: $shadow-soft;
}

.form-input {
  flex: 1;
  height: 108rpx;
  padding: 0 $spacing-4 0 0;
  background: transparent;
  border: none;
  font-size: $font-size-lg;
  color: $on-surface;
  box-sizing: border-box;
  outline: none;
  transition: box-shadow $transition-base;
  line-height: 108rpx;

  &:focus {
    box-shadow: inset 0 0 0 4rpx rgba($primary, 0.2);
  }

  &::placeholder {
    color: $outline-variant;
  }
}

.amount-symbol {
  font-size: $font-size-lg;
  font-weight: 900;
  color: $on-surface;
  margin-right: $spacing-2;
}

.amount-input {
  flex: 1;
  height: 108rpx;
  padding-right: $spacing-4;
  font-size: $font-size-lg;
  color: $on-surface;
  font-family: $font-family-mono;
  background: transparent;
  border: none;
  outline: none;
}

.amount-placeholder {
  color: $outline-variant;
}

.form-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 108rpx;
  padding: 0 $spacing-4;
  background: #FFFFFF;
  border-radius: $rounded-xl;
  font-size: $font-size-lg;
  color: $on-surface;
  box-shadow: $shadow-soft;
}

.picker-placeholder {
  color: $outline-variant;
}

.picker-arrow {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-4;
  background: $surface-container-lowest;
  z-index: 100;
}

.bottom-content {
  max-width: 720rpx;
  margin: 0 auto;
}

.confirm-btn {
  width: 100%;
  padding: 24rpx;
  background-color: $primary !important;
  border-radius: $rounded-full;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;

  &:active {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.btn-text {
  font-size: $font-size-lg;
  font-weight: 900;
  color: $on-primary;
}
</style>