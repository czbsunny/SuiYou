<template>
  <view class="portfolio-page">
        <!-- 顶部总资产卡片 -->
        <view class="summary-section">
            <view class="summary-card">
                 <!-- 背景装饰 -->
                 <view class="bg-decoration-1"></view>
                 <view class="bg-decoration-2"></view>

                 <!-- 卡片上部：资产信息 -->
                 <view class="card-top">
                     <view class="total-assets-box">
                         <view class="label-row">
                            <text class="label-text">总资产 (元)</text>
                            <image src="/static/images/eye.png" class="eye-icon" />
                         </view>
                         <text class="amount-text">
                             {{ formatCurrency(summary.totalValue) }}
                         </text>
                     </view>

                     <view class="income-row">
                         <view class="income-item">
                             <text class="income-label">日收益</text>
                             <text :class="['income-val', summary.dailyReturn >= 0 ? 'text-gain-light' : 'text-loss-light']">
                                 {{ summary.dailyReturn > 0 ? '+' : '' }}{{ formatNumber(summary.dailyReturn) }}
                             </text>
                         </view>
                         <view class="income-item border-left">
                             <text class="income-label">累计收益</text>
                             <text :class="['income-val', summary.totalReturn >= 0 ? 'text-gain-light' : 'text-loss-light']">
                                 {{ summary.totalReturn > 0 ? '+' : '' }}{{ formatNumber(summary.totalReturn) }}
                             </text>
                         </view>
                     </view>
                 </view>

                 <view class="divider"></view>

                 <!-- 卡片下部：透视分析 -->
                 <view class="card-bottom">
                     <view class="analysis-header">
                         <view class="analysis-title">
                            <image src="/static/images/chart-pie.png" class="analysis-icon" /> 
                            <text>全组合底层透视</text>
                         </view>
                         <image src="/static/images/chevron-right.png" class="arrow-icon-light" />
                     </view>

                     <!-- 资产配置进度条 -->
                     <view class="progress-bar-container">
                         <view :style="{ width: `${macroAlloc.stock}%` }" class="bar-segment bg-stock"></view>
                         <view :style="{ width: `${macroAlloc.bond}%` }" class="bar-segment bg-bond"></view>
                         <view :style="{ width: `${macroAlloc.commodity}%` }" class="bar-segment bg-commodity"></view>
                         <view :style="{ width: `${macroAlloc.cash}%` }" class="bar-segment bg-cash"></view>
                         <view :style="{ width: `${macroAlloc.other}%` }" class="bar-segment bg-other"></view>
                     </view>

                     <view class="legend-row">
                         <view v-for="item in allocItems" :key="item.label" class="legend-item">
                             <view :class="['legend-dot', item.colorKey]"></view>
                             <text>{{ item.val.toFixed(0) }}%</text>
                         </view>
                     </view>
                 </view>
            </view>
        </view>
        
        <!-- 组合列表区域 -->
        <view class="list-section">
            <view class="section-header">
                <text class="section-title">
                    组合列表
                </text>
                <view @click="handleCreate" class="create-btn">
                    <image src="/static/images/plus-blue.png" class="btn-icon" />
                    <text>新组合</text>
                </view>
            </view>

            <view class="portfolio-list">
                <!-- 空状态 -->
                <view v-if="portfolios.length === 0" class="empty-state">
                    <view class="empty-icon-box">
                        <image src="/static/images/chart-pie-gray.png" class="empty-img" />
                    </view>
                    <text class="empty-text">还没有组合，点击右上角创建</text>
                </view>
                
                <!-- 列表项 -->
                <view 
                    v-else
                    v-for="p in portfolios"
                    :key="p.id" 
                    @click="openDetail(p.id)"
                    class="portfolio-card"
                >
                   <view class="p-card-header">
                       <view class="p-info">
                           <!-- 风险等级图标 -->
                           <view :class="['risk-icon-box', getRiskClass(p.riskLevel).bgLight, getRiskClass(p.riskLevel).text]">
                               <image src="/static/icons/wallet.png" class="risk-icon" />
                           </view>
                           <view class="p-text-col">
                               <text class="p-name">{{ p.name }}</text>
                               <view class="p-tags">
                                   <text class="tag-risk">
                                       {{ getRiskClass(p.riskLevel).name }}
                                   </text>
                                   <text v-if="p.tags && p.tags.length > 0" class="tag-custom">#{{ p.tags[0] }}</text>
                               </view>
                           </view>
                       </view>
                       <image src="/static/images/chevron-right.png" class="arrow-icon-gray" />
                   </view>

                   <view class="p-data-grid">
                       <view class="data-col">
                           <text class="data-label">总资产</text>
                           <text class="data-val-main">
                              {{formatCurrency(p.currentValue) }}
                           </text>
                       </view>
                       
                       <view class="data-col align-right">
                           <text class="data-label">累计收益</text>
                           <text :class="['data-val-main', p.totalReturn >= 0 ? 'text-gain' : 'text-loss']">
                               {{ p.totalReturn > 0 ? '+' : '' }}{{ formatNumber(p.totalReturn)}}
                           </text>
                       </view>

                       <view class="data-footer">
                           <view class="footer-left">
                              <view class="footer-item">
                                  <text class="footer-label">日收益</text>
                                  <text :class="['footer-val', p.dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
                                      {{ p.dailyReturn > 0 ? '+' : '' }}{{ formatNumber(p.dailyReturn) }}
                                  </text>
                              </view>
                              <view class="footer-item ml-large">
                                  <text class="footer-label">收益率</text>
                                  <text :class="['footer-val', p.dailyReturnRate >= 0 ? 'text-gain' : 'text-loss']">
                                      {{ p.dailyReturnRate > 0 ? '+' : '' }}{{ formatPercent(p.dailyReturnRate) }}
                                  </text>
                              </view>
                           </view>
                           
                           <text class="update-time">
                               更新于 15:00
                           </text>
                       </view>
                   </view>

                   <!-- 背景装饰圆 -->
                   <view :class="['bg-blob', getRiskClass(p.riskLevel).bgLight]"></view>
                </view>
            </view>
        </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getPortfolios, getPortfolioSummary } from '../../services/portfolioService';
