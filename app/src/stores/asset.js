import { defineStore } from 'pinia'
import {
  getAssetList,
  getAssetDetail,
  createAsset,
  updateAsset,
  deleteAsset,
  getAssetCategories,
  getInstitutions,
  getAssetSummary,
  getAssetStructure
} from '@/api/modules/asset'

export const useAssetStore = defineStore('asset', {
  state: () => ({
    assetList: [],
    currentAsset: null,
    categories: [],
    institutions: [],
    summary: null,
    structure: null,
    loading: false,
    total: 0
  }),

  getters: {
    getAssetList: (state) => state.assetList,
    getCurrentAsset: (state) => state.currentAsset,
    getCategories: (state) => state.categories,
    getInstitutions: (state) => state.institutions,
    getSummary: (state) => state.summary,
    getStructure: (state) => state.structure
  },

  actions: {
    async fetchAssetList(params = {}) {
      this.loading = true
      try {
        const result = await getAssetList(params)
        this.assetList = result.list || result
        this.total = result.total || this.assetList.length
        return this.assetList
      } catch (error) {
        console.error('获取资产列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchAssetDetail(id) {
      try {
        const detail = await getAssetDetail(id)
        this.currentAsset = detail
        return detail
      } catch (error) {
        console.error('获取资产详情失败:', error)
        throw error
      }
    },

    async createAsset(data) {
      try {
        const result = await createAsset(data)
        await this.fetchAssetList()
        return result
      } catch (error) {
        console.error('创建资产失败:', error)
        throw error
      }
    },

    async updateAsset(id, data) {
      try {
        const result = await updateAsset(id, data)
        await this.fetchAssetList()
        return result
      } catch (error) {
        console.error('更新资产失败:', error)
        throw error
      }
    },

    async deleteAsset(id) {
      try {
        const result = await deleteAsset(id)
        await this.fetchAssetList()
        return result
      } catch (error) {
        console.error('删除资产失败:', error)
        throw error
      }
    },

    async fetchCategories() {
      try {
        const categories = await getAssetCategories()
        this.categories = categories
        return categories
      } catch (error) {
        console.error('获取资产分类失败:', error)
        throw error
      }
    },

    async fetchInstitutions() {
      try {
        const institutions = await getInstitutions()
        this.institutions = institutions
        return institutions
      } catch (error) {
        console.error('获取机构列表失败:', error)
        throw error
      }
    },

    async fetchSummary() {
      try {
        const summary = await getAssetSummary()
        this.summary = summary
        return summary
      } catch (error) {
        console.error('获取资产概要失败:', error)
        throw error
      }
    },

    async fetchStructure() {
      try {
        const structure = await getAssetStructure()
        this.structure = structure
        return structure
      } catch (error) {
        console.error('获取资产结构失败:', error)
        throw error
      }
    },

    clearAssetData() {
      this.assetList = []
      this.currentAsset = null
      this.summary = null
      this.structure = null
    }
  }
})
