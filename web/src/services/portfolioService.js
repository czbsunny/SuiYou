/**
 * 组合服务，处理组合相关的API调用
 */
import { get, post, put, del } from './apiService';

/**
 * 获取用户的所有组合列表
 * @returns {Promise<Object>} 组合列表数据
 */
export const getPortfolios = async () => {
  try {
    const res = await get('/api/portfolios');
    
    if (res.statusCode === 200 && res.data) {
      const portfoliosData = Array.isArray(res.data) ? res.data : [];
      return {
        success: true,
        data: portfoliosData
      };
    } else {
      console.error('获取组合列表失败:', res.data?.message || '未知错误');
      return {
        success: false,
        data: [],
        message: res.data?.message || '获取组合列表失败'
      };
    }
  } catch (error) {
    console.error('获取组合列表异常:', error);
    return {
      success: false,
      data: [],
      message: error.message || '网络异常，请稍后重试'
    };
  }
};

/**
 * 获取组合中的基金列表
 * @param {number|string} portfolioId - 组合ID
 * @returns {Promise<Object>} 基金列表数据
 */
export const getPortfolioItems = async (portfolioId) => {
  try {
    const res = await get(`/api/portfolios/${portfolioId}/items`);
    
    if (res.statusCode === 200 && res.data) {
      const fundList = res.data.map(item => ({
        id: item.id,
        name: item.name || '',
        symbol: item.symbol || '',
        holdingAmount: item.amount || 0, // Normalized to match existing UI usage
        returnValue: item.returnValue || 0,
        returnRate: item.returnRate || 0,
        dailyReturn: item.dailyReturn || 0,
        dailyReturnRate: item.dailyReturnRate || 0,
        dailyReturnDate: '',
        // Maintain backward compatibility fields if needed
        code: item.symbol || '',
        totalReturn: item.returnValue || 0,
        totalGainPercent: item.returnRate ? item.returnRate * 100 : 0,
        dayGrowthPercent: item.dailyReturnRate ? item.dailyReturnRate * 100 : 0
      }));
      
      return {
        success: true,
        data: fundList
      };
    } else {
      console.error('获取组合基金列表失败:', res.data?.message || '未知错误');
      return {
        success: false,
        data: [],
        message: res.data?.message || '获取组合基金列表失败'
      };
    }
  } catch (error) {
    console.error('获取组合基金列表异常:', error);
    return {
      success: false,
      data: [],
      message: error.message || '网络异常，请稍后重试'
    };
  }
};

/**
 * 创建新组合
 * @param {Object} portfolioData - 组合数据
 * @param {string} portfolioData.name - 组合名称
 * @param {string} portfolioData.description - 组合描述
 * @param {number} portfolioData.initialInvestment - 初始投资金额
 * @param {string} portfolioData.riskLevel - 风险等级
 * @param {Array} portfolioData.tags - 标签数组
 * @returns {Promise<Object>} 创建结果
 */
export const createPortfolio = async (portfolioData) => {
  try {
    const res = await post('/api/portfolios', {
      name: portfolioData.name,
      description: portfolioData.description,
      initialInvestment: portfolioData.initialInvestment || 0,
      riskLevel: portfolioData.riskLevel,
      tags: portfolioData.tags
    });
    
    if (res.statusCode === 201 && res.data) {
      return {
        success: true,
        data: {
          portfolioId: res.data.id,
          ...res.data
        }
      };
    } else {
      console.error('创建组合失败:', res.data?.message || '未知错误');
      return {
        success: false,
        message: res.data?.message || '创建组合失败'
      };
    }
  } catch (error) {
    console.error('创建组合异常:', error);
    return {
      success: false,
      message: error.message || '网络异常，请稍后重试'
    };
  }
};

/**
 * 更新组合
 * @param {string} id - 组合ID
 * @param {Object} data - 更新数据
 * @returns {Promise<Object>} 更新结果
 */
export const updatePortfolio = async (id, data) => {
  try {
    const res = await put(`/api/portfolios/${id}`, data);
    if (res.statusCode === 200) {
      return { success: true, data: res.data };
    }
    return { success: false, message: res.data?.message || '更新失败' };
  } catch (error) {
    return { success: false, message: error.message || '网络异常' };
  }
};

/**
 * 删除组合
 * @param {string} id - 组合ID
 * @returns {Promise<Object>} 删除结果
 */
export const deletePortfolio = async (id) => {
  try {
    const res = await del(`/api/portfolios/${id}`);
    if (res.statusCode === 200 || res.statusCode === 204) {
      return { success: true };
    }
    return { success: false, message: res.data?.message || '删除失败' };
  } catch (error) {
    return { success: false, message: error.message || '网络异常' };
  }
};

/**
 * 获取组合汇总信息
 * @param {Array} portfolios - 组合列表
 * @returns {Object} 汇总信息
 */
export const getPortfolioSummary = (portfolios) => {
  if (!Array.isArray(portfolios) || portfolios.length === 0) {
    return {
      totalCount: 0,
      totalValue: 0,
      dailyReturn: 0,
      dailyReturnRate: 0,
      totalReturn: 0,
      totalReturnRate: 0
    };
  }
  
  const totalCount = portfolios.length;
  const totalValue = portfolios.reduce((sum, p) => sum + (p.currentValue || 0), 0);
  const totalReturn = portfolios.reduce((sum, p) => sum + (p.totalReturn || 0), 0);
  const dailyReturn = portfolios.reduce((sum, p) => sum + (p.dailyReturn || 0), 0);
  
  const totalInvestment = totalValue - totalReturn;
  const totalReturnRate = totalInvestment > 0 ? (totalReturn / totalInvestment) * 100 : 0;
  const dailyReturnRate = totalValue > 0 ? (dailyReturn / totalValue) * 100 : 0;
  
  return {
    totalCount,
    totalValue,
    dailyReturn,
    dailyReturnRate,
    totalReturn,
    totalReturnRate
  };
};

// 默认导出，同时支持命名导入和默认导入
export default {
  getPortfolios,
  getPortfolioItems,
  createPortfolio,
  updatePortfolio,
  deletePortfolio,
  getPortfolioSummary
};