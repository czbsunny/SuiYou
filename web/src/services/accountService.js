// 账户相关API服务模块
import { get, post, put, del } from './apiService';

/**
 * 获取用户的所有资产账户
 * @returns {Promise<Array>} 账户列表
 */
export const getAssetAccounts = async () => {
  try {
    const response = await get('/api/asset-account');
    
    if (response.statusCode === 200) {
      // 格式化账户数据
      return response.data.map(account => ({
        id: String(account.id), // 确保id为字符串类型
        name: account.name,
        icon: account.icon || '💰',
        totalAsset: account.totalAsset || 0.00,
        dailyReturn: account.dailyReturn || 0.00,
        dailyReturnRate: account.dailyReturnRate || 0.00,
        totalReturn: account.totalReturn || 0.00,
        totalReturnRate: account.totalReturnRate || 0.00
      }));
    } else {
      throw new Error('获取账户数据失败');
    }
  } catch (error) {
    console.error('获取账户数据错误:', error);
    throw error;
  }
};

/**
 * 创建新的资产账户
 * @param {Object} accountData - 账户数据
 * @param {string} accountData.name - 账户名称
 * @param {string} [accountData.description] - 账户描述
 * @param {string} [accountData.platform] - 账户平台
 * @param {string} [accountData.icon] - 账户图标
 * @returns {Promise<Object>} 创建的账户信息
 */
export const createAssetAccount = async (accountData) => {
  try {
    // 准备创建数据，确保只包含后端接口接受的字段
    const createData = {
      name: accountData.name
    };
    
    if (accountData.description !== undefined) {
      createData.description = accountData.description;
    }
    
    const response = await post('/api/asset-account', createData);
    
    if (response.statusCode === 201 || response.statusCode === 200) {
      // 格式化返回的账户数据
      return {
        id: String(response.data.id),
        name: response.data.name,
        icon: response.data.icon || '',
        totalAsset: response.data.totalAsset || 0.00,
        dailyReturn: response.data.dailyReturn || 0.00,
        dailyReturnRate: response.data.dailyReturnRate || 0.00,
        totalReturn: response.data.totalReturn || 0.00,
        totalReturnRate: response.data.totalReturnRate || 0.00
      };
    } else if (response.statusCode === 400) {
      throw new Error(response.data?.message || '参数错误');
    } else {
      throw new Error(response.data?.message || '创建账户失败');
    }
  } catch (error) {
    console.error('创建账户错误:', error);
    throw error;
  }
};

/**
 * 更新资产账户信息
 * @param {string} accountId - 资产账户ID
 * @param {Object} data - 更新数据
 * @param {string} [data.name] - 账户名称
 * @param {string} [data.description] - 账户描述
 * @param {string} [data.platform] - 账户平台
 * @param {string} [data.icon] - 账户图标
 * @returns {Promise<Object>} 更新后的账户信息
 */
export const updateAssetAccount = async (accountId, data) => {
  try {
    // 准备更新数据，确保只包含后端接口接受的字段
    const updateData = {};
    if (data.name !== undefined) updateData.name = data.name;
    if (data.description !== undefined) updateData.description = data.description;
    
    const response = await put(`/api/asset-account/${accountId}`, updateData);
    
    if (response.statusCode === 200) {
      // 格式化返回的账户数据
      return {
        id: String(response.data.id),
        name: response.data.name,
        icon: response.data.icon || '',
        totalAsset: response.data.totalAsset || 0.00,
        dailyReturn: response.data.dailyReturn || 0.00,
        dailyReturnRate: response.data.dailyReturnRate || 0.00,
        totalReturn: response.data.totalReturn || 0.00,
        totalReturnRate: response.data.totalReturnRate || 0.00
      };
    } else if (response.statusCode === 404) {
      throw new Error('账户不存在');
    } else if (response.statusCode === 403) {
      throw new Error('无权修改此账户');
    } else {
      throw new Error(response.data?.message || '更新账户失败');
    }
  } catch (error) {
    console.error('更新账户错误:', error);
    throw error;
  }
};

/**
 * 删除资产账户
 * @param {string} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const deleteAssetAccount = async (accountId) => {
  try {
    const response = await del(`/api/asset-account/${accountId}`);
    
    if (response.statusCode === 204 || response.statusCode === 200) {
      // 删除成功，返回空
      return;
    } else if (response.statusCode === 404) {
      throw new Error('账户不存在');
    } else if (response.statusCode === 403) {
      throw new Error('无权删除此账户');
    } else {
      throw new Error(response.data?.message || '删除账户失败');
    }
  } catch (error) {
    console.error('删除账户错误:', error);
    throw error;
  }
};

/**
 * 获取指定账户的持仓数据
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<Array>} 持仓数据列表
 */