import { RiskLevel, RISK_TEMPLATES } from '../../constants';
import { formatCurrency, formatNumber, formatPercent } from '../../services/formatUtil';

const portfolios = ref([]);

// 加载数据
const loadData = async () => {
  const res = await getPortfolios();
  if (res.success) {
      portfolios.value = res.data;
  } else {
      uni.showToast({ title: '加载组合失败', icon: 'none' });
  }
};

// 每次显示页面时刷新数据
onShow(loadData);

// 跳转创建页面
const handleCreate = () => {
    uni.navigateTo({
        url: '/components/portfolio/create'
    });
};

// 跳转详情页 (同目录下的 detail 页面)
const openDetail = (id) => {
    // 检查组合是否存在
    const portfolio = portfolios.value.find(p => p.id === id);
    if (!portfolio) {
        uni.showToast({ title: '组合不存在', icon: 'none' });
        return;
    }

    uni.setStorageSync('cachedPortfolio', portfolio);
    
    uni.navigateTo({
        url: `/components/portfolio/detail?id=${id}`
    });
};

// 获取风险等级对应的样式
const getRiskClass = (risk) => {
    const t = RISK_TEMPLATES.find(t => t.type === risk) || RISK_TEMPLATES.find(t => t.type === RiskLevel.BALANCED);
    
    // 映射颜色逻辑到 SCSS 类
    let colorType = 'blue';
    if (t.color && t.color.includes('red')) colorType = 'red';
    else if (t.color && t.color.includes('yellow')) colorType = 'yellow';
    else if (t.color && t.color.includes('green')) colorType = 'green';
    else if (t.color && t.color.includes('stone')) colorType = 'stone';

    return {
        name: t.name,
        text: `text-${colorType}`,
        bgLight: `bg-${colorType}-light`
    };
};

const summary = computed(() => getPortfolioSummary(portfolios.value));

const macroAlloc = computed(() => {
    let totalStock = 0, totalBond = 0, totalCommodity = 0, totalCash = 0, totalOther = 0;
    const tVal = summary.value.totalValue;

    if (tVal === 0) return { stock: 0, bond: 0, commodity: 0, cash: 0, other: 0 };

    portfolios.value.forEach(p => {
        const alloc = p.assetAllocation || { stock: 0, bond: 0, commodity: 0, cash: 100, other: 0 };
        const pVal = p.totalValue || 0;
        totalStock += (pVal * alloc.stock);
        totalBond += (pVal * alloc.bond);
        totalCommodity += (pVal * (alloc.commodity || 0));
        totalCash += (pVal * alloc.cash);
        totalOther += (pVal * alloc.other);
    });

    return {
        stock: (totalStock / tVal) || 0,
        bond: (totalBond / tVal) || 0,
        commodity: (totalCommodity / tVal) || 0,
        cash: (totalCash / tVal) || 0,
        other: (totalOther / tVal) || 0
    };
});

