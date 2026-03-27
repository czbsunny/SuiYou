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
          <image :src="fromAccount?.logoUrl || '/static/images/account.png'" class="account-icon" />
        </view>
        <view class="account-info">
          <text class="direction-label">从（转出账户）</text>
          <text class="account-name" :class="{ 'placeholder': !fromAccount }">
            {{ fromAccount?.accountName || '请选择转出账户' }}
          </text>
        </view>
        <uni-icons v-if="!isFromLocked" type="right" size="16" color="#ccd4d2"></uni-icons>
        <uni-icons v-else type="lock" size="14" color="#d1d5db"></uni-icons>
      </view>

      <!-- 2. 中间切换图标 -->
      <view class="connector">
        <image src="/static/images/switch.png" class="switch-icon" />
      </view>

      <!-- 转入账户 -->
      <view 
        class="account-row" 
        :class="{ locked: isToLocked }"
        @click="!isToLocked && handleSelect('to')"
      >
        <view class="account-icon-box bg-income">
          <image :src="toAccount?.logoUrl || '/static/images/account.png'" class="account-icon" />
        </view>
        <view class="account-info">
          <text class="direction-label">至（转入账户）</text>
          <text class="account-name" :class="{ 'placeholder': !toAccount }">
            {{ toAccount?.accountName || '请选择转入账户' }}
          </text>
        </view>
        <uni-icons v-if="!isToLocked" type="right" size="16" color="#ccd4d2"></uni-icons>
        <uni-icons v-else type="lock" size="14" color="#d1d5db"></uni-icons>
      </view>
    </view>

    <!-- 3. 信息输入面板 -->
    <view class="input-panel card-warm">
      <!-- 金额显示 -->
      <view class="info-row">
        <view class="amount-container text-transfer">
          <text class="currency-symbol">￥</text>
          <text class="amount-display num-font">{{ amount || '0.00' }}</text>
        </view>
      </view>

      <!-- 操作栏（时间、手续费、备注） -->
      <view class="meta-row">
        <!-- 时间选择 -->
        <uni-datetime-picker 
          v-model="datetime" 
          type="datetime" 
          :border="false" 
          :clear-icon="false"
          @change="onDateTimeChange"
        >
          <view class="meta-btn">
            <image src="/static/images/calendar.png" class="meta-icon" />
            <text class="btn-text">{{ displayDateTime }}</text>
          </view>
        </uni-datetime-picker>

        <!-- 手续费 -->
        <view class="meta-btn" :class="{ active: showFeeInput }" @click="toggleFeeInput">
          <image src="/static/transfers/fee.png" class="meta-icon" />
          <text class="btn-text" v-if="!feeAmount || !showFeeInput">手续费</text>
          <text class="btn-text num-font" v-else>{{ feeAmount }}</text>
        </view>

        <!-- 备注 -->
        <view class="remark-box-inline">
          <input 
            type="text" 
            v-model="remark" 
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
          <view class="action-btn next" hover-class="key-hover" @click="saveAndNext">再记</view>
          <view class="action-btn finish" hover-class="key-hover" @click="save">完成</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();

const fromAccount = ref(null);
const toAccount = ref(null);
const isFromLocked = ref(false);
const isToLocked = ref(false);
const currentDirection = ref(null);
const amount = ref('');
const feeAmount = ref('');
const remark = ref('');
const showFeeInput = ref(false);

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
const datetime = ref(formatNow());

const displayDateTime = computed(() => {
  if (!datetime.value) return '日期';
  const dt = datetime.value.replace('T', ' ');
  const nowStr = new Date().toISOString().split('T')[0];
  if (dt.startsWith(nowStr)) {
    return '今天 ' + dt.split(' ')[1].substring(0, 5);
  }
  return dt.substring(5, 16);
});

onLoad((options) => {
  uni.$on('acceptAccountFromSelector', (res) => {
    const instConfig = configStore.institutionMap[res.account.institution] || {};
    res.account.logoUrl = instConfig.logoUrl || '/static/icons/default-bank.png';
    if (currentDirection.value === 'from') fromAccount.value = res.account;
    else toAccount.value = res.account;
    uni.vibrateShort();
  });

  if (options.fromAccount) {
    fromAccount.value = JSON.parse(decodeURIComponent(options.fromAccount));
    isFromLocked.value = true;
  }
  if (options.toAccount) {
    toAccount.value = JSON.parse(decodeURIComponent(options.toAccount));
    isToLocked.value = true;
  }
});

