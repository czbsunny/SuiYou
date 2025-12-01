<template>
  <view class="report-page">
     <scroll-view scroll-y class="scroll-content">
         
         <!-- 1. 收益概览卡片 (保持不变) -->
         <view class="data-card overview-card">
             <view class="card-header">
                  <text class="title-with-border">收益概览</text>
             </view>
             <view class="overview-grid">
                 <view class="overview-item">
                     <text class="label">当前市值</text>
                     <text class="value-main">
                         ¥{{ portfolio.totalValue ? portfolio.totalValue.toLocaleString() : '0' }}
                     </text>
                 </view>
                 <view class="overview-item border-left">
                     <text class="label">累计收益</text>
                     <text :class="['value-main', (portfolio.totalReturn || 0) >= 0 ? 'text-gain' : 'text-loss']">
                         {{ (portfolio.totalReturn || 0) > 0 ? '+' : '' }}{{ (portfolio.totalReturn || 0).toLocaleString() }}
                     </text>
                 </view>
                 <view class="overview-item border-left">
                     <text class="label">收益率</text>
                     <text :class="['value-main', (portfolio.totalGainPercent || 0) >= 0 ? 'text-gain' : 'text-loss']">
                         {{ (portfolio.totalGainPercent || 0) > 0 ? '+' : '' }}{{ (portfolio.totalGainPercent || 0).toFixed(2) }}%
                     </text>
                 </view>
             </view>
         </view>

         <!-- 2. 收益日历卡片 -->
         <view class="data-card calendar-card">
             <view class="card-header">
                 <text class="title-with-border">收益日历</text>
             </view>
             
             <!-- 顶部筛选栏 -->
             <view class="filter-bar">
                 <view class="period-tabs">
                     <view 
                      v-for="t in tabs"
                      :key="t.key"
                      @click="handleTabChange(t.key)"
                      :class="['period-tab', calendarTab === t.key ? 'is-active' : '']"
                     >
                         <text>{{ t.label }}</text>
                     </view>
                 </view>
                 <view class="unit-switch">
                     <view 
                      @click="unitTab = '%'"
                      :class="['unit-btn', unitTab === '%' ? 'is-active' : '']"
                     ><text>%</text></view>
                     <view 
                      @click="unitTab = '¥'"
                      :class="['unit-btn', unitTab === '¥' ? 'is-active' : '']"
                     ><text>¥</text></view>
                 </view>
             </view>

             <!-- 日期导航 -->
             <view class="date-navigator" v-if="calendarTab !== 'Y'">
                 <view class="nav-btn" @click="changeDate(-1)">
                      <image src="/static/images/chevron-right-blue.png" class="nav-icon rotate-180" />
                 </view>
                 <text class="current-date">{{ viewTitle }}</text>
                 <view class="nav-btn" @click="changeDate(1)">
                      <image src="/static/images/chevron-right-blue.png" class="nav-icon" />
                 </view>
             </view>
             <view class="date-navigator disabled" v-else>
                 <text class="current-date">历史年度表现</text>
             </view>

             <!-- 日历网格 -->
             <view class="calendar-content fade-in">
                 <!-- 【修改点1】日视图表头：只显示周一到周五 -->
                 <view v-if="calendarTab === 'D'" class="week-header">
                     <text v-for="d in ['一', '二', '三', '四', '五']" :key="d" class="week-day">{{ d }}</text>
                 </view>
                 
                 <view class="calendar-grid" :class="`cols-${gridCols}`">
                     <!-- 占位格 (仅日视图) -->
                     <template v-if="calendarTab === 'D'">
                         <view v-for="n in startDayOffset" :key="`pad-${n}`" class="grid-cell empty"></view>
                     </template>

                     <template v-for="(item, idx) in gridData" :key="idx">
                         
                         <!-- ===== 周视图 (支持展开) ===== -->
                         <view v-if="calendarTab === 'W'" class="week-wrapper">
                             <view 
                                class="list-row"
                                :class="{'is-selected': selectedIdx === idx}"
                                @click="handleItemClick(idx, item)"
                             >
                                 <view class="row-left">
                                     <text class="row-label">{{ item.label }}</text>
                                     <text class="row-sub">{{ item.dateRange }}</text>
                                 </view>
                                 <text :class="['row-value', item.val >= 0 ? 'text-gain' : 'text-loss']">
                                     {{ item.val >= 0 ? '+' : '' }}{{ formatDisplayValue(item.val) }}{{ unitTab === '%' ? '%' : '' }}
                                 </text>
                                 <image 
                                    src="/static/images/chevron-right-blue.png" 
                                    class="expand-icon"
                                    :class="{'rotate-90': selectedIdx === idx}" 
                                 />
                             </view>

                             <!-- 展开的每日详情 -->
                             <view v-if="selectedIdx === idx" class="week-details">
                                 <view v-for="(day, dIdx) in item.dailyData" :key="dIdx" class="detail-row">
                                     <!-- 【修改点】周详情日期展示优化 -->
                                     <text class="detail-date">{{ day.dateStr }}</text>
                                     <text :class="['detail-val', day.val >= 0 ? 'text-gain' : 'text-loss']">
                                         {{ day.val >= 0 ? '+' : '' }}{{ formatDisplayValue(day.val) }}{{ unitTab === '%' ? '%' : '' }}
                                     </text>
                                 </view>
                             </view>
                         </view>

                         <!-- ===== 网格模式 (日/月/年) ===== -->
                         <view 
                          v-else
                          class="grid-cell"
                          :class="{ 
                              'is-today': item.isToday, 
                              'is-selected': selectedIdx === idx 
                          }"
                          @click="handleItemClick(idx, item)"
                         >
                             <view 
                                v-if="calendarTab === 'D' && selectedIdx === idx && unitTab === '¥'" 
                                class="magnifier-tip"
                             >
                                 <text class="tip-val">{{ item.val >= 0 ? '+' : '' }}{{ item.val.toFixed(2) }}</text>
                                 <view class="tip-arrow"></view>
                             </view>

                             <text class="cell-label">{{ item.label }}</text>
                             
                             <text :class="['cell-value', item.val >= 0 ? 'text-gain' : 'text-loss']">
                                 {{ item.val > 0 ? '+' : '' }}{{ formatDisplayValue(Math.abs(item.val)) }}{{ unitTab === '%' ? '%' : '' }}
                             </text>
                         </view>
                     </template>
                 </view>
             </view>
         </view>

         <!-- 3. 盈亏榜单卡片 (保持不变) -->
         <view class="data-card ranking-card">
             <view class="card-header">
                  <text class="title-with-border">盈亏榜单</text>
             </view>
             <view class="ranking-header">
                 <text class="col-rank">排名</text>
                 <text class="col-name">基金名称</text>
                 <text class="col-val">日盈亏</text>
             </view>
             <view class="ranking-list">
                 <view v-for="(h, idx) in sortedHoldings" :key="h.id" class="ranking-item">
                     <view class="col-rank center">
                         <text :class="['rank-badge', getRankClass(idx)]">
                             {{ idx + 1 }}
                         </text>
                     </view>
                     <view class="col-name">
                         <text class="fund-name">{{ h.name }}</text>
                         <text class="fund-code">{{ h.code }}</text>
                     </view>
                     <text :class="['col-val font-mono', h.dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
                         {{ h.dailyReturn > 0 ? '+' : '' }}{{ h.dailyReturn.toFixed(2) }}
                     </text>
                 </view>
             </view>
             <text v-if="sortedHoldings.length === 0" class="empty-text">暂无持仓数据</text>
         </view>

         <!-- 4. 收益走势 (保持不变) -->
         <view class="data-card trend-card">
             <view class="card-header">
                 <text class="title-with-border">收益走势</text>
             </view>
             <view class="chart-box">
                 <view class="chart-placeholder">
                     <text class="placeholder-text">Chart Placeholder</text>
                 </view>
             </view>
         </view>
         
         <view class="footer-spacer"></view>
     </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPortfolioItems } from '../../services/portfolioService';

