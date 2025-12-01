<template>
  <view class="create-page">
    <!-- 图表区域 -->
    <view class="diagram-container">
        <!-- 背景环 -->
        <view class="ring-wrapper">
            <view class="ring-outer"></view>
        </view>
        <view class="ring-wrapper">
            <view class="ring-inner"></view>
        </view>

        <!-- 中心节点 (自选) -->
        <view class="center-node-wrapper">
            <view 
              @click="setSelectedLevel('CUSTOM')"
              :class="['center-node', selectedLevel === 'CUSTOM' ? 'is-active' : '']"
            >
                <image src='/static/icons/infinity-gray.png' class="center-icon" :class="selectedLevel === 'CUSTOM' ? 'icon-dark' : 'icon-light'" />
                <text class="center-text">无极·自选</text>
            </view>
        </view>

        <!-- 策略节点 (五行) -->
        <view
            v-for="(strategy, index) in orderedStrategies"
            :key="strategy.type"
            @click="setSelectedLevel(strategy.type)"
            :style="getPositionStyle(index, 5, 36, strategy.type)"
            :class="['strategy-node', selectedLevel === strategy.type ? 'is-active' : '']"
        >
            <!-- 图标颜色通过 theme 类控制 -->
            <view :class="['node-icon-box', selectedLevel === strategy.type ? `text-${strategy.themeColor}` : 'text-gray']">
                <image :src="selectedLevel === strategy.type ? strategy.iconName : strategy.grayIconName" class="node-icon" />
            </view>
            <text :class="['node-text', selectedLevel === strategy.type ? 'text-dark' : 'text-light']">
                {{ strategy.name }}
            </text>
        </view>
    </view>

    <!-- 信息展示区域 -->
    <view class="info-area">
        <!-- 自选模式卡片 -->
        <view v-if="selectedLevel === 'CUSTOM'" class="info-card fade-in">
             <view class="card-header">
                 <view class="header-icon-box bg-gray-light">
                     <image src='/static/icons/infinity.png' class="header-icon" />
                 </view>
                 <view>
                     <text class="header-title">无极 · 自选组合</text>
                     <text class="header-subtitle">自定义您的投资策略</text>
                 </view>
             </view>
             
             <view class="description-box">
                 <text class="desc-text">
                    “无极生太极，太极生两仪。”
                    在这里，您可以打破五行框架，根据个人独特的投资视角，自由从全市场基金中构建专属组合。
                 </text>
             </view>

             <view class="input-wrapper">
                 <input 
                    type="text" 
                    placeholder="给您的组合起个名字..."
                    v-model="customName"
                    class="name-input"
                    placeholder-class="input-placeholder"
                 />
             </view>

             <button 
                @click="handleCreate"
                :disabled="!customName"
                :class="['action-btn', customName ? 'btn-dark' : 'btn-disabled']"
             >
                开始构建
             </button>
        </view>

        <!-- 模板模式卡片 -->
        <view v-else-if="activeTemplate" class="info-card fade-in">
             <!-- 顶部颜色条 -->
             <view :class="['top-bar', `bg-${activeTemplate.themeColor}`]"></view>
             
             <view class="template-header">
                 <view>
                     <view class="title-row">
                        <text :class="['template-title', `text-${activeTemplate.themeColor}`]">
                            {{ activeTemplate.name }} · {{ activeTemplate.element }}
                        </text>
                     </view>
                     <text :class="['return-rate', `text-${activeTemplate.themeColor}`]">
                         {{ (12 + (activeTemplate.stars.return * 2.5)).toFixed(2) }}%
                     </text>
                     <text class="rate-label">近五年年化</text>
                 </view>
                 <text :class="['tag', `bg-${activeTemplate.themeColor}-light`, `text-${activeTemplate.themeColor}`]">
                     {{ activeTemplate.tags[0] }} · {{ activeTemplate.tags[1] }}
                 </text>
             </view>

             <view class="description-box">
                 <text class="quote-text">
                    【{{ activeTemplate.desc.split('，')[0].replace('如','').replace('般','').replace('。','') }}】
                 </text>
                 <text class="desc-text">
                    {{ activeTemplate.detail }}
                 </text>
             </view>

             <button 
                @click="handleCreate"
                :class="['action-btn', `bg-${activeTemplate.themeColor}`]"
             >
                一键创建 · {{ activeTemplate.name }}
             </button>
        </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
// 确保路径正确，根据你的项目结构调整
import { RISK_TEMPLATES, RiskLevel } from '../../constants'; 
// 假设你有 API 服务
import { createPortfolio } from '../../services/portfolioService'; 

const selectedLevel = ref('CUSTOM');
const customName = ref('');

const setSelectedLevel = (level) => {
    selectedLevel.value = level;
};

