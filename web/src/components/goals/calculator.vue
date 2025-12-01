<template>
  <view class="calculator-page">
      <!-- 顶部 Tab 切换 -->
      <view class="tab-card">
          <view class="tab-list">
              <view 
                  v-for="tab in tabs"
                  :key="tab.id"
                  @click="setMode(tab.id)"
                  :class="['tab-item', mode === tab.id ? 'is-active' : '']"
              >
                  <text>{{ tab.label }}</text>
                  <view v-if="mode === tab.id" class="tab-indicator"></view>
              </view>
          </view>
      </view>

      <view class="content-area">
          <!-- 表单区域 -->
          <view class="form-list">
              <view class="input-item">
                  <text class="input-label">当前已有资金 (¥)</text>
                  <input type="digit" v-model="currentVal" class="input-field" placeholder="0" />
              </view>
              
              <template v-if="mode === 'YEARS'">
                  <view class="input-item">
                      <text class="input-label">预计每月定投 (¥)</text>
                      <input type="digit" v-model="monthlyInv" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">目标金额 (¥)</text>
                      <input type="digit" v-model="targetVal" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">预期年化收益 (%)</text>
                      <input type="digit" v-model="rate" class="input-field" placeholder="0" />
                  </view>
              </template>

              <template v-if="mode === 'RATE'">
                  <view class="input-item">
                      <text class="input-label">预计每月定投 (¥)</text>
                      <input type="digit" v-model="monthlyInv" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">目标金额 (¥)</text>
                      <input type="digit" v-model="targetVal" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">计划投资年限 (年)</text>
                      <input type="digit" v-model="duration" class="input-field" placeholder="0" />
                  </view>
              </template>
              
              <template v-if="mode === 'AMOUNT'">
                  <view class="input-item">
                      <text class="input-label">计划投资年限 (年)</text>
                      <input type="digit" v-model="duration" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">目标金额 (¥)</text>
                      <input type="digit" v-model="targetVal" class="input-field" placeholder="0" />
                  </view>
                  <view class="input-item">
                      <text class="input-label">预期年化收益 (%)</text>
                      <input type="digit" v-model="rate" class="input-field" placeholder="0" />
                  </view>
              </template>
          </view>

          <!-- 计算按钮 -->
          <view class="action-section">
              <button @click="calculate" class="calc-btn hover-opacity">
                  <image src="/static/images/calculator.png" class="btn-icon" />
                  <text>{{ getButtonText }}</text>
              </button>
          </view>

          <!-- 计算结果卡片 -->
          <view v-if="resultData" class="result-card fade-in">
              <view class="result-header">
                  <view class="result-row">
                      <text class="result-label">{{ resultData.label }}</text>
                      <view class="result-num-group">
                          <text class="result-big-num">{{ resultData.value }}</text>
                          <text class="result-unit">{{ resultData.unit }}</text>
                      </view>
                  </view>
                  
                  <!-- 简要数据概览 / 图例 -->
                  <view class="summary-grid">
                      <view class="sum-item">
                          <view class="sum-title-row">
                              <view class="dot color-total"></view>
                              <text class="sum-label">累计总额</text>
                          </view>
                          <text class="sum-val text-blue">¥{{ formatNumber(resultData.totalAssets) }}</text>
                      </view>
                      
                      <view class="sum-item">
                          <view class="sum-title-row">
                              <view class="dot color-interest"></view>
                              <text class="sum-label">累计收益</text>
                          </view>
                          <text class="sum-val highlight">+¥{{ formatNumber(resultData.totalInterest) }}</text>
                      </view>
                      
                      <view class="sum-item">
                          <view class="sum-title-row">
                              <view class="dot color-invest"></view>
                              <text class="sum-label">累计定投</text>
                          </view>
                          <text class="sum-val text-invest">¥{{ formatNumber(resultData.investPrincipal) }}</text>
                      </view>
                  </view>
              </view>

              <!-- uCharts 图表区域 -->
              <view class="chart-container">
                  <view class="chart-title">资产积累趋势 (单位: 万)</view>
                  <view class="ucharts-box">
                      <!-- 
                        type="column": 柱状图
                        :stack="true": 开启堆叠
                      -->
                      <qiun-data-charts 
                        type="column"
                        :opts="chartOpts"
                        :chartData="chartData"
                        :animation="true"
                        :ontouch="true"
                        canvas2d
                        background="none"
                        :stack="true"
                      />
                  </view>
              </view>

              <!-- 智能建议 -->
              <view class="advice-box">
                  <view class="advice-icon">💡</view>
                  <text class="advice-text">{{ resultData.advice }}</text>
              </view>
          </view>
      </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

