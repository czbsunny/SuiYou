<template>
  <view class="analysis-page">
    <!-- 滚动内容区 -->
    <scroll-view scroll-y class="scroll-content">
        <!-- 累计收益走势卡片 -->
        <view class="chart-card">
            <view class="card-header">
                <text class="title-with-border">累计收益走势</text>
                <view class="badge-wrapper">
                    <text class="badge-red">跑赢基准 5.2%</text>
                </view>
            </view>
            <view class="chart-container">
               <view class="chart-placeholder">
                   <text class="chart-text">收益走势图表区域</text>
               </view>
            </view>
        </view>

        <!-- 核心指标与资产分布网格 -->
        <view class="metrics-grid">
            <view class="metric-card">
                <view class="card-title-row ind">
                    <image src="/static/images/activity.png" class="icon-sm text-primary" />
                    <text class="title-text">核心指标</text>
                </view>
                <view v-if="!loadingMetrics" class="metrics-data-grid">
                    <view v-for="m in metricsItems" :key="m.label" class="metric-item">
                        <text class="metric-label">{{ m.label }}</text>
                        <text :class="['metric-value', m.color ? 'text-primary' : 'text-main']">
                            {{ m.format === 'PERCENT' ? `${(m.value * 100).toFixed(2)}%` : m.value.toFixed(2) }}
                        </text>
                    </view>
                </view>
                <view v-else class="loading-box">
                    <text class="loading-text">指标计算中...</text>
                </view>
            </view>

            <view class="distribution-card">
                <view class="card-title-row start">
                    <image src="/static/images/chart-pie-blue.png" class="icon-sm text-primary" />
                    <text class="title-text">资产分布</text>
                </view>
                <view class="pie-chart-wrapper">
                    <view class="pie-circle"></view>
                </view>
                <view class="legend-grid">
                     <view v-for="s in chartSegments" :key="s.label" class="legend-item">
                         <view :class="['legend-dot', s.colorClass]"></view>
                         <text class="legend-text">{{ s.label }}</text>
                     </view>
                </view>
            </view>
        </view>

        <!-- 【优化后】相关性矩阵区域 -->
        <!-- 仅当持仓数量大于1时显示 -->
        <view v-if="!loadingMetrics && portfolio.holdings && portfolio.holdings.length > 1" class="matrix-card">
            <view class="card-header-row">
                <view class="header-left">
                    <image src="/static/images/layout-grid.png" class="icon-sm text-primary" />
                    <text class="title-text">相关性矩阵</text>
                </view>
                <view class="info-btn">
                    <image src="/static/images/info.png" class="icon-xs text-muted" />
                    <text class="info-text">说明</text>
                </view>
            </view>
            
            <!-- 矩阵容器：增加内衬优化视觉 -->
            <view class="matrix-container">
                <scroll-view scroll-x class="matrix-scroll-view">
                    <view class="matrix-table">
                        <!-- 表头行 (圆圈数字 1, 2, 3...) -->
                        <view class="matrix-row header-row">
                            <view class="matrix-cell-header empty"></view>
                            <view v-for="(h, index) in portfolio.holdings" :key="index" class="matrix-cell-header">
                                <view class="circle-badge header-badge">{{ index + 1 }}</view>
                            </view>
                        </view>
                        
                        <!-- 数据行 -->
                        <view v-for="(row, rIndex) in correlationMatrix" :key="rIndex" class="matrix-row">
                            <!-- 行首 (圆圈数字) -->
                            <view class="matrix-cell-header row-label">
                                <view class="circle-badge row-badge">{{ rIndex + 1 }}</view>
                            </view>
                            
                            <!-- 数据单元格 -->
                            <view 
                                v-for="(val, cIndex) in row" 
                                :key="cIndex" 
                                :class="['matrix-cell', getCorrelationClass(val)]"
                            >
                                <!-- 对角线显示破折号，减少视觉干扰 -->
                                <text class="cell-text">{{ val === 1 ? '—' : val.toFixed(2) }}</text>
                            </view>
                        </view>
                    </view>
                </scroll-view>
            </view>

            <!-- 矩阵图例 (圆圈数字对应基金名) -->
            <view class="matrix-legend-box">
                <view v-for="(h, index) in portfolio.holdings" :key="h.id" class="matrix-legend-item">
                    <view class="circle-badge legend-badge">{{ index + 1 }}</view>
                    <text class="legend-name text-ellipsis">{{ h.name }}</text>
                </view>
            </view>
        </view>

        <!-- AI 诊断区域 -->
        <view id="ai-section" class="ai-section">
            <view class="ai-header">
                <view class="ai-title-group">
                    <image src="/static/images/sparkles.png" class="icon-md text-primary" />
                    <text class="ai-title">AI 深度优化建议</text>
                </view>
                
                <view 
                    v-if="!suggestions.length && !analyzingAi && !loadingMetrics" 
                    @click="handleAiDiagnosis" 
                    class="ai-start-btn pulse-anim"
                >
                    <image src="/static/images/zap.png" class="icon-xs" /> 
                    <text>启动 AI 诊断</text>
                </view>
            </view>

            <view v-if="loadingMetrics && !suggestions.length" class="status-box">
                <text class="status-text">正在加载基础数据...</text>
            </view>
            
            <view v-else-if="analyzingAi" class="status-box analysis-mode">
                <view class="loading-spinner"></view>
                <text class="status-text">AI 正在扫描全市场基金数据...</text>
            </view>
            
            <view v-else-if="suggestions.length > 0" class="suggestions-list fade-in">
                <view 
                    v-for="s in suggestions" 
                    :key="s.id" 
                    :class="['suggestion-item', s.selected ? 'is-selected' : '']" 
                    @click="toggleSuggestion(s)"
                >
                    <view class="checkbox-wrapper">
                        <view :class="['checkbox', s.selected ? 'checked' : '']">
                             <image v-if="s.selected" src="/static/images/check.png" class="check-icon" />
                        </view>
                    </view>
                    
                    <view class="content-wrapper">
                        <view class="item-header">
                            <text :class="['item-title', s.selected ? 'text-primary' : 'text-sub']">{{ s.text }}</text>
                            <text v-if="s.type === 'FUND'" class="recommend-tag">同类优选</text>
                        </view>
                        <text class="item-detail">{{ s.detail }}</text>
                    </view>
                </view>
            </view>
            
            <view v-else class="intro-text">
                点击上方按钮，AI 将基于当前组合信息，给出如提升夏普比率、同类基金高费率平替等针对性优化建议。
            </view>
        </view>
    </scroll-view>

    <!-- 底部固定按钮 -->
    <view v-if="suggestions.length > 0" class="bottom-bar slide-up">
        <button 
            @click="handleRunOptimization"
            :disabled="optimizing"
            class="optimize-btn"
            :class="{ 'is-disabled': optimizing }"
        >
            <template v-if="optimizing">
                <image src="/static/images/refresh-cw.png" class="icon-lg spin-anim" />
                <text>正在进行多因子约束优化...</text>
            </template>
            <template v-else>
                <image src="/static/images/zap.png" class="icon-lg text-yellow" />
                <text>一键生成优化组合</text>
            </template>
        </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const portfolioId = ref('');
