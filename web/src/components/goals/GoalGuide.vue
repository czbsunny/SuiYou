<template>
  <view class="goal-selector">
    <!-- 顶部状态标题 (随步骤动态变化) -->
    <view class="header-area" :class="{ 'header-step2': currentStep === 2 }">
      <!-- 第一步：大标题 -->
      <view class="step1-header" v-if="currentStep === 1">
        <view class="headline">开启你的生活目标</view>
        <view class="sub-slogan">
          <text>明确每一阶段的财务规划</text>
          <text>让财富积累更有意义</text>
        </view>
      </view>
      
      <!-- 第二步：展示已选中的分类块 -->
      <view class="step2-header" v-else>
        <view class="selected-breadcrumb-card" @click="handleBack">
            <!-- 复用分类图标块 -->
            <view class="icon-box" :style="{ backgroundColor: selectedCat?.bgColor }">
              <image :src="selectedCat?.iconUrl" mode="aspectFit" class="cat-icon" />
            </view>
            
            <view class="info-content">
              <view class="info-name" :style="{ color: selectedCat?.bgColor }">{{ selectedCat?.name }}</view>
              <view class="info-desc">{{ selectedCat?.slogan }}</view>
            </view>

            <!-- 右侧操作引导：更温暖的提示 -->
            <view class="action-hint">
               <text>点击更换</text>
               <view class="arrow-up"></view>
            </view>
        </view>
      </view>
    </view>

    <!-- 下方滑动容器 -->
    <view class="slide-wrapper" :style="{ transform: `translateX(${currentStep === 1 ? '0' : '-50%'})` }">
      
      <!-- 第一步：分类选择列表 -->
      <view class="step-panel">
        <view class="section-label">选择一个目标分类</view>
        <view 
          v-for="cat in categories" 
          :key="cat.categoryCode" 
          class="goal-card" 
          @click="toStepTwo(cat)"
        >
          <view class="icon-box" :style="{ backgroundColor: cat.bgColor }">
            <image :src="cat.iconUrl" mode="aspectFit" class="cat-icon" />
          </view>
          <view class="info-content">
            <view class="info-name">{{ cat.name }}</view>
            <view class="info-desc">{{ cat.slogan }}</view>
          </view>
          <view class="arrow-right"></view>
        </view>
      </view>

      <!-- 第二步：具体模板选择 -->
      <view class="step-panel">
        <view class="section-label">选择具体目标模板</view>
        
        <view v-if="filteredTemplates.length === 0" class="empty-hint">
          该分类下暂无模板
        </view>

        <view 
          v-for="tpl in filteredTemplates" 
          :key="tpl.templateCode" 
          class="goal-card tpl-card" 
          @click="handleFinalSelect(tpl)"
        >
          <view class="tpl-icon-box">
            <image :src="tpl.iconUrl" mode="aspectFit" class="tpl-icon" />
          </view>
          <view class="info-content">
            <view class="info-name">
              {{ tpl.name }}
              <text v-if="tpl.isHot" class="hot-tag">HOT</text>
            </view>
            <view class="suggest-amount">建议目标 <text class="amt">¥{{ formatMoney(tpl.defaultAmount) }}</text></view>
          </view>
          <image src="/static/images/plus-gray.png" mode="aspectFit" class="plus-icon" />
        </view>
        
        <view class="safe-bottom"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  categories: { type: Array, default: () => [] },
  templates: { type: Array, default: () => [] }
});

const currentStep = ref(1);
const selectedCat = ref(null);

const filteredTemplates = computed(() => {
  if (!selectedCat.value) return [];
  // 修正：确保使用正确的 categoryCode
  return props.templates.filter(t => t.categoryCode === selectedCat.value.code);
});

const toStepTwo = (cat) => {
  selectedCat.value = cat;
  currentStep.value = 2;
  uni.pageScrollTo({ scrollTop: 0, duration: 300 });
};

const handleBack = () => {
  currentStep.value = 1;
  uni.pageScrollTo({ scrollTop: 0, duration: 300 });
};

const handleFinalSelect = (tpl) => {
  const params = [
    `tpl=${tpl.code}`,
  ].join('&');
  uni.navigateTo({ url: `/pages/goals/add?${params}` });
};

