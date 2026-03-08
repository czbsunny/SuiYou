<!-- components/assets/InstitutionSelector.vue -->
<template>
  <view class="institution-selector-overlay" @tap="handleOverlayClick">
    <view class="selector-container" @tap.stop>
      <!-- 头部 -->
      <view class="selector-header">
        <view class="header-left" @tap="$emit('close')">
          <uni-icons type="closeempty" size="20" color="#666"></uni-icons>
        </view>
        <text class="header-title">选择机构</text>
        <view class="header-right"></view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-section">
        <view class="search-bar">
          <uni-icons type="search" size="16" color="#999"></uni-icons>
          <input 
            type="text" 
            v-model="searchText" 
            placeholder="搜索机构名称" 
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

      <!-- 业态切换 Tab -->
      <view class="tabs-section" v-if="!searchText && institutionTypes.length > 1">
        <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
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

      <!-- 机构列表 -->
      <scroll-view 
        class="list-scroll" 
        scroll-y 
        :scroll-into-view="scrollIntoId"
        scroll-with-animation
        @scroll="onListScroll"
      >
        <!-- 搜索结果 -->
        <view v-if="searchText" class="search-results">
          <view 
            v-for="item in filteredList" 
            :key="item.id" 
            class="institution-item"
            @tap="selectInstitution(item)"
          >
            <view class="logo-wrapper">
              <image 
                :src="item.logoUrl || '/static/icons/default-bank.png'" 
                class="institution-logo" 
                mode="aspectFit" 
              />
            </view>
            <view class="institution-info">
              <text class="institution-name">{{ item.instName }}</text>
              <text v-if="item.instTypeName" class="institution-type">{{ item.instTypeName }}</text>
            </view>
            <uni-icons type="right" size="12" color="#ccc"></uni-icons>
          </view>
          <view v-if="filteredList.length === 0" class="empty-tip">未找到相关机构</view>
        </view>

        <!-- 分组列表 -->
        <view v-else class="grouped-list">
          <view 
            v-for="group in groupedInstitutions" 
            :key="group.indexLetter" 
            :id="formatId(group.indexLetter)"
            class="group-block"
          >
            <view class="group-title">
              {{ group.indexLetter === '热' ? '热门机构' : group.indexLetter }}
            </view>
            <view 
              v-for="item in group.data" 
              :key="item.id" 
              class="institution-item"
              @tap="selectInstitution(item)"
            >
              <view class="logo-wrapper">
                <image 
                  :src="item.logoUrl || '/static/icons/default-bank.png'" 
                  class="institution-logo" 
                  mode="aspectFit" 
                />
              </view>
              <view class="institution-info">
                <text class="institution-name">{{ item.instName }}</text>
                <text v-if="item.instTypeName" class="institution-type">{{ item.instTypeName }}</text>
              </view>
              <uni-icons type="right" size="12" color="#ccc"></uni-icons>
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
import { ASSET_INSTITUTION_DISPLAY } from '@/configs/assets.js';

