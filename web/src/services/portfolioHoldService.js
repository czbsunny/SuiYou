// 组合持仓相关API服务模块
import { get, post, put, del } from './apiService';

const PORTFOLIO_HOLDING_API_BASE = '/portfolioholdings';

/**
 * 根据ID获取组合持仓
 * @param {number|string} holdingId - 持仓ID
 * @returns {Promise<Object>} 持仓对象
 */
export const getPortfolioHoldingById = async (holdingId) => {
  try {
    const response = await get(`${PORTFOLIO_HOLDING_API_BASE}/${holdingId}`);
    
    if (response.statusCode === 200 && response.data?.holding) {
      return response.data.holding;
    } else {
      throw new Error(response.data?.message || '获取持仓失败');
    }
  } catch (error) {
    console.error('获取持仓错误:', error);
    throw error;
  }
};

/**
 * 创建组合持仓
 * @param {Object} holdingData - 持仓数据
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Object>} 创建结果
 */
export const createPortfolioHolding = async (holdingData, portfolioId) => {
  try {
    const response = await post(PORTFOLIO_HOLDING_API_BASE, holdingData, { params: { portfolioId } });
    
    if (response.statusCode === 201) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '创建持仓失败');
    }
  } catch (error) {
    console.error('创建持仓错误:', error);
    throw error;
  }
};

/**
 * 更新组合持仓
 * @param {number|string} holdingId - 持仓ID
 * @param {Object} holdingData - 持仓数据
 * @returns {Promise<Object>} 更新结果
 */
export const updatePortfolioHolding = async (holdingId, holdingData) => {
  try {
    const response = await put(`${PORTFOLIO_HOLDING_API_BASE}/${holdingId}`, holdingData);
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '更新持仓失败');
    }
  } catch (error) {
    console.error('更新持仓错误:', error);
    throw error;
  }
};

/**
 * 删除组合持仓
 * @param {number|string} holdingId - 持仓ID
 * @returns {Promise<Object>} 删除结果
 */
export const deletePortfolioHolding = async (holdingId) => {
  try {
    const response = await del(`${PORTFOLIO_HOLDING_API_BASE}/${holdingId}`);
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '删除持仓失败');
    }
  } catch (error) {
    console.error('删除持仓错误:', error);
    throw error;
  }
};

export default {
  getPortfolioHoldingById,
  createPortfolioHolding,
  updatePortfolioHolding,
  deletePortfolioHolding
};