<template>
  <view class="container">
    <!-- 1. 固定顶部的搜索栏 -->
    <view class="header-section">
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input 
          type="text" 
          v-model="searchText" 
          placeholder="搜索机构名称、首字母"
          class="search-input"
        />
        <uni-icons v-if="searchText" type="closeempty" size="18" color="#ccc" @click="searchText = ''"></uni-icons>
      </view>
    </view>
    
    <!-- 2. 主体列表区 -->
    <scroll-view 
      class="list-scroll" 
      scroll-y 
      :scroll-into-view="scrollIntoId" 
      scroll-with-animation
    >
      <!-- 搜索状态显示扁平列表 -->
      <view v-if="searchText">
        <view 
          v-for="item in filteredList" 
          :key="item.id" 
          class="institution-item"
          @click="selectInstitution(item)"
        >
          <text class="institution-name">{{ item.instName }}</text>
        </view>
        <view v-if="filteredList.length === 0" class="empty-tip">未找到相关机构</view>
      </view>

      <!-- 正常状态显示索引列表 -->
      <view v-else>
        <view v-for="group in groupedInstitutions" :key="group.letter" :id="'letter-' + group.letter">
          <view class="group-title">{{ group.letter }}</view>
          <view 
            v-for="item in group.data" 
            :key="item.id" 
            class="institution-item"
            @click="selectInstitution(item)"
          >
            <text class="institution-name">{{ item.instName }}</text>
          </view>
        </view>
      </view>
      
      <!-- 底部留白，防止被遮挡 -->
      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- 3. 右侧索引条 (搜索时隐藏) -->
    <view class="index-bar" v-if="!searchText">
      <view 
        v-for="letter in alphabet" 
        :key="letter" 
        class="index-item"
        @touchstart="scrollToLetter(letter)"
      >
        {{ letter }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app'; // 必须引入 onLoad 来接收参数
import { useConfigStore } from '@/stores/config.js';

const configStore = useConfigStore();
const searchText = ref('');
const scrollIntoId = ref('');
const subCode = ref(''); // 用于存储接收到的子分类 Code

// 1. 接收从上个页面传来的参数
onLoad((options) => {
  if (options.subCode) {
    subCode.value = options.subCode;
    console.log('当前筛选子分类:', subCode.value);
  }
});

// 2. 核心：获取数据源 (改为基于 subCode 过滤)
const rawInstitutions = computed(() => {
  return configStore.getInstitutionsBySubCategoryCode(subCode.value);
});

// 3. 字母表索引 (基于过滤后的数据生成)
const alphabet = computed(() => {
  return groupedInstitutions.value.map(g => g.letter);
});

// 4. 将过滤后的数据按字母分组
const groupedInstitutions = computed(() => {
  const groups = {};
  console.log(rawInstitutions.value);
  rawInstitutions.value.forEach(item => {
    // 优先使用后端返回的 initial 字段，如果没有则默认为 #
    const letter = (item.initial || '#').toUpperCase(); 
    if (!groups[letter]) groups[letter] = [];
    groups[letter].push(item);
  });
  
  return Object.keys(groups).sort().map(key => ({
    letter: key,
    data: groups[key]
  }));
});

// 5. 搜索过滤 (在当前已过滤的机构池中进行搜索)
const filteredList = computed(() => {
  const kw = searchText.value.toLowerCase();
  if (!kw) return [];
  return rawInstitutions.value.filter(item => 
    item.name.toLowerCase().includes(kw) || 
    (item.pinyin && item.pinyin.toLowerCase().includes(kw))
  );
});

// 6. 跳转定位
const scrollToLetter = (letter) => {
  scrollIntoId.value = 'letter-' + letter;
  uni.vibrateShort(); // 触感反馈
};

// 7. 选择并返回
const selectInstitution = (institution) => {
  // 发送全局事件
  uni.$emit('institutionSelected', institution);
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f7f7f7;
}

.header-section {
  padding: 20rpx 30rpx;
  background-color: #fff;
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  padding: 0 20rpx;
  border-radius: 36rpx;
  height: 72rpx;
  
  .search-input {
    flex: 1;
    margin-left: 10rpx;
    font-size: 28rpx;
  }
}

.list-scroll {
  flex: 1;
  overflow: hidden;
}

.group-title {
  padding: 10rpx 30rpx;
  font-size: 24rpx;
  color: #999;
  background-color: #f7f7f7;
}

.institution-item {
  padding: 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #eee;
  &:active {
    background-color: #f9f9f9;
  }
}

.institution-name {
  font-size: 30rpx;
  color: #333;
}

/* 右侧索引条 */
.index-bar {
  position: fixed;
  right: 10rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255,255,255,0.8);
  border-radius: 20rpx;
  padding: 10rpx 0;
  box-shadow: 0 0 10rpx rgba(0,0,0,0.05);
  
  .index-item {
    padding: 6rpx 10rpx;
    font-size: 22rpx;
    color: #007AFF;
    font-weight: bold;
  }
}

.empty-tip {
  padding: 100rpx;
  text-align: center;
  color: #999;
}

.safe-bottom {
  height: 100rpx;
}
</style>