const mode = ref('YEARS');
const currentVal = ref('');
const monthlyInv = ref('2000');
const targetVal = ref('1000000');
const rate = ref('6');
const duration = ref('5');

const resultData = ref(null); 
const chartData = ref({}); 

const tabs = [
    { id: 'YEARS', label: '算时间' },
    { id: 'RATE', label: '算收益率' },
    { id: 'AMOUNT', label: '算定投' },
];

/**
 * uCharts 配置项 (纯 Column 堆叠)
 */
const chartOpts = ref({
    // 颜色顺序对应 Series: [初始本金(绿), 累计定投(橙), 累计收益(红)]
    color: ["#f97316", "#2a806c", "#ef4444"], 
    padding: [15, 15, 0, 15],
    dataLabel: false, // 关闭柱子上的具体数字，保持清爽
    legend: {
        show: false // 使用上方自定义图例
    },
    xAxis: {
        disableGrid: true,
        axisLine: true,
        itemCount: 5, // X轴标签数量
        fontSize: 11
    },
    yAxis: {
        data: [{
            position: "left",
            min: 0,
            axisLine: false,
            tofix: 1,
            unit: "w",
            fontSize: 10
        }]
    },
    extra: {
        column: {
            type: "stack", // 显式指定堆叠类型
            width: 20,     // 柱子宽度
            seriesGap: 0,  // 间距
            barBorderRadius: [0, 0, 0, 0] // 直角
        },
        tooltip: {
            showBox: true,
            bgColor: "#000000",
            bgOpacity: 0.7,
            fontColor: "#FFFFFF",
            legendShow: true // 显示每段的数值
        }
    }
});

const getButtonText = computed(() => {
    const map = {
        'YEARS': '计算达成时间',
        'RATE': '计算所需收益率',
        'AMOUNT': '计算每月定投额'
    };
    return map[mode.value];
});

const setMode = (m) => {
    mode.value = m;
    resultData.value = null;
    chartData.value = {};
};

const formatNumber = (num) => {
    if (num >= 10000) return (num / 10000).toFixed(1) + 'w';
    return Number(num).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
};

