/**
 * 组合服务，处理组合相关的API调用
 */
import { get, post, put, del } from './apiService';
const PORTFOLIO_API_BASE = '/api/portfolios';

/**
 * 获取组合列表
 * @returns {Promise<Array>} 组合列表
 */
export const getPortfolios = async () => {
  try {
    const res = await get(PORTFOLIO_API_BASE);
    
    if (res.statusCode === 200 && res.data?.portfolios) {
      return res.data.portfolios;
    } else {
      console.error('获取组合列表失败:', res.data?.message || '未知错误');
      return [];
    }
  } catch (error) {
    console.error('获取组合列表异常:', error);
    return [];
  }
};

/**
 * 创建组合
 * @param {Object} portfolioData - 组合数据
 * @returns {Promise<Object>} 创建结果
 */
export const createPortfolio = async (portfolioData) => {
  try {
    const res = await post(PORTFOLIO_API_BASE, portfolioData);
    
    if (res.statusCode === 201) {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('创建组合失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '创建组合失败');
    }
  } catch (error) {
    console.error('创建组合异常:', error);
    throw error;
  }
};

/**
 * 获取单个组合
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Object|null>} 组合对象
 */
export const getPortfolioById = async (portfolioId) => {
  try {
    const res = await get(`${PORTFOLIO_API_BASE}/${portfolioId}`);
    
    if (res.statusCode === 200 && res.data?.portfolio) {
      return res.data.portfolio;
    } else {
      console.error('获取组合失败:', res.data?.message || '未知错误');
      return null;
    }
  } catch (error) {
    console.error('获取组合异常:', error);
    return null;
  }
};

/**
 * 删除组合
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Object>} 删除结果
 */
export const deletePortfolio = async (portfolioId) => {
  try {
    const res = await del(`${PORTFOLIO_API_BASE}/${portfolioId}`);
    
    if (res.statusCode === 200) {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('删除组合失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '删除组合失败');
    }
  } catch (error) {
    console.error('删除组合异常:', error);
    throw error;
  }
};

/**
 * 获取组合持仓
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Array>} 持仓列表
 */
export const getPortfolioHoldings = async (portfolioId) => {
  try {
    const res = await get(`${PORTFOLIO_API_BASE}/${portfolioId}/holdings`);
    
    if (res.statusCode === 200 && res.data?.holdings) {
      return res.data.holdings;
    } else {
      console.error('获取组合持仓失败:', res.data?.message || '未知错误');
      return [];
    }
  } catch (error) {
    console.error('获取组合持仓异常:', error);
    return [];
  }
};

/**
 * 更新组合持仓
 * @param {number|string} portfolioId - 组合ID
 * @param {Object} holdingsData - 持仓数据
 * @returns {Promise<Object>} 更新结果
 */
export const updatePortfolioHoldings = async (portfolioId, holdingsData) => {
  try {
    const res = await put(`${PORTFOLIO_API_BASE}/${portfolioId}/holdings`, holdingsData);
    
    if (res.statusCode === 200) {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('更新组合持仓失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '更新组合持仓失败');
    }
  } catch (error) {
    console.error('更新组合持仓异常:', error);
    throw error;
  }
};

/**
 * 清空组合持仓
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Object>} 清空结果
 */
export const clearPortfolioHoldings = async (portfolioId) => {
  try {
    const res = await del(`${PORTFOLIO_API_BASE}/${portfolioId}/holdings`);
    
    if (res.statusCode === 200) {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('清空组合持仓失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '清空组合持仓失败');
    }
  } catch (error) {
    console.error('清空组合持仓异常:', error);
    throw error;
  }
};

// 默认导出，同时支持命名导入和默认导入
export default {
  getPortfolios,
  createPortfolio,
  getPortfolioById,
  deletePortfolio,
  getPortfolioHoldings,
  updatePortfolioHoldings,
  clearPortfolioHoldings
};