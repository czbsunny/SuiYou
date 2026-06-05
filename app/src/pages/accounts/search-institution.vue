<template>
  <view class="search-page">
    <!-- Header 部分 -->
    <view class="header-section">
      <view class="search-box">
        <image src="/static/images/search.png" class="search-icon" mode="aspectFit" />
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
            class="institution-card" 
            @click="selectInstitution(institution)"
          >
            <view class="card-left">
              <view class="logo-wrapper" :class="getLogoBgClass(institution)">
                <image :src="formatImageUrl(institution.logoUrl)" class="institution-logo" mode="aspectFit" />
              </view>
              <view class="institution-info">
                <view class="institution-name-row">
                  <text class="institution-name">{{ institution.instName }}</text>
                  <text v-if="institution.isHot" class="hot-tag">热门</text>
                </view>
                <text class="type-tag">{{ institution.institutionType?.typeName || '机构类型' }}</text>
              </view>
            </view>
            <image src="/static/images/chevron_right.png" class="arrow-icon" mode="aspectFit" />
          </view>
        </view>

        <!-- 字母分组列表 -->
        <view v-else-if="!searchQuery" id="scroll-content">
          <view v-if="loading" class="loading-container">
            <text class="loading-text">加载中...</text>
          </view>

          <view v-else>
            <view v-for="group in groupedInstitutions" :key="group.indexLetter" :id="formatId(group.indexLetter)" class="group-block">
              <view class="group-header">
                <text class="group-title">{{ group.indexLetter === '热' ? '常用' : group.indexLetter }}</text>
              </view>
              <view v-for="institution in group.data" :key="institution.instCode || institution.id" class="institution-card" @click="selectInstitution(institution)">
                <view class="card-left">
                  <view class="logo-wrapper" :class="getLogoBgClass(institution)">
                    <image :src="formatImageUrl(institution.logoUrl)" class="institution-logo" mode="aspectFit" />
                  </view>
                  <view class="institution-info">
                    <view class="institution-name-row">
                      <text class="institution-name">{{ institution.instName }}</text>
                      <text v-if="institution.isHot" class="hot-tag">热门</text>
                    </view>
                    <text class="type-tag">{{ institution.institutionType?.typeName || '机构类型' }}</text>
                  </view>
                </view>
                <image src="/static/images/chevron_right.png" class="arrow-icon" mode="aspectFit" />
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

const getLogoBgClass = () => ''

const getTypeName = (typeCode) => {
  const typeMap = {
    'BK': '银行',
    'PY': '支付',
    'ZQ': '证券',
    'JJ': '基金平台',
    'BX': '保险',
    'OTHER': '其他'
  }
  return typeMap[typeCode] || typeCode || '其他'
}

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
  background-color: #FAF9F6; 
}

.scroll-content { 
  flex: 1; 
  overflow: hidden; 
}