const portfolioId = ref('');
const portfolio = ref({
    totalValue: 0,
    totalReturn: 0,
    totalGainPercent: 0,
    holdings: []
});

const tabs = [
    { key: 'D', label: '日' },
    { key: 'W', label: '周' },
    { key: 'M', label: '月' },
    { key: 'Y', label: '年' }
];
const calendarTab = ref('D'); 
const unitTab = ref('%');
const currentDate = ref(new Date());

const selectedIdx = ref(-1); 
let lastTapTime = 0; 

onLoad((options) => {
    if (options.id) {
        portfolioId.value = options.id;
        loadData();
    }
});

const loadData = async () => {
    portfolio.value = {
        totalValue: 120500.00,
        totalReturn: 5200.00,
        totalGainPercent: 4.32,
    };
};

const formatDisplayValue = (val) => {
    if (unitTab.value === '%') return val.toFixed(2);
    const absVal = Math.abs(val);
    if (absVal >= 10000) {
        return (absVal / 10000).toFixed(2) + 'w';
    }
    return absVal.toFixed(2);
};

const handleItemClick = (idx, item) => {
    const now = Date.now();
    const DOUBLE_CLICK_DELAY = 300; 
    
    if (now - lastTapTime < DOUBLE_CLICK_DELAY) {
        if (calendarTab.value === 'M') {
            const targetMonth = parseInt(item.label) - 1; 
            const y = currentDate.value.getFullYear();
            currentDate.value = new Date(y, targetMonth, 1);
            calendarTab.value = 'D';
            selectedIdx.value = -1;
        } else if (calendarTab.value === 'Y') {
            const targetYear = parseInt(item.label);
            currentDate.value = new Date(targetYear, 0, 1);
            calendarTab.value = 'M';
            selectedIdx.value = -1;
        }
        lastTapTime = 0; 
    } else {
        if (selectedIdx.value === idx) {
            selectedIdx.value = -1; 
        } else {
            selectedIdx.value = idx; 
        }
        lastTapTime = now;
    }
};