onUnmounted(() => {
  uni.$off('acceptAccountFromSelector');
});

const handleSelect = (direction) => {
  currentDirection.value = direction;
  uni.navigateTo({
    url: '/pages/assets/account-selector'
  });
};

const onDateTimeChange = (val) => {
  datetime.value = val;
};

const onKeyPress = (key) => {
  let target = showFeeInput.value ? feeAmount : amount;
  if (key === '.' && (target.value.includes('.') || !target.value)) return;
  if (target.value.includes('.')) {
    const parts = target.value.split('.');
    if (parts[1].length >= 2) return;
  }
  if (parseFloat(target.value + key) >= 100000000) return;
  target.value += key;
};

const onDelete = () => {
  if (showFeeInput.value) feeAmount.value = feeAmount.value.slice(0, -1);
  else amount.value = amount.value.slice(0, -1);
};

const toggleFeeInput = () => {
  showFeeInput.value = !showFeeInput.value;
};

const save = () => {
  if (!fromAccount.value || !toAccount.value) return uni.showToast({ title: '请选择账户', icon: 'none' });
  if (fromAccount.value.id === toAccount.value.id) return uni.showToast({ title: '账户不能相同', icon: 'none' });
  if (!amount.value || parseFloat(amount.value) <= 0) return uni.showToast({ title: '请输入金额', icon: 'none' });
  
  const payload = {
    type: 'TRANSFER',
    amount: parseFloat(amount.value),
    fee: parseFloat(feeAmount.value || 0),
    transTime: datetime.value.replace(' ', 'T'),
    sourceAssetId: fromAccount.value.id,
    targetAssetId: toAccount.value.id,
    description: remark.value
  };
  console.log('提交调拨:', payload);
  uni.showLoading({ title: '处理中' });
  setTimeout(() => {
    uni.hideLoading();
    uni.navigateBack();
  }, 600);
};

const saveAndNext = () => {
  uni.showToast({ title: '已保存', icon: 'success' });
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

.account-selector-card {
  margin: 30rpx $spacing-md;
  padding: 40rpx 32rpx;
  
  .account-row {
    display: flex; align-items: center; gap: 24rpx; padding: 24rpx;
    border-radius: $radius-base; background-color: $bg-subtle;
    transition: all 0.2s;
    
    &.locked {
      background-color: #f9fafb;
      opacity: 0.9;
    }
    
    .account-icon-box {
      width: 80rpx; height: 80rpx; border-radius: 24rpx; @include flex-center;
      &.bg-transfer { background-color: $bg-transfer; }
      &.bg-income { background-color: $bg-income; }
      .account-icon { width: 48rpx; height: 48rpx; }
    }
    
    .account-info {
      flex: 1;
      .direction-label { font-size: 20rpx; color: $text-muted; margin-bottom: 4rpx; display: block;}
      .account-name { 
        font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; 
        &.placeholder { color: $text-placeholder; font-weight: $fw-regular; }
      }
    }
  }

  /* 简化后的连接器 */
  .connector {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 16rpx 0;
    .switch-icon {
      width: 44rpx;
      height: 44rpx;
      opacity: 0.2;
    }
  }
}

.input-panel {
  margin: 0 $spacing-md 20rpx;
  padding: 24rpx 32rpx;
  
  .info-row {
    display: flex; justify-content: flex-start; align-items: center;
    .amount-container {
      display: flex; align-items: baseline;
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
    }

    .meta-btn, .remark-box-inline {
      background-color: $bg-page;
      padding: 10rpx 20rpx;
      border-radius: $radius-base;
      display: flex;
      align-items: center;
      gap: 8rpx;
      height: 52rpx; /* 固定高度确保对齐 */
      
      .meta-icon {
        width: 28rpx;
        height: 28rpx;
        flex-shrink: 0;
        display: block;
      }

      .btn-text {
        font-size: 22rpx;
        color: $text-sub;
        font-weight: $fw-medium;
        line-height: 1; /* 强制文字不产生额外行间距 */
      }
      
      &.active {
        background-color: $bg-transfer;
        .btn-text { color: $color-transfer; font-weight: $fw-bold; }
      }
    }
    
    .remark-box-inline {
      flex: 1;
      min-width: 180rpx;
      .meta-icon { opacity: 0.4; }
      input {
        flex: 1;
        font-size: 22rpx;
        color: $text-main;
        height: 100%;
        line-height: 1;
      }
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

.text-transfer { color: $color-transfer; }
.placeholder-style { color: $text-placeholder; }
</style>