const portfolio = ref({});
const loadingMetrics = ref(true);
const analyzingAi = ref(false);
const optimizing = ref(false);
const metrics = ref(null);
const suggestions = ref([]);

// 相关性矩阵数据
const correlationMatrix = ref([]);

onLoad((options) => {
    if (options.id) {
        portfolioId.value = options.id;
        loadData();
    } else {
        // 预览模式
        loadData();
    }
});

const loadData = async () => {
    loadingMetrics.value = true;
    try {
        await mockFetchPortfolioData();
        metrics.value = calculateMetrics(portfolio.value.riskLevel);
    } catch (e) {
        console.error(e);
        uni.showToast({ title: '数据加载失败', icon: 'none' });
    } finally {
        loadingMetrics.value = false;
    }
};

const mockFetchPortfolioData = () => {
    return new Promise(resolve => {
        setTimeout(() => {
            portfolio.value = {
                id: portfolioId.value || '123',
                riskLevel: 'BALANCED',
                assetAllocation: { stock: 0.4, bond: 0.4, commodity: 0.1, cash: 0.1, other: 0 },
                holdings: [
                    { id: 'f1', name: '易方达蓝筹精选混合' },
                    { id: 'f2', name: '中欧医疗健康混合C' },
                    { id: 'f3', name: '招商中证白酒指数C' },
                    { id: 'f4', name: '兴全合润混合(LOF)' },
                    { id: 'f5', name: '博时黄金ETF联接C' }
                ]
            };
            
            // 模拟 5x5 矩阵
            correlationMatrix.value = [
                [1.00, 0.25, 0.85, 0.45, 0.10],
                [0.25, 1.00, 0.15, 0.30, -0.10],
                [0.85, 0.15, 1.00, 0.55, 0.05],
                [0.45, 0.30, 0.55, 1.00, 0.12],
                [0.10, -0.10, 0.05, 0.12, 1.00]
            ];
            
            resolve();
        }, 500);
    });
};

