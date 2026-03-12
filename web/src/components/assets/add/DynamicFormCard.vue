<template>
  <view class="section-card" v-if="fields.length > 0">
    <view class="card-title">补充信息</view>
    <view class="card-body form-group">
      <template v-for="(field, index) in fields" :key="field.key">
        <!-- 文本/数字 -->
        <view 
          v-if="field.type !== 'textarea'" 
          class="form-row"
          :class="{ 'last-row': index === fields.length - 1 }"
        >
          <text class="row-label">{{ field.label }}</text>
          <input 
            :value="modelValue[field.key]"
            @input="e => updateField(field.key, e.detail.value)"
            :type="field.type" 
            :placeholder="field.placeholder || '选填'"
            class="row-input" 
            placeholder-class="input-placeholder"
          />
        </view>
        
        <!-- 备注 -->
        <view v-else class="form-row column-layout last-row">
          <text class="row-label mb-8">{{ field.label }}</text>
          <textarea 
            :value="modelValue[field.key]"
            @input="e => updateField(field.key, e.detail.value)"
            :placeholder="field.placeholder || '请输入备注信息...'" 
            class="row-textarea"
            auto-height
            placeholder-class="input-placeholder"
          />
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
const props = defineProps(['fields', 'modelValue']);
const emit = defineEmits(['update:modelValue']);

const updateField = (key, value) => {
  const newData = { ...props.modelValue, [key]: value };
  emit('update:modelValue', newData);
};
</script>

<style lang="scss" scoped>
$primary-light: rgba(42, 128, 108, 0.08);

$border-light: rgba(0, 0, 0, 0.04);
$tag-inactive: #F5F7FA;  // 未选中标签背景

.section-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 20px 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03); // 轻柔阴影
  
  .card-title {
    font-size: 14px;
    font-weight: 700;
    color: $text-main;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 3px solid $primary; // 金色装饰条
    line-height: 1;
  }
}

.form-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid $border-light;

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
  font-size: 15px;
  color: $text-main;
  width: 90px;
  flex-shrink: 0;
  font-weight: 500;
}

.currency-label {
  font-size: 12px;
  color: $primary;
  margin-left: 4px;
}

.row-input {
  flex: 1;
  font-size: 15px;
  color: $text-main;
  text-align: right;
  height: 24px;
  line-height: 24px;
  
  &.amount-input {
    font-size: 20px; 
    font-weight: 600;
    color: $primary;
  }
}

.input-placeholder {
  color: $text-placeholder;
  font-size: 14px;
  font-weight: 400;
}

.row-textarea {
  width: 100%;
  font-size: 14px;
  color: $text-main;
  min-height: 80px;
  background-color: $tag-inactive; // 浅灰底色区域
  border-radius: 8px;
  padding: 12px;
  box-sizing: border-box;
}

</style>