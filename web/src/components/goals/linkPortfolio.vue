<template>
  <view class="link-page">
      <!-- 头部卡片：展示目标概览与进度 -->
      <view class="header-card">
          <text class="goal-title">
              {{ goal.name }}
          </text>
          
          <view class="goal-stats">
              <text class="stat-tag">目标 ¥{{ goal.targetAmount.toLocaleString() }}</text>
              <text class="stat-text">当前 ¥{{ currentTotal.toLocaleString() }}</text>
          </view>

          <!-- 环形图展示达成率 -->
          <view class="chart-container">
              <view class="chart-circle">
                  <view class="chart-content">
                      <text class="chart-value">
                          {{ percentage.toFixed(0) }}<text class="unit">%</text>
                      </text>
                      <text class="chart-label">达成率</text>
                  </view>
              </view>
          </view>

          <!-- 提示信息 -->
          <view class="info-tip">
              <image src="/static/images/info-blue.png" class="tip-icon" />
              <text>勾选下方组合，其资产将计入该目标进度</text>
          </view>
      </view>

      <!-- 滚动列表区域：展示可选组合 -->
      <scroll-view scroll-y class="scroll-content">
          <view class="section-header">
               <text class="section-title">可选组合 ({{ allPortfolios.length }})</text>
          </view>
          
          <view class="portfolio-list">
              <view v-if="allPortfolios.length === 0" class="empty-state">
                  <text>暂无可用投资组合</text>
              </view>
              
              <view 
                  v-for="p in allPortfolios"
                  :key="p.id" 
                  @click="toggleSelection(p.id)"
                  :class="['portfolio-item', isSelected(p.id) ? 'is-selected' : '']"
              >
                  <view class="item-left">
                      <!-- 选中框 -->
                      <view :class="['checkbox', isSelected(p.id) ? 'checked' : '']">
                          <image v-if="isSelected(p.id)" src="/static/images/check.png" class="check-icon" />
                      </view>
                       
                      <!-- 组合信息 -->
                      <view>
                          <text class="item-name">{{ p.name }}</text>
                          <text class="item-value">¥{{ p.totalValue.toLocaleString() }}</text>
                      </view>
                  </view>
                  
                  <!-- 已关联标签 -->
                  <text v-if="isSelected(p.id)" class="status-tag">已关联</text>
              </view>

              <!-- 新建按钮 (可选，根据业务需求保留) -->
              <view class="create-btn" @click="handleCreatePortfolio">
                  <image src="/static/images/plus-gray.png" class="btn-icon" /> 
                  <text>新建投资组合</text>
              </view>
          </view>
          
          <!-- 底部占位 -->
          <view class="footer-spacer"></view>
      </scroll-view>

      <!-- 底部保存按钮 -->
      <view class="footer-bar">
          <button 
              @click="handleSave"
              class="save-btn"
              :disabled="loading"
          >
              {{ loading ? '保存中...' : '保存关联设置' }}
          </button>
      </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';

// 接收父组件传递的真实数据
const props = defineProps({
    goal: {
        type: Object,
        required: true,
        default: () => ({
            id: '',
            name: '',
            targetAmount: 0,
            portfolioIds: [] // 目标当前关联的组合ID列表
        })
    },
    allPortfolios: {
        type: Array,
        required: true,
        default: () => []
    }
});

const emit = defineEmits(['save']);

const selectedIds = ref([]);
const loading = ref(false);

// 初始化选中状态
const initSelection = () => {
    if (props.goal && props.goal.portfolioIds) {
        // 复制一份数组，避免直接修改 props
        selectedIds.value = [...props.goal.portfolioIds];
    }
};

// 监听 goal 变化，确保数据同步
watch(() => props.goal, initSelection, { deep: true });

onMounted(() => {
    initSelection();
});

// 判断是否选中
const isSelected = (id) => selectedIds.value.includes(id);

// 切换选择状态
const toggleSelection = (id) => {
    const index = selectedIds.value.indexOf(id);
    if (index > -1) {
        selectedIds.value.splice(index, 1);
    } else {
        selectedIds.value.push(id);
    }
};

// 计算当前选中组合的总价值
const currentTotal = computed(() => {
    if (!props.allPortfolios.length) return 0;
    return props.allPortfolios
        .filter(p => selectedIds.value.includes(p.id))
        .reduce((sum, p) => sum + (p.totalValue || 0), 0);
});

// 计算进度百分比
const percentage = computed(() => {
    if (!props.goal || !props.goal.targetAmount) return 0;
    return Math.min(100, (currentTotal.value / props.goal.targetAmount) * 100);
});