// 获取相关性颜色样式类 (更新逻辑)
const getCorrelationClass = (val) => {
    if (val === 1) return 'cell-self';     // 自身 (灰色/淡化)
    if (val >= 0.8) return 'cell-high';    // 高相关 (红色/风险)
    if (val >= 0.5) return 'cell-mid';     // 中相关 (橙色/注意)
    if (val < 0) return 'cell-neg';        // 负相关 (深绿/优质分散)
    return 'cell-low';                     // 低相关 (浅绿/良好)
};

const calculateMetrics = (level) => {
    const bases = {
        'AGGRESSIVE': { sharpeRatio: 0.7, volatility: 0.25, maxDrawdown: 0.35, annualizedReturn: 0.18, calmarRatio: 0.51, infoRatio: 0.65 },
        'GROWTH': { sharpeRatio: 0.9, volatility: 0.20, maxDrawdown: 0.25, annualizedReturn: 0.15, calmarRatio: 0.60, infoRatio: 0.72 },
        'BALANCED': { sharpeRatio: 1.40, volatility: 0.05, maxDrawdown: 0.05, annualizedReturn: 0.08, calmarRatio: 1.60, infoRatio: 1.10 }, 
        'STEADY': { sharpeRatio: 1.4, volatility: 0.05, maxDrawdown: 0.05, annualizedReturn: 0.06, calmarRatio: 1.20, infoRatio: 0.95 },
        'CONSERVATIVE': { sharpeRatio: 1.8, volatility: 0.02, maxDrawdown: 0.01, annualizedReturn: 0.03, calmarRatio: 3.00, infoRatio: 0.50 },
    };
    return bases[level] || bases['BALANCED'];
};

const metricsItems = computed(() => {
    if(!metrics.value) return [];
    return [
        { label: "夏普比率", value: metrics.value.sharpeRatio, color: true },
        { label: "信息比率", value: metrics.value.infoRatio },
        { label: "年化收益", value: metrics.value.annualizedReturn, format: "PERCENT" },
        { label: "卡玛比率", value: metrics.value.calmarRatio },
        { label: "波动率", value: metrics.value.volatility, format: "PERCENT" },
        { label: "最大回撤", value: metrics.value.maxDrawdown, format: "PERCENT" }
    ];
});

const chartSegments = computed(() => {
    const alloc = portfolio.value.assetAllocation || { stock: 0, bond: 0, commodity: 0, cash: 100, other: 0 };
    return [
        { label: '股票', value: alloc.stock, colorClass: 'bg-blue' },
        { label: '债券', value: alloc.bond, colorClass: 'bg-yellow' },
        { label: '商品', value: alloc.commodity, colorClass: 'bg-purple' },
        { label: '现金', value: alloc.cash, colorClass: 'bg-green' },
    ].filter(s => s.value > 0);
});

const handleAiDiagnosis = () => {
    analyzingAi.value = true;
    setTimeout(() => {
        generateSuggestions();
        analyzingAi.value = false;
    }, 2000);
};

const generateSuggestions = () => {
    suggestions.value = [
        { 
            id: '1', 
            text: '组合夏普比率优化', 
            detail: `当前 ${metrics.value?.sharpeRatio?.toFixed(2)}，建议目标 > ${(metrics.value?.sharpeRatio * 1.3).toFixed(2)}`,
            type: 'METRIC', 
            selected: true 
        },
        { 
            id: '2', 
            text: '同类基金费率优选', 
            detail: '建议替换 [泉果研究精选] (1.5%) 为 [中欧医疗C] (0.6%)',
            type: 'FUND', 
            selected: true 
        },
        { 
            id: '3', 
            text: '最大回撤控制', 
            detail: `将预期最大回撤控制在 ${(metrics.value?.maxDrawdown * 0.8 * 100).toFixed(0)}% 以内`,
            type: 'METRIC', 
            selected: true 
        },
    ];
};

const toggleSuggestion = (s) => {
    s.selected = !s.selected;
};

const handleRunOptimization = async () => {
    if (suggestions.value.filter(s => s.selected).length === 0) {
        uni.showToast({ title: '请至少选择一个优化方向', icon: 'none' });
        return;
    }
    optimizing.value = true;
    try {
        setTimeout(() => {
            optimizing.value = false;
            uni.showToast({ title: '优化方案已生成', icon: 'success' });
            setTimeout(() => {
                uni.navigateTo({ url: '/components/portfolio/optimize' });
            }, 1500);
        }, 2000);
    } catch(e) {
        optimizing.value = false;
        uni.showToast({ title: '优化失败', icon: 'none' });
    }
};
</script>