const formatMoney = (val) => Number(val).toLocaleString();
</script>

<style lang="scss" scoped>
.goal-selector { width: 100%; min-height: 100vh; background-color: #F8F7F2; }

/* 头部动态区域：增加 Step 2 时的紧凑感 */
.header-area {
  padding: 60rpx 48rpx 40rpx;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.header-step2 {
    padding: 60rpx 48rpx 20rpx;
  }
}

.step1-header {
  text-align: center;
  .headline { font-size: 56rpx; font-weight: 800; color: #1F2937; margin-bottom: 24rpx; }
  .sub-slogan { font-size: 28rpx; color: #9CA3AF; line-height: 1.6; text { display: block; } }
}

/* 核心：第二步选中的卡片样式 */
.selected-breadcrumb-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 32rpx;
  border-radius: 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.04);
  border: 1rpx solid rgba(255,255,255,0.8);
  /* 增加动画效果 */
  animation: slideInDown 0.4s ease-out;

  &:active {
    transform: scale(0.98);
    opacity: 0.8;
  }

  .action-hint {
    text-align: right;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    
    text { font-size: 20rpx; color: #9CA3AF; font-weight: 600; }
    .arrow-up {
      width: 10rpx; height: 10rpx;
      border-top: 3rpx solid #D1D1D6;
      border-left: 3rpx solid #D1D1D6;
      transform: rotate(45deg);
    }
  }
}

/* 滑动容器逻辑 */
.slide-wrapper {
  display: flex;
  width: 200%;
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.step-panel {
  width: 50%;
  padding: 20rpx 48rpx 100rpx;
  box-sizing: border-box;
}

.section-label {
  font-size: 24rpx; font-weight: 700; color: #D1D5DB;
  margin-bottom: 24rpx; padding-left: 8rpx;
  text-transform: uppercase; letter-spacing: 2rpx;
}

/* 卡片样式基础 */
.goal-card {
  background: #ffffff;
  border-radius: 40rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
  border: 1rpx solid rgba(255,255,255,0.8);
  transition: all 0.2s;
  
  &:active { transform: scale(0.97); background-color: #FAFAFA; }
}

.icon-box {
  width: 88rpx; height: 88rpx; border-radius: 28rpx;
  display: flex; align-items: center; justify-content: center;
  margin-right: 28rpx; position: relative;
  flex-shrink: 0;
  .cat-icon { width: 64rpx; height: 64rpx; z-index: 1; }
  &::after {
    content: ''; position: absolute; inset: 0;
    opacity: 0.12; border-radius: inherit; background-color: inherit;
  }
}

.tpl-icon-box {
  width: 88rpx; height: 88rpx; border-radius: 24rpx;
  background-color: #F3F4F6;
  display: flex; align-items: center; justify-content: center;
  margin-right: 28rpx; flex-shrink: 0;
  .tpl-icon { width: 64rpx; height: 64rpx; }
}

.info-content {
  flex: 1;
  overflow: hidden;
  .info-name { font-size: 32rpx; font-weight: 800; color: #1F2937; margin-bottom: 4rpx; }
  .info-desc, .suggest-amount { font-size: 24rpx; color: #9CA3AF; font-weight: 500; }
  .amt { color: $primary; font-weight: 800; margin-left: 8rpx; }
}

.hot-tag {
  background: #FF6B6B; color: white; font-size: 18rpx;
  padding: 4rpx 12rpx; border-radius: 8rpx; margin-left: 12rpx;
  vertical-align: middle;
}
.plus-icon { width: 32rpx; height: 32rpx; opacity: 0.2; }

/* 纯 CSS 箭头 */
.arrow-right {
  width: 14rpx; height: 14rpx;
  border-top: 4rpx solid #D1D1D6; border-right: 4rpx solid #D1D1D6;
  transform: rotate(45deg); margin-right: 8rpx;
}

@keyframes slideInDown {
  from { opacity: 0; transform: translateY(-20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.safe-bottom { height: 80rpx; }
.empty-hint { text-align: center; padding: 100rpx 0; color: #9CA3AF; font-size: 28rpx; }
</style>