/**
 * goalService.js - 目标模块业务接口
 */
import api from './apiService'; // 假设你的 apiService.js 就在同级目录

const GOAL_API_BASE = '/api/goals'; // 根据后端实际 Controller 路径调整

/**
 * 创建新目标
 * @param {Object} goalData - 前端表单数据
 * @returns {Promise<Object>} 创建成功的对象
 */
export const createGoal = async (goalData) => {
  // 1. 严格按照后端 CreateGoalDTO 进行字段映射与格式化
  const payload = {
    name: goalData.title || goalData.name, // 适配你之前代码中的 title 字段
    targetAmount: parseFloat(goalData.targetAmount),
    deadline: goalData.deadline, 
    startDate: goalData.startDate || new Date().toISOString().split('T')[0],
    visibleScope: goalData.visibleScope || 'PRIVATE',
    isPrimary: goalData.isPrimary || false,
    iconUrl: goalData.iconUrl,
    bgUrl: goalData.bgUrl || ''
  };

  try {
    const res = await api.post(GOAL_API_BASE, payload);
    
    // apiService 返回的是 { statusCode, data }
    if (res.statusCode === 200 || res.statusCode === 201) {
      return res.data;
    } else {
      // 处理后端校验失败抛出的消息
      const errorMsg = res.data?.message || '创建目标失败';
      throw new Error(errorMsg);
    }
  } catch (error) {
    console.error('Goal Create Error:', error);
    throw error;
  }
};

/**
 * 获取当前用户的所有目标列表
 * @returns {Promise<Array>} 目标数组
 */
export const fetchUserGoals = async () => {
  try {
    const res = await api.get(GOAL_API_BASE);
    
    if (res.statusCode === 200) {
      // 这里的 res.data 就是后端 getUserGoals 返回的 List<Goal>
      return res.data || [];
    }
    return [];
  } catch (error) {
    console.error('Fetch Goals Error:', error);
    // 发生网络错误等，返回空数组防止页面崩溃
    return [];
  }
};

/**
 * 获取单个目标详情 (预留)
 * @param {String|Number} goalId 
 */
export const getGoalDetail = async (goalId) => {
  const res = await api.get(`${GOAL_API_BASE}/${goalId}`);
  return res.data;
};

/**
 * 删除目标 (预留)
 * @param {String|Number} goalId 
 */
export const deleteGoal = async (goalId) => {
  const res = await api.del(`${GOAL_API_BASE}/${goalId}`);
  return res.statusCode === 200 || res.statusCode === 204;
};

export const goalService = {
  createGoal,
  fetchUserGoals,
  getGoalDetail,
  deleteGoal
};

export default goalService;