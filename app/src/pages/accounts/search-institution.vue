
&lt;template&gt;
  &lt;view class="search-page"&gt;
    &lt;scroll-view scroll-y class="scroll-content"&gt;
      &lt;view class="search-section"&gt;
        &lt;view class="search-box"&gt;
          &lt;text class="search-icon"&gt;🔍&lt;/text&gt;
          &lt;input 
            class="search-input" 
            v-model="searchQuery" 
            placeholder="搜索机构或平台" 
            placeholder-class="placeholder"
          /&gt;
        &lt;/view&gt;
      &lt;/view&gt;

      &lt;view class="tabs-section"&gt;
        &lt;scroll-view scroll-x class="tabs-scroll" show-scrollbar="false"&gt;
          &lt;view class="tabs-container"&gt;
            &lt;view 
              v-for="tab in tabs" 
              :key="tab.code"
              class="tab-item"
              :class="{ active: activeTab === tab.code }"
              @tap="selectTab(tab.code)"
            &gt;
              &lt;text class="tab-text"&gt;{{ tab.name }}&lt;/text&gt;
            &lt;/view&gt;
          &lt;/view&gt;
        &lt;/scroll-view&gt;
      &lt;/view&gt;

      &lt;view class="institutions-section"&gt;
        &lt;view class="section-header"&gt;
          &lt;text class="section-title"&gt;{{ getSectionTitle() }}&lt;/text&gt;
          &lt;text v-if="activeTab === 'HOT'" class="sort-link"&gt;全部 A-Z&lt;/text&gt;
        &lt;/view&gt;

        &lt;view v-if="loading" class="loading-container"&gt;
          &lt;text class="loading-text"&gt;加载中...&lt;/text&gt;
        &lt;/view&gt;

        &lt;view v-else-if="filteredInstitutions.length &gt; 0" class="institutions-list"&gt;
          &lt;InstitutionCard
            v-for="institution in filteredInstitutions"
            :key="institution.instCode || institution.id"
            :institution="institution"
            :institution-type="getInstitutionType(institution)"
            @click="selectInstitution(institution)"
          /&gt;
        &lt;/view&gt;

        &lt;view v-else-if="!loading &amp;&amp; searchQuery" class="empty-state"&gt;
          &lt;text class="empty-text"&gt;未找到相关机构&lt;/text&gt;
        &lt;/view&gt;

        &lt;view v-else-if="!loading" class="empty-state"&gt;
          &lt;text class="empty-text"&gt;暂无机构数据&lt;/text&gt;
        &lt;/view&gt;

        &lt;view class="manual-add-card" @tap="handleManualAdd"&gt;
          &lt;view class="manual-add-icon"&gt;
            &lt;text class="plus-sign"&gt;+&lt;/text&gt;
          &lt;/view&gt;
          &lt;view class="manual-add-info"&gt;
            &lt;text class="manual-add-title"&gt;手动添加其他机构&lt;/text&gt;
            &lt;text class="manual-add-desc"&gt;未在列表中找到您的机构？&lt;/text&gt;
          &lt;/view&gt;
          &lt;text class="arrow-icon"&gt;›&lt;/text&gt;
        &lt;/view&gt;
      &lt;/view&gt;
    &lt;/scroll-view&gt;
  &lt;/view&gt;
&lt;/template&gt;

&lt;script setup&gt;
import { ref, computed, onMounted } from 'vue'
import { getInstitutionTypes, getHotInstitutions, getInstitutionsByType, getAllInstitutions } from '@/api/modules/asset'
import InstitutionCard from '@/components/accounts/InstitutionCard.vue'

const searchQuery = ref('')
const activeTab = ref('HOT')
const loading = ref(false)
const institutionTypes = ref([])
const allInstitutions = ref([])
const currentInstitutions = ref([])

const tabs = ref([
  { code: 'HOT', name: '热门' },
  { code: 'BANK', name: '银行' },
  { code: 'PAYMENT', name: '支付' },
  { code: 'SECURITY', name: '证券' },
  { code: 'FUND_PLATFORM', name: '基金平台' },
  { code: 'INSURANCE', name: '保险' },
  { code: 'OTHER', name: '其他' }
])

const filteredInstitutions = computed(() =&gt; {
  if (!searchQuery.value) {
    return currentInstitutions.value
  }
  const query = searchQuery.value.toLowerCase()
  return currentInstitutions.value.filter(inst =&gt; {
    const name = (inst.instName || inst.shortName || '').toLowerCase()
    const code = (inst.instCode || '').toLowerCase()
    return name.includes(query) || code.includes(query)
  })
})

