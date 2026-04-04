/**
 * 基金服务，处理基金相关的API调用
 */
import { get, post, del } from './apiService';

/**
 * 获取资产账户列表
 * @returns {Promise<Array>} 账户列表
 */
export const getAssetAccounts = async () => {
  try {
    const res = await get('/api/portfolio/accounts');
    
    if (res.statusCode === 200 && res.data) {
      return Array.isArray(res.data) ? res.data : [];
    } else {
      console.error('获取账户列表失败:', res.data?.message || '未知错误');
      return [];
    }
  } catch (error) {
    console.error('获取账户列表异常:', error);
    return [];
  }
};

/**
 * 计算汇总数据
 * @param {Array} accounts - 账户列表
 * @returns {Object} 汇总数据
 */
export const calculateSummaryData = (accounts) => {
  if (!Array.isArray(accounts) || accounts.length === 0) {
    return {
      totalAsset: 0,
      dailyReturn: 0,
      dailyReturnRate: 0,
      totalReturn: 0,
      totalReturnRate: 0
    };
  }
  
  const totalAsset = accounts.reduce((sum, account) => sum + (account.asset || 0), 0);
  const dailyReturn = accounts.reduce((sum, account) => sum + (account.dailyReturn || 0), 0);
  const totalReturn = accounts.reduce((sum, account) => sum + (account.totalReturn || 0), 0);
  
  const dailyReturnRate = totalAsset > 0 ? (dailyReturn / totalAsset * 100).toFixed(2) : 0;
  const totalReturnRate = totalAsset > 0 ? (totalReturn / totalAsset * 100).toFixed(2) : 0;
  
  return {
    totalAsset,
    dailyReturn,
    dailyReturnRate: parseFloat(dailyReturnRate),
    totalReturn,
    totalReturnRate: parseFloat(totalReturnRate)
  };
};

/**
 * 根据ID获取账户
 * @param {Array} accounts - 账户列表
 * @param {number|string} accountId - 账户ID
 * @returns {Object|null} 账户对象
 */
export const getAccountById = (accounts, accountId) => {
  if (!Array.isArray(accounts)) return null;
  return accounts.find(account => account.id === accountId) || null;
};

/**
 * 获取账户持仓
 * @param {number|string} accountId - 账户ID
 * @returns {Promise<Array>} 持仓列表
 */
export const getAccountHoldings = async (accountId) => {
  try {
    const res = await get(`/api/portfolio/${accountId}/holdings`);
    
    if (res.statusCode === 200 && res.data) {
      return Array.isArray(res.data) ? res.data : [];
    } else {
      console.error('获取账户持仓失败:', res.data?.message || '未知错误');
      return [];
    }
  } catch (error) {
    console.error('获取账户持仓异常:', error);
    return [];
  }
};

/**
 * 删除持仓
 * @param {number|string} accountId - 账户ID
 * @param {number|string} holdingId - 持仓ID
 * @returns {Promise<Object>} 响应结果
 */
export const deleteHolding = async (accountId, holdingId) => {
  try {
    const res = await del(`/api/portfolio/${accountId}/holdings/${holdingId}`);
    
    if (res.statusCode === 200 && res.data.status === 'success') {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('删除持仓失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '删除持仓失败');
    }
  } catch (error) {
    console.error('删除持仓异常:', error);
    throw error;
  }
};


/**
 * 基金搜索
 * @param {string} keyword - 搜索关键词
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页条数
 * @returns {Promise<Object>} 搜索结果
 */
export const fundSearch = async (keyword, page = 1, pageSize = 20) => {
  try {
    const res = await get('/api/fund/search', {
      keyword,
      page,
      pageSize
    });
    
    if (res.statusCode === 200) {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('基金搜索失败:', res.data?.message || '未知错误');
      return {
        success: false,
        data: null,
        message: res.data?.message || '基金搜索失败'
      };
    }
  } catch (error) {
    console.error('基金搜索异常:', error);
    return {
      success: false,
      data: null,
      message: error.message || '网络异常，请稍后重试'
    };
  }
};


/**
 * 添加基金持仓
 * @param {number|string} accountId - 账户ID
 * @param {Array} holdingList - 持仓列表
 * @returns {Promise<Object>} 响应结果
 */
export const addFundHolding = async (accountId, holdingList) => {
  try {
    const res = await post(`/api/portfolio/${accountId}/funds`, holdingList);
    
    if (res.statusCode === 200 && res.data.status === 'success') {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('添加基金持仓失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '添加基金持仓失败');
    }
  } catch (error) {
    console.error('添加基金持仓异常:', error);
    throw error;
  }
};

/**
 * 图片识别导入基金持仓
 * @param {string} filePath - 图片文件路径
 * @returns {Promise<Object>} 识别结果
 */
export const importFundHolding = async (filePath) => {
  try {
    const token = uni.getStorageSync('token');
    const res = await uni.uploadFile({
      url: '/api/portfolio/import/image',
      filePath: filePath,
      name: 'file',
      header: { 'Authorization': token ? `Bearer ${token}` : '' }
    });
    
    const responseData = JSON.parse(res.data);
    if (res.statusCode === 200 && responseData.status === 'success') {
      return {
        success: true,
        data: responseData
      };
    } else {
      console.error('图片识别失败:', responseData?.message || '未知错误');
      return {
        success: false,
        data: null,
        message: responseData?.message || '图片识别失败'
      };
    }
  } catch (error) {
    console.error('图片识别异常:', error);
    return {
      success: false,
      data: null,
      message: error.message || '网络异常，请稍后重试'
    };
  }
};

/**
 * 更新基金持仓
 * @param {number|string} accountId - 账户ID
 * @param {string} holdingId - 持仓ID
 * @param {Object} holdingData - 更新的持仓数据
 * @returns {Promise<Object>} 响应结果
 */
export const updateFundHoldings = async (accountId, holdingId, holdingData) => {
  try {
    const res = await post(`/api/portfolio/${accountId}/funds/${holdingId}`, holdingData);
    
    if (res.statusCode === 200 && res.data.status === 'success') {
      return {
        success: true,
        data: res.data
      };
    } else {
      console.error('更新基金持仓失败:', res.data?.message || '未知错误');
      throw new Error(res.data?.message || '更新基金持仓失败');
    }
  } catch (error) {
    console.error('更新基金持仓异常:', error);
    throw error;
  }
};


