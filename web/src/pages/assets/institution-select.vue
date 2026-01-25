<template>
  <view class="container">
    <!-- 1. 顶部搜索与分类区域 -->
    <view class="header-sticky">
      <view class="search-section">
        <view class="search-bar">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input type="text" v-model="searchText" placeholder="搜索机构名称" class="search-input" />
          <uni-icons v-if="searchText" type="closeempty" size="18" color="#ccc" @tap="searchText = ''"></uni-icons>
        </view>
      </view>

      <!-- 🟢 业态切换 Tab：横向滑动修正版 -->
      <view class="tabs-section" v-if="!subCode && institutionTypes.length > 1">
        <scroll-view 
          scroll-x 
          class="tabs-scroll" 
          :show-scrollbar="false" 
          :enhanced="true"
        >
          <!-- 核心：这个 view 必须是 inline-block 或 inline-flex -->
          <view class="tabs-content">
            <view 
              v-for="type in institutionTypes" 
              :key="type.code"
              class="tab-item"
              :class="{ 'active': activeType === type.code }"
              @tap="handleTypeChange(type.code)"
            >
              <text class="tab-text">{{ type.name }}</text>
              <view class="tab-line" v-if="activeType === type.code"></view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 2. 主体列表区 (代码保持不变) -->
    <scroll-view 
      class="list-scroll" 
      scroll-y 
      :scroll-into-view="scrollIntoId" 
      scroll-with-animation
      @scroll="onListScroll"
    >
      <view v-if="searchText" class="search-results">
        <view v-for="item in filteredList" :key="item.id" class="institution-item" @click="selectInstitution(item)">
          <view class="logo-wrapper">
            <image :src="item.logoUrl || '/static/icons/default-bank.png'" class="institution-logo" mode="aspectFit" />
          </view>
          <text class="institution-name">{{ item.instName }}</text>
        </view>
        <view v-if="filteredList.length === 0" class="empty-tip">未找到相关机构</view>
      </view>

      <view v-else id="scroll-content">
        <view 
          v-for="group in groupedInstitutions" 
          :key="group.indexLetter" 
          :id="formatId(group.indexLetter)"
          class="group-block"
        >
          <view class="group-title">{{ group.indexLetter === '热' ? '热门机构' : group.indexLetter }}</view>
          <view v-for="item in group.data" :key="item.id" class="institution-item" @click="selectInstitution(item)">
            <view class="logo-wrapper">
              <image :src="item.logoUrl || '/static/icons/default-bank.png'" class="institution-logo" mode="aspectFit" />
            </view>
            <text class="institution-name">{{ item.instName }}</text>
          </view>
        </view>
        <view class="safe-bottom"></view>
      </view>
    </scroll-view>

    <!-- 3. 右侧索引条 (代码保持不变) -->
    <view class="index-bar" v-if="!searchText">
      <view 
        v-for="letter in alphabet" 
        :key="letter" 
        class="index-item"
        :class="{ 'active-letter': activeLetter === letter }"
        @touchstart="scrollToLetter(letter)"
      >
        {{ letter }}
      </view>
    </view>
  </view>
</template>

<script setup>
// ... JS 逻辑完全保持不变，重点在下方的 CSS ...
import { ref, computed, nextTick } from 'vue';
import { onLoad, onReady } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

const configStore = useConfigStore();
const searchText = ref('');
const scrollIntoId = ref('');
const activeLetter = ref('热');
const subCode = ref('');
const groupOffsets = ref([]);
const activeType = ref('ALL');

onLoad((options) => {
  if (options.subCode) subCode.value = options.subCode;
});

const institutionTypes = computed(() => {
  const types = [...new Set(configStore.institutionData.map(i => i.instType))];
  const mapped = types.map(t => ({
    code: t,
    name: ASSET_INSTITUTION_DISPLAY[t]?.name || '其他'
  }));
  return [{ code: 'ALL', name: '全部' }, ...mapped];
});

const rawInstitutions = computed(() => {
  if (subCode.value) return configStore.getInstitutionsBySubCategoryCode(subCode.value);
  if (activeType.value === 'ALL') return configStore.institutionData;
  return configStore.institutionData.filter(i => i.instType === activeType.value);
});

const handleTypeChange = (code) => {
  if (activeType.value === code) return;
  activeType.value = code;
  scrollIntoId.value = '';
  activeLetter.value = '热';
  nextTick(() => { setTimeout(() => calculateOffsets(), 300); });
};

const formatId = (letter) => letter === '热' ? 'letter-HOT' : `letter-${letter}`;

const groupedInstitutions = computed(() => {
  const groups = {};
  const hotList = [];
  rawInstitutions.value.forEach(item => {
    if (item.isHot) hotList.push(item);
    const letter = (item.indexLetter || '#').toUpperCase(); 
    if (!groups[letter]) groups[letter] = [];
    groups[letter].push(item);
  });
  const result = Object.keys(groups).sort().map(key => ({ indexLetter: key, data: groups[key] }));
  if (hotList.length > 0) result.unshift({ indexLetter: '热', data: hotList });
  return result;
});

const alphabet = computed(() => groupedInstitutions.value.map(g => g.indexLetter));

const filteredList = computed(() => {
  const kw = searchText.value.trim().toLowerCase();
  if (!kw) return [];
  return configStore.institutionData.filter(item => item.instName.toLowerCase().includes(kw));
});

const scrollToLetter = (letter) => {
  const targetId = formatId(letter);
  scrollIntoId.value = ''; 
  nextTick(() => {
    scrollIntoId.value = targetId;
    activeLetter.value = letter;
    uni.vibrateShort();
  });
};

