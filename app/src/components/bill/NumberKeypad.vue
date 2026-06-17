<template>
  <view class="keypad-container">
    <view class="keypad-grid">
      <!-- 第一行 -->
      <view class="keypad-btn" @tap="onKeyPress('1')"><text class="num-text">1</text></view>
      <view class="keypad-btn" @tap="onKeyPress('2')"><text class="num-text">2</text></view>
      <view class="keypad-btn" @tap="onKeyPress('3')"><text class="num-text">3</text></view>
      <view class="keypad-btn double-btn">
        <view class="sub-btn" @tap="onOperator('+')"><text class="op-text">+</text></view>
        <view class="sub-btn" @tap="onOperator('-')"><text class="op-text">-</text></view>
      </view>
      
      <!-- 第二行 -->
      <view class="keypad-btn" @tap="onKeyPress('4')"><text class="num-text">4</text></view>
      <view class="keypad-btn" @tap="onKeyPress('5')"><text class="num-text">5</text></view>
      <view class="keypad-btn" @tap="onKeyPress('6')"><text class="num-text">6</text></view>
      <view class="keypad-btn double-btn">
        <view class="sub-btn" @tap="onOperator('×')"><text class="op-text">×</text></view>
        <view class="sub-btn" @tap="onOperator('÷')"><text class="op-text">÷</text></view>
      </view>
      
      <!-- 第三行 -->
      <view class="keypad-btn" @tap="onKeyPress('7')"><text class="num-text">7</text></view>
      <view class="keypad-btn" @tap="onKeyPress('8')"><text class="num-text">8</text></view>
      <view class="keypad-btn" @tap="onKeyPress('9')"><text class="num-text">9</text></view>
      <view class="keypad-btn secondary-btn" @tap="onSaveAndAgain">
        <text class="again-text">再记</text>
      </view>
      
      <!-- 第四行 -->
      <view class="keypad-btn" @tap="onKeyPress('.')"><text class="num-text">.</text></view>
      <view class="keypad-btn" @tap="onKeyPress('0')"><text class="num-text">0</text></view>
      <view class="keypad-btn delete-btn" @tap="onDelete">
        <!-- 矢量退格图标，完美匹配图片效果 -->
        <svg class="delete-icon-svg" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#2D312E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 4H8l-7 8 7 8h13a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"></path>
          <line x1="18" y1="9" x2="12" y2="15"></line>
          <line x1="12" y1="9" x2="18" y2="15"></line>
        </svg>
      </view>
      <view class="keypad-btn primary-btn" @tap="onConfirm">
        <text class="confirm-text">完成</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'delete', 'confirm', 'saveAndAgain', 'operator'])

const onKeyPress = (key) => {
  if (props.disabled) return
  
  let newValue = props.modelValue
  
  if (key === '.' && (newValue.includes('.') || !newValue)) {
    if (!newValue) {
      newValue = '0.'
    } else {
      return
    }
  } else {
    newValue += key
  }
  
  if (newValue.includes('.')) {
    const parts = newValue.split('.')
    if (parts[1].length > 2) return
  }
  
  if (parseFloat(newValue) >= 100000000) {
    uni.showToast({ title: '单笔金额上限为1亿', icon: 'none' })
    return
  }
  
  emit('update:modelValue', newValue)
}

const onDelete = () => {
  if (props.disabled) return
  if (props.modelValue.length <= 1) {
    emit('update:modelValue', '')
  } else {
    emit('update:modelValue', props.modelValue.slice(0, -1))
  }
  emit('delete')
}

const onOperator = (op) => {
  emit('operator', op)
}

const onSaveAndAgain = () => {
  if (props.disabled) return
  emit('saveAndAgain')
}

const onConfirm = () => {
  if (props.disabled) return
  emit('confirm')
}
</script>

<style lang="scss" scoped>
// 定义局部颜色变量，适配深绿主题
$border-color: #F1F3F0;  // 极细网格线颜色
$text-dark: #1E211F;     // 数字颜色
$theme-green: #247D6D;   // 品牌主深绿色（完成按钮）
$bg-active: #F4F6F4;     // 点击态灰色

.keypad-container {
  background: #FFFFFF;
}

.keypad-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background-color: $border-color; // 利用 gap+背景色，画出干净不重叠的 1px 细线
  gap: 1rpx;
  border-top: 1rpx solid $border-color;
  border-bottom: 1rpx solid $border-color;
}

.keypad-btn {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FFFFFF;
  transition: background-color 0.1s;
  box-sizing: border-box;

  // 数字字体属性
  .num-text {
    font-size: 42rpx;
    font-weight: 500;
    color: $text-dark;
  }

  // 默认普通按钮点击反馈
  &:active {
    background-color: $bg-active;
  }
}

// 并列双操作符按键槽（无中线分隔，纯左右 Flex 排列）
.double-btn {
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .sub-btn {
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.1s;

    .op-text {
      font-size: 34rpx;
      font-weight: 500;
      color: $text-dark;
    }

    &:active {
      background-color: $bg-active;
    }
  }
  
  // 禁用双按键外层 active 状态，采用内层 sub-btn 点击态
  &:active {
    background-color: #FFFFFF;
  }
}

// “再记”按钮（浅色背景，高亮绿色文本）
.secondary-btn {
  .again-text {
    font-size: 30rpx;
    font-weight: 700;
    color: $theme-green;
  }
}

// “完成”按钮（主色深绿色背景，白字）
.primary-btn {
  background-color: $theme-green;

  .confirm-text {
    font-size: 30rpx;
    font-weight: 700;
    color: #FFFFFF;
  }

  &:active {
    background-color: #1A5F52; // 点击完成按钮时加深绿色
  }
}

// 删除退格按钮样式
.delete-btn {
  .delete-icon-svg {
    display: block;
    opacity: 0.9;
  }
}
</style>