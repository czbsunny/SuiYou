<template>
  <view class="reconcile-page">
    <!-- 1. 顶部资产卡片 -->
    <view class="asset-info-section animate-fade-in">
      <view class="asset-card card-warm">
        <view class="item-header">
          <view class="item-icon-rect">
            <image :src="selectedAccount?.selectedAsset?.iconUrl || '/static/images/account.png'" class="item-icon" mode="aspectFit" />
          </view>
          <view class="item-title" :class="{ 'disabled': isLocked }" @click="showAccountPicker">
            <text class="name">{{ selectedAccount?.selectedAsset?.name || '未选资产' }}</text>
            <text class="account">
              {{ selectedAccount?.accountName || '点击选择账户' }}
            </text>
          </view>
        </view>

        <!-- 对比区域：当前 vs 目标 -->
        <view class="comparison-grid">
          <view class="comp-item">
            <text class="label">当前账面余额</text>
            <text class="val num-font">{{ formatMoney(systemAmount) }}</text>
          </view>
          <view class="comp-divider">
            <image src="/static/images/switch.png" class="arrow-icon" />
          </view>
          <view class="comp-item right">
            <text class="label">实际最新余额</text>
            <text class="val num-font" :class="actualAmount ? 'text-main' : 'text-placeholder'">
              {{ actualAmount ? formatMoney(actualAmount) : '0.00' }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 2. 差额动态反馈 (核心变动提示) -->
    <view class="diff-feedback-section animate-fade-in" v-if="actualAmount">
      <view class="diff-card" :class="diffValue >= 0 ? 'is-gain' : 'is-loss'">
        <view class="diff-header">
          <text class="diff-label">变动额 ({{ recordAsProfit ? '投资盈亏' : '账面校准' }})</text>
          <text class="diff-val num-font">{{ diffValue >= 0 ? '+' : '' }}{{ formatMoney(diffValue) }}</text>
        </view>
        <view class="diff-tip">
          <text v-if="recordAsProfit">记录为盈亏，将计入该资产的投资收益统计</text>
          <text v-else>记录为校准，仅用于对账，不计入投资损益</text>
        </view>
      </view>
    </view>

    <!-- 3. 设置面板 (盈亏开关 + 备注) -->
    <view class="settings-panel card-warm">
      <!-- 盈亏开关 -->
      <view class="setting-row">
        <view class="s-left">
          <text class="s-title">标记为投资收益/亏损</text>
          <text class="s-hint">由于净值变动导致的金额变化</text>
        </view>
        <switch 
          :checked="recordAsProfit" 
          @change="onProfitSwitchChange" 
          color="#2a806c" 
          style="transform: scale(0.8);" 
        />
      </view>
      
      <view class="divider"></view>

      <!-- 备注输入 -->
      <view class="setting-row">
        <image src="/static/images/edit_fund.png" class="edit-icon" />
        <input 
          type="text" 
          v-model="remark" 
          placeholder="备注(可选)" 
          placeholder-class="placeholder-style"
        />
      </view>
    </view>

    <!-- 4. 数字键盘区 -->
    <view class="keyboard-container">
      <view class="key-grid">
        <view class="num-keys">
          <view v-for="key in ['1','2','3','4','5','6','7','8','9','0','.']" 
            :key="key" class="key-btn" hover-class="key-hover" @click="onKeyPress(key)">
            {{ key }}
          </view>
          <view class="key-btn" hover-class="key-hover" @click="onDelete">
            <image src="/static/images/del-key.png" mode="aspectFit" class="del-icon" />
          </view>
        </view>
        <view class="action-keys">
          <view class="action-btn cancel" hover-class="key-hover" @click="goBack">取消</view>
          <view class="action-btn finish" :class="{ disabled: !actualAmount }" hover-class="key-hover" @click="save">
            确定<br/>校准
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createTransaction } from '@/services/transactionService.js';