// 保存逻辑
const handleSave = () => {
    loading.value = true;
    // 触发父组件的保存事件，将选中的 ID 列表传出去
    emit('save', selectedIds.value);
    
    // 模拟保存加载效果，实际由父组件控制后续逻辑
    setTimeout(() => {
        loading.value = false;
    }, 500);
};

const handleCreatePortfolio = () => {
    uni.showToast({
        title: '跳转至创建组合页面',
        icon: 'none'
    });
    uni.navigateTo({ 
      url: '/components/portfolio/create' 
    });
};
</script>

<style lang="scss" scoped>
/* 颜色变量 */
$primary-color: #2a806c;
$bg-page: #f5f7fa;
$bg-white: #ffffff;
$bg-subtle-hover: #f3f4f6;
$border-color: #e5e7eb;

$text-main: #1f2937;
$text-sub: #9ca3af;
$text-muted: #6b7280;

.link-page {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
  width: 100%;
  padding-top: 16px;
  /* 底部预留空间给 footer */
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 头部卡片 */
.header-card {
  background-color: $bg-white;
  margin: 0 16px 16px 16px;
  padding: 24px;
  border-radius: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 10;
}

.goal-title {
  font-size: 24px;
  font-weight: bold;
  color: $text-main;
  margin-bottom: 4px;
  text-align: center;
}

.goal-stats {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: $text-muted;
  margin-top: 4px;
  margin-bottom: 24px;
}

.stat-tag {
  background-color: $bg-subtle-hover;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 8px;
}

/* 环形图区域 */
.chart-container {
  position: relative;
  width: 144px;
  height: 144px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.chart-circle {
  width: 128px;
  height: 128px;
  border-radius: 50%;
  border: 8px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.chart-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chart-value {
  font-size: 36px;
  font-weight: bold;
  color: $primary-color;
  font-family: monospace;
}

.unit {
  font-size: 14px;
}

.chart-label {
  font-size: 10px;
  color: $text-sub;
  margin-top: 4px;
}

/* 提示框 */
.info-tip {
  margin-top: 24px;
  background-color: rgba(42, 128, 108, 0.05);
  border: 1px solid rgba(42, 128, 108, 0.1);
  padding: 12px;
  border-radius: 12px;
  font-size: 12px;
  color: $primary-color;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
  box-sizing: border-box;
}

.tip-icon {
  width: 12px;
  height: 12px;
  margin-right: 4px;
}

/* 列表区域 */
.scroll-content {
  flex: 1;
  padding: 0 16px;
  background-color: $bg-page;
  box-sizing: border-box;
}

.section-header {
  margin-bottom: 12px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: bold;
  font-size: 14px;
  color: $text-muted;
}

.portfolio-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
    padding: 40px;
    text-align: center;
    font-size: 14px;
    color: $text-sub;
}

/* 列表项 */
.portfolio-item {
  padding: 16px;
  border-radius: 12px;
  border: 1px solid transparent;
  background-color: $bg-white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
  margin-bottom: 8px;

  /* 选中状态样式 */
  &.is-selected {
    border-color: $primary-color;
    box-shadow: 0 4px 6px -1px rgba(42, 128, 108, 0.1);
    background-color: $bg-white;
  }
}

.item-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

/* 勾选框 */
.checkbox {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  background-color: $bg-white;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &.checked {
    border-color: $primary-color;
    background-color: $primary-color;
  }
}

.check-icon {
  width: 14px;
  height: 14px;
}

.item-name {
  font-size: 14px;
  font-weight: bold;
  display: block;
  color: $text-main;
}

/* 选中时文字变色 */
.portfolio-item.is-selected .item-name {
  color: $primary-color;
}

.item-value {
  font-size: 12px;
  color: $text-sub;
  margin-top: 2px;
  font-family: monospace;
  display: block;
}

.status-tag {
  font-size: 10px;
  color: $primary-color;
  background-color: rgba(42, 128, 108, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

/* 新建按钮 */
.create-btn {
  width: 100%;
  padding: 16px 0;
  margin-top: 8px;
  border: 2px dashed $border-color;
  border-radius: 12px;
  color: $text-sub;
  font-size: 12px;
  font-weight: bold;
  background-color: rgba(255, 255, 255, 0.5);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}

.btn-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
}

.footer-spacer {
    height: 40px;
}

/* 底部固定栏 */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-white;
  padding: 16px;
  border-top: 1px solid $border-color;
  /* 适配底部安全区 */
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  z-index: 30;
}

.save-btn {
  width: 100%;
  padding: 14px 0;
  background-color: $primary-color;
  color: #ffffff;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2);
  border: none;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    opacity: 0.9;
  }
  
  &[disabled] {
      opacity: 0.7;
      background-color: $text-sub;
  }
}
</style>