const handleTabChange = (key) => {
    calendarTab.value = key;
    selectedIdx.value = -1; 
};

const changeDate = (delta) => {
    const newDate = new Date(currentDate.value);
    if (calendarTab.value === 'D' || calendarTab.value === 'W') {
        newDate.setMonth(newDate.getMonth() + delta);
    } else if (calendarTab.value === 'M') {
        newDate.setFullYear(newDate.getFullYear() + delta);
    }
    currentDate.value = newDate;
    selectedIdx.value = -1; 
};

const viewTitle = computed(() => {
    const y = currentDate.value.getFullYear();
    const m = currentDate.value.getMonth() + 1;
    if (calendarTab.value === 'D' || calendarTab.value === 'W') return `${y}年${m}月`;
    if (calendarTab.value === 'M') return `${y}年`;
    return '';
});

// 【修改点2】Grid 列数控制
const gridCols = computed(() => {
    switch (calendarTab.value) {
        case 'D': return 5; // 日视图改为5列 (周一至周五)
        case 'M': return 4; 
        case 'Y': return 4; 
        case 'W': return 1; 
        default: return 1;
    }
});

// 【修改点3】日视图：计算月初空白格 (Offset)
// 如果1号是周六或周日，则偏移量为0（因为不显示，直接从下周一开始排，或者如果1号是周六，列表直接从3号周一开始）
// 如果1号是周一，偏移 0；周二偏移 1 ... 周五偏移 4
const startDayOffset = computed(() => {
    if (calendarTab.value !== 'D') return 0;
    const y = currentDate.value.getFullYear();
    const m = currentDate.value.getMonth();
    const firstDay = new Date(y, m, 1).getDay();
    
    // getDay(): 0(Sun), 1(Mon), 2(Tue)... 6(Sat)
    // 如果是周六(6)或周日(0)，则它们不展示，当月数据直接从第一个工作日开始排，不需要 Padding。
    // 如果是周一(1) -> 0 padding
    // 如果是周二(2) -> 1 padding
    if (firstDay === 0 || firstDay === 6) return 0;
    return firstDay - 1;
});