const calculateOffsets = () => {
  const query = uni.createSelectorQuery();
  query.selectAll('.group-block').boundingClientRect(rects => {
    if(!rects.length) return;
    const baseTop = rects[0].top;
    groupOffsets.value = rects.map((rect, index) => ({
      top: rect.top,
      offset: rect.top - baseTop,
      letter: alphabet.value[index]
    }));
  }).exec();
};

const onListScroll = (e) => {
  if (!groupOffsets.value.length) return;
  const scrollTop = e.detail.scrollTop + 20; 
  for (let i = groupOffsets.value.length - 1; i >= 0; i--) {
    if (scrollTop >= groupOffsets.value[i].offset) {
      if (activeLetter.value !== groupOffsets.value[i].letter) {
        activeLetter.value = groupOffsets.value[i].letter;
      }
      break;
    }
  }
};

onReady(() => { setTimeout(() => calculateOffsets(), 800); });

const selectInstitution = (institution) => {
  uni.$emit('institutionSelected', institution);
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background-color: #f7f7f7; }

.header-sticky {
  background-color: #fff;
  position: sticky; // 确保使用 sticky
  top: 0;
  z-index: 200;    /* 头部层级设为 200 */
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
}

.search-section { padding: 24rpx 32rpx 16rpx; }
.search-bar { 
  display: flex; align-items: center; background-color: #f3f4f6; 
  padding: 0 24rpx; border-radius: 40rpx; height: 84rpx; 
  .search-input { flex: 1; margin-left: 16rpx; font-size: 28rpx; color: #1e293b; }
}

/* 🟢 彻底修复文字垂直问题的 CSS 方案 */

.tabs-section {
  width: 100%;
  background-color: #fff;
  border-bottom: 1rpx solid #f1f5f9;

  .tabs-scroll { 
    width: 100%; 
    /* 核心 1：强制内部不换行 */
    white-space: nowrap; 
  }

  .tabs-content { 
    /* 核心 2：使用 flex 且不限制宽度 */
    display: inline-flex; 
    padding: 0 32rpx;
    height: 96rpx;
    align-items: center;
  }

  .tab-item {
    /* 核心 3：行内块显示，并保持相对定位用于放置下划线 */
    display: inline-block; 
    padding: 0 18rpx;
    position: relative;
    height: 100%;
    /* 垂直居中内容 */
    line-height: 96rpx;
    
    .tab-text {
      font-size: 28rpx;
      color: #94a3b8;
      font-weight: 600;
      /* 核心 4：防止文字自身换行 */
      white-space: nowrap; 
      display: block;
      transition: all 0.3s ease;
    }
    
    &.active {
      .tab-text {
        color: #2D7A68;
        font-weight: 800;
      }
      .tab-line {
        position: absolute;
        bottom: 8rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 32rpx;
        height: 6rpx;
        background-color: #2D7A68;
        border-radius: 4rpx;
      }
    }
  }
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}

/* 其他列表样式... */
.list-scroll { flex: 1; overflow: hidden; }

.group-title {
  /* 标题也增加右侧留白 */
  padding: 16rpx 60rpx 16rpx 32rpx; 
  font-size: 22rpx; 
  font-weight: 800; 
  color: #94a3b8; 
  background-color: #f8fafc; 
  text-transform: uppercase; 
  letter-spacing: 2rpx; 
}

.institution-item {
  /* 增加右侧 padding，防止文字被字母栏挡住 */
  padding: 32rpx 60rpx 32rpx 32rpx; 
  display: flex; 
  align-items: center; 
  background-color: #fff; 
  border-bottom: 1rpx solid #f1f5f9; 
  &:active { background-color: #f8fafc; } 
}

.logo-wrapper { width: 76rpx; height: 76rpx; background-color: #f8fafc; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; margin-right: 28rpx; overflow: hidden; border: 1rpx solid rgba(0,0,0,0.03); }
.institution-logo { width: 52rpx; height: 52rpx; }
.institution-name { font-size: 30rpx; color: #1e293b; font-weight: 600; }

.index-bar {
  position: fixed; 
  right: 8rpx;    /* 稍微往右挪一点，贴近边缘 */
  /* 计算顶部偏移：避开搜索框(110rpx) + Tab栏(96rpx) */
  top: 58%;       
  transform: translateY(-50%);
  display: flex; 
  flex-direction: column; 
  align-items: center;
  background-color: rgba(255,255,255,0.92); /* 提高不透明度，增加可辨识度 */
  border-radius: 30rpx;
  padding: 20rpx 0; 
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1); 
  
  /* 核心修复：确保层级最高，能压住吸顶头部和列表 */
  z-index: 999; 
  
  /* 增加指针穿透保护（可选，确保底层列表能滚动，但字母能点中） */
  pointer-events: auto; 

  .index-item { 
    width: 44rpx; 
    height: 44rpx; 
    line-height: 44rpx; 
    text-align: center; 
    font-size: 18rpx; 
    color: #64748b; 
    font-weight: 800; 
    margin: 2rpx 0; 
    border-radius: 50%; 
    transition: all 0.2s;

    &.active-letter { 
      background-color: #2D7A68; 
      color: #ffffff; 
      transform: scale(1.15); /* 选中时稍微放大，更有质感 */
    } 
  }
}

.empty-tip { padding: 120rpx 0; text-align: center; color: #94a3b8; font-size: 26rpx; }
.safe-bottom { height: 160rpx; }
</style>