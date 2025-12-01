<template>
  <view class="compare-page">
    <!-- 顶部固定区域：标题 -->
    <view class="page-header">
      <text class="header-title">配置优化方案</text>
      <view class="header-badges">
        <view class="badge-pill gray">优化前</view>
        <view class="icon-arrow">VS</view>
        <view class="badge-pill primary">优化后 ✨</view>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-container">
      <view class="content-wrapper">
        
        <!-- 1. 核心指标对比卡片 -->
        <view class="metrics-card">
          <!-- 表头 -->
          <view class="metrics-header">
            <text class="col-title">当前组合</text>
            <text class="col-title highlight">AI 建议</text>
          </view>
          
          <!-- 夏普比率 -->
          <view class="metric-row main-row">
            <view class="m-val">{{ metrics.sharpeRatio.toFixed(2) }}</view>
            <view class="m-label">夏普比率</view>
            <view class="m-val highlight">{{ optimizedMetrics.sharpeRatio.toFixed(2) }}</view>
          </view>

          <!-- 细分指标 -->
          <view class="divider"></view>
          <view class="sub-metrics-row">
            <view class="sub-col">
              <text class="label">波动率</text>
              <view class="vals">
                <text>{{ (metrics.volatility * 100).toFixed(0) }}%</text>
                <text class="arrow">→</text>
                <text class="highlight">{{ (optimizedMetrics.volatility * 100).toFixed(0) }}%</text>
              </view>
            </view>
            <view class="sub-col">
              <text class="label">最大回撤</text>
              <view class="vals">
                <text>{{ (metrics.maxDrawdown * 100).toFixed(0) }}%</text>
                <text class="arrow">→</text>
                <text class="highlight">{{ (optimizedMetrics.maxDrawdown * 100).toFixed(0) }}%</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 2. 持仓明细对比 (Diff View) -->
        <view class="diff-list-container">
          <view class="diff-header">
            <text class="th left">持仓调整</text>
            <text class="th right">目标权重</text>
          </view>

          <view class="diff-list">
            <view 
              v-for="row in comparisonRows" 
              :key="row.code" 
              class="diff-row"
              :class="row.type"
            >
              <!-- 左侧：优化前 (或者留白) -->
              <view class="cell left">
                <template v-if="row.type !== 'new'">
                  <view class="fund-info">
                    <text class="f-name text-ellipsis">{{ row.name }}</text>
                    <text class="f-weight">{{ (row.weightBefore * 100).toFixed(1) }}%</text>
                  </view>
                </template>
                <view v-else class="empty-placeholder"></view>
              </view>

              <!-- 中间：连接符 -->
              <view class="cell middle">
                <!-- 删除图标 -->
                <view v-if="row.type === 'removed'" class="action-icon icon-del">×</view>
                <!-- 新增图标 -->
                <view v-else-if="row.type === 'new'" class="action-icon icon-add">+</view>
                <!-- 增减箭头 -->
                <view v-else class="arrow-box">
                  <text class="arrow-line">➜</text>
                </view>
              </view>

              <!-- 右侧：优化后 (或者留白) -->
              <view class="cell right">
                <template v-if="row.type !== 'removed'">
                  <view class="fund-info">
                    <!-- 如果是新增，名字显示在右边 -->
                    <text v-if="row.type === 'new'" class="f-name text-ellipsis">{{ row.name }}</text>
                    
                    <view class="weight-wrapper">
                      <text class="f-weight highlight">{{ (row.weightAfter * 100).toFixed(1) }}%</text>
                      
                      <!-- 变化幅度小字 -->
                      <text v-if="row.type === 'increased'" class="delta up">+{{ (row.delta * 100).toFixed(1) }}%</text>
                      <text v-if="row.type === 'decreased'" class="delta down">{{ (row.delta * 100).toFixed(1) }}%</text>
                    </view>
                  </view>
                </template>
                <view v-else class="tag-removed">已剔除</view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 底部垫高 -->
        <view class="footer-spacer"></view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="footer-action">
      <view class="btn-grid">
        <button class="btn btn-cancel" @click="handleCancel">放弃调整</button>
        <button class="btn btn-confirm" @click="handleApplyChanges">
          <text class="icon-check">✓</text> 确认更新配置
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

