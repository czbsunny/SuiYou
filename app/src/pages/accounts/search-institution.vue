<template>
  <view class="search-page">
    <!-- Header 部分 -->
    <view class="header-section">
      <view class="search-box">
        <image src="/static/images/search.png" class="search-icon" />
        <input 
          class="search-input" 
          v-model="searchQuery" 
          placeholder="搜索机构或平台" 
          placeholder-class="placeholder"
        />
        <view v-if="searchQuery" class="clear-btn" @tap="clearSearch">
          <text class="clear-icon">×</text>
        </view>
      </view>

      <view class="tabs-section" v-if="!searchQuery">
        <scroll-view 
          scroll-x 
          class="tabs-scroll" 
          :show-scrollbar="false" 
          :enhanced="true"
        >
          <view class="tabs-container">
            <view 
              v-for="tab in tabs" 
              :key="tab.code"
              class="tab-item"
              :class="{ active: activeTab === tab.code }"
              @tap="selectTab(tab.code)"
            >
              <text class="tab-text">{{ tab.name }}</text>
              <view class="tab-line" v-if="activeTab === tab.code"></view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <scroll-view 
      scroll-y 
      class="scroll-content" 
      :scroll-into-view="scrollIntoId" 
      scroll-with-animation
      @scroll="onListScroll"
    >

      <!-- 机构列表区域 -->
      <view class="institutions-section">
        <!-- 搜索结果 -->
        <view v-if="searchQuery && filteredInstitutions.length > 0" class="search-results">
          <view 
            v-for="institution in filteredInstitutions" 
            :key="institution.instCode || institution.id" 
            class="institution-item" 
            @click="selectInstitution(institution)"
          >
            <view class="logo-wrapper">
              <image :src="formatImageUrl(institution.logoUrl)" class="institution-logo" mode="aspectFit" />
            </view>
            <text class="institution-name">{{ institution.instName }}</text>
          </view>
        </view>

        <!-- 字母分组列表 -->
        <view v-else-if="!searchQuery" id="scroll-content">
          <view v-if="loading" class="loading-container">
            <text class="loading-text">加载中...</text>
          </view>

          <view v-else>
            <view 
              v-for="group in groupedInstitutions" 
              :key="group.indexLetter" 
              :id="formatId(group.indexLetter)"
              class="group-block"
            >
              <view class="group-title">{{ group.indexLetter === '热' ? '热门机构' : group.indexLetter }}</view>
              <view v-for="institution in group.data" :key="institution.instCode || institution.id" class="institution-item" @click="selectInstitution(institution)">
                <view class="logo-wrapper">
                  <image :src="formatImageUrl(institution.logoUrl)" class="institution-logo" mode="aspectFit" />
                </view>
                <text class="institution-name">{{ institution.instName }}</text>
              </view>
            </view>
            <view class="safe-bottom"></view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="!loading && searchQuery && filteredInstitutions.length === 0" class="empty-state">
          <text class="empty-text">未找到相关机构</text>
        </view>

        <view v-else-if="!loading && !searchQuery && currentInstitutions.length === 0" class="empty-state">
          <text class="empty-text">暂无机构数据</text>
        </view>
      </view>
    </scroll-view>

    <!-- 右侧索引条 -->
    <view class="index-bar" v-if="!searchQuery && alphabet.length > 0">
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { getInstitutionTypes, getHotInstitutions, getInstitutionsByType, getAllInstitutions } from '@/api/modules/asset'

const searchQuery = ref('')
const activeTab = ref('HOT')
const loading = ref(false)
const institutionTypes = ref([])
const currentInstitutions = ref([])
const scrollIntoId = ref('')
const activeLetter = ref('热')
const groupOffsets = ref([])

const tabs = computed(() => {
  const defaultTabs = [
    { code: 'HOT', name: '热门' },
    { code: 'ALL', name: '全部' }
  ]
  const typeTabs = Array.isArray(institutionTypes.value) ? institutionTypes.value.map(type => ({
    code: type.typeCode,
    name: type.typeName
  })) : []
  return [...defaultTabs, ...typeTabs]
})

const formatImageUrl = (url) => {
  if (!url) return ''
  let formattedUrl = url.startsWith('/') ? url.substring(1) : url
  return '/' + formattedUrl
}

const formatId = (letter) => letter === '热' ? 'letter-HOT' : `letter-${letter}`

const filteredInstitutions = computed(() => {
  const kw = searchQuery.value.trim().toLowerCase()
  if (!kw) return []
  return currentInstitutions.value.filter(item => {
    const name = (item.instName || item.shortName || '').toLowerCase()
    return name.includes(kw)
  })
})

const groupedInstitutions = computed(() => {
  const groups = {}
  const hotList = []
  currentInstitutions.value.forEach(item => {
    if (item.isHot) hotList.push(item)
    const letter = (item.indexLetter || '#').toUpperCase()
    if (!groups[letter]) groups[letter] = []
    groups[letter].push(item)
  })
  const result = Object.keys(groups).sort().map(key => ({ indexLetter: key, data: groups[key] }))
  if (hotList.length > 0) result.unshift({ indexLetter: '热', data: hotList })
  return result
})

const alphabet = computed(() => groupedInstitutions.value.map(g => g.indexLetter))

const selectTab = async (tabCode) => {
  activeTab.value = tabCode
  activeLetter.value = '热'
  await loadInstitutions()
  nextTick(() => { setTimeout(() => calculateOffsets(), 300) })
}

