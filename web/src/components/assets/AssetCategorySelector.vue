<!-- components/assets/AssetCategorySelector.vue -->
<template>
  <view class="category-selector-overlay" @tap="handleOverlayClick">
    <view class="selector-container" @tap.stop>
      <!-- 头部 -->
      <view class="selector-header">
        <view class="header-left" @tap="$emit('close')">
          <uni-icons type="closeempty" size="20" color="#666"></uni-icons>
        </view>
        <text class="header-title">选择资产类型</text>
        <view class="header-right"></view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-section">
        <view class="search-bar">
          <uni-icons type="search" size="16" color="#999"></uni-icons>
          <input 
            type="text" 
            v-model="searchText" 
            placeholder="搜索资产类型" 
            class="search-input"
          />
          <uni-icons 
            v-if="searchText" 
            type="closeempty" 
            size="16" 
            color="#ccc" 
            @tap="searchText = ''"
          ></uni-icons>
        </view>
      </view>

      <!-- 资产分类列表 -->
      <scroll-view 
        class="list-scroll" 
        scroll-y
        @scroll="onListScroll"
      >
        <!-- 搜索结果 -->
        <view v-if="searchText" class="search-results">
          <view 
            v-for="item in searchResults" 
            :key="item.categoryCode"
            class="category-item"
            @tap="selectCategory(item)"
          >
            <view class="category-info">
              <view class="category-icon-box" :style="{ backgroundColor: item.color + '20' }">
                <image v-if="item.iconUrl" :src="item.iconUrl" mode="aspectFit" class="category-icon" />
                <view v-else class="icon-placeholder" :style="{ backgroundColor: item.color }">
                  <text class="icon-text">{{ item.name.charAt(0) }}</text>
                </view>
              </view>
              <view class="category-text">
                <text class="category-name">{{ item.name }}</text>
                <text v-if="item.description" class="category-desc">{{ item.description }}</text>
              </view>
            </view>
            <uni-icons type="right" size="14" color="#ccc"></uni-icons>
          </view>
          <view v-if="searchResults.length === 0" class="empty-tip">未找到相关资产类型</view>
        </view>

        <!-- 分类列表 -->
        <view v-else class="category-list">
          <view 
            v-for="category in assetCategories" 
            :key="category.categoryCode"
            class="category-section"
          >
            <!-- 大类标题 -->
            <view class="section-header" :id="`category-${category.categoryCode}`">
              <view class="section-icon-box" :style="{ backgroundColor: category.color + '20' }">
                <image :src="category.iconUrl" mode="aspectFit" class="section-icon" />
              </view>
              <text class="section-name">{{ category.name }}</text>
            </view>
            
            <!-- 细分类列表 -->
            <view class="subcategory-list">
              <view 
                v-for="sub in category.children" 
                :key="sub.categoryCode"
                class="subcategory-item"
                @tap="selectCategory(sub)"
              >
                <view class="sub-info">
                  <image 
                    v-if="sub.iconUrl" 
                    :src="sub.iconUrl" 
                    mode="aspectFit" 
                    class="sub-icon" 
                  />
                  <view v-else class="sub-icon-placeholder" :style="{ backgroundColor: category.color }">
                    <text class="sub-icon-text">{{ sub.name.charAt(0) }}</text>
                  </view>
                  <view class="sub-text">
                    <text class="sub-name">{{ sub.name }}</text>
                    <text v-if="sub.description" class="sub-desc">{{ sub.description }}</text>
                  </view>
                </view>
                <uni-icons type="right" size="14" color="#ccc"></uni-icons>
              </view>
            </view>
          </view>
          <view class="safe-bottom"></view>
        </view>
      </scroll-view>

      <!-- 右侧索引条 -->
      <view class="index-bar" v-if="!searchText && alphabet.length > 0">
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
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const emit = defineEmits(['select', 'close']);

const configStore = useConfigStore();

// 搜索状态
const searchText = ref('');
const activeLetter = ref('');
const groupOffsets = ref([]);

// 资产分类数据
const assetCategories = computed(() => configStore.assetCategories);

// 搜索结果
const searchResults = computed(() => {
  const kw = searchText.value.trim().toLowerCase();
  if (!kw) return [];
  
  const results = [];
  assetCategories.value.forEach(category => {
    if (category.name.toLowerCase().includes(kw)) {
      results.push({
        ...category,
        parentName: null
      });
    }
    category.children?.forEach(sub => {
      if (sub.name.toLowerCase().includes(kw) || 
          (sub.description && sub.description.toLowerCase().includes(kw))) {
        results.push({
          ...sub,
          parentName: category.name,
          color: category.color
        });
      }
    });
  });
  return results;
});

// 字母索引
const alphabet = computed(() => {
  const letters = new Set();
  assetCategories.value.forEach(category => {
    const firstLetter = category.name.charAt(0).toUpperCase();
    if (/[A-Z一-龥]/.test(firstLetter)) {
      letters.add(firstLetter);
    }
  });
  return Array.from(letters).sort();
});