// --- 模拟数据 ---
const metrics = ref({ sharpeRatio: 1.25, volatility: 0.18, maxDrawdown: 0.22 });
const optimizedMetrics = ref({ sharpeRatio: 1.68, volatility: 0.14, maxDrawdown: 0.15 });

const holdingsBefore = ref([
  { id: 1, code: '005827', name: '易方达蓝筹精选', holdingAmount: 50000 },
  { id: 2, code: '003095', name: '中欧医疗健康', holdingAmount: 30000 },
  { id: 3, code: '161725', name: '招商中证白酒', holdingAmount: 20000 },
]);

const holdingsAfter = ref([
  { id: 1, code: '005827', name: '易方达蓝筹精选', holdingAmount: 40000 }, // 减仓
  { id: 3, code: '161725', name: '招商中证白酒', holdingAmount: 25000 }, // 加仓
  { id: 4, code: '001668', name: '汇添富全球互联', holdingAmount: 35000 }, // 新增
]);

// --- 核心逻辑：生成 Diff 对照行 ---
const comparisonRows = computed(() => {
  const totalBefore = holdingsBefore.value.reduce((acc, h) => acc + h.holdingAmount, 0) || 1;
  const totalAfter = holdingsAfter.value.reduce((acc, h) => acc + h.holdingAmount, 0) || 1;

  // 1. 获取所有涉及的基金代码
  const allCodes = new Set([
    ...holdingsBefore.value.map(h => h.code),
    ...holdingsAfter.value.map(h => h.code)
  ]);

  const rows = [];

  allCodes.forEach(code => {
    const before = holdingsBefore.value.find(h => h.code === code);
    const after = holdingsAfter.value.find(h => h.code === code);
    
    const weightBefore = before ? (before.holdingAmount / totalBefore) : 0;
    const weightAfter = after ? (after.holdingAmount / totalAfter) : 0;
    const delta = weightAfter - weightBefore;

    let type = 'unchanged'; // unchanged, increased, decreased, new, removed
    if (!before && after) type = 'new';
    else if (before && !after) type = 'removed';
    else if (delta > 0.001) type = 'increased';
    else if (delta < -0.001) type = 'decreased';

    rows.push({
      code,
      name: before ? before.name : after.name,
      weightBefore,
      weightAfter,
      delta,
      type
    });
  });

  // 排序：删除的排最后，新增排最前，或者按权重排序
  // 这里简单按：新增 -> 修改 -> 删除 排序
  const sortOrder = { 'new': 0, 'increased': 1, 'decreased': 2, 'unchanged': 3, 'removed': 4 };
  return rows.sort((a, b) => sortOrder[a.type] - sortOrder[b.type]);
});

// --- 交互 ---
const handleCancel = () => {
  uni.navigateBack();
};

const handleApplyChanges = async () => {
  uni.showLoading({ title: '正在调仓...' });
  try {
    // API 调用伪代码
    // await api.updatePortfolio(...);
    setTimeout(() => {
      uni.hideLoading();
      uni.showToast({ title: '更新成功', icon: 'success' });
      setTimeout(() => uni.navigateBack(), 1500);
    }, 1000);
  } catch (e) {
    uni.hideLoading();
  }
};
</script>

<style lang="scss" scoped>

