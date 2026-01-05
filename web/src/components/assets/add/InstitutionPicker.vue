<!-- components/assets/InstitutionPicker.vue -->
<template>
  <view class="section-card">
    <view class="card-title">机构账户</view>
    <view class="card-body form-group">
      
      <!-- 机构选择行 -->
      <view class="form-row" @click="$emit('click')">
        <text class="row-label">所属机构</text>
        <view class="picker-trigger">
          <view class="selected-inst-box">
            <view v-if="selected" class="mini-logo-wrapper">
              <image 
                :src="selected.logoUrl || '/static/icons/default-bank.png'" 
                class="mini-logo" 
                mode="aspectFit" 
              />
            </view>
            <text class="inst-display-text" :class="{ 'placeholder': !selected }">
              {{ selected ? selected.instName : '请选择机构' }}
            </text>
          </view>
        </view>
      </view>

      <!-- 标识码输入行 (例如卡号末4位) -->
      <view class="form-row last-row">
        <view class="row-label">
          标识码 <text class="desc-tag">末4位</text>
        </view>
        <input 
          :value="identifier"
          @input="e => $emit('update:identifier', e.detail.value)"
          type="number" 
          maxlength="4"
          placeholder="如卡号/账号末4位(必填)"
          class="row-input identifier-input"
          placeholder-class="input-placeholder"
        />
      </view>

    </view>
  </view>
</template>

<script setup>
defineProps({
  selected: Object,      // 选中的机构对象
  identifier: String     // 4位标识码
});

defineEmits(['click', 'update:identifier']);
</script>

<style lang="scss" scoped>
/* 统一样式变量 */
$border-color: rgba(0, 0, 0, 0.04);

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
  border-bottom: 1px solid $border-color;
  
  &.last-row {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.row-label {
  font-size: 15px;
  color: #1F2937;
  width: 200rpx;
  flex-shrink: 0;
  font-weight: 500;
  display: flex;
  align-items: center;
  
  .desc-tag {
    font-size: 10px;
    background-color: #F3F4F6;
    color: #9CA3AF;
    padding: 2rpx 8rpx;
    border-radius: 4rpx;
    margin-left: 8rpx;
    font-weight: normal;
  }
}

.picker-trigger {
  flex: 1;
}

.selected-inst-box {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  
  .mini-logo-wrapper {
    width: 44rpx;
    height: 44rpx;
    background-color: #f8f9fa;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12rpx;
    overflow: hidden;
    border: 1rpx solid rgba(0,0,0,0.03);
  }

  .mini-logo {
    width: 40rpx;
    height: 40rpx;
  }

  .inst-display-text {
    font-size: 15px;
    color: #1F2937;
    margin-right: 8rpx;
    
    &.placeholder {
      color: #9CA3AF;
      font-size: 14px;
    }
  }
}

.row-input {
  flex: 1;
  font-size: 15px;
  color: #1F2937;
  text-align: right;
  height: 48rpx;
  
  &.identifier-input {
    letter-spacing: 2rpx;
    font-family: 'Monaco', 'Courier New', monospace; // 数字等宽显示，更有质感
  }
}

.input-placeholder {
  color: #D1D5DB;
  font-size: 14px;
}
</style>