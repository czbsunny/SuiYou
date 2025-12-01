<template>
  <view class="allocation-page">
    <!-- 1. 组合信息卡片 -->
    <view class="header-card">
      <text class="label-text text-white-60">当前组合</text>
      <view class="portfolio-title-row">
          <text class="portfolio-name">{{ portfolio.name || '加载中...' }}</text>
      </view>
      <view class="portfolio-value-tag">
          <!-- 注意：配置页面主要关注比例，如果没有总市值字段，可以隐藏或显示 '--' -->
          <text>目标配置方案</text>
      </view>
    </view>

    <!-- 2. 持仓账户部分 -->
    <view class="account-section">
        <text class="section-label">关联账户</text>
        <view class="account-box">
            <view class="icon-circle">
                <image src="/static/icons/wallet.png" class="icon-sm" />
            </view>
            <text class="account-name">默认账户</text>
        </view>
    </view>

    <!-- 3. 配置明细列表 -->
    <view class="holdings-list">
        <view class="list-header">
            <text class="title-text">配置明细</text>
            <view @click="handleDiagnose" class="diagnose-btn">
                <image src="/static/images/activity.png" class="icon-xs text-primary" />
                <text>诊断</text>
            </view>
        </view>
        
        <view class="table-header-row">
            <text>基金名称</text>
            <text>目标占比</text>
        </view>
        
        <!-- 加载中 -->
        <view v-if="loading" class="status-state">
            <text class="status-text">数据加载中...</text>
        </view>

        <!-- 空状态 -->
        <view v-else-if="!holdings || holdings.length === 0" class="empty-state">
            <image src="/static/images/chart-pie-gray.png" class="empty-icon" />
            <text class="empty-text">暂无配置数据</text>
        </view>

        <!-- 列表项 -->
        <view v-else>
            <view v-for="(h, i) in holdings" :key="h.id" class="holding-item">
              <view class="item-left">
                  <!-- 颜色条：根据索引循环颜色 -->
                  <view :class="['color-bar', getColorClass(i)]"></view>
                    
                  <view class="item-info">
                      <text class="holding-name">{{ h.fundName }}</text>
                      <!-- 如果后端返回了代码则显示，否则隐藏 -->
                      <text v-if="h.fundCode" class="holding-code">{{ h.fundCode }}</text>
                  </view>
              </view>
              <!-- 直接显示计算好的百分比 -->
              <text class="holding-percent">{{ h.percentage }}%</text>
          </view>
        </view>
    </view>


    <!-- 底部按钮区 -->
    <view class="footer-bar">
        <button class="action-btn" @click="handleModify">
            <image src="/static/images/settings.png" class="btn-icon" />
            <text>修改配置</text>
        </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { get } from '../../services/apiService.js'; // 确保路径正确

// 状态定义
const portfolioId = ref('');
const loading = ref(true);
const portfolio = ref({ name: '' });
const holdings = ref([]);

// 页面加载
onLoad((options) => {
    if (options.id) {
        portfolioId.value = options.id;
        loadData();
    } else if (options.portfolio_id) {
        // 兼容你参考代码中的参数命名
        portfolioId.value = options.portfolio_id;
        loadData();
    }
});

// 加载数据
const loadData = async () => {
    loading.value = true;
    try {
        // 1. 并行请求组合详情和配置列表
        const [pRes, aRes] = await Promise.all([
            get(`/api/portfolios/${portfolioId.value}`),
            get(`/api/allocations/${portfolioId.value}`)
        ]);

        // 2. 处理组合详情
        if (pRes.statusCode === 200) {
            portfolio.value = pRes.data || { name: '未命名组合' };
        }

        // 3. 处理配置列表
        if (aRes.statusCode === 200 && Array.isArray(aRes.data)) {
            holdings.value = aRes.data.map(item => ({
                id: item.id,
                // 适配后端可能返回的字段名
                fundName: item.fundName || item.fund_name || '未知基金',
                fundCode: item.fundCode || item.fund_code || '', 
                // 将小数 (0.15) 转换为百分比字符串 (15.00)
                percentage: (Number(item.targetRatio || 0) * 100).toFixed(2)
            }));
        } else {
            holdings.value = [];
        }

    } catch (error) {
        console.error('加载数据失败:', error);
        uni.showToast({
            title: '网络请求失败',
            icon: 'none'
        });
    } finally {
        loading.value = false;
    }
};

// 辅助函数：获取颜色条的类名
const getColorClass = (index) => {
    const remainder = index % 3;
    if (remainder === 0) return 'bar-primary';
    if (remainder === 1) return 'bar-orange';
    return 'bar-blue';
};

