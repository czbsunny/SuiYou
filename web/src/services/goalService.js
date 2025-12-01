/**
 * 理财目标服务类
 * 提供理财目标相关的API调用功能
 */

import apiService from './apiService.js'

/**
 * 获取用户的理财目标列表
 * @returns {Promise<Array>} 理财目标列表
 */
export const getUserGoals = async () => {
  try {
    const res = await apiService.get("/api/financial-goals")
    if (res.statusCode === 200) {
      const goals = res.data || []
      return goals.map((goal) => ({
        ...goal,
        completionRate: goal.targetAmount > 0 ? (goal.currentAmount / goal.targetAmount * 100).toFixed(2) : 0
      }))
    }
    throw new Error((res.data?.detail) || "获取理财目标失败")
  } catch (error) {
    console.error("获取理财目标失败:", error)
    throw error
  }
}

/**
 * 创建新的理财目标
 * @param {Object} goalData - 理财目标数据
 * @returns {Promise<Object>} 创建的理财目标
 */
export const createGoal = async (goalData) => {
  try {
    const res = await apiService.post("/api/financial-goals", goalData)
    if (res.statusCode === 200 && res.data.success) {
      return res.data.data
    }
    throw new Error((res.data?.message) || "创建理财目标失败")
  } catch (error) {
    console.error("创建理财目标失败:", error)
    throw error
  }
}

/**
 * 关联投资组合到理财目标
 * @param {string} goalId - 理财目标ID
 * @param {Object} changes - 关联变更数据
 * @returns {Promise<Object>} 关联结果
 */
export const associatePortfolios = async (goalId, changes) => {
  try {
    const res = await apiService.post("/api/financial-goals/associate-portfolios", {
      goal_id: goalId,
      changes
    })
    if (res.statusCode === 200 && res.data.success) {
      return res.data.data
    }
    throw new Error((res.data?.message) || "关联投资组合失败")
  } catch (error) {
    console.error("关联投资组合失败:", error)
    throw error
  }
}

/**
 * 更新理财目标
 * @param {string} goalId - 理财目标ID
 * @param {Object} goalData - 更新的理财目标数据
 * @returns {Promise<Object>} 更新后的理财目标
 */
export const updateGoal = async (goalId, goalData) => {
  try {
    const res = await apiService.put(`/api/financial-goals/${goalId}`, goalData)
    if (res.statusCode === 200 && res.data.success) {
      return res.data.data
    }
    throw new Error((res.data?.message) || "更新理财目标失败")
  } catch (error) {
    console.error("更新理财目标失败:", error)
    throw error
  }
}

/**
 * 删除理财目标
 * @param {string} goalId - 理财目标ID
 * @returns {Promise<boolean>} 是否删除成功
 */
export const deleteGoal = async (goalId) => {
  try {
    const res = await apiService.del(`/api/financial-goals/${goalId}`)
    if (res.statusCode === 200 && res.data.success) {
      return true
    }
    throw new Error((res.data?.message) || "删除理财目标失败")
  } catch (error) {
    console.error("删除理财目标失败:", error)
    throw error
  }
}

/**
 * 获取理财目标详情
 * @param {string} goalId - 理财目标ID
 * @returns {Promise<Object>} 理财目标详情
 */
export const getGoalDetail = async (goalId) => {
  try {
    const res = await apiService.get(`/api/financial-goals/${goalId}`)
    if (res.statusCode === 200) {
      const goal = res.data
      return {
        ...goal,
        completionRate: goal.targetAmount > 0 ? (goal.currentAmount / goal.targetAmount * 100).toFixed(2) : 0
      }
    }
    throw new Error((res.data?.detail) || "获取理财目标详情失败")
  } catch (error) {
    console.error("获取理财目标详情失败:", error)
    throw error
  }
}

/**
 * 添加资金到理财目标
 * @param {string} goalId - 理财目标ID
 * @param {number} amount - 金额
 * @returns {Promise<Object>} 更新后的理财目标
 */
export const addFundsToGoal = async (goalId, amount) => {
  try {
    const res = await apiService.post(`/api/financial-goals/${goalId}/add-funds`, {
      amount
    })
    if (res.statusCode === 200 && res.data.success) {
      return res.data.data
    }
    throw new Error((res.data?.message) || "添加资金失败")
  } catch (error) {
    console.error("添加资金失败:", error)
    throw error
  }
}

/**
 * 从理财目标中提取资金
 * @param {string} goalId - 理财目标ID
 * @param {number} amount - 金额
 * @returns {Promise<Object>} 更新后的理财目标
 */
export const withdrawFromGoal = async (goalId, amount) => {
  try {
    const res = await apiService.post(`/api/financial-goals/${goalId}/withdraw-funds`, {
      amount
    })
    if (res.statusCode === 200 && res.data.success) {
      return res.data.data
    }
    throw new Error((res.data?.message) || "提取资金失败")
  } catch (error) {
    console.error("提取资金失败:", error)
    throw error
  }
}

/**
 * 获取理财目标的关联投资组合
 * @param {string} goalId - 理财目标ID
 * @returns {Promise<Array>} 关联的投资组合列表
 */
export const getGoalPortfolios = async (goalId) => {
  try {
    const res = await apiService.get(`/api/financial-goals/${goalId}/portfolios`)
    if (res.statusCode === 200) {
      return res.data || []
    }
    throw new Error((res.data?.detail) || "获取关联投资组合失败")
  } catch (error) {
    console.error("获取关联投资组合失败:", error)
    throw error
  }
}

// 导出所有函数
export default {
  getUserGoals,
  createGoal,
  updateGoal,
  deleteGoal,
  getGoalDetail,
  addFundsToGoal,
  withdrawFromGoal,
  associatePortfolios,
  getGoalPortfolios
}