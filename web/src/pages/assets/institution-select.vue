<template>
  <view class="container">
    <!-- 1. 搜索栏 (保持不变) -->
    <view class="header-section">
      <view class="search-bar">
        <input type="text" v-model="searchText" placeholder="搜索机构名称" class="search-input" />
      </view>
    </view>
    
    <!-- 2. 主体列表区 (增加 @scroll 监听) -->
    <scroll-view 
      class="list-scroll" 
      scroll-y 
      :scroll-into-view="scrollIntoId" 
      scroll-with-animation
      @scroll="onListScroll"
    >
      <view v-if="searchText">
        <view v-for="item in filteredList" :key="item.id" class="institution-item" @click="selectInstitution(item)">
          <view class="logo-wrapper">
            <image :src="item.logoUrl || '/static/icons/default-bank.png'" class="institution-logo" mode="aspectFit" />
          </view>
          <text class="institution-name">{{ item.instName }}</text>
        </view>
        <view v-if="filteredList.length === 0" class="empty-tip">未找到相关机构</view>
      </view>

      <view v-else id="scroll-content">
        <!-- 注意这里 ID 改为字母或 HOT -->
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
      </view>
      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- 3. 右侧索引条 (高亮逻辑改为 activeLetter) -->
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
import { ref, computed, nextTick } from 'vue';
import { onLoad, onReady } from '@dcloudio/uni-app';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const searchText = ref('');
const scrollIntoId = ref('');
const activeLetter = ref('热'); // 当前高亮的字母
const subCode = ref('');
const groupOffsets = ref([]); // 存储每个分组的距离顶部的距离

onLoad((options) => {
  if (options.subCode) subCode.value = options.subCode;
});

// 核心逻辑：ID 格式化（避开中文防止 Bug）
const formatId = (letter) => {
  return letter === '热' ? 'letter-HOT' : `letter-${letter}`;
};

const rawInstitutions = computed(() => configStore.getInstitutionsBySubCategoryCode(subCode.value));

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
  return rawInstitutions.value.filter(item => item.instName.toLowerCase().includes(kw));
});

// --- Bug 1 修复：点击跳转 ---
const scrollToLetter = (letter) => {
  const targetId = formatId(letter);
  scrollIntoId.value = ''; // 先清空，确保相同 ID 也能触发监听
  nextTick(() => {
    scrollIntoId.value = targetId;
    activeLetter.value = letter;
    uni.vibrateShort();
  });
};

// --- Bug 2 修复：滚动联动 ---

// 计算所有分组的坐标位置
const calculateOffsets = () => {
  const query = uni.createSelectorQuery();
  query.selectAll('.group-block').boundingClientRect(rects => {
    groupOffsets.value = rects.map((rect, index) => ({
      top: rect.top, // 相对于页面的初始位置
      letter: alphabet.value[index]
    }));
  }).exec();
};

// 监听列表滚动
let isClickScrolling = false; // 防止点击跳转触发的滚动干扰联动
const onListScroll = (e) => {
  const scrollTop = e.detail.scrollTop + 50; // 偏移 50px 容错
  
  // 查找当前滚动到了哪个字母区间
  for (let i = groupOffsets.value.length - 1; i >= 0; i--) {
    // 这里需要相对于 scroll-view 容器的相对位移
    // 在 uniapp 中，简单做法是比较 scrollTop 与我们预存的分组位置
    // 假设第一个分组 top 为 0
    const offset = groupOffsets.value[i].top - groupOffsets.value[0].top;
    if (scrollTop >= offset) {
      if (activeLetter.value !== groupOffsets.value[i].letter) {
        activeLetter.value = groupOffsets.value[i].letter;
      }
      break;
    }
  }
};

onReady(() => {
  // 数据渲染后，延迟计算位置
  setTimeout(() => {
    calculateOffsets();
  }, 500);
});

const selectInstitution = (institution) => {
  uni.$emit('institutionSelected', institution);
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
/* 原有样式保持不变 */
.container { display: flex; flex-direction: column; height: 100vh; background-color: #f7f7f7; }
.header-section { padding: 20rpx 30rpx; background-color: #fff; }
.search-bar { display: flex; align-items: center; background-color: #f5f5f5; padding: 0 24rpx; border-radius: 36rpx; height: 72rpx; .search-input { flex: 1; margin-left: 12rpx; font-size: 28rpx; color: #333; } }
.list-scroll { flex: 1; overflow: hidden; }
.group-title { padding: 12rpx 30rpx; font-size: 24rpx; font-weight: 600; color: #9ca3af; background-color: #f7f7f7; }
.institution-item { display: flex; align-items: center; padding: 28rpx 30rpx; background-color: #fff; border-bottom: 1rpx solid #f2f2f2; &:active { background-color: #f9f9f9; } }
.logo-wrapper { width: 64rpx; height: 64rpx; background-color: #f8f9fa; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; margin-right: 24rpx; overflow: hidden; border: 1rpx solid rgba(0,0,0,0.03); }
.institution-logo { width: 64rpx; height: 64rpx; }
.institution-name { font-size: 30rpx; color: #1f2937; font-weight: 500; }

.index-bar {
  position: fixed;
  right: 12rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255,255,255,0.85);
  border-radius: 30rpx;
  padding: 20rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  z-index: 10;
  
  .index-item {
    width: 44rpx;
    height: 44rpx;
    line-height: 44rpx;
    text-align: center;
    font-size: 20rpx;
    color: #6b7280;
    font-weight: 700;
    margin: 2rpx 0;
    border-radius: 50%;
    transition: all 0.2s;
    
    &.active-letter {
      background-color: #2A806C;
      color: #ffffff;
      transform: scale(1.1);
    }
  }
}

.empty-tip { padding: 120rpx 0; text-align: center; color: #9ca3af; font-size: 28rpx; }
.safe-bottom { height: 120rpx; }
</style>