// 跳转诊断
const handleDiagnose = () => {
    uni.navigateTo({
        url: `/components/portfolio/diagnosis?id=${portfolioId.value}`
    });
};

// 跳转修改页面
const handleModify = () => {
    uni.navigateTo({
        url: `/components/portfolio/modifyAllocation?portfolio_id=${portfolioId.value}`
    });
};
</script>

<style lang="scss" scoped>

.portfolio-detail-page {
  min-height: 100vh;
  background-color: $bg-cream;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.scroll-content {
  flex: 1;
  background-color: $bg-white;
  border-top-left-radius: 48px;
  border-top-right-radius: 48px;
  position: relative;
  z-index: 10;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

/* 顶部资产卡片 */
.header-card {
  background-color: $primary;
  border-radius: 16px;
  margin: 12px 12px;
  padding: 24px;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2);
  color: #ffffff;
}

.label-text {
  font-size: 12px;
  margin-bottom: 4px;
  display: block;
  &.text-white-60 { color: rgba(255, 255, 255, 0.6); }
}

.portfolio-title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.portfolio-name {
  font-size: 24px;
  font-weight: bold;
}

.portfolio-value-tag {
  margin-top: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  padding: 4px 8px;
  border-radius: 8px;
  display: inline-block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

/* 持仓账户区域 */
.account-section {
  background-color: $bg-white;
  margin: 12px 12px;
  border-radius: 16px;
  border-bottom: 1px dashed $border-color;
}

.section-label {
  font-size: 12px;
  color: $text-muted;
  font-weight: bold;
  padding: 12px 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  display: block;
}

.account-box {
  display: flex;
  flex-direction: row;
  align-items: center;
  background-color: $bg-subtle;
  padding: 12px;
  border-radius: 16px;
}

.icon-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: $bg-white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: $primary;
}

.icon-sm { width: 16px; height: 16px; }

.account-name {
  font-size: 14px;
  font-weight: bold;
  color: $text-sub;
}

/* 列表部分 */
.holdings-list {
  background-color: $bg-white;
  display: flex;
  flex-direction: column;
  margin: 12px 12px;
  border-radius: 16px;
}

.list-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  margin-bottom: 12px;
  padding: 0 8px;
}

.title-text {
  font-weight: bold;
  color: $text-main;
  border-left: 4px solid $primary;
  padding-left: 12px;
  font-size: 14px;
}

.diagnose-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  color: $primary;
  background-color: $bg-subtle;
  padding: 4px 8px;
  border-radius: 8px;
}

.icon-xs { width: 14px; height: 14px; margin-right: 6px; }
.text-primary { color: $primary; }

.table-header-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  color: $text-muted;
  font-size: 12px;
  padding: 0 8px;
}

/* 状态提示 */
.status-state, .empty-state {
  text-align: center;
  padding: 48px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: $bg-subtle;
  border-radius: 16px;
  border: 1px dashed #e5e7eb;
}

.status-text { font-size: 12px; color: $text-muted; }
.empty-text { font-size: 12px; color: $text-muted; }

.empty-icon {
  width: 32px;
  height: 32px;
  margin-bottom: 8px;
  opacity: 0.2;
}

/* 列表项样式 */
.holding-item {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 16px 8px;
  border-bottom: 1px solid $bg-subtle;
  border-radius: 8px;
  transition: background-color 0.2s;

  &:active {
    background-color: $bg-subtle;
  }
}

.item-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex: 1;
  margin-right: 16px;
  overflow: hidden;
}

.color-bar {
  width: 4px;
  height: 32px;
  border-radius: 9999px;
  margin-right: 12px;
  flex-shrink: 0;
}
.bar-primary { background-color: $bar-primary; }
.bar-orange { background-color: $bar-orange; }
.bar-blue { background-color: $bar-blue; }

.item-info {
  flex: 1;
  overflow: hidden;
}

.holding-name {
  color: $text-main;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.holding-code {
  font-size: 10px;
  color: $text-muted;
  font-family: monospace;
  margin-top: 2px;
  display: block;
}

.holding-percent {
  color: $text-main;
  font-family: monospace;
  font-weight: bold;
  font-size: 14px;
  margin-left: 16px;
}

/* 底部按钮栏 */
.footer-bar {
  padding: 16px;
  background-color: $bg-white;
  border-top: 1px solid $border-color;
  /* 适配安全区 */
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

.action-btn {
  width: 100%;
  padding: 14px 0;
  background-color: $primary;
  color: #ffffff;
  border-radius: 12px;
  font-weight: bold;
  box-shadow: 0 10px 15px -3px rgba(42, 128, 108, 0.2);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  border: none;
}

.btn-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
  opacity: 0.8;
}
</style>