.compare-page {
  height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 1. 头部 */
.page-header {
  background-color: $bg-white;
  padding: 12px 16px;
  border-bottom: 1px solid $border-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    font-size: 16px;
    font-weight: bold;
    color: $text-main;
  }
  
  .header-badges {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .badge-pill {
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 12px;
    font-weight: 500;
    
    &.gray { background: #f3f4f6; color: $text-sub; }
    &.primary { background: $primary-alpha-10; color: $primary; }
  }
  
  .icon-arrow {
    font-size: 10px;
    color: #9ca3af;
    font-weight: bold;
    font-style: italic;
  }
}

.scroll-container {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
}

/* 2. 指标卡片 */
.metrics-card {
  background: $bg-white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  
  .metrics-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    .col-title {
      font-size: 12px;
      color: $text-sub;
      &.highlight { color: $primary; font-weight: 600; }
    }
  }
  
  .metric-row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    &.main-row { margin-bottom: 16px; }
    
    .m-val { font-size: 22px; font-weight: bold; font-family: monospace; }
    .m-label { font-size: 12px; color: $text-sub; }
    .highlight { color: $primary; }
  }
  
  .divider { height: 1px; background: $border-color; margin: 8px 0 12px; }
  
  .sub-metrics-row {
    display: flex;
    .sub-col {
      flex: 1;
      .label { font-size: 11px; color: #9ca3af; display: block; margin-bottom: 4px; }
      .vals {
        display: flex; align-items: center; gap: 6px;
        font-size: 13px; font-family: monospace; font-weight: 500;
        .arrow { color: #d1d5db; font-size: 10px; }
        .highlight { color: $primary; }
      }
    }
  }
}

/* 3. Diff 列表 */
.diff-list-container {
  background: $bg-white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}

.diff-header {
  display: flex;
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid $border-color;
  .th { font-size: 12px; color: $text-sub; font-weight: 500; width: 45%; }
  .th.right { text-align: right; }
}

.diff-row {
  display: flex;
  align-items: stretch; /* 让中间线撑满 */
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
  
  &:last-child { border-bottom: none; }

  /* 列布局 */
  .cell {
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    &.left { width: 42%; padding-left: 16px; }
    &.middle { width: 16%; align-items: center; }
    &.right { width: 42%; padding-right: 16px; align-items: flex-end; text-align: right; }
  }
  
  .text-ellipsis {
    overflow: hidden; white-space: nowrap; text-overflow: ellipsis; max-width: 100%;
    display: block;
  }
  
  .fund-info {
    display: flex; flex-direction: column;
    .f-name { color: $text-main; font-weight: 500; margin-bottom: 4px; }
    .f-weight { font-family: monospace; color: $text-sub; font-size: 12px; }
  }

  /* --- 状态样式 --- */
  
  /* 1. 新增 (New) - 主题色 */
  &.new {
    background-color: $primary-alpha-10; /* 整个行浅绿色背景 */
    .cell.right .f-name { color: $primary; font-weight: bold; }
    .cell.right .f-weight { color: $primary; font-weight: bold; }
    .action-icon { background: $primary; color: #fff; }
  }
  
  /* 2. 删除 (Removed) - 红色 */
  &.removed {
    background-color: $red-light; /* 整个行浅红色背景 */
    .cell.left { opacity: 0.7; }
    .cell.left .f-name { text-decoration: line-through; color: $red; }
    .cell.left .f-weight { text-decoration: line-through; color: $red; }
    .tag-removed { font-size: 10px; color: $red; background: #fff; padding: 2px 6px; border-radius: 4px; opacity: 0.8; }
    .action-icon { background: $red; color: #fff; }
  }
  
  /* 3. 调仓 (Modified) */
  &.increased, &.decreased, &.unchanged {
    .weight-wrapper { display: flex; flex-direction: column; align-items: flex-end; }
    .highlight { font-weight: bold; color: $text-main; }
    .delta { font-size: 10px; margin-top: 2px; }
    .delta.up { color: $red; } /* 涨为红 */
    .delta.down { color: $primary; } /* 跌为绿 (根据国内习惯，或者反过来) */
    
    .arrow-box { 
      color: #d1d5db; 
      font-size: 12px; 
      display: flex; 
      align-items: center; 
    }
  }
  
  /* 图标样式 */
  .action-icon {
    width: 20px; height: 20px;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-size: 14px; line-height: 1;
    font-weight: bold;
  }
}

.footer-spacer { height: 80px; }

/* 底部按钮 */
.footer-action {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: $bg-white; padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid $border-color;
  z-index: 99;
  
  .btn-grid { display: grid; grid-template-columns: 1fr 2fr; gap: 12px; }
  
  .btn {
    border-radius: 40px; font-size: 14px; font-weight: 600; height: 44px;
    display: flex; align-items: center; justify-content: center; border: none;
    &::after { border: none; }
  }
  
  .btn-cancel { background: #f3f4f6; color: $text-sub; }
  .btn-confirm { background: $primary; color: #fff; box-shadow: 0 4px 10px rgba(42, 128, 108, 0.3); }
  .icon-check { margin-right: 4px; }
}
</style>