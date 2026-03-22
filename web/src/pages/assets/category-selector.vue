<template>
  <view class="container">
    <!-- 主体列表区 -->
    <scroll-view 
      class="list-scroll" 
      scroll-y 
    >
      <!-- 分类列表 -->
      <view class="category-list">
        <view 
          v-for="category in assetCategories" 
          :key="category.categoryCode"
          class="category-block"
        >
          <!-- 大类标题 -->
          <view class="block-header">
            <view class="block-icon-box">
              <image :src="category.iconUrl" mode="aspectFit" class="block-icon" />
            </view>
            <text class="block-name">{{ category.name }}</text>
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
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();



// 当前选中的分类（用于需要选择机构的情况）
const selectedCategory = ref(null);
const selectedSubCategory = ref(null);

// 资产分类数据
const assetCategories = computed(() => configStore.assetCategories);



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
      url: `/pages/assets/institution-selector?subCategoryCode=${item.categoryCode}&categoryCode=${parent.categoryCode}`
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
  
  console.log('跳转参数:', queryString);
  uni.redirectTo({
    url: `/pages/assets/add?${queryString}`
  });
};

onMounted(() => {
  // 初始化逻辑
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

// 分类列表
.category-list {
  padding: 20rpx; // 10px = 20rpx
}

// 资产大类块
.category-block {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 20rpx; // 10px = 20rpx
  overflow: hidden;
}

// 块头部
.block-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx; // 10px = 20rpx
  border-bottom: 1rpx solid #f1f5f9;

  .block-icon-box {
    width: 56rpx;
    height: 56rpx;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .block-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: 8rpx;
    }
  }

  .block-name {
    font-size: 30rpx;
    font-weight: 700;
    color: #1e293b;
  }
}

// 细分类列表
.subcategory-list {
  padding: 8rpx 0;
}

.subcategory-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx; // 10px = 20rpx

  &:active {
    background: #f8fafc;
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
    border-radius: 8rpx;
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



.safe-bottom {
  height: 100rpx;
}

::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>