// 方法
const selectCategory = (category) => {
  emit('select', category);
};

const handleOverlayClick = () => {
  emit('close');
};

const scrollToLetter = (letter) => {
  const category = assetCategories.value.find(c => 
    c.name.charAt(0).toUpperCase() === letter
  );
  if (category) {
    const targetId = `category-${category.categoryCode}`;
    uni.createSelectorQuery().in(this)
      .select(`#${targetId}`)
      .boundingClientRect(rect => {
        if (rect) {
          uni.pageScrollTo({
            scrollTop: rect.top - 100,
            duration: 300
          });
        }
      })
      .exec();
    activeLetter.value = letter;
    uni.vibrateShort();
  }
};

const onListScroll = (e) => {
  if (!groupOffsets.value.length) return;
  const scrollTop = e.detail.scrollTop + 100;
  for (let i = groupOffsets.value.length - 1; i >= 0; i--) {
    if (scrollTop >= groupOffsets.value[i].offset) {
      if (activeLetter.value !== groupOffsets.value[i].letter) {
        activeLetter.value = groupOffsets.value[i].letter;
      }
      break;
    }
  }
};

const calculateOffsets = () => {
  const query = uni.createSelectorQuery().in(this);
  query.selectAll('.section-header').boundingClientRect(rects => {
    if (!rects || !rects.length) return;
    const baseTop = rects[0].top;
    groupOffsets.value = rects.map((rect, index) => {
      const category = assetCategories.value[index];
      return {
        top: rect.top,
        offset: rect.top - baseTop,
        letter: category.name.charAt(0).toUpperCase()
      };
    });
  }).exec();
};

onMounted(() => {
  setTimeout(() => calculateOffsets(), 500);
});
</script>

<style lang="scss" scoped>
.category-selector-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.selector-container {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #f1f5f9;

  .header-left {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .header-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #1e293b;
  }

  .header-right {
    width: 60rpx;
  }
}

.search-section {
  padding: 24rpx 32rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  padding: 0 24rpx;
  border-radius: 40rpx;
  height: 80rpx;

  .search-input {
    flex: 1;
    margin-left: 16rpx;
    font-size: 28rpx;
    color: #1e293b;
  }
}

.list-scroll {
  flex: 1;
  overflow: hidden;
}

// 搜索结果
.search-results {
  padding: 0 32rpx;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f8fafc;
  }

  .category-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    flex: 1;
  }

  .category-icon-box {
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .category-icon {
      width: 40rpx;
      height: 40rpx;
    }

    .icon-placeholder {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon-text {
        font-size: 28rpx;
        font-weight: 700;
        color: #fff;
      }
    }
  }

  .category-text {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  .category-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1e293b;
  }

  .category-desc {
    font-size: 24rpx;
    color: #94a3b8;
  }
}

// 分类列表
.category-list {
  padding: 0 32rpx;
}

.category-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f1f5f9;

  .section-icon-box {
    width: 48rpx;
    height: 48rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .section-icon {
      width: 28rpx;
      height: 28rpx;
    }
  }

  .section-name {
    font-size: 28rpx;
    font-weight: 700;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 2rpx;
  }
}

.subcategory-list {
  margin-top: 16rpx;
  background: #f8fafc;
  border-radius: 20rpx;
  overflow: hidden;
}

.subcategory-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f1f5f9;
  }

  .sub-info {
    display: flex;
    align-items: center;
    gap: 16rpx;
    flex: 1;
  }

  .sub-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 14rpx;
  }

  .sub-icon-placeholder {
    width: 56rpx;
    height: 56rpx;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .sub-icon-text {
      font-size: 24rpx;
      font-weight: 700;
      color: #fff;
    }
  }

  .sub-text {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  .sub-name {
    font-size: 28rpx;
    font-weight: 600;
    color: #1e293b;
  }

  .sub-desc {
    font-size: 22rpx;
    color: #94a3b8;
  }
}

.empty-tip {
  padding: 120rpx 0;
  text-align: center;
  color: #94a3b8;
  font-size: 26rpx;
}

.safe-bottom {
  height: 100rpx;
}

.index-bar {
  position: absolute;
  right: 8rpx;
  top: 50%;
  transform: translateY(-40%);
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 30rpx;
  padding: 16rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: 100;

  .index-item {
    width: 40rpx;
    height: 40rpx;
    line-height: 40rpx;
    text-align: center;
    font-size: 18rpx;
    color: #64748b;
    font-weight: 700;
    margin: 2rpx 0;
    border-radius: 50%;
    transition: all 0.2s;

    &.active-letter {
      background-color: #2D7A68;
      color: #ffffff;
      transform: scale(1.1);
    }
  }
}

::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>
