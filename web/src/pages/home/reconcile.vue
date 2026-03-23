<template>
  <view class="reconcile-page">
    <!-- 1. 待校准资产信息 -->
    <view class="asset-info-section animate-fade-in">
      <view class="asset-card card-warm">
        <view class="item-header">
          <view class="item-icon-rect">
            <image :src="assetItem?.iconUrl || '/static/images/default-asset.png'" class="item-icon" />
          </view>
          <view class="item-title">
            <text class="name">{{ assetItem?.name || '未知资产' }}</text>
            <text class="account">{{ accountInfo?.institutionName || '所属账户' }}</text>
          </view>
        </view>

        <!-- 对比区域 -->
        <view class="comparison-grid">
          <view class="comp-item">
            <text class="label">系统当前金额</text>
            <text class="val num-font">{{ formatMoney(systemAmount) }}</text>
          </view>
          <view class="comp-divider">
            <uni-icons type="refreshempty" size="20" color="#ccd4d2"></uni-icons>
          </view>
          <view class="comp-item right">
            <text class="label">校准后实盘金额</text>
            <text class="val num-font" :class="actualAmount ? 'text-main' : 'text-placeholder'">
              {{ actualAmount ? formatMoney(actualAmount) : '0.00' }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 差额计算反馈 -->
    <view class="diff-feedback animate-fade-in" v-if="actualAmount && diffValue !== 0">
      <view class="diff-card" :class="diffValue > 0 ? 'bg-gain' : 'bg-loss'">
        <view class="diff-content">
          <text class="diff-label">{{ diffValue > 0 ? '校准溢盈' : '校准亏空' }}</text>
          <text class="diff-val num-font">
            {{ diffValue > 0 ? '+' : '' }}{{ formatMoney(diffValue) }}
          </text>
        </view>
        <text class="diff-tip">确认后系统将自动生成一笔“资产校准”记录</text>
      </view>
    </view>

    <!-- 3. 备注补充 -->
    <view class="input-panel card-warm">
      <view class="remark-box">
        <uni-icons type="compose" size="18" color="#a3b0ad"></uni-icons>
        <input 
          type="text" 
          v-model="remark" 
          placeholder="备注（如：更新净值、漏记补账）" 
          placeholder-class="placeholder-style"
        />
      </view>
    </view>

    <!-- 4. 数字键盘 -->
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
          <view class="action-btn cancel" hover-class="key-hover" @click="goBack">取消</view>
          <view class="action-btn finish" :class="{ disabled: !actualAmount }" hover-class="key-hover" @click="save">确定校准</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useAccountStore } from '@/stores/account.js';

const accountStore = useAccountStore();
const accountInfo = ref(null);
const assetItem = ref(null);

const systemAmount = ref(0); // 系统余额
const actualAmount = ref(''); // 用户输入的实盘余额
const remark = ref('');

onLoad(() => {
  // 从 Pinia 或 EventChannel 获取当前要校准的资产
  accountInfo.value = accountStore.currentAccount;
  assetItem.value = accountStore.selectedAsset;
  
  if (assetItem.value) {
    systemAmount.value = assetItem.value.totalBalance || 0;
  }
});

// 计算差额
const diffValue = computed(() => {
  if (!actualAmount.value) return 0;
  return Number(actualAmount.value) - systemAmount.value;
});

const formatMoney = (val) => {
  return Math.abs(Number(val) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const onKeyPress = (key) => {
  if (key === '.' && (actualAmount.value.includes('.') || !actualAmount.value)) return;
  if (actualAmount.value.includes('.') && actualAmount.value.split('.')[1].length >= 2) return;
  actualAmount.value += key;
};

const onDelete = () => actualAmount.value = actualAmount.value.slice(0, -1);

const goBack = () => uni.navigateBack();

const save = () => {
  if (!actualAmount.value) return;
  
  uni.showModal({
    title: '确认校准',
    content: `将资产余额调整为 ¥${formatMoney(actualAmount.value)}，差额将被记录为校准${diffValue.value > 0 ? '收入' : '支出'}。`,
    confirmColor: '#2a806c',
    success: (res) => {
      if (res.confirm) {
        executeReconcile();
      }
    }
  });
};

const executeReconcile = () => {
  uni.showLoading({ title: '校准中' });
  // 调用后端 API，更新总额并创建 transaction
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '校准完成', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1000);
  }, 800);
};
</script>

<style lang="scss" scoped>
.reconcile-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-page;
}

/* 1. 资产展示 */
.asset-info-section {
  padding: $spacing-md $spacing-md 0;
  .asset-card {
    padding: 32rpx;
    .item-header {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-bottom: 40rpx;
      .item-icon-rect {
        width: 80rpx; height: 80rpx; background-color: $bg-page; border-radius: $radius-base;
        @include flex-center;
        .item-icon { width: 56rpx; height: 56rpx; }
      }
      .item-title {
        .name { font-size: 30rpx; font-weight: $fw-semibold; color: $text-main; display: block; }
        .account { font-size: 22rpx; color: $text-placeholder; }
      }
    }
  }
}

.comparison-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .comp-item {
    flex: 1;
    .label { font-size: 20rpx; color: $text-muted; display: block; margin-bottom: 8rpx; }
    .val { font-size: 36rpx; font-weight: $fw-bold; color: $text-sub; }
    &.right { text-align: right; .val { font-size: 40rpx; } }
  }
  .comp-divider { padding: 0 20rpx; padding-top: 30rpx; }
}

/* 2. 差额反馈卡片 */
.diff-feedback {
  padding: 24rpx $spacing-md;
  .diff-card {
    padding: 24rpx 32rpx;
    border-radius: $radius-base;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    &.bg-gain { background-color: $bg-income; .diff-val { color: $color-income; } .diff-label { color: $color-income; } }
    &.bg-loss { background-color: $bg-error; .diff-val { color: $color-error; } .diff-label { color: $color-error; } }
    
    .diff-content {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      .diff-label { font-size: 24rpx; font-weight: $fw-bold; }
      .diff-val { font-size: 44rpx; font-weight: $fw-bold; }
    }
    .diff-tip { font-size: 20rpx; color: $text-placeholder; }
  }
}

/* 3. 输入区 */
.input-panel {
  margin: 0 $spacing-md;
  padding: 24rpx 32rpx;
  .remark-box {
    display: flex;
    align-items: center;
    gap: 16rpx;
    input { flex: 1; font-size: 28rpx; color: $text-main; }
  }
}

/* 4. 键盘 */
.keyboard-container {
  margin-top: auto;
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
    font-size: 28rpx;
    &.finish { 
      background-color: $primary; color: $white; flex: 1; font-weight: $fw-bold; 
      &.disabled { background-color: $gray-100; color: $text-placeholder; }
    }
    &.cancel { color: $text-regular; }
  }
}

.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
</style>