// --- 生成 堆叠柱状图(Column Stack) 数据 ---
const generateStackData = (pv, pmt, rYear, totalMonths) => {
    // 1. 动态步长计算
    // <=3年(36月): 半年(6月)一根
    // <=10年(96月): 1年(12月)一根
    // <=20年(240月): 2年(24月)一根
    // <=30年(360月): 3年(36月)一根
    // >30年(360月): 动态计算以年为单位的步长
    let step = 12;
    if (totalMonths <= 36) {
        step = 6;
    } else if (totalMonths <= 120) {
        step = 12;
    } else if (totalMonths <= 240) {
        step = 24;
    } else if (totalMonths <= 360) {
        step = 36;
    } else {
        step = Math.ceil(totalMonths / 120) * 12;
    }

    const categories = [];
    const seriesInitial = []; // 底层
    const seriesInvest = [];  // 中层
    const seriesInterest = []; // 顶层
    
    const mr = rYear / 100 / 12;

    // 从第1个步长开始
    for (let m = step; m <= totalMonths + step; m += step) {
        let currentM = m;
        if (currentM > totalMonths) currentM = totalMonths;
        
        // 防止重复添加终点
        if (categories.length > 0) {
             const lastLabel = categories[categories.length - 1];
             const currentLabelCheck = (currentM / 12).toFixed(1).replace('.0', '') + '年';
             if (lastLabel === currentLabelCheck) break;
        }

        const label = (currentM / 12).toFixed(1).replace('.0', '') + '年';
        categories.push(label);

        const valInitial = pv;
        const valInvest = pmt * currentM;
        const valTotal = pv * Math.pow(1 + mr, currentM) + 
                         pmt * ((Math.pow(1 + mr, currentM) - 1) / mr);
        const valInterest = valTotal - (valInitial + valInvest);

        // 转换为 "万" 单位
        const vInit = Number((valInitial / 10000).toFixed(2));
        const vInv = Number((valInvest / 10000).toFixed(2));
        const vInt = Number((valInterest / 10000).toFixed(2));

        seriesInitial.push(vInit);
        seriesInvest.push(vInv);
        seriesInterest.push(vInt);

        if (currentM === totalMonths) break;
    }

    return {
        categories: categories,
        series: [
            // 堆叠顺序：索引 0 在最下，索引 2 在最上
            { 
                name: "初始本金", 
                data: seriesInitial,
                color: "#f97316" 
            },
            { 
                name: "累计定投", 
                data: seriesInvest,
                color: "#2a806c"
            },
            { 
                name: "累计收益", 
                data: seriesInterest,
                color: "#ef4444"
            }
        ]
    };
};

/**
 * 生成建议文案
 * @param {string} type 模式 'AMOUNT' | 'RATE' | 'YEARS'
 * @param {number} val 核心判断数值 (pmt, rYear, years)
 * @param {number} extraVal 辅助判断数值 (用于更精准的判断，可选)
 */
const getAdvice = (type, val) => {
    if (type === 'AMOUNT') {
        const pmt = val;
        if (pmt <= 0) return "🎉 恭喜！靠现有资金的复利增长即可覆盖目标，无需额外追加定投。";
        if (pmt > 20000) return "⚠️ 每月定投金额较高，请务必检查现金流。建议适当延长达成目标的年限或降低目标金额。";
        if (pmt > 10000) return "💪 充满挑战的储蓄计划。建议在发薪日设置自动扣款，强制储蓄，避免冲动消费。";
        if (pmt > 2000) return "📈 定投金额适中，适合大多数职场人士。建议构建“核心-卫星”策略，长期持有。";
        return "🌱 积少成多，聚沙成塔！非常轻松的起步金额，关键在于开始和坚持。";
    }
    
    if (type === 'RATE') {
        const r = val;
        if (r <= 0) return "📉 目标金额小于本金投入，无需收益即可达成（甚至允许亏损）。";
        if (r > 25) return "🚨 警报：除极少数顶级投资者外很难长期维持。请警惕高利诱惑，谨防诈骗！建议降低预期。";
        if (r >= 15) return "🔥 收益率要求极高，伴随巨大本金亏损风险。主要依赖激进型股票，非专业人士请慎重。";
        if (r >= 10) return "📊 收益率要求较高，需高比例配置偏股型基金。请做好资产回撤20%-30%的心理准备。";
        if (r >= 6) return "🎯 合理的进取型目标。建议采用“股债平衡策略”或定投沪深300/中证500等宽基指数。";
        if (r >= 3) return "🛡️ 收益率要求稳健。通过配置纯债基金、固收+理财或大额存单，大概率可达成。";
        return "💰 收益率要求较低，通过货币基金（如余额宝）或定期存款即可轻松实现。";
    }
    
    if (type === 'YEARS') {
        const y = val;
        if (y >= 50) return "🛑 按当前参数达成目标极其困难。建议大幅增加定投金额，或先设定较小的阶段性目标。";
        if (y < 1) return "⚡️ 目标近在咫尺！时间极短，建议存入货币基金或短期理财，确保本金安全。";
        if (y < 3) return "🗓️ 短期目标：建议以防御为主，配置债券基金或稳健理财，避免因市场回调导致资金缩水。";
        if (y < 7) return "⚖️ 中期目标：涵盖完整牛熊周期。建议“50%股票+50%债券”平衡策略，攻守兼备。";
        if (y < 15) return "📈 长期目标：时间是你的朋友。建议高配指数基金，忽略短期波动，利用微笑曲线增值。";
        return "👴 超长期/养老规划：复利效应将在后期爆发。建议配置红利类资产，为未来提供源源不断的现金流。";
    }
    return "";
};