// 使用 colorKey 对应 SCSS 里的颜色类
const allocItems = computed(() => [
     { label: '权益', val: macroAlloc.value.stock, colorKey: 'bg-stock' },
     { label: '固收', val: macroAlloc.value.bond, colorKey: 'bg-bond' },
     { label: '商品', val: macroAlloc.value.commodity, colorKey: 'bg-commodity' },
     { label: '现金', val: macroAlloc.value.cash, colorKey: 'bg-cash' },
 ]);
</script>

<style lang="scss" scoped>
/* 动态风险等级颜色 */
.text-red { color: #ef4444; }
.bg-red-light { background-color: #fef2f2; }
.text-yellow { color: #ca8a04; }
.bg-yellow-light { background-color: #fefce8; }
.text-green { color: #16a34a; }
.bg-green-light { background-color: #f0fdf4; }
.text-stone { color: #57534e; }
.bg-stone-light { background-color: #fafaf9; }
.text-blue { color: #3b82f6; }
.bg-blue-light { background-color: #eff6ff; }

/* 资产配置条颜色 */
.bg-stock { background-color: $color-stock; }
.bg-bond { background-color: $color-bond; }
.bg-commodity { background-color: $color-commodity; }
.bg-cash { background-color: $color-cash; }
.bg-other { background-color: $color-other; }

.portfolio-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 128px; /* pb-32 */
  padding-top: 12px; /* pt-6 */
  position: relative;
  width: 100%;
}

/* 总资产卡片 */
.summary-section {
  padding: 0 20px; /* px-5 */
  margin-bottom: 12px; /* mb-8 */
}

.summary-card {
  background-color: $primary;
  border-radius: 28px;
  box-shadow: 0 20px 25px -5px rgba(42, 128, 108, 0.2);
  overflow: hidden;
  position: relative;
}

/* 背景装饰球 */
.bg-decoration-1 {
  position: absolute;
  top: -64px;
  right: -64px;
  width: 256px;
  height: 256px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  filter: blur(64px);
  pointer-events: none;
}

.bg-decoration-2 {
  position: absolute;
  bottom: -48px;
  left: -48px;
  width: 192px;
  height: 192px;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
}

.card-top {
  padding: 24px; /* p-6 */
  padding-bottom: 20px; /* pb-5 */
  position: relative;
  z-index: 10;
}

.total-assets-box {
  margin-bottom: 12px; /* mb-6 */
}

.label-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 4px; /* mb-1 */
}

.label-text {
  font-size: 12px; /* text-xs */
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.eye-icon {
  width: 14px;
  height: 14px;
  margin-left: 8px; /* ml-2 */
  opacity: 0.4;
}

.amount-text {
  font-size: 36px; /* text-6xl */
  font-weight: bold;
  color: #ffffff;
  font-family: monospace;
  letter-spacing: -0.05em; /* tracking-tighter */
  line-height: 1;
}

.income-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.income-item {
  &.border-left {
    padding-left: 32px; /* pl-8 */
    margin-left: 32px; /* ml-8 */
    border-left: 1px solid rgba(255, 255, 255, 0.1);
  }
}

.income-label {
  font-size: 12px; /* text-xs */
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

.income-val {
  font-size: 14px; /* text-sm */
  font-family: monospace;
  font-weight: 500;
  display: flex;
  flex-direction: row;
  align-items: center;
}

/* 涨跌色 (亮色背景) */
.text-gain-light { color: $text-gain-light; }
.text-loss-light { color: $text-loss-light; }
.text-gain { color: $text-gain; }
.text-loss { color: $text-loss; }

.divider {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
  margin: 0 24px; /* mx-6 */
}

.card-bottom {
  padding: 20px; /* p-5 */
  padding-top: 16px; /* pt-4 */
  background-color: $primary-dark;
  position: relative;
  z-index: 10;
}

.analysis-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px; /* mb-3 */
}

.analysis-title {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
}

.analysis-icon {
  width: 12px;
  height: 12px;
  margin-right: 6px; /* mr-1.5 */
}

.arrow-icon-light {
  width: 12px;
  height: 12px;
  opacity: 0.4;
}

.progress-bar-container {
  height: 10px; /* h-2.5 */
  display: flex;
  flex-direction: row;
  border-radius: 9999px;
  overflow: hidden;
  background-color: rgba(0, 0, 0, 0.2);
  margin-bottom: 12px; /* mb-3 */
}

.bar-segment {
  height: 100%;
  /* stock specific shadow */
  &.bg-stock {
    box-shadow: 0 0 10px rgba(96, 165, 250, 0.4);
  }
}

.legend-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 10px; /* text-[10px] */
  color: rgba(255, 255, 255, 0.6);
}

.legend-item {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.legend-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

/* 列表区域 */
.list-section {
  padding: 0 20px; /* px-5 */
}

.section-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px; /* mb-3 */
  padding: 0 4px; /* px-1 */
}

.section-title {
  font-weight: bold;
  color: $text-main;
  font-size: 18px; /* text-lg */
  display: flex;
  align-items: center;
}

.create-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 12px; /* text-xs */
  font-weight: bold;
  color: $primary;
  background-color: rgba(42, 128, 108, 0.1); /* bg-primary/10 */
  padding: 6px 12px; /* px-3 py-1.5 */
  border-radius: 9999px;
  transition: all 0.3s;

  &:active {
    background-color: $primary;
    color: #ffffff;
  }
}

.btn-icon {
  width: 14px;
  height: 14px;
  margin-right: 4px;
}

/* 组合列表 */
.portfolio-list {
  display: flex;
  flex-direction: column;
  gap: 16px; /* space-y-4 */
  padding-bottom: 40px; /* pb-10 */
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 64px 0; /* py-16 */
  background-color: $bg-white;
  border-radius: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px dashed #e5e7eb; /* border-gray-200 */
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon-box {
  width: 48px;
  height: 48px;
  background-color: $bg-subtle;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #d1d5db; /* gray-300 */
}

.empty-img { width: 24px; height: 24px; }

.empty-text {
  color: $text-muted;
  font-size: 14px;
}

/* 组合卡片 */
.portfolio-card {
  background-color: $bg-white;
  border-radius: 24px;
  padding: 20px; /* p-5 */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  position: relative;
  overflow: hidden;
}

.p-card-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px; /* mb-5 */
}

.p-info {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.risk-icon-box {
  width: 48px; /* w-12 */
  height: 48px;
  border-radius: 16px; /* rounded-2xl */
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  margin-right: 12px; /* space-x-3 logic */
}

.risk-icon { width: 22px; height: 22px; }

.p-text-col {
  margin-left: 12px; /* ml-3 */
}

.p-name {
  font-weight: bold;
  color: $text-main;
  font-size: 18px; /* text-lg */
  line-height: 1;
  margin-bottom: 6px; /* mb-1.5 */
  display: block;
}

.p-tags {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 4px;
}

.tag-risk {
  font-size: 10px; /* text-[10px] */
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 9999px;
  background-color: $bg-subtle-hover;
  color: $text-sub;
}

.tag-custom {
  font-size: 10px;
  color: $text-muted;
  margin-left: 8px; /* ml-2 */
}

.arrow-icon-gray {
  width: 18px;
  height: 18px;
}

/* 网格数据区 */
.p-data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 32px; /* gap-x-8 */
  row-gap: 8px; /* gap-y-2 */
}

.data-col {
  &.align-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    text-align: right;
  }
}

.data-label {
  font-size: 11px; /* text-[11px] */
  color: $text-muted;
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

.data-val-main {
  font-size: 16px; /* text-base */
  font-weight: bold;
  font-family: monospace;
  color: $text-main;
  display: block;
}

.data-footer {
  grid-column: span 2;
  margin-top: 8px; /* mt-2 */
  padding-top: 12px; /* pt-3 */
  border-top: 1px solid $bg-subtle;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.footer-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 11px;
  color: $text-muted;
  
  &.ml-large { margin-left: 16px; }
}

.footer-label { margin-right: 4px; }

.footer-val {
  font-weight: 500;
  margin-left: 4px;
}

.update-time {
  font-size: 10px;
  color: #d1d5db; /* gray-300 */
}

/* 装饰背景 */
.bg-blob {
  position: absolute;
  right: -24px;
  bottom: -24px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  opacity: 0.2;
  filter: blur(40px);
  pointer-events: none;
}
</style>