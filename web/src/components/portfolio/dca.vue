<template>
  <view class="dca-page">

    <scroll-view scroll-y class="content-scroll">
      <!-- 2. 头部：组合信息卡片 -->
      <view class="portfolio-header-card">
         <view class="bg-decoration-1"></view>
         <view class="bg-decoration-2"></view>
         
         <view class="ph-content">
             <view class="ph-label">定投目标组合</view>
             <text class="ph-name">{{ portfolioName }}</text>
             <view class="ph-tag">
                 <text>自动按当前持仓配比买入</text>
             </view>
         </view>
      </view>

      <!-- 3. 定投设置表单 -->
      <view class="form-card">
        <!-- 金额设置 -->
        <view class="amount-section">
            <text class="section-label">每期定投金额</text>
            <view class="amount-input-box">
                <text class="currency-symbol">¥</text>
                <input 
                    type="digit" 
                    v-model="dcaAmount" 
                    class="amount-input" 
                    placeholder="0.00" 
                    placeholder-class="input-placeholder"
                />
            </view>
        </view>

        <view class="divider"></view>

        <!-- 周期设置 -->
        <view class="setting-row">
            <text class="row-label">定投周期</text>
            <view class="period-selector">
                <view 
                    v-for="item in periodOptions" 
                    :key="item.value"
                    class="period-chip"
                    :class="{ active: dcaPeriod === item.value }"
                    @click="dcaPeriod = item.value"
                >
                    {{ item.label }}
                </view>
            </view>
        </view>

        <!-- 日期设置 (如果是每天，则隐藏具体的“几号”概念，但仍需设置开始日期) -->
        <view class="setting-row">
            <text class="row-label">开始执行日期</text>
            <picker 
                mode="date" 
                :value="firstDeductionDate" 
                :start="minDate" 
                end="2030-12-31" 
                @change="onDateChange"
            >
                <view class="date-display">
                    <text class="date-text">{{ formattedDateStr }}</text>
                    <image src="/static/images/chevron-right-gray.png" class="arrow-icon" />
                </view>
            </picker>
        </view>
      </view>

      <!-- 4. 规则说明 -->
      <view class="rules-section">
          <text class="rules-title">定投规则说明</text>
          <view class="rule-item">
              <view class="dot"></view>
              <text>资金将按组合当前的最新配置比例自动分配。</text>
          </view>
          <view class="rule-item">
              <view class="dot"></view>
              <text>扣款日如遇非交易日，将顺延至下一个交易日执行。</text>
          </view>
          <view class="rule-item">
              <view class="dot"></view>
              <text>请确保账户余额充足，连续扣款失败可能导致计划暂停。</text>
          </view>
      </view>
      
      <!-- 底部占位 -->
      <view style="height: 100px;"></view>
    </scroll-view>

    <!-- 5. 底部固定按钮 -->
    <view class="footer-bar">
        <button 
            class="submit-btn" 
            :disabled="!canSave" 
            :class="{ disabled: !canSave }"
            @click="saveDCAPlan"
        >
            {{ canSave ? '确认开启定投' : '请输入金额' }}
        </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { get, post } from '../../services/apiService.js';

// --- 状态定义 ---
const statusBarHeight = ref(20);
const portfolioId = ref('');
const portfolioName = ref('加载中...');
const dcaAmount = ref('');
const dcaPeriod = ref('weekly'); // 默认改为每周，比较常见
const firstDeductionDate = ref('');
const minDate = ref(''); // 最小可选日期

const periodOptions = [
    { label: '每天', value: 'daily' },
    { label: '每周', value: 'weekly' },
    { label: '每两周', value: 'biweekly' },
    { label: '每月', value: 'monthly' },
];

// --- 计算属性 ---

// 按钮状态：只要金额有效即可，因为比例由后端/组合决定
const canSave = computed(() => {
    return dcaAmount.value && parseFloat(dcaAmount.value) > 0;
});

// 格式化显示的日期文案
const formattedDateStr = computed(() => {
    if (!firstDeductionDate.value) return '请选择';
    // 简单的年月日展示，可根据需求优化
    return firstDeductionDate.value;
});

// --- 生命周期 ---
onLoad((options) => {
    const sys = uni.getSystemInfoSync();
    statusBarHeight.value = sys.statusBarHeight || 20;

    // 初始化日期限制：只能从今天开始
    const today = new Date();
    minDate.value = formatDate(today);
    firstDeductionDate.value = formatDate(today); // 默认选中今天

    if (options.portfolio_id) {
        portfolioId.value = options.portfolio_id;
        loadPortfolioInfo();
    }
});

// --- 方法 ---

const formatDate = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
};

const goBack = () => uni.navigateBack();

const onDateChange = (e) => {
    firstDeductionDate.value = e.detail.value;
};

const loadPortfolioInfo = async () => {
    try {
        const res = await get(`/api/portfolios/${portfolioId.value}`);
        if (res.data) {
            portfolioName.value = res.data.name;
        }
        
        // 可选：如果需要回显之前的定投设置
        // loadExistingSettings(); 
    } catch (e) {
        console.error(e);
    }
};