const calculate = () => {
    const pv = parseFloat(currentVal.value) || 0;
    let pmt = 0, fv = 0, rYear = 0, months = 0;

    // 1. 算时间
    if (mode.value === 'YEARS') {
        pmt = parseFloat(monthlyInv.value) || 0;
        fv = parseFloat(targetVal.value) || 0;
        rYear = parseFloat(rate.value) || 0;
        const mr = rYear / 100 / 12;

        if (fv <= pv) { uni.showToast({ title: '目标需大于当前资金', icon: 'none' }); return; }
        
        let amount = pv;
        const limitMonths = 1200;
        while(amount < fv && months < limitMonths) {
            amount = amount * (1 + mr) + pmt;
            months++;
        }
        
        const years = (months / 12).toFixed(1);
        const totalPrincipal = pv + pmt * months;
        const totalInterest = amount - totalPrincipal;

        resultData.value = {
            label: '预计耗时', value: years, unit: '年',
            totalPrincipal, totalInterest, investPrincipal: pmt * months, totalAssets: amount,
            advice: getAdvice('YEARS', years)
        };
        chartData.value = generateStackData(pv, pmt, rYear, months);
    }

    // 2. 算收益率
    else if (mode.value === 'RATE') {
        pmt = parseFloat(monthlyInv.value) || 0;
        fv = parseFloat(targetVal.value) || 0;
        const years = parseFloat(duration.value) || 0;
        months = years * 12;

        if (years <= 0) return;

        let low = 0, high = 1.0, guessRate = 0;
        for(let i=0; i<50; i++) {
            guessRate = (low + high) / 2;
            const mr = guessRate / 12;
            const currFV = pv * Math.pow(1 + mr, months) + pmt * ((Math.pow(1 + mr, months) - 1) / mr);
            if (currFV > fv) high = guessRate; else low = guessRate;
        }
        rYear = guessRate * 100;
        const totalPrincipal = pv + pmt * months;
        const totalInterest = fv - totalPrincipal;

        resultData.value = {
            label: '所需年化', value: rYear.toFixed(2), unit: '%',
            totalPrincipal, totalInterest, investPrincipal: pmt * months, totalAssets: fv,
            advice: getAdvice('RATE', rYear)
        };
        chartData.value = generateStackData(pv, pmt, rYear, months);
    }

    // 3. 算定投
    else if (mode.value === 'AMOUNT') {
        fv = parseFloat(targetVal.value) || 0;
        rYear = parseFloat(rate.value) || 0;
        const years = parseFloat(duration.value) || 0;
        months = years * 12;
        const mr = rYear / 100 / 12;

        if (years <= 0) return;

        const compoundFactor = Math.pow(1 + mr, months);
        const numerator = fv - (pv * compoundFactor);
        const denominator = (compoundFactor - 1) / mr;
        pmt = denominator !== 0 ? numerator / denominator : 0;
        if (pmt < 0) pmt = 0;

        const totalPrincipal = pv + pmt * months;
        const totalInterest = fv - totalPrincipal;
        
        resultData.value = {
            label: '每月定投', value: formatNumber(pmt), unit: '元',
            totalPrincipal, totalInterest, investPrincipal: pmt * months, totalAssets: fv,
            advice: getAdvice('AMOUNT', pmt)
        };
        chartData.value = generateStackData(pv, pmt, rYear, months);
    }
};
</script>