const gridData = computed(() => {
    const y = currentDate.value.getFullYear();
    const m = currentDate.value.getMonth();
    const today = new Date();
    
    // --- 日维度 (过滤周末) ---
    if (calendarTab.value === 'D') {
        const daysInMonth = new Date(y, m + 1, 0).getDate();
        const daysList = [];
        
        for (let i = 1; i <= daysInMonth; i++) {
            const currentDayDate = new Date(y, m, i);
            const dayOfWeek = currentDayDate.getDay();
            
            // 【核心逻辑】跳过周六(6)和周日(0)
            if (dayOfWeek === 6 || dayOfWeek === 0) continue;

            const isBig = i % 5 === 0;
            const rawVal = (Math.sin(i + m) * (isBig ? 15000 : 200)); 
            const isToday = (today.getFullYear() === y && today.getMonth() === m && today.getDate() === i);
            
            daysList.push({ 
                label: i.toString(), 
                val: rawVal, 
                isToday 
            });
        }
        return daysList;
    }
    
    // --- 周维度 (过滤周末详情) ---
    if (calendarTab.value === 'W') {
        const weeks = [];
        const firstDayOfMonth = new Date(y, m, 1);
        const lastDayOfMonth = new Date(y, m + 1, 0);
        
        let currentMonday = new Date(firstDayOfMonth);
        const dayOfWeek = currentMonday.getDay();
        const diff = currentMonday.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); 
        currentMonday.setDate(diff); 

        while (currentMonday <= lastDayOfMonth) {
            const startStr = `${currentMonday.getMonth() + 1}.${currentMonday.getDate()}`;
            const currentSunday = new Date(currentMonday);
            currentSunday.setDate(currentMonday.getDate() + 6);
            const endStr = `${currentSunday.getMonth() + 1}.${currentSunday.getDate()}`;
            
            // 【核心逻辑】生成详情时，跳过周六日
            const dailyData = [];
            for(let i=0; i<7; i++) {
                let d = new Date(currentMonday);
                d.setDate(d.getDate() + i);
                
                // 跳过周六和周日
                if (d.getDay() === 0 || d.getDay() === 6) continue;

                const weekDayName = ['日','一','二','三','四','五','六'][d.getDay()];
                dailyData.push({
                    dateStr: `${d.getMonth()+1}.${d.getDate()} (周${weekDayName})`,
                    val: (Math.random() * 200) - 50
                });
            }

            // 只有当这一周有交易日数据时才添加（避免刚好跨月全是周末的情况）
            if (dailyData.length > 0) {
                weeks.push({
                    label: `第${weeks.length + 1}周`,
                    dateRange: `${startStr} ~ ${endStr}`,
                    val: (Math.random() * 4) - 2,
                    dailyData
                });
            }
            
            currentMonday.setDate(currentMonday.getDate() + 7);
        }
        return weeks;
    }
    
    // --- 月维度 ---
    if (calendarTab.value === 'M') {
        const months = [];
        const limitMonth = (y === today.getFullYear()) ? today.getMonth() : 11;
        for (let i = 0; i <= 11; i++) {
            if (y === today.getFullYear() && i > limitMonth) break;
            months.push({ label: `${i + 1}月`, val: (Math.cos(i) * 5) });
        }
        return months;
    }
    
    // --- 年维度 ---
    if (calendarTab.value === 'Y') {
        const years = [];
        const currentYear = today.getFullYear();
        for (let i = 0; i < 8; i++) {
             const yearLabel = currentYear - i;
             years.push({ label: yearLabel.toString(), val: (Math.random() * 20) - 5 });
        }
        return years;
    }
    
    return [];
});