<style lang="scss" scoped>

/* 通用工具类 */
.bg-blue { background-color: #60a5fa; }
.bg-yellow { background-color: #fbbf24; }
.bg-purple { background-color: #e879f9; }
.bg-green { background-color: #34d399; }
.text-primary { color: $primary; }
.text-main { color: $text-main; }
.text-sub { color: $text-sub; }
.text-muted { color: $text-muted; }
.text-yellow { color: $yellow; }
.text-ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.analysis-page {
  min-height: 100vh;
  background-color: $bg-cream;
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow: hidden;
}

.scroll-content {
  flex: 1;
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 通用卡片样式 */
.chart-card, .metric-card, .distribution-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.distribution-card { margin-bottom: 0; }

/* ----------- 相关性矩阵卡片样式 (美化版) ----------- */
.matrix-card {
  background-color: $bg-white;
  border-radius: 16px;
  padding: 24px 20px; /* 增加上下内边距 */
  box-shadow: 0 4px 20px rgba(0,0,0,0.03); /* 更柔和的阴影 */
  margin-top: 16px;
  display: flex;
  flex-direction: column;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left { display: flex; align-items: center; }

.title-text { font-size: 15px; font-weight: 600; color: $text-main; }

.info-btn {
  display: flex; align-items: center; padding: 4px 10px;
  background-color: #f3f4f6; border-radius: 12px;
}
.info-text { font-size: 11px; color: $text-sub; margin-left: 4px; }

/* 矩阵容器 */
.matrix-container {
  background-color: #ffffff;
}

.matrix-scroll-view { width: 100%; white-space: nowrap; }

.matrix-table {
  display: inline-flex; flex-direction: column;
  min-width: 100%; padding-bottom: 8px;
}

.matrix-row {
  display: flex; flex-direction: row; align-items: center;
  margin-bottom: 8px; /* 行间距 */
  
  &.header-row { margin-bottom: 12px; } /* 表头距离大一点 */
}

/* 单元格尺寸 */
$cell-size: 42px;

.matrix-cell-header, .matrix-cell {
  width: $cell-size; height: $cell-size;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; margin-right: 8px; /* 列间距 */
}

/* 圆形数字徽章 */
.circle-badge {
  width: 24px; height: 24px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: bold;
  font-family: sans-serif;
}

/* 表头和行首的徽章样式 */
.header-badge, .row-badge { background-color: #f3f4f6; color: $text-sub; }

/* 数据单元格样式 */
.matrix-cell {
  border-radius: 10px; /* 圆角 */
  transition: all 0.2s;
  
  .cell-text {
      font-size: 12px; font-weight: 600; font-family: monospace; 
  }
}

/* --- 配色方案 (更现代、柔和) --- */
/* 1. 自身 (1.0) */
.cell-self { background-color: #f9fafb; color: #d1d5db; }
/* 2. 高相关 (>0.8) - 红色风险 */
.cell-high { background-color: #fef2f2; color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.1); }
/* 3. 中相关 (0.5-0.8) - 橙色 */
.cell-mid { background-color: #fffbeb; color: #f59e0b; }
/* 4. 低相关 (0-0.5) - 浅绿 */
.cell-low { background-color: #ecfdf5; color: #10b981; }
/* 5. 负相关 (<0) - 深绿优质 */
.cell-neg { background-color: #d1fae5; color: #059669; border: 1px solid rgba(5, 150, 105, 0.2); }

/* 图例列表美化 */
.matrix-legend-box {
  margin-top: 24px;
  display: grid; grid-template-columns: repeat(2, 1fr); /* 两列布局 */
  gap: 10px;
  border-top: 1px dashed $border-color; padding-top: 16px;
}

.matrix-legend-item {
  display: flex; align-items: center;
  background-color: #f9fafb; padding: 8px 10px; border-radius: 8px;
}

.legend-badge {
  background-color: $primary; /* 深色背景 */
  color: $text-inverse;
  width: 20px; height: 20px; font-size: 10px; margin-right: 8px; flex-shrink: 0;
}

.legend-name { font-size: 12px; color: $text-main; font-weight: 500; }

/* ----------- 原有样式保持不变 ----------- */

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-with-border {
  font-weight: bold; color: $text-main; font-size: 14px;
  border-left: 4px solid $primary; padding-left: 12px;
}
.badge-wrapper { display: flex; }
.badge-red {
  font-size: 10px; padding: 2px 8px; background-color: $red-light; color: $red; border-radius: 4px;
}
.chart-container { height: 160px; width: 100%; position: relative; }
.chart-placeholder {
  width: 100%; height: 100%; background-color: $bg-subtle;
  display: flex; align-items: center; justify-content: center; border-radius: 8px;
}
.chart-text { font-size: 12px; color: $text-muted; }

.metrics-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-top: 16px; }
.card-title-row {
  font-weight: bold; color: $text-main; font-size: 14px; 
  display: flex; align-items: center;
  &.start { align-self: flex-start; }
  &.ind { margin-bottom: 12px;}
}

.icon-sm { width: 14px; height: 14px; margin-right: 6px; }
.icon-md { width: 16px; height: 16px; margin-right: 8px; }
.icon-xs { width: 12px; height: 12px; margin-right: 6px; }
.icon-lg { width: 18px; height: 18px; margin-right: 8px; }

.metrics-data-grid { display: grid; grid-template-columns: repeat(2, 1fr); row-gap: 20px; column-gap: 8px; flex: 1; }
.metric-item { display: flex; flex-direction: column; }
.metric-label { font-size: 10px; color: $text-muted; margin-bottom: 2px; }
.metric-value { font-size: 16px; font-weight: bold; font-family: monospace; }

.loading-box { flex: 1; display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 12px; color: $text-muted; }

.pie-chart-wrapper {
  position: relative; width: 128px; height: 128px; 
  display: flex; align-items: center; justify-content: center;
}
.pie-circle { width: 96px; height: 96px; border-radius: 50%; border: 8px solid $primary; }
.legend-grid { width: 100%; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; padding: 0 4px; }
.legend-item { display: flex; align-items: center; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.legend-text { font-size: 10px; color: $text-sub; }

.ai-section {
  background: linear-gradient(to bottom, #ffffff, rgba(42, 128, 108, 0.05));
  border-radius: 16px; padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(42, 128, 108, 0.1);
  margin-top: 16px; margin-bottom: 32px;
}
.ai-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.ai-title-group { font-weight: bold; color: $text-main; font-size: 14px; display: flex; align-items: center; }
.ai-start-btn {
  font-size: 12px; background-color: $primary; color: #ffffff; padding: 8px 16px;
  border-radius: 9999px; display: flex; align-items: center;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2); transition: all 0.2s;
  &:active { transform: scale(0.95); }
}
.pulse-anim { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }

.status-box {
  padding: 16px 0; text-align: center;
  &.analysis-mode { padding: 32px 0; display: flex; flex-direction: column; align-items: center; }
}
.status-text { font-size: 12px; color: $text-muted; }
.loading-spinner {
  width: 40px; height: 40px; border: 4px solid rgba(42, 128, 108, 0.2);
  border-top-color: $primary; border-radius: 50%; margin-bottom: 16px;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.suggestions-list { display: flex; flex-direction: column; gap: 12px; }
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

.suggestion-item {
  display: flex; align-items: flex-start; padding: 16px; border-radius: 12px;
  border: 1px solid $border-color; background-color: $bg-white; transition: all 0.2s;
  &.is-selected { border-color: $primary; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
}
.checkbox-wrapper { position: relative; display: flex; align-items: center; margin-top: 4px; margin-right: 12px; }
.checkbox {
  width: 20px; height: 20px; border: 2px solid $text-muted; border-radius: 4px;
  display: flex; align-items: center; justify-content: center; transition: background-color 0.2s;
  &.checked { background-color: $primary; border-color: $primary; }
}
.check-icon { width: 12px; height: 12px; }
.content-wrapper { flex: 1; margin-left: 4px; }
.item-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.item-title { font-size: 14px; font-weight: bold; }
.recommend-tag { font-size: 9px; background-color: $orange-light; color: $orange; padding: 2px 6px; border-radius: 4px; }
.item-detail { font-size: 12px; color: $text-sub; line-height: 1.625; display: block; }
.intro-text { font-size: 12px; color: $text-muted; line-height: 1.625; padding: 8px; text-align: center; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background-color: $bg-white;
  padding: 16px; border-top: 1px solid $border-color;
  box-shadow: 0 -5px 20px rgba(0,0,0,0.05);
  padding-bottom: calc(16px + env(safe-area-inset-bottom)); z-index: 30;
  display: flex; justify-content: center;
}
.slide-up { animation: slideUp 0.3s ease-out; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }

.optimize-btn {
  width: 100%; padding: 14px 0; background-color: $primary; color: #ffffff;
  border-radius: 12px; font-weight: bold; box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.3);
  display: flex; align-items: center; justify-content: center; transition: transform 0.1s; border: none;
  &:active { transform: scale(0.98); }
  &.is-disabled { opacity: 0.7; }
}
.spin-anim { animation: spin 1s linear infinite; }
</style>