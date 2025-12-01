<template>
  <view class="holdings-panel">
    <!-- 头部标题区 -->
    <view class="panel-header">
      <view class="section-title">
        持仓明细
      </view>
      <view class="header-actions">
         <view @click="$emit('analyze')" class="analyze-btn">
            <image src="/static/images/chart-pie-gray.png" class="icon-sm mr-1" />
            <text>资产配置</text>
         </view>
      </view>
    </view>

    <!-- 表头 -->
    <view class="table-header">
      <view class="col-main">基金名称</view>
      <view class="col-right">当日涨幅</view>
      <view class="col-right">持有收益</view>
    </view>

    <!-- 空状态 -->
    <view v-if="!holdings || holdings.length === 0" class="empty-state">
      <view class="empty-icon-bg">
        <image src="/static/icons/wallet.png" class="icon-lg" />
      </view>
      <text class="empty-text">暂无持仓数据</text>
      <button @click="$emit('import')" class="btn-import-small">
        导入持有基金
      </button>
    </view>

    <!-- 持仓列表 -->
    <view v-else class="holding-list">
      <view v-for="h in holdings" :key="h.id" class="holding-item">
        <view class="grid-row">
          <!-- 基金名称与代码 -->
          <view class="col-main">
             <text class="holding-name">{{ h.name }}</text>
             <view class="holding-code-tag">
               <text>{{ h.code }}</text>
             </view>
          </view>
          
          <!-- 当日收益 -->
          <view class="col-right">
             <text :class="['value-text', h.dailyReturn >= 0 ? 'text-gain' : 'text-loss']">
              {{ h.dailyReturn > 0 ? '+' : '' }}{{ formatNumber(h.dailyReturn) }}
             </text>
             <text :class="['rate-text', h.dailyReturnRate >= 0 ? 'sub-gain' : 'sub-loss']">
               {{ h.dailyReturnRate > 0 ? '+' : '' }}{{ formatPercent(h.dailyReturnRate) }}
             </text>
          </view>

          <!-- 持有收益 -->
          <view class="col-right">
             <text :class="['value-text', h.returnValue >= 0 ? 'text-gain' : 'text-loss']">
               {{ h.returnValue > 0 ? '+' : '' }}{{ formatNumber(h.returnValue) }}
             </text>
             <text class="rate-text text-gray">
               {{ formatPercent(h.returnRate) }}
             </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部大按钮 -->
    <view v-if="holdings && holdings.length > 0" class="footer-actions">
       <button @click="$emit('import')" class="btn-import-block">
           导入我的持有基金
       </button>
       <text class="footer-tip">
           已支持支付宝、天天基金、腾讯理财通、雪球基金等平台的一键导入
       </text>
    </view>
  </view>
</template>

<script setup>
import { formatNumber, formatPercent } from '../../services/formatUtil';
defineProps(['account', 'holdings']);
defineEmits(['import', 'analyze']);
</script>

<style lang="scss" scoped>
.holdings-panel {
  background-color: $bg-white;
  border-top-left-radius: 16px; /* rounded-t-2xl */
  border-top-right-radius: 16px;
  min-height: 50vh;
  padding: 12px 16px; /* py-3 px-4 */
  position: relative;
  z-index: 10;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* mb-4 */
}

.section-title {
  font-size: 18px; /* text-lg */
  font-weight: bold;
  color: $text-main;
  border-left: 4px solid $primary;
  padding-left: 12px; /* pl-3 */
}

.analyze-btn {
  font-size: 12px; /* text-xs */
  display: flex;
  align-items: center;
  color: $text-sub;
  background-color: $bg-page;
  padding: 6px 12px; /* px-3 py-1.5 */
  border-radius: 9999px; /* rounded-full */
  transition: all 0.3s;

  &:active, &:hover {
    background-color: rgba($primary, 0.1);
    color: $primary;
  }
}

.mr-1 { margin-right: 4px; }

.icon-sm { width: 12px; height: 12px; }
.icon-lg { width: 24px; height: 24px; }

/* 表格布局通用 Grid */
.grid-row, .table-header {
  display: grid;
  /* 2:1:1 比例，模拟 col-span-2 */
  grid-template-columns: 2fr 1fr 1fr; 
  gap: 8px; /* px-2 gutter */
  align-items: center;
}

.table-header {
  font-size: 12px; /* text-xs */
  color: $text-muted;
  margin-bottom: 8px; /* mb-2 */
  padding: 0 8px; /* px-2 */
}

.col-main {
  /* 默认左对齐 */
  overflow: hidden; /* 防止文字溢出撑开 */
}

.col-right {
  text-align: right;
}

/* 列表项样式 */
.holding-list {
  display: flex;
  flex-direction: column;
  gap: 4px; /* space-y-1 */
}

.holding-item {
  background-color: $bg-white;
  border-bottom: 1px solid $border-color;
  padding: 8px 8px; /* py-2 px-2 */
  border-radius: 8px;
  transition: background-color 0.2s;

  &:active {
    background-color: $bg-page;
  }
}

.holding-name {
  font-size: 14px; /* text-sm */
  font-weight: 500;
  color: $text-main;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-right: 8px;
}

.holding-code-tag {
  display: inline-block;
  font-size: 10px;
  color: $text-muted;
  background-color: $bg-page;
  padding: 2px 4px;
  border-radius: 4px;
  margin-top: 4px;
}

.value-text {
  display: block;
  font-size: 14px; /* text-sm */
  font-weight: 500;
  
  &.text-gain { color: $color-gain; }
  &.text-loss { color: $color-loss; }
}

.rate-text {
  display: block;
  font-size: 10px;
  margin-top: 2px;
  opacity: 0.8;

  &.sub-gain { color: $color-gain; }
  &.sub-loss { color: $color-loss; }
  &.text-gray { color: $text-muted; }
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0; /* py-10 */
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon-bg {
  width: 64px; /* w-16 */
  height: 64px;
  background-color: $bg-page;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px; /* mb-3 */
  color: $text-muted; /* text-gray-300 */
}

.empty-text {
  color: $text-muted;
  font-size: 14px;
  margin-bottom: 24px; /* mb-6 */
}

.btn-import-small {
  background-color: $primary;
  color: #fff;
  padding: 8px 24px;
  border-radius: 9999px;
  font-size: 14px;
  box-shadow: 0 10px 15px -3px rgba($primary, 0.3);
  border: none;
}

/* 底部操作区 */
.footer-actions {
  margin-top: 24px; /* mt-8 */
}

.btn-import-block {
  width: 100%;
  padding: 12px 0; /* py-3 */
  background-color: $primary;
  color: #fff;
  border-radius: 12px; /* rounded-xl */
  font-weight: bold;
  box-shadow: 0 10px 15px -3px rgba($primary, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }
}

.footer-tip {
  display: block;
  text-align: center;
  font-size: 10px;
  color: $text-muted;
  margin-top: 8px;
}
</style>