const getSectionTitle = () =&gt; {
  if (activeTab.value === 'HOT') {
    return '常用金融机构'
  }
  const tab = tabs.value.find(t =&gt; t.code === activeTab.value)
  return tab ? tab.name : '金融机构'
}

const getInstitutionType = (institution) =&gt; {
  const typeCode = institution.instType
  if (!typeCode) return null
  return institutionTypes.value.find(type =&gt; type.typeCode === typeCode)
}

const selectTab = async (tabCode) =&gt; {
  activeTab.value = tabCode
  await loadInstitutions()
}

const selectInstitution = (institution) =&gt; {
  uni.navigateTo({
    url: `/pages/accounts/add-account?instCode=${institution.instCode}`
  })
}

const handleManualAdd = () =&gt; {
  uni.showToast({
    title: '手动添加功能开发中',
    icon: 'none'
  })
}

const loadInstitutionTypes = async () =&gt; {
  try {
    const res = await getInstitutionTypes()
    if (res &amp;&amp; res.data) {
      institutionTypes.value = res.data
    }
  } catch (error) {
    console.error('加载机构类型失败:', error)
  }
}

const loadInstitutions = async () =&gt; {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'HOT') {
      res = await getHotInstitutions()
    } else if (activeTab.value === 'OTHER') {
      res = await getAllInstitutions()
    } else {
      res = await getInstitutionsByType(activeTab.value)
    }
    
    if (res &amp;&amp; res.data) {
      currentInstitutions.value = res.data
    }
  } catch (error) {
    console.error('加载机构列表失败:', error)
    currentInstitutions.value = getMockInstitutions()
  } finally {
    loading.value = false
  }
}

const getMockInstitutions = () =&gt; {
  return [
    { instCode: 'ICBC', instName: '中国工商银行', shortName: '工行', instType: 'BANK', isHot: true },
    { instCode: 'CCB', instName: '中国建设银行', shortName: '建行', instType: 'BANK', isHot: true },
    { instCode: 'ABC', instName: '中国农业银行', shortName: '农行', instType: 'BANK', isHot: true },
    { instCode: 'BOC', instName: '中国银行', shortName: '中行', instType: 'BANK', isHot: true },
    { instCode: 'CMB', instName: '招商银行', shortName: '招行', instType: 'BANK', isHot: true },
    { instCode: 'ALIPAY', instName: '支付宝', shortName: '支付宝', instType: 'PAYMENT', isHot: true },
    { instCode: 'WECHAT', instName: '微信支付', shortName: '微信', instType: 'PAYMENT', isHot: true },
    { instCode: 'CITIC', instName: '中信证券', shortName: '中信', instType: 'SECURITY', isHot: false }
  ]
}

onMounted(async () =&gt; {
  await loadInstitutionTypes()
  await loadInstitutions()
})
&lt;/script&gt;

&lt;style lang="scss" scoped&gt;
@import '@/styles/variables.scss';

.search-page {
  min-height: 100vh;
  background: $background;
}

.scroll-content {
  height: 100vh;
}

.search-section {
  padding: 32rpx;
}

.search-box {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: $surface-container-low;
  border-radius: 24rpx;
  gap: 16rpx;
}

.search-icon {
  font-size: 32rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: $on-surface;
}

.placeholder {
  color: rgba(107, 114, 128, 0.6);
}

.tabs-section {
  padding: 0 32rpx 32rpx;
}

.tabs-scroll {
  width: 100%;
}

.tabs-container {
  display: flex;
  gap: 16rpx;
  padding-right: 32rpx;
}

.tab-item {
  flex-shrink: 0;
  padding: 16rpx 32rpx;
  border-radius: 999rpx;
  background: $surface-container-high;
  transition: all 0.2s;
}

.tab-item.active {
  background: $primary;
}

.tab-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $on-surface-variant;
}

.tab-item.active .tab-text {
  color: $on-primary;
  font-weight: 700;
}

.institutions-section {
  padding: 0 32rpx 64rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: 700;
  color: $on-surface-variant;
}

.sort-link {
  font-size: 24rpx;
  font-weight: 700;
  color: $primary;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 64rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: $outline;
}

.institutions-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 64rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: $outline;
}

.manual-add-card {
  margin-top: 48rpx;
  display: flex;
  align-items: center;
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  border: 2rpx dashed $outline-variant;
}

.manual-add-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: $surface-container;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
}

.plus-sign {
  font-size: 48rpx;
  font-weight: 300;
  color: $outline;
}

.manual-add-info {
  flex: 1;
}

.manual-add-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $on-surface;
  display: block;
}

.manual-add-desc {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $outline;
  display: block;
}

.arrow-icon {
  font-size: 48rpx;
  font-weight: 300;
  color: $outline-variant;
}
&lt;/style&gt;

