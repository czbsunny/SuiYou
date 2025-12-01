<template>
  <view class="modify-page">
    <!-- 顶部状态卡片：展示配置进度 -->
    <view class="status-section">
      <view :class="['status-card', isOverLimit ? 'bg-error' : 'bg-primary']">
        <view class="status-header">
          <text class="status-title">配置进度</text>
          <text class="status-percent">{{ configuredPercentage.toFixed(2) }}%</text>
        </view>
        
        <view class="progress-track">
          <view 
            class="progress-fill" 
            :style="{ width: `${Math.min(configuredPercentage, 100)}%`, backgroundColor: isOverLimit ? '#feb2b2' : '#ffffff' }"
          ></view>
        </view>
        
        <view class="status-footer">
          <text v-if="isOverLimit" class="status-tip">⚠️ 已超出 {{ (configuredPercentage - 100).toFixed(2) }}%，请调整</text>
          <text v-else class="status-tip">剩余可配置 {{ (100 - configuredPercentage).toFixed(2) }}%</text>
        </view>
      </view>
    </view>

    <!-- 操作栏 & 列表 -->
    <scroll-view scroll-y class="scroll-content">
      
      <!-- 快捷操作 -->
      <view class="action-bar">
        <text class="section-title">基金配置</text>
        <view class="quick-btns">
          <view class="capsule-btn" @click="applyEqualAllocation">
            <text>一键平均</text>
          </view>
          <view class="zero-btn" @click="restoreOriginalAllocation">
            <text>清零</text>
          </view>
        </view>
      </view>

      <!-- 基金列表 -->
      <view class="fund-list">
        <view v-if="fundList.length === 0" class="empty-state">
          <image src="/static/images/chart-pie-gray.png" class="empty-icon" />
          <text class="empty-text">暂无配置数据，请添加基金</text>
        </view>
        
        <view 
          v-for="(item, index) in fundList" 
          :key="item.fundCode || index" 
          class="fund-card"
        >
          <view class="card-left">
            <text class="fund-name">{{ item.fundName }}</text>
            <text class="fund-code">{{ item.fundCode }}</text>
          </view>
          
          <view class="card-right">
            <!-- 百分比控制器 -->
            <view class="stepper-box">
              <view class="stepper-btn minus" @click="decreasePercentage(index)">-</view>
              <view class="input-wrapper">
                <input 
                  type="digit" 
                  class="percent-input" 
                  :value="item.percentage" 
                  @input="handleInputPercentage(index, $event)" 
                  @blur="finalizePercentage(index)"
                  maxlength="6" 
                />
                <text class="unit">%</text>
              </view>
              <view class="stepper-btn plus" @click="increasePercentage(index)">+</view>
            </view>
            
            <!-- 删除按钮 -->
            <view class="delete-btn" @click="removeFund(index)">
              <image src="/static/images/trash.png" class="trash-icon" />
            </view>
          </view>
        </view>

        <!-- 添加按钮 (跳转搜索页) -->
        <view class="add-card" @click="showFundSearch">
          <image src="/static/images/plus-blue.png" class="add-icon" />
          <text>添加更多基金</text>
        </view>
      </view>
      
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 底部固定栏 -->
    <view class="footer-bar">
      <button 
        class="confirm-btn" 
        :disabled="!isValid" 
        @click="confirmAllocation"
        :class="{ 'disabled': !isValid }"
      >
        {{ getBtnText }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onUnload } from '@dcloudio/uni-app';
import { get, post } from '../../services/apiService.js';

const portfolioId = ref('');
const fundList = ref([]);
const configuredPercentage = ref(0);

// 计算属性：是否超限
const isOverLimit = computed(() => configuredPercentage.value > 100.01);

// 计算属性：是否有效（总和接近100）
const isValid = computed(() => Math.abs(configuredPercentage.value - 100) <= 0.01);

// 按钮文字
const getBtnText = computed(() => {
  if (configuredPercentage.value > 100) return `已超出 ${ (configuredPercentage.value - 100).toFixed(2) }%`;
  if (configuredPercentage.value < 100) return `还差 ${ (100 - configuredPercentage.value).toFixed(2) }%`;
  return '确认修改方案';
});

onLoad((options) => {
  if (options.portfolio_id) {
    portfolioId.value = options.portfolio_id;
    loadPortfolioConfig();
  }
  
  // 监听来自搜索页面的事件
  uni.$on('fund-selected', (selectedFunds) => {
    handleFundSelect(selectedFunds);
  });
});

// 页面销毁时移除监听
onUnload(() => {
  uni.$off('fund-selected');
});

