<template>
  <view class="container">
    <!-- 主体列表区 -->
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
          @click="selectCategory(item)"
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
              @click="selectCategory(sub, category)"
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useConfigStore } from '@/stores/config.js';
import { onLoad } from '@dcloudio/uni-app';

const configStore = useConfigStore();

// 搜索状态
const searchText = ref('');
const activeLetter = ref('');
const groupOffsets = ref([]);

// 当前选中的分类（用于需要选择机构的情况）
const selectedCategory = ref(null);
const selectedSubCategory = ref(null);

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

// 检查细分类是否需要选择机构（通过 relations 判断）
const needSelectInstitution = (subCategoryCode) => {
  console.log('subCategoryCode:', subCategoryCode);
  const institutions = configStore.getInstitutionsBySubCategoryCode(subCategoryCode);
  return institutions && institutions.length > 0;
};

// 处理分类选择
const selectCategory = (item, parentCategory = null) => {
  // 如果点击的是大类（有子分类），不处理
  if (item.children && item.children.length > 0) return;
  
  // 找到父分类
  const parent = parentCategory || assetCategories.value.find(c => 
    c.children?.some(sub => sub.categoryCode === item.categoryCode)
  );

  if (!parent) return;
  
  selectedCategory.value = parent;
  selectedSubCategory.value = item;
  
  // 判断是否需要选择机构
  if (needSelectInstitution(item.categoryCode)) {
    // 需要选择机构，跳转到机构选择页面
    uni.navigateTo({
      url: `/pages/assets/institution-selector?subCode=${item.categoryCode}&categoryCode=${parent.categoryCode}&categoryName=${encodeURIComponent(parent.name)}&subCategoryName=${encodeURIComponent(item.name)}&color=${parent.color}`
    });
  } else {
    // 不需要选择机构，直接跳转到添加账户页面
    navigateToAddAccount();
  }
};

// 跳转到添加账户页面
const navigateToAddAccount = (institution = null) => {
  const params = {
    categoryCode: selectedCategory.value?.categoryCode,
    subCategoryCode: selectedSubCategory.value?.categoryCode
  };
  
  if (institution) {
    params.instCode = institution.instCode;
  }
  
  const queryString = Object.entries(params)
    .map(([key, value]) => `${key}=${encodeURIComponent(value || '')}`)
    .join('&');
  
  uni.redirectTo({
    url: `/pages/assets/add-account?${queryString}`
  });
};

// 监听机构选择事件
onLoad(() => {
  setTimeout(() => calculateOffsets(), 500);
});

const scrollToLetter = (letter) => {
  const category = assetCategories.value.find(c => 
    c.name.charAt(0).toUpperCase() === letter
  );
  if (category) {
    const targetId = `category-${category.categoryCode}`;
    uni.createSelectorQuery()
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
  const query = uni.createSelectorQuery();
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
.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f7f7f7;
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
  background-color: #fff;

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
  position: fixed;
  right: 8rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 30rpx;
  padding: 16rpx 0;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: 999;

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