export const getAccountHoldings = async (accountId) => {
  try {
    const response = await get(`/api/asset-account/${accountId}/holdings`);
    
    if (response.statusCode === 200) {
      return response.data.map(holding => ({
        id: holding.id,
        code: holding.symbol,
        name: holding.name,
        quantity: holding.quantity,
        dailyReturn: holding.dailyReturn || 0.00,
        dailyReturnRate: holding.dailyReturnRate || 0.00,
        returnValue: holding.returnValue || 0.00,
        returnRate: holding.returnRate || 0.00,
        amount: holding.amount || 0.00
      }));
    } else {
      throw new Error(response.data.message || '获取账户持仓数据失败');
    }
  } catch (error) {
    console.error('获取账户持仓数据错误:', error);
    throw error;
  }
};

/**
 * 检查用户登录状态
 * @returns {Object} 包含登录状态和用户信息的对象
 */
export const checkLoginStatus = () => {
  const token = uni.getStorageSync('token');
  const userInfo = uni.getStorageSync('userInfo');
  
  return {
    isLoggedIn: !!token && !!userInfo,
    userInfo: userInfo
  };
};

/**
 * 计算账户汇总数据
 * @param {Array} accounts - 账户列表
 * @returns {Object} 汇总数据
 */
export const calculateSummaryData = (accounts) => {
  let totalAsset = 0;
  let dailyReturn = 0;
  let totalReturn = 0;
  
  // 遍历所有账户计算总和
  accounts.forEach(account => {
    totalAsset += account.asset;
    dailyReturn += account.dailyReturn;
    totalReturn += account.totalReturn;
  });
  
  return {
    totalAsset,
    dailyReturn,
    totalReturn,
    dailyReturnRate: totalAsset > 0 ? (dailyReturn / totalAsset ).toFixed(2) : 0,
    totalReturnRate: totalAsset > 0 ? (totalReturn / totalAsset ).toFixed(2) : 0
  };
};

/**
 * 获取指定账户的数据
 * @param {Array} accounts - 账户列表
 * @param {string} accountId - 账户ID
 * @returns {Object|null} 账户数据
 */
export const getAccountById = (accounts, accountId) => {
  return accounts.find(acc => acc.id === accountId) || null;
};

/**
 * 为资产账户添加持仓
 * @param {number} accountId - 资产账户ID
 * @param {Array} portfolios - 持仓列表
 * @returns {Promise<Array>} 添加的持仓列表
 */
export const addPortfoliosToAccount = async (accountId, portfolios) => {
  try {
    const results = [];
    
    // 逐个添加持仓项
    for (const portfolio of portfolios) {
      const data = {
        symbol: portfolio.symbol || '',
        name: portfolio.name || '',
        amount: parseFloat(portfolio.amount || 0) || 0,
        returnValue: parseFloat(portfolio.returnValue || 0) || 0
      }
      console.log('添加持仓项:', data);
      const response = await post(`/api/asset-account/${accountId}/fund-holdings`, data);
      
      if (response.statusCode === 201) {
        results.push(response.data);
      } else {
        throw new Error(response.data.message || `添加持仓项${portfolio.name}失败`);
      }
    }
    
    return results;
  } catch (error) {
    console.error('添加持仓项错误:', error);
    throw error;
  }
};

/**
 * 将资产账户同步到组合
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<Object>} 包含新创建的组合信息的对象
 */
export const syncAssetAccountToPortfolio = async (accountId) => {
  try {
    const response = await post(`/api/asset-account/${accountId}/sync-to-portfolio`);
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '同步资产账户到组合失败');
    }
  } catch (error) {
    console.error('同步资产账户到组合错误:', error);
    throw error;
  }
};

export const reorderAccounts = async (ids) => { return; };

/**
 * 清空账户的所有持仓
 * @param {string} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const clearAccountHoldings = async (accountId) => {
  try {
    // 先获取该账户的所有持仓
    const holdings = await getAccountHoldings(accountId);
    
    // 逐个删除持仓项
    for (const holding of holdings) {
      await del(`/api/asset-account/${accountId}/holdings/${holding.id}`);
    }
    
    return;
  } catch (error) {
    console.error('清空账户持仓错误:', error);
    throw error;
  }
};