// 加载配置
const loadPortfolioConfig = async () => {
  try {
    const res = await get(`/api/allocations/${portfolioId.value}`);
    if (res.statusCode === 200) {
      fundList.value = res.data.map(item => ({
        id: item.id,
        fundCode: item.fundCode,
        fundName: item.fundName,
        percentage: Number((item.targetRatio * 100).toFixed(2)),
        type: 'other'
      }));
      calculateConfiguredPercentage();
    }
  } catch (error) {
    console.error('加载配置失败', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

// 计算总比例
const calculateConfiguredPercentage = () => {
  const total = fundList.value.reduce((sum, item) => sum + Number(item.percentage), 0);
  configuredPercentage.value = Math.round(total * 100) / 100;
};

// 输入处理
const handleInputPercentage = (index, event) => {
  let value = event.detail.value;
  fundList.value[index].percentage = value; 
};

const finalizePercentage = (index) => {
  let val = parseFloat(fundList.value[index].percentage);
  if (isNaN(val) || val < 0) val = 0;
  val = Math.round(val * 100) / 100;
  fundList.value[index].percentage = val;
  calculateConfiguredPercentage();
};

// 微调按钮
const increasePercentage = (index) => {
  const item = fundList.value[index];
  const remaining = 100 - configuredPercentage.value;
  if (configuredPercentage.value >= 100) {
      uni.showToast({ title: '已达100%', icon: 'none' });
      return;
  }
  item.percentage = Math.min(item.percentage + 1, item.percentage + remaining);
  item.percentage = Math.round(item.percentage * 100) / 100;
  calculateConfiguredPercentage();
};

const decreasePercentage = (index) => {
  const item = fundList.value[index];
  if (item.percentage <= 0) return;
  item.percentage = Math.max(0, item.percentage - 1);
  item.percentage = Math.round(item.percentage * 100) / 100;
  calculateConfiguredPercentage();
};

const removeFund = (index) => {
  fundList.value.splice(index, 1);
  calculateConfiguredPercentage();
};

// 批量操作
const applyEqualAllocation = () => {
  if (fundList.value.length === 0) return;
  const count = fundList.value.length;
  const avg = Math.floor((100 / count) * 100) / 100;
  
  let currentSum = 0;
  fundList.value.forEach((item, i) => {
    if (i === count - 1) {
        item.percentage = Number((100 - currentSum).toFixed(2));
    } else {
        item.percentage = avg;
        currentSum += avg;
    }
  });
  calculateConfiguredPercentage();
};

const restoreOriginalAllocation = () => {
  // 将所有基金比例清零
  fundList.value.forEach(item => item.percentage = 0);
  calculateConfiguredPercentage();
};

// 跳转到基金搜索页面
const showFundSearch = () => {
  // 将当前已选的基金列表传递给搜索页，以便回显选中状态
  const selectedFundsStr = encodeURIComponent(JSON.stringify(fundList.value.map(f => f.fundCode)));
  uni.navigateTo({
    url: `/components/portfolio/fundSearch?selected=${selectedFundsStr}`
  });
};

// 处理搜索页面返回的选择结果
const handleFundSelect = (selectedFunds) => {
  const existingCodes = new Set(fundList.value.map(f => f.fundCode));
  let hasNew = false;
  
  selectedFunds.forEach(fund => {
    if (!existingCodes.has(fund.fundCode)) {
      fundList.value.push({
        fundCode: fund.fundCode,
        fundName: fund.fundName,
        percentage: 0, // 新添加的基金默认为 0%
        type: fund.type || 'other'
      });
      hasNew = true;
    }
  });
  
  if (hasNew) {
    calculateConfiguredPercentage();
  }
};

// 提交
const confirmAllocation = async () => {
  if (!isValid.value) return;
  
  try {
    uni.showLoading({ title: '保存中' });
    const payload = fundList.value.map(item => ({
        id: item.id || null,
        fundCode: item.fundCode,
        fundName: item.fundName,
        targetRatio: Number((item.percentage / 100).toFixed(4))
    }));
    
    // 使用 portfolioId Int 类型
    const pid = parseInt(portfolioId.value);
    const res = await post(`/api/allocations/${pid}`, payload);
    
    if (res.statusCode >= 200 && res.statusCode < 300) {
        uni.showToast({ title: '保存成功' });
        setTimeout(() => {
            uni.navigateBack();
        }, 1000);
    } else {
        throw new Error('保存失败');
    }
  } catch (e) {
    console.error(e);
    uni.showToast({ title: '保存失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};
</script>

<style lang="scss" scoped>

$text-light: #d1d5db; /* gray-300 */

.modify-page {
  min-height: 100vh;
  background-color: $bg-cream;
  display: flex;
  flex-direction: column;
  width: 100%;
}

/* 1. 状态卡片 */
.status-section {
  padding: 16px;
  background-color: $bg-cream;
  z-index: 10;
}

.status-card {
  border-radius: 20px;
  padding: 20px;
  color: #fff;
  box-shadow: 0 8px 20px rgba(42, 128, 108, 0.2);
  transition: background-color 0.3s;
}

.bg-primary { background-color: $primary; }
.bg-error { background-color: $red; }

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-title { font-size: 14px; opacity: 0.9; }
.status-percent { font-size: 24px; font-weight: bold; font-family: monospace; }

.progress-track {
  height: 8px;
  background-color: rgba(255,255,255,0.2);
  border-radius: 99px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 0.3s ease, background-color 0.3s;
}

.status-footer {
  font-size: 12px;
  opacity: 0.8;
  text-align: right;
}

/* 2. 滚动内容 */
.scroll-content {
  flex: 1;
  height: 0;
  padding: 0 16px;
  box-sizing: border-box;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: $text-main;
}

.quick-btns {
  display: flex;
  gap: 8px;
}

.capsule-btn {
  padding: 6px 12px;
  background-color: rgba(42, 128, 108, 0.1);
  border-radius: 99px;
  font-size: 12px;
  color: $primary;
  font-weight: 500;
  
  &:active { opacity: 0.7; }
}

.zero-btn {
  padding: 6px 12px;
  background-color: $bg-warning;
  border-radius: 99px;
  font-size: 12px;
  color: $text-warning;
  font-weight: 500;
  
  &:active { opacity: 0.7; }
}

/* 基金列表 */
.fund-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fund-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  border: 1px solid $border-color;
}

.card-left {
  flex: 1; /* 占据剩余空间 */
  min-width: 0; /* 关键：防止过长的文字撑破 Flex 容器 */
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.fund-name {
  font-size: 14px;
  font-weight: bold;
  color: $text-main;
  display: block;
  margin-bottom: 4px;
  /* 增加文字截断 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fund-code {
  font-size: 12px;
  color: $text-muted;
  background-color: $bg-subtle-hover;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block; /* 包裹内容 */
  align-self: flex-start; /* 左对齐 */
}

.card-right {
  flex-shrink: 0; /* 防止被压缩 */
  display: flex;
  align-items: center;
}

/* 步进器 */
.stepper-box {
  display: flex;
  align-items: center;
  background-color: $bg-subtle-hover;
  border-radius: 8px;
  padding: 2px;
  margin-right: 4px;
}

.stepper-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: $primary;
  font-weight: bold;
  
  &:active { background-color: rgba(0,0,0,0.05); border-radius: 6px; }
}

.input-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  border-left: 1px solid #eee;
  border-right: 1px solid #eee;
  height: 20px;
}

.percent-input {
  width: 40px;
  text-align: right;
  font-size: 14px;
  font-weight: bold;
  color: $text-main;
  height: 20px;
  min-height: 20px;
  background-color: $bg-input;
}

.unit {
  font-size: 10px;
  color: $text-muted;
  margin-left: 2px;
}

/* 删除按钮 */
.delete-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trash-icon { width: 16px; height: 16px; opacity: 0.4; }

/* 添加卡片 */
.add-card {
  background-color: $bg-white;
  border: 1px dashed $border-color;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  font-size: 14px;
  font-weight: 500;
  
  &:active { background-color: $bg-subtle; }
}

.add-icon { width: 16px; height: 16px; margin-right: 6px; }

/* 空状态 */
.empty-state {
  padding: 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-icon { width: 32px; height: 32px; opacity: 0.3; margin-bottom: 8px; }
.empty-text { font-size: 12px; color: $text-muted; }

.bottom-spacer { height: 100px; }

/* 底部栏 */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-white;
  padding: 16px 24px;
  box-shadow: 0 -4px 10px rgba(0,0,0,0.05);
  z-index: 20;
}

.confirm-btn {
  width: 100%;
  background-color: $primary;
  color: #fff;
  border-radius: 12px;
  font-weight: bold;
  font-size: 16px;
  border: none;
  height: 48px;
  line-height: 48px;
  
  &:active { opacity: 0.9; }
  
  &.disabled {
    background-color: $text-placeholder;
    color: $bg-white;
    opacity: 0.8;
  }
}
</style>