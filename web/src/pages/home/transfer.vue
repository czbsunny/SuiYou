<template>
  <view class="transfer-page">
    <!-- 1. 账户选择卡片区 -->
    <view class="account-selector-card card-warm">
      <!-- 转出账户 -->
      <view 
        class="account-row" 
        :class="{ locked: isFromLocked }"
        @click="!isFromLocked && handleSelect('from')"
      >
        <view class="account-icon-box bg-transfer">
          <image :src="fromAccount?.iconUrl || '/static/images/wallet-fill.png'" class="account-icon" />
        </view>
        <view class="account-info">
          <text class="direction-label">从（转出账户）</text>
          <text class="account-name" :class="{ 'placeholder': !fromAccount }">
            {{ fromAccount?.name || '请选择转出账户' }}
          </text>
        </view>
        <uni-icons v-if="!isFromLocked" type="right" size="16" color="#ccd4d2"></uni-icons>
        <uni-icons v-else type="lock" size="16" color="#ccd4d2"></uni-icons>
      </view>

      <!-- 连接线/箭头 -->
      <view class="connector">
        <view class="line"></view>
        <view class="icon-circle">
          <uni-icons type="bottom" size="14" color="#2a806c"></uni-icons>
        </view>
        <view class="line"></view>
      </view>

      <!-- 转入账户 -->
      <view 
        class="account-row" 
        :class="{ locked: isToLocked }"
        @click="!isToLocked && handleSelect('to')"
      >
        <view class="account-icon-box bg-income">
          <image :src="toAccount?.iconUrl || '/static/images/wallet-fill.png'" class="account-icon" />
        </view>
        <view class="account-info">
          <text class="direction-label">至（转入账户）</text>
          <text class="account-name" :class="{ 'placeholder': !toAccount }">
            {{ toAccount?.name || '请选择转入账户' }}
          </text>
        </view>
        <uni-icons v-if="!isToLocked" type="right" size="16" color="#ccd4d2"></uni-icons>
        <uni-icons v-else type="lock" size="16" color="#ccd4d2"></uni-icons>
      </view>
    </view>

    <!-- 2. 信息输入面板 -->
    <view class="input-panel card-warm">
      <view class="info-row">
        <!-- 备注 -->
        <view class="remark-box">
          <input type="text" v-model="remark" placeholder="添加备注..." placeholder-class="placeholder-style" />
        </view>
        <!-- 转账金额显示 -->
        <view class="amount-display num-font text-transfer">
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

        <!-- 手续费输入开关 -->
        <view class="meta-btn" :class="{ active: showFeeInput }" @click="toggleFeeInput">
          <uni-icons type=" chemistry" size="14" :color="showFeeInput ? '#2a806c' : '#7a8582'"></uni-icons>
          <text v-if="!feeAmount || !showFeeInput">手续费</text>
          <text v-else class="num-font">{{ feeAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 3. 自定义键盘 -->
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
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const fromAccount = ref(null);
const toAccount = ref(null);
const isFromLocked = ref(false);
const isToLocked = ref(false);

const amount = ref('');
const feeAmount = ref('');
const remark = ref('');
const date = ref(new Date().toISOString().split('T')[0]);
const showFeeInput = ref(false); // 切换当前输入是金额还是手续费

onLoad((options) => {
  // 传参逻辑处理
  if (options.fromAccount) {
    fromAccount.value = JSON.parse(decodeURIComponent(options.fromAccount));
    isFromLocked.value = true;
  }
  if (options.toAccount) {
    toAccount.value = JSON.parse(decodeURIComponent(options.toAccount));
    isToLocked.value = true;
  }
});

const isToday = computed(() => date.value === new Date().toISOString().split('T')[0]);

// 键盘输入逻辑
const onKeyPress = (key) => {
  let target = showFeeInput.value ? feeAmount : amount;
  
  if (key === '.') {
    if (target.value.includes('.') || target.value === '') return;
  }
  if (target.value.includes('.') && target.value.split('.')[1].length >= 2) return;
  
  target.value += key;
};

const onDelete = () => {
  let target = showFeeInput.value ? feeAmount : amount;
  target.value = target.value.slice(0, -1);
};

const toggleFeeInput = () => {
  showFeeInput.value = !showFeeInput.value;
  if (showFeeInput.value) {
    uni.showToast({ title: '正在输入手续费', icon: 'none', duration: 1000 });
  }
};

const handleSelect = (type) => {
  // 跳转到账户选择列表，带回选择结果
  uni.navigateTo({ url: `/pages/accounts/selector?type=${type}` });
};

const onDateChange = (e) => { date.value = e.detail.value; };

const save = () => {
  if (!fromAccount.value || !toAccount.value) return uni.showToast({ title: '请选择完整账户', icon: 'none' });
  if (fromAccount.value.id === toAccount.value.id) return uni.showToast({ title: '转出与转入账户不能相同', icon: 'none' });
  if (!amount.value || Number(amount.value) <= 0) return uni.showToast({ title: '请输入金额', icon: 'none' });
  
  uni.showLoading({ title: '处理中' });
  // 模拟提交
  setTimeout(() => {
    uni.hideLoading();
    uni.navigateBack();
  }, 600);
};

const saveAndNext = () => {
  uni.showToast({ title: '调拨成功', icon: 'success' });
  amount.value = '';
  feeAmount.value = '';
  remark.value = '';
};
</script>

<style lang="scss" scoped>
.transfer-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-page;
}

/* 1. 账户选择卡片 */
.account-selector-card {
  margin: 30rpx $spacing-md;
  padding: 40rpx 32rpx;
  
  .account-row {
    display: flex;
    align-items: center;
    gap: 24rpx;
    padding: 24rpx;
    border-radius: $radius-base;
    background-color: $bg-subtle;
    transition: all 0.2s;
    
    &.locked {
      background-color: $gray-50;
      opacity: 0.8;
    }
    
    .account-icon-box {
      width: 80rpx; height: 80rpx; border-radius: 20rpx;
      @include flex-center;
      &.bg-transfer { background-color: $bg-transfer; }
      &.bg-income { background-color: $bg-income; }
      .account-icon { width: 48rpx; height: 48rpx; }
    }
    
    .account-info {
      flex: 1;
      .direction-label { font-size: 20rpx; color: $text-placeholder; display: block; margin-bottom: 4rpx; }
      .account-name { 
        font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; 
        &.placeholder { color: $text-placeholder; font-weight: $fw-regular; }
      }
    }
  }

  /* 垂直连接线样式 */
  .connector {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: -10rpx 0;
    .line { width: 1rpx; height: 30rpx; background-color: $gray-200; }
    .icon-circle {
      width: 48rpx; height: 48rpx; border-radius: 50%;
      background-color: $bg-white; border: 1rpx solid $gray-100;
      @include flex-center;
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
      z-index: 10;
    }
  }
}

/* 2. 输入面板 */
.input-panel {
  margin: 0 $spacing-md 20rpx;
  padding: 32rpx;
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    .remark-box {
      flex: 1;
      input { font-size: 30rpx; color: $text-main; }
    }
    .amount-display {
      font-size: 60rpx;
      font-weight: $fw-bold;
      color: $color-transfer; /* 使用紫色调拨色 */
    }
  }
  .meta-row {
    display: flex;
    gap: 16rpx;
    .meta-btn {
      background-color: $bg-page;
      padding: 10rpx 24rpx;
      border-radius: 8rpx;
      display: flex;
      align-items: center;
      gap: 8rpx;
      transition: all 0.2s;
      text { font-size: 24rpx; color: $text-sub; }
      &.active {
        background-color: $bg-transfer;
        text { color: $color-transfer; font-weight: $fw-bold; }
      }
    }
  }
}

/* 3. 键盘 (同收支页面) */
.keyboard-container {
  background-color: $bg-white;
  padding: 12rpx 12rpx calc(env(safe-area-inset-bottom) + 12rpx);
  .key-grid { display: flex; gap: 12rpx; }
  .num-keys { flex: 3; display: grid; grid-template-columns: repeat(3, 1fr); gap: 12rpx; }
  .action-keys { flex: 1; display: flex; flex-direction: column; gap: 12rpx; }
  .key-btn, .action-btn {
    height: 106rpx;
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