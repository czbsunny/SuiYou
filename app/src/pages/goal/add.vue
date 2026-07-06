<template>
  <view class="add-goal-container">
    <view class="amount-card">
      <text class="label">预期目标金额</text>
      <view class="input-row">
        <text class="symbol">¥</text>
        <input 
          type="digit" 
          v-model="form.targetAmount" 
          placeholder="0.00" 
          class="amount-input"
          placeholder-class="amount-placeholder"
        />
      </view>
    </view>

    <view class="form-group">
      <view class="form-item">
        <text class="item-label">目标名称</text>
        <input 
          v-model="form.title" 
          placeholder="例如：换台新电脑" 
          class="item-input" 
          placeholder-class="placeholder"
        />
      </view>

      <view class="divider"></view>

      <view class="form-item" @click="toggleIconPicker">
        <text class="item-label">选择图标</text>
        <view class="item-value">
          <image v-if="isUrlIcon" :src="form.iconUrl" class="selected-icon-img" mode="aspectFit" />
          <text v-else class="selected-icon-emoji">{{ form.iconUrl }}</text>
          <view class="arrow-right"></view>
        </view>
      </view>

      <view class="divider"></view>

      <picker mode="date" :value="form.deadline" @change="onDateChange">
        <view class="form-item">
          <text class="item-label">达成日期</text>
          <view class="item-value">
            <text :class="{ 'placeholder': !form.deadline }">
              {{ form.deadline || '点击选择日期' }}
            </text>
            <view class="arrow-right"></view>
          </view>
        </view>
      </picker>
    </view>

    <view class="form-group">
      <view class="form-item">
        <view class="item-label-group">
          <text class="item-label">设为主线目标</text>
          <text class="item-desc">开启后将置顶展示并可设置背景图</text>
        </view>
        <switch :checked="form.isPrimary" color="#2A806C" @change="onPrimaryChange" />
      </view>
    </view>

    <view class="icon-picker-mask" v-if="showIconPicker" @click="toggleIconPicker">
      <view class="icon-picker-content" @click.stop>
        <view class="picker-header">选择一个好意头</view>
        <view class="icon-grid">
          <view 
            v-for="emoji in emojiList" 
            :key="emoji" 
            class="emoji-item"
            :class="{ active: form.iconUrl === emoji }"
            @click="selectIcon(emoji)"
          >
            {{ emoji }}
          </view>
        </view>
      </view>
    </view>

    <view class="footer-btn-box">
      <button class="save-btn" @click="handleSave">确认开启目标</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useGoalStore } from '@/stores/goal.js';

const goalStore = useGoalStore();

const form = ref({
  title: '',
  targetAmount: '',
  iconUrl: '💰',
  deadline: '',
  isPrimary: false,
  categoryCode: '',
  templateCode: '',
  bgUrl: ''
});

const showIconPicker = ref(false);
const emojiList = ['💰', '🏠', '🚗', '🏔️', '📷', '💻', '🎓', '✈️', '💍', '🎁', '👶', '🏥'];

const isUrlIcon = computed(() => {
  return form.value.iconUrl && (form.value.iconUrl.startsWith('http') || form.value.iconUrl.startsWith('/static'));
});

onLoad((options) => {
  if (options.tpl) {
    const tplData = goalStore.templates.find(t => (t.code === options.tpl) || (t.templateCode === options.tpl));
    if (tplData) {
      form.value.templateCode = tplData.templateCode;
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
  }
});

const toggleIconPicker = () => {
  showIconPicker.value = !showIconPicker.value;
};

const selectIcon = (emoji) => {
  form.value.iconUrl = emoji;
  showIconPicker.value = false;
};

const onDateChange = (e) => {
  form.value.deadline = e.detail.value;
};

const onPrimaryChange = (e) => {
  form.value.isPrimary = e.detail.value;
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
      isPrimary: form.value.isPrimary,
      iconUrl: form.value.iconUrl,
      bgUrl: form.value.bgUrl,
      categoryCode: form.value.categoryCode,
      templateCode: form.value.templateCode
    };
    
    await goalStore.createGoal(createData);

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

.add-goal-container {
  min-height: 100vh;
  background-color: $background;
  padding: $spacing-3 $spacing-4;
}

.amount-card {
  border-radius: $rounded-md;
  padding: $spacing-4 $spacing-4;
  margin-bottom: $spacing-4;
  color: #fff;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  background-color: $primary;
  
  .label {
    font-size: 32rpx;
    opacity: 0.8;
    margin-bottom: $spacing-4;
    display: block;
  }

  .input-row {
    display: flex;
    align-items: baseline;
    .symbol { font-size: 60rpx; font-weight: bold; margin-right: 16rpx; }
    .amount-input {
      font-size: 80rpx;
      height: 100rpx;
      font-family: $font-family-mono;
      font-weight: bold;
      flex: 1;
    }
    .amount-placeholder { color: rgba(255, 255, 255, 0.3); }
  }
}

.form-group {
  background-color: #fff;
  border-radius: $rounded-md;
  padding: 0 $spacing-4;
  margin-bottom: $spacing-4;
}

.form-item {
  min-height: 110rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;

  .item-label { font-size: 28rpx; color: #333; font-weight: 500; }
  .item-label-group {
    display: flex; flex-direction: column;
    .item-desc { font-size: 20rpx; color: #999; margin-top: 4rpx; }
  }

  .item-input { text-align: right; font-size: 28rpx; color: #333; flex: 1; margin-left: 40rpx; }

  .item-value {
    display: flex; align-items: center; gap: 10rpx;
    
    .selected-icon-img {
      width: 60rpx;
      height: 60rpx;
      border-radius: $rounded-md;
    }
    .selected-icon-emoji { font-size: 44rpx; }
    .placeholder { color: #ccc; font-size: 28rpx; }
  }
}

.divider { height: 1rpx; background-color: #f5f5f5; }

.icon-picker-mask {
  position: fixed; inset: 0; background-color: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.icon-picker-content {
  width: 100%; background-color: #fff; border-radius: $rounded-lg $rounded-lg 0 0;
  padding: $spacing-5; padding-bottom: calc($spacing-5 + env(safe-area-inset-bottom));
  .picker-header { font-size: 30rpx; font-weight: bold; margin-bottom: $spacing-5; text-align: center; }
  .icon-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: $spacing-3; }
  .emoji-item {
    height: 100rpx; display: flex; align-items: center; justify-content: center;
    font-size: 50rpx; background-color: #f8fafc; border-radius: $rounded-md;
    &.active { border: 4rpx solid $primary; background-color: #eefdf5; }
  }
}

.footer-btn-box {
  .save-btn {
    color: #fff; height: 100rpx; line-height: 100rpx; border-radius: $rounded-md; background-color: $primary;
    font-size: 32rpx; font-weight: bold; box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.1);
    &:active { transform: scale(0.98); }
  }
}

.arrow-right {
  width: 14rpx; height: 14rpx;
  border-top: 3rpx solid #ccc; border-right: 3rpx solid #ccc;
  transform: rotate(45deg);
}
</style>