.header-section {
  position: sticky;
  top: 0;
  z-index: 200;
  background-color: #FAF9F6;
  padding: $spacing-2 $spacing-3;

  .search-box { 
    display: flex; 
    align-items: center; 
    background-color: #F4F3F1; 
    padding: 0 $spacing-3; 
    border-radius: 48rpx; 
    height: 96rpx; 

    .search-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: $spacing-1;
      opacity: 0.6;
    }

    .search-input { 
      flex: 1; 
      font-size: 28rpx; 
      color: #1A1C1A; 
      background-color: transparent;
    }

    .clear-btn {
      width: 40rpx;
      height: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .clear-icon {
        font-size: 36rpx;
        color: #6F7975;
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
  margin-top: $spacing-2;

  .tabs-scroll { 
    width: 100%; 
    white-space: nowrap; 
  }

  .tabs-container { 
    display: inline-flex; 
    padding: 0 $spacing-1;
    gap: $spacing-1;
  }

  .tab-item {
    display: inline-flex; 
    padding: 0 $spacing-4;
    height: 72rpx;
    align-items: center;
    border-radius: 100rpx;
    transition: all 0.3s ease;
    
    .tab-text {
      font-size: 26rpx;
      font-weight: 600;
      white-space: nowrap; 
      display: block;
      transition: all 0.3s ease;
    }
    
    &.active {
      background-color: #006754;
      .tab-text {
        color: #FFFFFF;
        font-weight: 700;
      }
    }
    
    &:not(.active) {
      background-color: #E9E8E5;
      .tab-text {
        color: #3F4945;
      }
      &:active {
        background-color: #E3E2E0;
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
  padding: $spacing-1 $spacing-3;
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
  margin-top: $spacing-2;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-2;
  
  .group-title {
    font-size: 24rpx; 
    font-weight: 700; 
    color: #3F4945; 
  }
  
  .group-more {
    font-size: 22rpx;
    font-weight: 600;
    color: #006754;
  }
}

/* 机构卡片 */
.institution-card {
  display: flex; 
  align-items: center; 
  justify-content: space-between;
  background-color: #FFFFFF; 
    border-radius: 40rpx;
    padding: $spacing-3;
    margin-bottom: $spacing-2;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  
  &:active {
    background-color: #F4F3F1;
  }
  
  .card-left {
    display: flex;
    align-items: center;
    flex: 1;
  }
  
  .logo-wrapper {
    width: 88rpx; 
    height: 88rpx; 
    border-radius: 100rpx; 
    display: flex; 
    align-items: center; 
    justify-content: center; 
    margin-right: $spacing-3; 
    overflow: hidden;
    
    &.logo-red {
      background-color: #FEE2E2;
    }
    &.logo-green {
      background-color: #DCFCE7;
    }
    &.logo-orange {
      background-color: #FFEDD5;
    }
    &.logo-purple {
      background-color: #F3E8FF;
    }
    &.logo-blue {
      background-color: #DBEAFE;
    }
    &.logo-gray {
      background-color: #F4F3F1;
    }
  }
  
  .institution-logo { 
    width: 88rpx; 
    height: 88rpx; 
  }
  
  .institution-info {
    flex: 1;
    
    .institution-name-row {
      display: flex;
      align-items: center;
      gap: $spacing-1;
    }
    
    .institution-name { 
      font-size: 30rpx; 
      color: #1A1C1A; 
      font-weight: 600; 
    }
    
    .hot-tag {
      font-size: 20rpx;
      font-weight: 700;
      color: #BA1A1A;
      background-color: #FFDAD6;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
    }
    
    .type-tag {
      font-size: 22rpx;
      color: #3F4945;
      background-color: #F4F3F1;
      padding: 6rpx 16rpx;
      border-radius: 100rpx;
      margin-top: $spacing-1;
      display: inline-block;
    }
    
  }
  
  .arrow-icon {
    width: 40rpx;
    height: 40rpx;
    margin-left: $spacing-2;
    opacity: 0.6;
  }
}

/* 搜索结果 */
.search-results {
  background-color: transparent;
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
  right: $spacing-1;
  top: 50%;       
  transform: translateY(-50%);
  display: flex; 
  flex-direction: column; 
  align-items: center;
  background-color: rgba(255,255,255,0.92);
  border-radius: 100rpx;
  padding: $spacing-2 0; 
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.08); 
  z-index: 999; 
  pointer-events: auto; 

  .index-item { 
    width: 56rpx; 
    height: 56rpx; 
    line-height: 56rpx; 
    text-align: center; 
    font-size: 22rpx; 
    color: #6F7975; 
    font-weight: 600; 
    border-radius: 50%; 
    transition: all 0.2s;

    &.active-letter { 
      background-color: #006754; 
      color: #FFFFFF; 
      transform: scale(1.15); 
    } 
  }
}

.safe-bottom { 
  height: 160rpx; 
}
</style>