const systemAmount = ref(0);
const actualAmount = ref('');
const remark = ref('');
const recordAsProfit = ref(false);
const selectedAccount = ref(null);
const isLocked = ref(false);
const assignAccountId = ref(null);

onLoad((options) => {
  // 监听账户选择结果
  uni.$on('acceptAccountFromSelector', (res) => {
    selectedAccount.value = {
      ...res.account,
      selectedAsset: res.asset
    };
    systemAmount.value = res.asset.totalBalance || 0;
    isLocked.value = false;
  });

  // 解析传入数据
  if (options.data) {
    const data = JSON.parse(decodeURIComponent(options.data));
    if (data.account) {
        selectedAccount.value = {
          ...data.account,
          selectedAsset: data.asset || null
        };
        assignAccountId.value = data.account.id;
        if (data.asset) {
          systemAmount.value = data.asset.totalBalance || 0;
          isLocked.value = true; // 锁定账户选择
        }
    } 
    console.log("selectedAccount", selectedAccount.value)
    if (data.recordAsProfit !== undefined) {
      recordAsProfit.value = data.recordAsProfit;
    }
  }
});

onUnmounted(() => {
  uni.$off('acceptAccountFromSelector');
});

// 计算差额：输入值 - 系统值
const diffValue = computed(() => {
  if (!actualAmount.value) return 0;
  return Number(actualAmount.value) - systemAmount.value;
});