const sortedHoldings = computed(() => {
    return portfolio.value.holdings ? [...portfolio.value.holdings].sort((a, b) => b.dailyReturn - a.dailyReturn) : [];
});

const getRankClass = (idx) => {
    if (idx === 0) return 'rank-1';
    if (idx === 1) return 'rank-2';
    if (idx === 2) return 'rank-3';
    return 'rank-normal';
};
</script>

<style lang="scss" scoped>
/* 保持原有变量 */
$primary: #2a806c;
$bg-cream: #fdfbf7;
$bg-white: #ffffff;
$bg-subtle: #f9fafb;
$bg-subtle-hover: #f3f4f6;
$text-main: #1f2937;
$text-sub: #374151;
$text-muted: #9ca3af;
$border-color: #e5e7eb;

$color-gain: #e53935;
$color-loss: #43a047;
$color-selected-bg: rgba(42, 128, 108, 0.08);

/* 布局 */
.report-page { height: 100vh; background-color: $bg-cream; display: flex; flex-direction: column; }
.scroll-content { flex: 1; height: 0; background-color: $bg-cream; }
.footer-spacer { height: 40px; margin-bottom: env(safe-area-inset-bottom); }

/* 卡片 */
.data-card { background-color: $bg-white; margin: 16px 12px 0 12px; border-radius: 16px; padding: 20px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.card-header { margin-bottom: 20px; }
.title-with-border { font-weight: bold; color: $text-main; font-size: 14px; border-left: 4px solid $primary; padding-left: 12px; }

/* 概览 (简写) */
.overview-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; text-align: center; }
.overview-item.border-left { border-left: 1px solid $bg-subtle-hover; }
.label { font-size: 12px; color: $text-muted; display: block; margin-bottom: 8px; }
.value-main { font-size: 16px; font-weight: bold; color: $text-main; font-family: monospace; }
.text-gain { color: $color-gain; }
.text-loss { color: $color-loss; }

/* 筛选 */
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.period-tabs { display: flex; background-color: $bg-subtle; padding: 4px; border-radius: 8px; }
.period-tab { font-size: 12px; padding: 6px 14px; border-radius: 6px; color: $text-muted; &.is-active { background-color: $bg-white; color: $primary; font-weight: bold; box-shadow: 0 1px 2px rgba(0,0,0,0.05); } }
.unit-switch { display: flex; gap: 8px; }
.unit-btn { width: 28px; height: 28px; border-radius: 4px; border: 1px solid $border-color; display: flex; align-items: center; justify-content: center; font-size: 12px; color: $text-muted; &.is-active { background-color: rgba(42, 128, 108, 0.1); color: $primary; border-color: $primary; } }