const props = defineProps({
  // 可选：限制只显示某些机构类型
  filterTypes: {
    type: Array,
    default: () => []
  },
  // 可选：限制只显示与某个细分类关联的机构
  subCategoryCode: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['select', 'close']);

const configStore = useConfigStore();

// 搜索和筛选状态
const searchText = ref('');
const scrollIntoId = ref('');
const activeLetter = ref('热');
const activeType = ref('ALL');
const groupOffsets = ref([]);

// 业态类型列表
const institutionTypes = computed(() => {
  const types = [...new Set(configStore.institutionData.map(i => i.instType))];
  const mapped = types.map(t => ({
    code: t,
    name: ASSET_INSTITUTION_DISPLAY[t]?.name || '其他'
  }));
  return [{ code: 'ALL', name: '全部' }, ...mapped];
});

// 原始机构数据（根据条件过滤）
const rawInstitutions = computed(() => {
  let data = configStore.institutionData;
  
  // 如果指定了细分类，只显示关联的机构
  if (props.subCategoryCode) {
    data = configStore.getInstitutionsBySubCategoryCode(props.subCategoryCode);
  }
  
  // 如果指定了机构类型过滤
  if (props.filterTypes && props.filterTypes.length > 0) {
    data = data.filter(i => props.filterTypes.includes(i.instType));
  }
  
  // 按业态筛选
  if (activeType.value !== 'ALL') {
    data = data.filter(i => i.instType === activeType.value);
  }
  
  // 添加机构类型名称
  return data.map(item => ({
    ...item,
    instTypeName: ASSET_INSTITUTION_DISPLAY[item.instType]?.name
  }));
});

// 分组后的机构列表
const groupedInstitutions = computed(() => {
  const groups = {};
  const hotList = [];
  
  rawInstitutions.value.forEach(item => {
    if (item.isHot) hotList.push(item);
    const letter = (item.indexLetter || '#').toUpperCase();
    if (!groups[letter]) groups[letter] = [];
    groups[letter].push(item);
  });
  
  const result = Object.keys(groups)
    .sort()
    .map(key => ({ indexLetter: key, data: groups[key] }));
  
  if (hotList.length > 0) {
    result.unshift({ indexLetter: '热', data: hotList });
  }
  
  return result;
});

// 字母索引
const alphabet = computed(() => groupedInstitutions.value.map(g => g.indexLetter));

// 搜索结果
const filteredList = computed(() => {
  const kw = searchText.value.trim().toLowerCase();
  if (!kw) return [];
  
  let data = configStore.institutionData;
  
  if (props.subCategoryCode) {
    data = configStore.getInstitutionsBySubCategoryCode(props.subCategoryCode);
  }
  
  if (props.filterTypes && props.filterTypes.length > 0) {
    data = data.filter(i => props.filterTypes.includes(i.instType));
  }
  
  return data
    .filter(item => item.instName.toLowerCase().includes(kw))
    .map(item => ({
      ...item,
      instTypeName: ASSET_INSTITUTION_DISPLAY[item.instType]?.name
    }));
});

// 方法
const formatId = (letter) => letter === '热' ? 'letter-HOT' : `letter-${letter}`;

const handleTypeChange = (code) => {
  if (activeType.value === code) return;
  activeType.value = code;
  scrollIntoId.value = '';
  activeLetter.value = '热';
  nextTick(() => {
    setTimeout(() => calculateOffsets(), 300);
  });
};

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
  const query = uni.createSelectorQuery().in(this);
  query.selectAll('.group-block').boundingClientRect(rects => {
    if (!rects || !rects.length) return;
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

const selectInstitution = (institution) => {
  emit('select', institution);
};

const handleOverlayClick = () => {
  emit('close');
};

onMounted(() => {
  setTimeout(() => calculateOffsets(), 500);
});
</script>

<style lang="scss" scoped>
.institution-selector-overlay {
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

.tabs-section {
  border-bottom: 1rpx solid #f1f5f9;

  .tabs-scroll {
    white-space: nowrap;
  }

  .tabs-content {
    display: inline-flex;
    padding: 0 24rpx;
    height: 88rpx;
    align-items: center;
  }

  .tab-item {
    display: inline-block;
    padding: 0 20rpx;
    position: relative;
    height: 100%;
    line-height: 88rpx;

    .tab-text {
      font-size: 28rpx;
      color: #94a3b8;
      font-weight: 600;
      white-space: nowrap;
      display: block;
      transition: all 0.3s ease;
    }

    &.active {
      .tab-text {
        color: #2D7A68;
        font-weight: 700;
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

.list-scroll {
  flex: 1;
  overflow: hidden;
}

.group-title {
  padding: 16rpx 32rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #94a3b8;
  background-color: #f8fafc;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.institution-item {
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  background-color: #fff;
  border-bottom: 1rpx solid #f1f5f9;

  &:active {
    background-color: #f8fafc;
  }
}

.logo-wrapper {
  width: 72rpx;
  height: 72rpx;
  background-color: #f8fafc;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  overflow: hidden;
  border: 1rpx solid rgba(0, 0, 0, 0.03);
}

.institution-logo {
  width: 72rpx;
  height: 72rpx;
}

.institution-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.institution-name {
  font-size: 30rpx;
  color: #1e293b;
  font-weight: 600;
}

.institution-type {
  font-size: 24rpx;
  color: #94a3b8;
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