const saveDCAPlan = async () => {
    if (!canSave.value) return;

    uni.showLoading({ title: '提交中...' });
    
    try {
        const startDateObj = new Date(firstDeductionDate.value);
        
        const payload = {
            strategyName: '智能定投计划',
            periodicAmount: parseFloat(dcaAmount.value),
            periodType: dcaPeriod.value,
            startDate: startDateObj.toISOString(),
            // 根据周期类型计算辅助字段 (后端可能需要，也可能只用 startDate 自动推算)
            dayOfWeek: dcaPeriod.value === 'weekly' || dcaPeriod.value === 'biweekly' ? (startDateObj.getDay() === 0 ? 7 : startDateObj.getDay()) : null,
            dayOfMonth: dcaPeriod.value === 'monthly' ? startDateObj.getDate() : null,
            // 注意：不再传递 allocations，后端应使用组合当前权重
        };

        const res = await post(`/api/dca/portfolio/${portfolioId.value}`, payload);
        
        if (res.statusCode === 200) {
            uni.showToast({ title: '定投已开启', icon: 'success' });
            setTimeout(() => {
                uni.navigateBack();
            }, 1500);
        } else {
            uni.showToast({ title: '设置失败', icon: 'none' });
        }
    } catch (error) {
        uni.showToast({ title: '网络请求失败', icon: 'none' });
    } finally {
        uni.hideLoading();
    }
};
</script>

<style lang="scss" scoped>
/* 
   引用颜色变量 
   注：这里尽量使用了你提供的第一个文件中的视觉风格。
   $primary 对应 Teal/Green 风格 (#2a806c 左右)
*/
$primary: #2a806c; /* 根据上一文件的阴影颜色推断的主题色 */
$bg-page: #f5f7fa;
$text-main: #1f2937;
$text-sub: #6b7280;
$white: #ffffff;

.dca-page {
    min-height: 100vh;
    background-color: $bg-page;
    display: flex;
    flex-direction: column;
}



/* 滚动内容区 */
.content-scroll {
    flex: 1;
    padding: 12px 20px;
    box-sizing: border-box;
}

/* 2. 头部组合卡片 (复用样式风格) */
.portfolio-header-card {
    background-color: $primary;
    border-radius: 24px;
    padding: 24px;
    margin-bottom: 20px;
    position: relative;
    overflow: hidden;
    box-shadow: 0 10px 20px -5px rgba(42, 128, 108, 0.3);
    color: #fff;
}

.bg-decoration-1 {
    position: absolute;
    top: -40px;
    right: -40px;
    width: 150px;
    height: 150px;
    background-color: rgba(255,255,255,0.1);
    border-radius: 50%;
    filter: blur(40px);
}
.bg-decoration-2 {
    position: absolute;
    bottom: -30px;
    left: -20px;
    width: 120px;
    height: 120px;
    background-color: rgba(0,0,0,0.1);
    border-radius: 50%;
    filter: blur(30px);
}

.ph-content { position: relative; z-index: 1; }
.ph-label { font-size: 12px; opacity: 0.8; margin-bottom: 8px; }
.ph-name { font-size: 24px; font-weight: bold; margin-bottom: 12px; display: block; }
.ph-tag {
    display: inline-block;
    padding: 4px 10px;
    background-color: rgba(255,255,255,0.15);
    border-radius: 12px;
    font-size: 11px;
}

/* 3. 表单卡片 */
.form-card {
    background-color: $white;
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.03);
    margin-bottom: 24px;
}

.section-label {
    font-size: 14px;
    color: $text-sub;
    margin-bottom: 12px;
    display: block;
}

.amount-input-box {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.currency-symbol {
    font-size: 32px;
    font-weight: 600;
    color: $text-main;
    margin-right: 8px;
    line-height: 1;
}

.amount-input {
    flex: 1;
    font-size: 40px;
    height: 48px;
    font-weight: bold;
    color: $text-main;
    caret-color: $primary;
}

.input-placeholder {
    color: #e5e7eb;
}

.divider {
    height: 1px;
    background-color: #f3f4f6;
    margin: 20px 0;
}

.setting-row {
    margin-bottom: 24px;
    &:last-child { margin-bottom: 0; }
}

.row-label {
    font-size: 14px;
    color: $text-main;
    font-weight: 600;
    margin-bottom: 12px;
    display: block;
}

/* 周期选择器 (Chip样式) */
.period-selector {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.period-chip {
    padding: 8px 16px;
    background-color: #f3f4f6;
    border-radius: 12px;
    font-size: 13px;
    color: $text-sub;
    transition: all 0.2s;
    border: 1px solid transparent;

    &.active {
        background-color: rgba($primary, 0.1); /* 主题色的浅色背景 */
        color: $primary;
        border-color: $primary;
        font-weight: 600;
    }
}

/* 日期选择展示 */
.date-display {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    background-color: #f9fafb;
    border-radius: 16px;
}

.date-text {
    font-size: 15px;
    color: $text-main;
    font-weight: 500;
    font-family: monospace; /* 让数字对齐更好看 */
}

.arrow-icon {
    width: 16px;
    height: 16px;
    opacity: 0.5;
}

/* 4. 规则说明 */
.rules-section {
    padding: 0 8px;
}
.rules-title {
    font-size: 13px;
    font-weight: 600;
    color: $text-sub;
    margin-bottom: 12px;
    display: block;
}
.rule-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 8px;
    font-size: 12px;
    color: #9ca3af;
    line-height: 1.5;
}
.dot {
    width: 4px;
    height: 4px;
    background-color: #d1d5db;
    border-radius: 50%;
    margin-top: 6px;
    margin-right: 8px;
    flex-shrink: 0;
}

/* 5. 底部按钮栏 */
.footer-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: $white;
    padding: 16px 24px;
    /* 适配 iPhone 底部安全区 */
    padding-bottom: calc(16px + constant(safe-area-inset-bottom));
    padding-bottom: calc(16px + env(safe-area-inset-bottom));
    box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
    z-index: 50;
}

.submit-btn {
    background-color: $primary;
    color: #fff;
    border-radius: 28px;
    height: 50px;
    line-height: 50px;
    font-size: 16px;
    font-weight: 600;
    border: none;
    
    &.disabled {
        background-color: #e5e7eb;
        color: #9ca3af;
    }
    
    &:active {
        opacity: 0.9;
    }
}
</style>