const selectInstitution = (institution) => {
  uni.navigateTo({
    url: `/pages/accounts/add-account?instCode=${institution.instCode}`
  })
}

const clearSearch = () => {
  searchQuery.value = ''
}

const scrollToLetter = (letter) => {
  const targetId = formatId(letter)
  scrollIntoId.value = ''
  nextTick(() => {
    scrollIntoId.value = targetId
    activeLetter.value = letter
    uni.vibrateShort()
  })
}

const calculateOffsets = () => {
  const query = uni.createSelectorQuery()
  query.selectAll('.group-block').boundingClientRect(rects => {
    if (!rects.length) return
    const baseTop = rects[0].top
    groupOffsets.value = rects.map((rect, index) => ({
      top: rect.top,
      offset: rect.top - baseTop,
      letter: alphabet.value[index]
    }))
  }).exec()
}

const onListScroll = (e) => {
  if (!groupOffsets.value.length) return
  const scrollTop = e.detail.scrollTop + 20
  for (let i = groupOffsets.value.length - 1; i >= 0; i--) {
    if (scrollTop >= groupOffsets.value[i].offset) {
      if (activeLetter.value !== groupOffsets.value[i].letter) {
        activeLetter.value = groupOffsets.value[i].letter
      }
      break
    }
  }
}

const loadInstitutionTypes = async () => {
  try {
    const res = await getInstitutionTypes()
    if (res && res.data && Array.isArray(res.data)) {
      institutionTypes.value = res.data
    } else {
      institutionTypes.value = []
    }
  } catch (error) {
    console.error('加载机构类型失败:', error)
    institutionTypes.value = []
  }
}

const loadInstitutions = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'HOT') {
      res = await getHotInstitutions()
    } else if (activeTab.value === 'ALL') {
      res = await getAllInstitutions()
    } else {
      res = await getInstitutionsByType(activeTab.value)
    }
    
    if (res && res.data && Array.isArray(res.data)) {
      currentInstitutions.value = res.data
    } else {
      currentInstitutions.value = []
    }
  } catch (error) {
    console.error('加载机构列表失败:', error)
    currentInstitutions.value = []
  } finally {
    loading.value = false
    nextTick(() => { setTimeout(() => calculateOffsets(), 500) })
  }
}

onMounted(async () => {
  await loadInstitutionTypes()
  await loadInstitutions()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.search-page { 
  display: flex; 
  flex-direction: column; 
  height: 100vh; 
  background-color: #f7f7f7; 
}

.scroll-content { 
  flex: 1; 
  overflow: hidden; 
}

.header-section {
  position: sticky;
  top: 0;
  z-index: 200;
  background-color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
  padding: $spacing-sm $spacing-base;

  .search-box { 
    display: flex; 
    align-items: center; 
    background-color: #f3f4f6; 
    padding: 0 24rpx; 
    border-radius: 40rpx; 
    height: 84rpx; 

    .search-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 16rpx;
      opacity: 0.3;
    }

    .search-input { 
      flex: 1; 
      font-size: 28rpx; 
      color: #1e293b; 
    }

    .clear-btn {
      width: 40rpx;
      height: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .clear-icon {
        font-size: 36rpx;
        color: #94a3b8;
        font-weight: 300;
      }
    }
  }
}

.placeholder {
  color: rgba(107, 114, 128, 0.6);
}

/* Tab 样式 */
.tabs-section {
  width: 100%;
  margin-top: $spacing-sm;

  .tabs-scroll { 
    width: 100%; 
    white-space: nowrap; 
  }

  .tabs-container { 
    display: inline-flex; 
    padding: 0 24rpx 0 8rpx;
    height: 96rpx;
    align-items: center;
  }

  .tab-item {
    display: inline-block; 
    padding: 0 18rpx;
    position: relative;
    height: 100%;
    line-height: 96rpx;
    
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

.institutions-section {
  padding: 0 $spacing-xs;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 64rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #94a3b8;
}

/* 分组块 */
.group-block {
  padding: 0 $spacing-sm;
}

.group-title {
  padding: $spacing-sm $spacing-base; 
  font-size: 22rpx; 
  font-weight: 800; 
  color: #94a3b8; 
  background-color: #f8fafc; 
  text-transform: uppercase; 
  letter-spacing: 2rpx; 
}

/* 机构项 */
.institution-item {
  padding: $spacing-sm $spacing-base;
  border-radius: $border-radius-base;
  display:  flex; 
  align-items:  center; 
  background-color: #fff; 
  border-bottom: 1rpx solid #f8fafc;
}

.logo-wrapper  { 
  width: 76rpx; 
  height: 76rpx; 
  background-color: #f8fafc; 
  border-radius: 20rpx; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin-right: 28rpx; 
  overflow: hidden; 
  border: 1rpx solid rgba(0,0,0,0.03); 
}
.institution-logo { 
  width: 76rpx; 
  height: 76rpx; 
}
.institution-name { 
  font-size: 30rpx; 
  color: #1e293b; 
  font-weight: 600; 
}

/* 搜索结果 */
.search-results {
  background-color: #fff;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 64rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}

/* 索引条 */
.index-bar {
  position: fixed; 
  right: 8rpx;
  top: 50%;       
  transform: translateY(-50%);
  display: flex; 
  flex-direction: column; 
  align-items: center;
  background-color: rgba(255,255,255,0.92);
  border-radius: 30rpx;
  padding: 20rpx 0; 
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1); 
  z-index: 999; 
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
      transform: scale(1.15); 
    } 
  }
}

.safe-bottom { 
  height: 160rpx; 
}
</style>