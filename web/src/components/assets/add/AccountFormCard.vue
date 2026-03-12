<template>
  <view class="section-card">
    <view class="card-title">账户信息</view>
    
    <!-- 1. 标识码输入和选择 -->
    <view class="form-row">
      <view class="row-label">账户标识</view>
      
      <!-- 输入框始终显示 -->
      <input 
        type="number" 
        maxlength="4"
        placeholder="标识码 (必填)" 
        class="row-input" 
        placeholder-class="input-placeholder"
        :value="identifier"
        @input="$emit('update:identifier', $event.detail.value)"
        :disabled="!isCreatingNew"
        :class="{ 'disabled-text': !isCreatingNew }"
      />
      
      <!-- 如果有可用的现有账户，提供 Picker 选择 -->
      <picker 
        v-if="availableAccounts.length > 0"
        class="row-picker" 
        :range="pickerRange" 
        @change="onAccountChange"
      >
        <view class="picker-btn">
          <image src="/static/images/chevron-right-blue.png" class="picker-arrow" />
        </view>
      </picker>
    </view>

    <!-- 2. 账户名称 (仅在新建时允许修改，复用时只读展示) -->
    <view class="form-row last-row">
      <view class="row-label">账户名称</view>
      <input 
        type="text" 
        class="row-input" 
        :value="accountName"
        @input="$emit('update:accountName', $event.detail.value)"
        placeholder="例如: 工资卡" 
        placeholder-class="input-placeholder"
        :disabled="!isCreatingNew"
        :class="{ 'disabled-text': !isCreatingNew }"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const props = defineProps({
  identifier: String,
  accountName: String,
  availableAccounts: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:identifier', 'update:accountName']);

const isCreatingNew = ref(true);

// 挂载时处理传入的可选账户
onMounted(() => {
  // 如果没有传入可用账户，默认为新建模式
  if (props.availableAccounts.length === 0) {
    isCreatingNew.value = true;
  }
});

// 构建 Picker 选项（简单数组格式）
const pickerRange = computed(() => {
  const options = props.availableAccounts.map(acc => acc.institutionIdentifier);
  // 永远在最后加一个"新建"选项
  options.push('+ 创建新账户标识');
  return options;
});

// 构建 Picker 选项（对象格式，用于获取完整信息）
const pickerOptions = computed(() => {
  const options = props.availableAccounts.map(acc => ({
    label: acc.institutionIdentifier,
    value: acc.institutionIdentifier,
    name: acc.accountName
  }));
  // 永远在最后加一个"新建"选项
  options.push({ label: '+ 创建新账户标识', value: 'CREATE_NEW', name: '' });
  return options;
});

// Picker 改变事件
const onAccountChange = (e) => {
  const index = e.detail.value;
  const option = pickerOptions.value[index];

  if (option.value === 'CREATE_NEW') {
    isCreatingNew.value = true;
    emit('update:identifier', '');
    emit('update:accountName', '');
  } else {
    isCreatingNew.value = false;
    emit('update:identifier', option.value);
    emit('update:accountName', option.name);
  }
};
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

.row-label {
  font-size: 30rpx;
  color: $text-main;
  width: 90px;
  flex-shrink: 0;
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
  margin-right: 16rpx;
  
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

.row-picker {
  flex-shrink: 0;
  .picker-btn {
    padding: 6rpx 6rpx;
    background-color: rgba($primary, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .picker-arrow {
    width: 32rpx;
    height: 32rpx;
    transform: rotate(90deg);
  }
}
.disabled-text {
  color: $text-muted;
}
</style>