import { defineStore } from 'pinia'
import {
  getGoalList,
  getGoalDetail,
  createGoal,
  updateGoal,
  deleteGoal,
  completeGoal,
  getGoalCategories,
  getGoalTemplates,
  getGoalProgress
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
        const result = await getGoalList(params)
        this.goalList = result.list || result
        this.total = result.total || this.goalList.length
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
        const detail = await getGoalDetail(id)
        this.currentGoal = detail
        return detail
      } catch (error) {
        console.error('获取目标详情失败:', error)
        throw error
      }
    },

    async createGoal(data) {
      try {
        const result = await createGoal(data)
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('创建目标失败:', error)
        throw error
      }
    },

    async updateGoal(id, data) {
      try {
        const result = await updateGoal(id, data)
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('更新目标失败:', error)
        throw error
      }
    },

    async deleteGoal(id) {
      try {
        const result = await deleteGoal(id)
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('删除目标失败:', error)
        throw error
      }
    },

    async completeGoalAction(id) {
      try {
        const result = await completeGoal(id)
        await this.fetchGoalList()
        return result
      } catch (error) {
        console.error('完成目标失败:', error)
        throw error
      }
    },

    async fetchCategories() {
      try {
        const categories = await getGoalCategories()
        this.categories = categories
        return categories
      } catch (error) {
        console.error('获取目标分类失败:', error)
        throw error
      }
    },

    async fetchTemplates() {
      try {
        const templates = await getGoalTemplates()
        this.templates = templates
        return templates
      } catch (error) {
        console.error('获取目标模板失败:', error)
        throw error
      }
    },

    async fetchProgress(id) {
      try {
        const progress = await getGoalProgress(id)
        this.progress = progress
        return progress
      } catch (error) {
        console.error('获取目标进度失败:', error)
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
