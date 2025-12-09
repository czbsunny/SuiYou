<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input 
        type="text" 
        v-model="searchText" 
        placeholder="请输入机构名称搜索"
        class="search-input"
        @input="onSearch"
      />
      <view class="search-icon">
        <uni-icons type="search" size="18" color="#999999"></uni-icons>
      </view>
    </view>
    
    <!-- 机构列表 -->
    <view class="institution-list">
      <view 
        v-for="(institution, index) in filteredInstitutions" 
        :key="index"
        class="institution-item"
        @click="selectInstitution(institution)"
      >
        <text class="institution-name">{{ institution.name }}</text>
      </view>
      
      <!-- 无结果提示 -->
      <view v-if="filteredInstitutions.length === 0" class="empty-tip">
        <text>暂无匹配的机构</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

// 接收传递过来的机构列表
const institutions = ref([]);
const searchText = ref('');

// 过滤后的机构列表
const filteredInstitutions = computed(() => {
  if (!searchText.value) {
    return institutions.value;
  }
  return institutions.value.filter(item => 
    item.name.includes(searchText.value)
  );
});

// 搜索处理
const onSearch = () => {
  // 搜索逻辑已在computed中实现
};

// 选择机构
const selectInstitution = (institution) => {
  // 返回上一页并传递选中的机构信息
  router.back();
  uni.$emit('institutionSelected', institution);
};

// 页面加载时获取机构列表
onMounted(() => {
  // 从路由参数中获取机构列表
  if (route.params.institutions) {
    institutions.value = JSON.parse(decodeURIComponent(route.params.institutions));
  }
});
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #F9F8F4;
  min-height: 100vh;
}

.search-bar {
  position: relative;
  margin-bottom: 20rpx;
}

.search-input {
  width: 100%;
  height: 80rpx;
  border-radius: 40rpx;
  background-color: #FFFFFF;
  padding-left: 80rpx;
  padding-right: 20rpx;
  font-size: 28rpx;
  color: #333333;
  border: 1px solid #E0E0E0;
}

.search-icon {
  position: absolute;
  left: 30rpx;
  top: 50%;
  transform: translateY(-50%);
}

.institution-list {
  background-color: #FFFFFF;
  border-radius: 10rpx;
  padding: 10rpx 0;
}

.institution-item {
  padding: 30rpx 20rpx;
  border-bottom: 1px solid #F0F0F0;
}

.institution-item:last-child {
  border-bottom: none;
}

.institution-name {
  font-size: 32rpx;
  color: #333333;
}

.empty-tip {
  padding: 100rpx 0;
  text-align: center;
  color: #999999;
  font-size: 28rpx;
}
</style>