<style lang="scss" scoped>
.calculator-page {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
  padding-top: 12px;
  box-sizing: border-box;
}

/* Tab & Inputs (不变) */
.tab-card { background: $bg-white; margin: 0 16px 20px; padding: 4px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.tab-list { display: flex; }
.tab-item { flex: 1; padding: 10px 0; font-size: 14px; font-weight: 600; color: $text-muted; text-align: center; position: relative; transition: all 0.3s; &.is-active { color: $primary; } }
.tab-indicator { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 20px; height: 3px; background: $primary; border-radius: 3px; }
.content-area { padding: 0 16px 40px; flex: 1; }
.form-list { display: flex; flex-direction: column; gap: 12px; }
.input-item { background: $bg-white; padding: 18px 20px; border-radius: 12px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.input-label { font-size: 14px; color: $text-sub; font-weight: 500; }
.input-field { text-align: right; font-weight: bold; font-size: 18px; color: $text-main; width: 140px; background: transparent; }
.action-section { padding-top: 24px; }
.calc-btn { width: 100%; background: linear-gradient(135deg, $primary, #206b59); color: #fff; padding: 14px 0; border-radius: 99px; font-size: 16px; font-weight: bold; display: flex; justify-content: center; align-items: center; box-shadow: 0 8px 16px -4px rgba(42, 128, 108, 0.4); border: none; &.hover-opacity:active { opacity: 0.9; transform: scale(0.98); } }
.btn-icon { width: 20px; height: 20px; margin-right: 8px; opacity: 0.9; }

/* --- 结果卡片 --- */
.result-card {
  margin-top: 24px;
  background: $bg-white;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border: 1px solid rgba(42, 128, 108, 0.05);
}

.result-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e5e7eb;
}

.result-label { font-size: 13px; color: $text-sub; margin-bottom: 4px; display: block; }
.result-big-num { font-size: 48px; font-weight: bold; color: $primary; font-family: 'DIN Alternate', sans-serif; line-height: 1; }
.result-unit { font-size: 14px; color: $text-sub; margin-left: 4px; }

/* 概览数据 / 图例 */
.summary-grid {
  display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; margin-top: 16px;
}
.sum-item { display: flex; flex-direction: column; align-items: center; width: 30%; }
.sum-title-row { display: flex; align-items: center; margin-bottom: 4px; }
.sum-label { font-size: 10px; color: $text-muted; }
.sum-val { font-size: 12px; font-weight: 600; color: $text-main; }
.highlight { color: #ef4444; }
.text-blue { color: #3b82f6; } /* 累计总额为蓝色，对应之前的设计，但现在没有曲线了，可以保留作为文字颜色 */
.text-invest { color: #2a806c; }

/* 图例圆点颜色 */
.dot { width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; }
.color-initial { background: #f97316; } /* 绿 */
.color-invest { background: #2a806c; }  /* 橙 */
.color-interest { background: #ef4444; } /* 红 */
.color-total { background: #3b82f6; }    /* 蓝 (虽然没曲线了，但总额文字还在) */

/* 图表容器 */
.chart-container { width: 100%; margin-bottom: 20px; }
.chart-title {
  font-size: 12px; font-weight: bold; color: $text-main;
  margin-bottom: 8px; padding-left: 4px; border-left: 3px solid $primary;
  line-height: 1;
}
.ucharts-box { width: 100%; height: 220px; }

/* 建议 */
.advice-box { background: #f9fafb; border-radius: 12px; padding: 14px; display: flex; align-items: flex-start; }
.advice-icon { font-size: 18px; margin-right: 10px; margin-top: 2px; }
.advice-text { font-size: 12px; color: $text-sub; line-height: 1.5; text-align: justify; flex: 1; }

.fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>