<!-- components/assets/AssetBasicForm.vue -->
<template>
  <view class="section-card">
    <view class="card-title">基本信息</view>
    <view class="card-body form-group">
      
      <!-- 资产名称 -->
      <view class="form-row">
        <text class="row-label">资产名称</text>
        <input 
          :value="name" 
          @input="e => $emit('update:name', e.detail.value)"
          type="text" 
          :placeholder="placeholder"
          class="row-input"
          placeholder-class="input-placeholder"
        />
      </view>
      
      <!-- 金额录入 -->
      <view class="form-row">
        <view class="row-label">
          金额 <text class="currency-label">{{ currency }}</text>
        </view>
        <input 
          :value="amount" 
          @input="e => $emit('update:amount', e.detail.value)"
          type="digit" 
          placeholder="0.00"
          class="row-input amount-input"
          placeholder-class="input-placeholder"
        />
      </view>

      <!-- 计入资产统计开关 -->
      <view class="form-row last-row">
        <view class="item-label-group">
          <text class="row-label">计入总资产</text>
          <text class="item-desc">开启后金额将纳入资产统计</text>
        </view>
        
        <view class="switch-wrapper">
          <switch 
            :checked="includeInNetWorth" 
            color="#2A806C" 
            @change="e => $emit('update:includeInNetWorth', e.detail.value)"
          />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  name: String,
  amount: [String, Number],
  placeholder: { type: String, default: '请输入名称' },
  currency: { type: String, default: 'CNY' },
  // 🟢 新增 Prop：是否计入净值，默认开启
  includeInNetWorth: { type: Boolean, default: true }
});

defineEmits(['update:name', 'update:amount', 'update:includeInNetWorth']);
</script>

<style lang="scss" scoped>
.section-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 20px 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  
  .card-title {
    font-size: 14px;
    font-weight: 700;
    color: #1F2937;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 3px solid #2A806C;
    line-height: 1;
  }
}

.form-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  justify-content: space-between; // 确保左右对齐
  
  &.last-row {
    border-bottom: none;
    padding-bottom: 0;
  }
}

// 左侧标签组样式
.item-label-group {
  display: flex;
  flex-direction: column;
  .item-desc {
    font-size: 22rpx;
    color: #9CA3AF;
    margin-top: 4rpx;
    font-weight: 400;
  }
}

.switch-wrapper {
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  /* 如果在小程序中觉得 switch 还是太小，可以设置这里的宽度来辅助对齐 */
  margin-right: -10rpx; 
}
.row-label {
  font-size: 15px;
  color: #1F2937;
  width: 200rpx; // 稍微加宽一点适配文字
  flex-shrink: 0;
  font-weight: 500;
}

.currency-label {
  font-size: 12px;
  color: #2A806C;
  margin-left: 4px;
}

.row-input {
  flex: 1;
  font-size: 15px;
  color: #1F2937;
  text-align: right;
  height: 48rpx;
  
  &.amount-input {
    font-size: 40rpx; 
    font-weight: 600;
    color: #2A806C;
    font-family: 'DIN Alternate', sans-serif;
  }
}

.input-placeholder {
  color: #9CA3AF;
  font-size: 28rpx;
  font-weight: 400;
}
</style>