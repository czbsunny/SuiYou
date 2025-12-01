<template>
  <view v-if="isOpen" class="modal-overlay">
    <!-- 背景遮罩 -->
    <view class="modal-backdrop" @click="$emit('close')"></view>
    
    <!-- 模态框内容 -->
    <view class="modal-content">
      
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">
          {{ initialData ? '编辑账户' : '创建新账户' }}
        </text>
        <view @click="$emit('close')" class="close-btn">
          <img src="/static/images/plus.png" class="close-icon" />
        </view>
      </view>

      <!-- 滚动区域 -->
      <scroll-view scroll-y class="modal-body" :show-scrollbar="false">
          <view class="form-container">
            <!-- 账户名称输入 -->
            <view class="form-group">
                <text class="label">账户名称</text>
                <view class="input-wrapper">
                    <input
                      type="text"
                      v-model="name"
                      placeholder="请输入账户名称"
                      maxlength="20"
                      class="custom-input"
                      placeholder-class="input-placeholder"
                    />
                    <text class="char-count">{{ name.length }}/20</text>
                </view>
            </view>

            <!-- 图标选择 -->
            <view class="form-group">
                <text class="label">选择图标</text>
                <view class="icon-grid">
                    <view
                        v-for="ic in ICON_OPTIONS"
                        :key="ic"
                        @click="icon = ic"
                        :class="['icon-item', icon === ic ? 'is-active' : '']"
                    >
                        <text class="icon-text">{{ ic }}</text>
                    </view>
                </view>
            </view>
          </view>
      </scroll-view>

      <!-- 底部按钮区 -->
      <view class="modal-footer">
          <!-- 取消按钮 -->
          <view
              @click="$emit('close')"
              class="btn btn-cancel"
          >
              <text>取消</text>
          </view>
          
          <!-- 编辑模式：显示删除和保存 -->
          <template v-if="initialData">
               <view class="btn-group-right">
                   <view 
                      @click="handleDelete"
                      class="btn btn-delete"
                   >
                       <text>删除</text>
                   </view>
                   <view
                      @click="handleSubmit"
                      :class="['btn btn-primary', (loading || !name) ? 'is-disabled' : '']"
                  >
                      <text>保存</text>
                  </view>
               </view>
          </template>
          
          <!-- 创建模式：仅显示确定 -->
          <template v-else>
              <view
                  @click="handleSubmit"
                  :class="['btn btn-primary', (loading || !name) ? 'is-disabled' : '']"
              >
                  <text>{{ loading ? '保存中...' : '确定' }}</text>
              </view>
          </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps(['isOpen', 'initialData']);
const emit = defineEmits(['close', 'save', 'delete']);

const name = ref('');
const icon = ref('💰');
const loading = ref(false);
const ICON_OPTIONS = ['💰', '🏦', '📈', '📊', '🏆', '💼', '🎯', '📚', '💎', '🐷'];

watch(() => props.initialData, (newVal) => {
    if (newVal) {
        name.value = newVal.name;
        icon.value = newVal.icon || '💰';
    } else {
        name.value = '';
        icon.value = '💰';
    }
}, { immediate: true });

const handleSubmit = async () => {
    if (!name.value) return;
    loading.value = true;
    try {
        await emit('save', {
            name: name.value,
            description: '',
            icon: icon.value,
            type: 'DEFAULT',
            totalValue: props.initialData ? props.initialData.totalValue : 0
        });
        emit('close');
    } finally {
        loading.value = false;
    }
};

const handleDelete = async () => {
    if (props.initialData) {
        uni.showModal({
            title: '确认删除',
            content: '确认删除该账户？删除后数据无法恢复。',
            success: async (res) => {
                if (res.confirm) {
                    loading.value = true;
                    await emit('delete', props.initialData.id);
                    emit('close');
                }
            }
        });
    }
};
</script>

<style lang="scss" scoped>

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 60;
  display: flex;
  flex-direction: column;
  justify-content: flex-end; /* 底部弹出 */
  
  /* 简单的居中适配大屏幕 */
  @media (min-width: 640px) {
    align-items: center;
    justify-content: center;
  }
}

.modal-backdrop {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  transition: opacity 0.3s;
}

.modal-content {
  position: relative;
  background-color: $bg-white;
  width: 100%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  z-index: 10;

  @media (min-width: 640px) {
    max-width: 448px; /* max-w-md */
    border-radius: 16px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  }
}

.modal-header {
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  color: $text-main;
  text-align: center;
  width: 100%;
}

.close-btn {
  position: absolute;
  right: 0;
  top: 0;
  padding: 8px;
  z-index: 10;
  /* 扩大点击热区 */
  margin: -8px;
}

.close-icon {
  width: 24px;
  height: 24px;
  transform: rotate(45deg);
  opacity: 0.4;
  
  &:active {
    opacity: 0.8;
  }
}

.modal-body {
  flex: 1;
  width: 100%;
}

.form-container {
  padding-bottom: 16px;
  /* 相当于 space-y-6 */
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.label {
  display: block;
  font-size: 14px;
  color: $text-sub;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  background-color: $bg-input;
  border-radius: 12px;
  overflow: hidden;
}

.custom-input {
  width: 100%;
  height: 48px; /* h-12 */
  padding: 16px;
  font-size: 16px;
  color: $text-main;
}

/* 对应 placeholder-class */
:deep(.input-placeholder) {
  color: $text-muted;
}

.char-count {
  position: absolute;
  right: 16px;
  top: 16px;
  font-size: 12px;
  color: $text-muted;
  pointer-events: none;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.icon-item {
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border: 2px solid $border-color;
  background-color: $bg-page;
  transition: all 0.2s;
  
  &:active {
    background-color: $border-color;
  }

  &.is-active {
    border-color: $primary;
    background-color: rgba($primary, 0.05);
  }
}

.modal-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.btn-group-right {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  width: 100%;
}

.btn {
  padding: 14px 0;
  border-radius: 12px;
  font-weight: bold;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }
}

.btn-cancel {
  background-color: $bg-white;
  color: $text-sub;
  border: 1px solid $border-color;
  
  &:active {
    background-color: #f9fafb;
  }
}

.btn-delete {
  background-color: $bg-error;
  color: $text-error;
  
  &:active {
    background-color: $bg-error-active;
  }
}

.btn-primary {
  background-color: $primary;
  color: $bg-white;
  box-shadow: 0 4px 12px rgba($primary, 0.2);
  
  &.is-disabled {
    opacity: 0.7;
    pointer-events: none;
  }
}
</style>