// 将颜色映射简化为主题名称，在 CSS 中处理具体色值
const mapThemeColor = (c) => {
    if (!c) return 'blue';
    if (c.includes('red')) return 'red';
    if (c.includes('yellow')) return 'yellow';
    if (c.includes('green')) return 'green';
    if (c.includes('stone')) return 'stone';
    return 'blue';
};

const orderedStrategies = [
  RISK_TEMPLATES.find(t => t.type === RiskLevel.AGGRESSIVE), 
  RISK_TEMPLATES.find(t => t.type === RiskLevel.GROWTH),     
  RISK_TEMPLATES.find(t => t.type === RiskLevel.BALANCED),   
  RISK_TEMPLATES.find(t => t.type === RiskLevel.STEADY),     
  RISK_TEMPLATES.find(t => t.type === RiskLevel.CONSERVATIVE) 
].filter(Boolean).map(t => ({ 
    ...t, 
    themeColor: mapThemeColor(t.color) 
}));

const activeTemplate = computed(() => {
    const t = RISK_TEMPLATES.find(t => t.type === selectedLevel.value);
    if(!t) return null;
    return {
        ...t,
        themeColor: mapThemeColor(t.color)
    };
});

const handleCreate = async () => {
  let payload = {};
  
  if (selectedLevel.value === 'CUSTOM') {
      if(!customName.value) return;
      payload = { 
          name: customName.value, 
          riskLevel: RiskLevel.CUSTOM, 
          tags: ['自选配置'] 
      };
  } else if (activeTemplate.value) {
      payload = { 
          name: `${activeTemplate.value.name}·组合`, 
          riskLevel: activeTemplate.value.type, 
          tags: activeTemplate.value.tags 
      };
  }
  
  uni.showLoading({ title: '创建中...' });
  
  try {
      // 调用 API 创建组合
      const res = await createPortfolio(payload);
      uni.hideLoading();
      
      if (res.success) {
          uni.showToast({ title: '创建成功', icon: 'success' });
          
          // 创建成功后跳转到详情页，或返回上一页
          // 这里演示跳转到新创建的组合详情页
          setTimeout(() => {
              uni.redirectTo({
                  url: `/pages/portfolio/detail?id=${res.data.id}`
              });
          }, 1500);
      } else {
          uni.showToast({ title: res.message || '创建失败', icon: 'none' });
      }
  } catch (error) {
      uni.hideLoading();
      uni.showToast({ title: '请求失败，请重试', icon: 'none' });
      console.error(error);
  }
};

const getBorderColor = (type) => {
    if (type === RiskLevel.AGGRESSIVE) return '#EF4444';
    if (type === RiskLevel.GROWTH) return '#CA8A04';
    if (type === RiskLevel.BALANCED) return '#16A34A';
    if (type === RiskLevel.STEADY) return '#57534E';
    return '#3B82F6';
};

const getPositionStyle = (index, total, radius, type) => {
    const angle = (index * (360 / total)) - 90;
    const radian = (angle * Math.PI) / 180;
    const x = 50 + radius * Math.cos(radian);
    const y = 50 + radius * Math.sin(radian);
    
    const style = { 
        left: `${x}%`, 
        top: `${y}%`,
    };
    
    if (selectedLevel.value === type) {
        style.borderColor = getBorderColor(type);
    }
    return style;
};
</script>

<style lang="scss" scoped>

.create-page {
  min-height: 100vh;
  background-color: $bg-cream;
  display: flex;
  flex-direction: column;
  padding-top: 16px; /* pt-4 */
  width: 100%;
}

/* 图表区域 */
.diagram-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  max-height: 800rpx;
  margin: 16px auto; /* mx-auto my-4 */
}

.ring-wrapper {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-outer {
  width: 85%;
  height: 85%;
  border: 1px solid rgba(229, 231, 235, 0.6); /* gray-200/60 */
  border-radius: 50%;
}

.ring-inner {
  width: 55%;
  height: 55%;
  border: 1px dashed #e5e7eb; /* gray-200 */
  border-radius: 50%;
}

/* 中心节点 */
.center-node-wrapper {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.center-node {
  width: 80px; /* w-20 */
  height: 80px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.5s;
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid #f3f4f6; /* gray-100 */

  &.is-active {
    background-color: $bg-white;
    transform: scale(1.1);
    border: 2px solid $text-main;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); /* shadow-xl */
  }
}

.center-icon { width: 28px; height: 28px; }
.center-icon.icon-dark { filter: brightness(0.2); } /* 模拟 text-gray-800 */
.center-icon.icon-light { filter: brightness(0.8); } /* 模拟 text-gray-300 */

.center-text {
  font-size: 11px;
  margin-top: 4px;
  font-weight: 500;
}

/* 策略节点 */
.strategy-node {
  position: absolute;
  width: 64px; /* w-16 */
  height: 64px;
  margin-left: -32px; /* -ml-8 */
  margin-top: -32px; /* -mt-8 */
  border-radius: 16px; /* rounded-2xl */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
  transition: all 0.3s;
  z-index: 20;
  background-color: $bg-white;
  border: 1px solid $bg-white;

  &.is-active {
    background-color: $bg-white;
    transform: scale(1.15) translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); /* shadow-lg */
    /* border color via style binding */
    border-width: 2px;
    border-style: solid;
  }
}