const formatMoney = (val) => {
  return Math.abs(Number(val) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

const onProfitSwitchChange = (e) => {
  recordAsProfit.value = e.detail.value;
  uni.vibrateShort();
};

const onKeyPress = (key) => {
  if (key === '.' && (actualAmount.value.includes('.') || !actualAmount.value)) return;
  if (actualAmount.value.includes('.') && actualAmount.value.split('.')[1].length >= 2) return;
  
  // 检查添加新字符后是否超过9999亿
  const newAmount = actualAmount.value + key;
  if (Number(newAmount) > 999900000000) return;
  
  actualAmount.value += key;
};

const onDelete = () => actualAmount.value = actualAmount.value.slice(0, -1);

const showAccountPicker = () => {
  const payload = {
    canSetDefault: true,
    initAccountId: selectedAccount.value?.id,
    initAssetId: selectedAccount.value?.selectedAsset?.id,
    assignAccountId: assignAccountId.value || null
  }
  const data = encodeURIComponent(JSON.stringify(payload));
  uni.navigateTo({
      url: `/pages/assets/account-selector?data=${data}`
  });
};

const goBack = () => uni.navigateBack();

const save = () => {
  if (!actualAmount.value || diffValue.value === 0) {
    uni.showToast({ title: '余额无变动', icon: 'none' });
    return;
  }
  
  if (!selectedAccount.value?.selectedAsset?.id) {
    return uni.showToast({ title: '请选择账户', icon: 'none' });
  }
  
  const typeText = recordAsProfit.value ? '投资收益' : '余额校准';
  uni.showModal({
    title: '确认校准',
    content: `将余额从 ¥${formatMoney(systemAmount.value)} 调整为 ¥${formatMoney(actualAmount.value)}。系统将生成一笔 ¥${formatMoney(diffValue.value)} 的${typeText}记录。`,
    confirmColor: '#2a806c',
    success: (res) => {
      if (res.confirm) executeReconcile();
    }
  });
};

const executeReconcile = async () => {
  uni.showLoading({ title: '正在提交' });
  
  const dto = {
    // 根据开关选择不同的枚举类型
    type: recordAsProfit.value ? 'INVESTMENT_RETURN' : 'ADJUSTMENT',
    amount: diffValue.value, 
    sourceAssetId: selectedAccount.value.selectedAsset.id,
    transTime: new Date().toISOString(),
    description: remark.value || (recordAsProfit.value ? '手动更新投资盈亏' : '资产余额校准')
  };

  try {
    await createTransaction(dto);
    uni.hideLoading();
    uni.showToast({ title: '校准成功', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1000);
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: '提交失败', icon: 'none' });
  }
};
</script>

<style lang="scss" scoped>
.reconcile-page {
  height: 100vh; background-color: $bg-page; display: flex; flex-direction: column;
}

/* 资产卡片 */
.asset-info-section {
  padding: $spacing-base $spacing-base 0;
  .asset-card {
    padding: 30rpx;
    .item-header {
      display: flex; align-items: center; gap: 20rpx; margin-bottom: 30rpx;
      .item-icon-rect { 
        width: 72rpx; height: 72rpx; background: $bg-page; border-radius: 12rpx; @include flex-center;
        .item-icon { width: 44rpx; height: 44rpx; }
      }
      .name { font-size: 28rpx; font-weight: $fw-bold; color: $text-main; }
      .account { font-size: 22rpx; color: $text-placeholder; display: block; }
    }
  }
}

.comparison-grid {
  display: flex; align-items: center; justify-content: space-between;
  .comp-item {
    flex: 1;
    .label { font-size: 22rpx; color: $text-muted; margin-bottom: 10rpx; display: block; }
    .val { font-size: 36rpx; font-weight: $fw-bold; color: $text-sub; }
    &.right { text-align: right; .val { font-size: 44rpx; color: $text-main; } }
  }
  .comp-divider {
    padding: 0 30rpx; padding-top: 30rpx;
    .arrow-icon { width: 32rpx; height: 32rpx; opacity: 0.2; }
  }
}

/* 差额提示 */
.diff-feedback-section {
  padding: $spacing-sm $spacing-base;
  .diff-card {
    padding: 24rpx 30rpx; border-radius: $radius-base;
    &.is-gain { background-color: $bg-income; .diff-val, .diff-label { color: $color-income; } }
    &.is-loss { background-color: $bg-error; .diff-val, .diff-label { color: $color-error; } }
    
    .diff-header {
      display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8rpx;
      .diff-label { font-size: 24rpx; font-weight: $fw-bold; }
      .diff-val { font-size: 40rpx; font-weight: $fw-bold; }
    }
    .diff-tip { font-size: 20rpx; color: $text-placeholder; }
  }
}

/* 设置面板 */
.settings-panel {
  margin: 0 $spacing-base; padding: 0 $spacing-base;
  .setting-row {
    display: flex; justify-content: space-between; align-items: center; padding: 30rpx 0;
    .s-title { font-size: 28rpx; color: $text-main; font-weight: 500; display: block; }
    .s-hint { font-size: 22rpx; color: $text-placeholder; }
    .edit-icon { width: 32rpx; height: 32rpx; margin-right: 16rpx; opacity: 0.4; }
    input { flex: 1; font-size: 26rpx; }
  }
  .divider { height: 1rpx; background: $gray-50; }
}

/* 数字键盘 (底部对齐) */
.keyboard-container {
  margin-top: auto;
  background-color: $white;
  padding: 20rpx 20rpx calc(env(safe-area-inset-bottom) + 20rpx);
  .key-grid { display: flex; gap: 16rpx; }
  .num-keys { flex: 3; display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; }
  .action-keys { flex: 1; display: flex; flex-direction: column; gap: 16rpx; }
  .key-btn, .action-btn {
    height: 100rpx; background: $white; border: 1rpx solid $gray-100; border-radius: 16rpx;
    @include flex-center; font-size: 40rpx; font-family: $font-family-money;
    .del-icon { width: 44rpx; height: 44rpx; }
  }
  .action-btn {
    font-size: 28rpx; flex: 1;
    &.cancel { color: $text-muted; }
    &.finish { 
      background: $primary; color: $white; font-weight: bold; 
      &.disabled { opacity: 0.3; }
    }
  }
  .key-hover { background: $bg-subtle; }
}

.num-font { font-family: $font-family-money; @include tabular-nums; }
.animate-fade-in { animation: fadeIn 0.4s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* 账户选择样式 */
.disabled {
  opacity: 0.6;
  pointer-events: none;
  filter: grayscale(1);
}
</style>