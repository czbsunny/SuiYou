<template>
  <view class="section-card">
    <view class="card-title">资产信息</view>
    
    <view class="form-row">
      <view class="label-with-currency">
        <view class="row-label">资产金额</view>
        <text class="currency-label">{{ currency }}</text>
      </view>
      <input 
        type="digit" 
        class="row-input amount-input" 
        :value="amount"
        @input="$emit('update:amount', $event.detail.value)"
        placeholder="0.00" 
        placeholder-class="input-placeholder"
      />
    </view>

    <view class="form-row">
      <view class="row-label">资产名称</view>
      <input 
        type="text" 
        class="row-input" 
        :value="assetName"
        @input="$emit('update:assetName', $event.detail.value)"
        :placeholder="defaultAssetName" 
        placeholder-class="input-placeholder"
      />
    </view>

    <view class="form-row last-row">
      <view class="row-label">计入总资产</view>
      <switch 
        :checked="includeInNetWorth" 
        @change="$emit('update:includeInNetWorth', $event.detail.value)" 
        color="#469e88" 
        style="text-align: right; width: 100%;"
      />
    </view>
  </view>
</template>

<script setup>
defineProps({
  amount: [String, Number],
  assetName: String,
  currency: String,
  includeInNetWorth: Boolean,
  defaultAssetName: String
});
defineEmits(['update:amount', 'update:assetName', 'update:includeInNetWorth']);
</script>

<style lang="scss" scoped>
/* --- 卡片通用样式 --- */
.section-card {
  background-color: $bg-white;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  
  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $text-main;
    margin-bottom: 24rpx;
    padding-left: 10rpx;
    border-left: 3px solid $primary;
    line-height: 1;
  }
}

/* --- 表单行通用样式 --- */
.form-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1rpx solid $border-light;

  &.last-row {
    border-bottom: none;
    padding-bottom: 0;
  }
  
  &.column-layout {
    flex-direction: column;
    align-items: flex-start;
  }
}

.label-with-currency {
  display: flex;
  align-items: center;
  width: 100px;
  flex-shrink: 0;
}

.row-label {
  font-size: 30rpx;
  color: $text-main;
  font-weight: 500;
}

/* --- 输入框通用样式 --- */
.row-input {
  flex: 1;
  font-size: 30rpx;
  color: $text-main;
  text-align: right;
  height: 24px;
  line-height: 24px;
  
  &.amount-input {
    font-size: 40rpx; 
    font-weight: 600;
    color: $text-main;
  }
}

.input-placeholder {
  color: $text-muted;
  font-size: 28rpx;
  font-weight: 400;
}

.currency-label {
  font-size: 28rpx;
  color: $primary;
  margin-left: 4px;
}
</style>