.node-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-icon { width: 24px; height: 24px; }

.node-text {
  font-size: 10px;
  margin-top: 4px;
  font-weight: bold;
}

.text-dark { color: $text-main; }
.text-light { color: #9ca3af; } /* gray-400 */
.text-gray { color: #d1d5db; } /* gray-300 */

/* 信息展示区 */
.info-area {
  flex: 1;
  padding: 0 16px 32px 16px; /* px-4 pb-8 */
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.info-card {
  background-color: $bg-white;
  border-radius: 24px;
  padding: 24px; /* p-6 */
  box-shadow: 0 10px 15px -3px rgba(243, 244, 246, 0.5); /* shadow-gray-100/50 */
  position: relative;
  overflow: hidden;
}

.fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 16px; /* mb-4 */
}

.header-icon-box {
  width: 40px; /* w-10 */
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px; /* mr-3 */
}
.bg-gray-light { background-color: $bg-subtle-hover; }

.header-icon { width: 20px; height: 20px; }

.header-title {
  font-size: 18px; /* text-lg */
  font-weight: bold;
  color: $text-main;
  display: block;
}

.header-subtitle {
  font-size: 12px; /* text-xs */
  color: $text-muted;
  display: block;
}

.description-box {
  background-color: $bg-cream;
  padding: 16px; /* p-4 */
  border-radius: 12px; /* rounded-xl */
  margin-bottom: 24px; /* mb-6 */
}

.desc-text {
  font-size: 14px; /* text-sm */
  color: $text-sub;
  line-height: 1.625; /* leading-relaxed */
  display: block;
}

.input-wrapper {
  margin-bottom: 24px; /* mb-6 */
}

.name-input {
  width: 100%;
  padding: 16px; /* p-4 */
  background-color: $bg-cream;
  border-radius: 12px; /* rounded-xl */
  color: $text-main;
  height: 48px; /* h-12 */
  font-size: 16px; /* text-base */
  box-sizing: border-box;
}

/* 按钮样式 */
.action-btn {
  width: 100%;
  padding: 16px 0; /* py-4 */
  border-radius: 12px; /* rounded-xl */
  font-weight: bold;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  transition: all 0.2s;
}

.btn-dark {
  background-color: $text-main;
  box-shadow: 0 10px 15px -3px rgba(31, 41, 55, 0.3);
}

.btn-disabled {
  background-color: #d1d5db; /* gray-300 */
}

/* 模板模式特有样式 */
.top-bar {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 6px; /* h-1.5 */
}

.template-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px; /* mb-4 */
}

.title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.template-title {
  font-size: 20px; /* text-xl */
  font-weight: bold;
}

.return-rate {
  font-size: 30px; /* text-3xl */
  font-family: monospace;
  font-weight: bold;
  margin-top: 8px; /* mt-2 */
  display: block;
}

.rate-label {
  font-size: 10px;
  color: $text-muted;
  margin-left: 4px;
  margin-top: 4px;
  display: block;
}

.tag {
  padding: 4px 12px; /* px-3 py-1 */
  border-radius: 9999px; /* rounded-full */
  font-size: 12px; /* text-xs */
  font-weight: 500;
}

.quote-text {
  font-weight: bold;
  font-size: 14px; /* text-sm */
  color: $text-main;
  margin-bottom: 4px; /* mb-1 */
  display: block;
}

/* 动态颜色类 (对应 JS 中的 mapThemeColor) */
.text-red { color: $red; }
.bg-red { background-color: $red; }
.bg-red-light { background-color: $red-light; }

.text-yellow { color: $yellow; }
.bg-yellow { background-color: $yellow; }
.bg-yellow-light { background-color: $yellow-light; }

.text-green { color: $green; }
.bg-green { background-color: $green; }
.bg-green-light { background-color: $green-light; }

.text-stone { color: $stone; }
.bg-stone { background-color: $stone; }
.bg-stone-light { background-color: $stone-light; }

.text-blue { color: $blue; }
.bg-blue { background-color: $blue; }
.bg-blue-light { background-color: $blue-light; }
</style>