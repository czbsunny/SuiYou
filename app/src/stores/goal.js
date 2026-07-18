import { defineStore } from 'pinia'
import {
  getGoalList,
  getGoalDetail,
  createGoal,
  deleteGoal,
  getGoalCategories,
  getGoalTemplates
} from '@/api/modules/goal'

export const useGoalStore = defineStore('goal', {
  state: () => ({
    goalList: [],
    currentGoal: null,
    categories: [],
    templates: [],
    progress: null,
    loading: false,
    total: 0
  }),

  getters: {
    getGoalList: (state) => state.goalList,
    getCurrentGoal: (state) => state.currentGoal,
    getCategories: (state) => state.categories,
    getTemplates: (state) => state.templates,
    getProgress: (state) => state.progress,
    activeGoals: (state) => state.goalList.filter(g => !g.completed),
    completedGoals: (state) => state.goalList.filter(g => g.completed)
  },

  actions: {
    async fetchGoalList(params = {}) {
      this.loading = true
      try {
        const res = await getGoalList(params)
        const result = res.data
        this.goalList = result.goals || []
        this.total = result.count || this.goalList.length
        return this.goalList
      } catch (error) {
        console.error('获取目标列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchGoalDetail(id) {
      try {
        const res = await getGoalDetail(id)
        const detail = res.data
        this.currentGoal = detail.goal || detail
        return this.currentGoal
      } catch (error) {
        console.error('获取目标详情失败:', error)
        throw error
      }
    },

    async createGoal(data) {
      try {
        const res = await createGoal(data)
        const result = res.data
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('创建目标失败:', error)
        throw error
      }
    },

    async deleteGoal(id) {
      try {
        const res = await deleteGoal(id)
        const result = res.data
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('删除目标失败:', error)
        throw error
      }
    },

    async fetchCategories() {
      try {
        const res = await getGoalCategories()
        const result = res.data
        this.categories = result.categories || []
        return this.categories
      } catch (error) {
        console.error('获取目标分类失败:', error)
        throw error
      }
    },

    async fetchTemplates(categoryCode) {
      try {
        const res = await getGoalTemplates(categoryCode)
        const result = res.data
        this.templates = result.templates || []
        return this.templates
      } catch (error) {
        console.error('获取目标模板失败:', error)
        throw error
      }
    },

    clearGoalData() {
      this.goalList = []
      this.currentGoal = null
      this.progress = null
    }
  }
})