/* 导航 */
.date-navigator { display: flex; justify-content: space-between; align-items: center; background-color: $bg-subtle; border-radius: 12px; padding: 10px 16px; margin-bottom: 16px; &.disabled { justify-content: center; background-color: transparent; } }
.nav-btn { padding: 4px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.nav-icon { width: 14px; height: 14px; color: $primary; }
.rotate-180 { transform: rotate(180deg); }
.current-date { font-size: 14px; font-weight: bold; color: $text-sub; }

/* 日历网格 */
/* 【修改点4】样式支持5列网格 */
.week-header { display: grid; grid-template-columns: repeat(5, 1fr); text-align: center; margin-bottom: 8px; }
.week-day { font-size: 12px; color: $text-muted; }
.calendar-grid { display: grid; gap: 8px; 
    &.cols-7 { grid-template-columns: repeat(7, 1fr); } 
    &.cols-5 { grid-template-columns: repeat(5, 1fr); } /* 新增 */
    &.cols-4 { grid-template-columns: repeat(4, 1fr); } 
    &.cols-1 { grid-template-columns: 1fr; } 
}

/* === 周视图样式 === */
.week-wrapper { margin-bottom: 8px; }
.list-row { display: flex; justify-content: space-between; align-items: center; padding: 12px; background-color: $bg-subtle; border-radius: 8px; border: 1px solid transparent; transition: all 0.2s; 
    &.is-selected { border-color: $primary; background-color: $bg-white; box-shadow: 0 2px 8px rgba(42,128,108,0.1); }
}
.row-left { display: flex; flex-direction: column; }
.row-label { font-size: 14px; font-weight: 500; color: $text-main; }
.row-sub { font-size: 11px; color: $text-muted; margin-top: 2px; }
.row-value { font-family: monospace; font-weight: bold; font-size: 14px; flex: 1; text-align: right; margin-right: 8px; }
.expand-icon { width: 12px; height: 12px; opacity: 0.5; transition: transform 0.2s; &.rotate-90 { transform: rotate(90deg); } }

/* 周详情展开 */
.week-details { margin-top: 4px; padding: 8px 12px; background-color: $bg-subtle-hover; border-radius: 8px; animation: slideDown 0.2s ease-out; }
.detail-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed $border-color; &:last-child { border-bottom: none; } }
.detail-date { font-size: 12px; color: $text-sub; }
.detail-val { font-size: 12px; font-family: monospace; font-weight: 500; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }

/* === 网格单元 === */
.grid-cell { position: relative; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 8px 0; border-radius: 8px; background-color: $bg-subtle; border: 1px solid transparent; 
    &.empty { background-color: transparent; border: none; }
    &.is-today { background-color: rgba(42, 128, 108, 0.05); color: $primary; .cell-label { color: $primary; font-weight: bold; } }
    &.is-selected { border-color: $primary; background-color: $color-selected-bg; }
}
.cell-label { font-size: 12px; color: $text-sub; margin-bottom: 2px; }
.cell-value { font-size: 10px; font-family: monospace; font-weight: bold; }

/* === 放大镜 Tooltip === */
.magnifier-tip { position: absolute; top: -34px; left: 50%; transform: translateX(-50%); background-color: $text-main; color: #fff; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-family: monospace; white-space: nowrap; z-index: 10; box-shadow: 0 4px 10px rgba(0,0,0,0.2); animation: popIn 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.tip-val { font-weight: bold; }
.tip-arrow { position: absolute; bottom: -4px; left: 50%; transform: translateX(-50%); width: 0; height: 0; border-left: 4px solid transparent; border-right: 4px solid transparent; border-top: 4px solid $text-main; }
@keyframes popIn { from { transform: translateX(-50%) scale(0.8); opacity: 0; } to { transform: translateX(-50%) scale(1); opacity: 1; } }

/* 其他通用 (简写) */
.ranking-header { display: flex; font-size: 12px; color: $text-muted; margin-bottom: 8px; padding: 0 8px; }
.col-rank { width: 30px; text-align: center; }
.col-name { flex: 1; margin-left: 10px; }
.col-val { width: 70px; text-align: right; }
.ranking-item { display: flex; align-items: center; padding: 8px; }
.rank-badge { width: 18px; height: 18px; border-radius: 50%; font-size: 10px; display: flex; align-items: center; justify-content: center; margin: 0 auto; }
.rank-1 { background: #fee2e2; color: #ef4444; } 
.rank-normal { background: #f3f4f6; color: #9ca3af; }
.fund-name { font-size: 13px; color: $text-main; }
.fund-code { font-size: 10px; color: $text-muted; display: block; }
.chart-box { height: 150px; background: $bg-subtle; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.placeholder-text { font-size: 12px; color: $text-muted; }
</style>