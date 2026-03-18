// 账户相关API服务模块
import { get, post, del, put, isLoggedIn } from '@/utils/apiService';
import { 
  getAccountsFromLocal, 
  getHoldingsFromLocal,
  createLocalAccount,
  addHoldingToLocal,
  updateHoldingInLocal,
  deleteHoldingFromLocal,
  updateLocalAccount,
  deleteLocalAccount,
  clearLocalAccountHoldings,
  reorderAccountsInLocal
} from '@/utils/localDataService';

/**
 * 获取用户的所有资产账户
 * @returns {Promise<Array>} 账户列表
 */
export const getAssetAccounts = async () => {
  if (!isLoggedIn()) {
    console.log('未登录状态，从本地存储获取账户数据');
    let localAccounts = getAccountsFromLocal();
    
    if (localAccounts.length === 0) {
      console.log('没有本地账户，创建默认账户');
      const defaultAccount = createLocalAccount({
        name: '默认账户',
        icon: '🤠'
      });
      
      // 确保默认账户的sortOrder为0
      if (defaultAccount) {
        defaultAccount.sortOrder = 0;
        updateLocalAccount(defaultAccount.id, { sortOrder: 0 });
        localAccounts = [defaultAccount];
      }
    }
    
    return localAccounts;
  }
  
  // 已登录，从服务器获取账户数据
  const assetAccounts = await getAssetAccountsToServer();
  return assetAccounts;
};

/**
 * 获取用户的所有资产账户
 * @returns {Promise<Array>} 账户列表
 */
export const getAssetAccountsToServer = async () => {
  try {
    const response = await get('/api/asset-account');
    
    if (response.statusCode === 200) {
      // 格式化账户数据
      return response.data.map(account => ({
        id: account.id,
        name: account.name,
        icon: account.icon || '💰',
        asset: account.totalAsset || 0.00,
        dailyReturn: account.dailyReturn || 0.00,
        dailyReturnRate: account.dailyReturnRate || 0.00,
        totalReturn: account.totalReturn || 0.00,
        totalReturnRate: account.totalReturnRate || 0.00,
        sortOrder: account.sortOrder !== undefined ? account.sortOrder : 9999
      }));
    } else {
      throw new Error('获取账户数据失败');
    }
  } catch (error) {
    console.error('获取账户数据错误:', error);
    
    throw error;
  }
}
/**
 * 创建新的资产账户
 * @param {Object} accountData - 账户数据
 * @param {string} accountData.name - 账户名称
 * @param {string} accountData.icon - 账户图标
 * @param {string} [accountData.description] - 账户描述
 * @param {string} [accountData.platform] - 账户平台
 * @returns {Promise<Object>} 创建的账户信息
 */
export const createAssetAccount = async (accountData) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，创建本地账户');
    const localAccount = createLocalAccount(accountData);
    return localAccount;
  }
  
  // 已登录，向服务器发送请求
  const newAccount = await createAssetAccountServer(accountData);
  return newAccount;
};

export const createAssetAccountServer = async (accountData) => {
  try {
    const response = await post('/api/asset-account', {
      name: accountData.name,
      icon: accountData.icon,
      description: accountData.description || '',
      platform: accountData.platform || ''
    });
    
    if (response.statusCode === 201) {
      const newAccount = response.data;
      // 返回格式化的账户数据
      return {
        id: newAccount.id,
        name: newAccount.name,
        icon: newAccount.icon || accountData.icon,
        asset: 0.00,
        dailyReturn: 0.00,
        dailyReturnRate: 0.00,
        totalReturn: 0.00,
        totalReturnRate: 0.00,
        sortOrder: response.data.sortOrder !== undefined ? response.data.sortOrder : 9999
      };
    } else {
      throw new Error(response.data.message || '创建账户失败');
    }
  } catch (error) {
    console.error('创建账户错误:', error);

    throw error;
  }
};

/**
 * 获取指定账户的持仓数据
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<Array>} 持仓数据列表
 */
export const getAccountHoldings = async (accountId) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，从本地存储获取持仓数据');
    const localHoldings = getHoldingsFromLocal(accountId);
    return localHoldings;
  }
  
  // 已登录，从服务器获取持仓数据
  const holdings = await getAccountHoldingsServer(accountId);
  return holdings;
};

/**
 * 从服务器获取指定账户的持仓数据
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<Array>} 持仓数据列表
 */
export const getAccountHoldingsServer = async (accountId) => {
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
        amount: holding.amount || 0.00,
        navUpdatedAt: holding.navUpdatedAt || null
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
    dailyReturnRate: totalAsset > 0 ? parseFloat((dailyReturn / totalAsset * 100).toFixed(2)) : 0,
    totalReturnRate: totalAsset > 0 ? parseFloat((totalReturn / totalAsset * 100).toFixed(2)) : 0
  };
};

/**
 * 获取指定账户的数据
 * @param {Array} accounts - 账户列表
 * @param {number} accountId - 账户ID
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
  if (!isLoggedIn()) {
    console.log('未登录状态，添加持仓到本地存储');
    const results = [];
    for (const portfolio of portfolios) {
      const localHolding = addHoldingToLocal(accountId, portfolio);
      results.push(localHolding);
    }
    return results;
  }
  
  // 已登录，向服务器发送请求
  return await addPortfoliosToAccountServer(accountId, portfolios);
};

/**
 * 为资产账户添加持仓
 * @param {number} accountId - 资产账户ID
 * @param {Array} portfolios - 持仓列表
 * @returns {Promise<Array>} 添加的持仓列表
 */
export const addPortfoliosToAccountServer = async (accountId, portfolios) => {
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
 * 更新基金持仓信息
 * @param {number} accountId - 资产账户ID
 * @param {number} itemId - 持仓ID
 * @param {Object} itemData - 持仓数据
 * @returns {Promise<Object>} 更新后的持仓信息
 */
export const updateAssetItem = async (accountId, itemId, itemData) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，更新本地持仓');
    const updatedHolding = updateHoldingInLocal(accountId, itemId, itemData);
    return updatedHolding;
  }

  return await updateAssetItemServer(accountId, itemId, itemData);
};

/**
 * 更新基金持仓信息
 * @param {number} accountId - 资产账户ID
 * @param {number} itemId - 持仓ID
 * @param {Object} itemData - 持仓数据
 * @returns {Promise<Object>} 更新后的持仓信息
 */
export const updateAssetItemServer = async (accountId, itemId, itemData) => {
  try {
    const data = {
      amount: parseFloat(itemData.amount || 0) || 0,
      returnValue: parseFloat(itemData.returnValue || 0) || 0
    };
    
    console.log('更新持仓项:', data);
    const response = await put(`/api/asset-account/${accountId}/holdings/${itemId}`, data);
    
    if (response.statusCode === 200 && response.data.success) {
      return response.data.data;
    } else {
      throw new Error(response.data.message || '更新持仓项失败');
    }
  } catch (error) {
    console.error('更新持仓项错误:', error);
    
    throw error;
  }
};

/**
 * 更新资产账户信息
 * @param {number} accountId - 资产账户ID
 * @param {Object} data - 更新数据
 * @param {string} [data.name] - 账户名称
 * @param {string} [data.description] - 账户描述
 * @param {string} [data.platform] - 账户平台
 * @param {string} [data.icon] - 账户图标
 * @returns {Promise<Object>} 更新后的账户信息
 */
export const updateAssetAccount = async (accountId, data) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，更新本地账户');
    const updatedAccount = updateLocalAccount(accountId, data);
    return updatedAccount;
  }
    
  return await updateAssetAccountServer(accountId, data);
};

/**
 * 更新资产账户信息
 * @param {number} accountId - 资产账户ID
 * @param {Object} data - 更新数据
 * @param {string} [data.name] - 账户名称
 * @param {string} [data.description] - 账户描述
 * @param {string} [data.platform] - 账户平台
 * @param {string} [data.icon] - 账户图标
 * @returns {Promise<Object>} 更新后的账户信息
 */
export const updateAssetAccountServer = async (accountId, data) => {
  try {
    const updateData = {};
    if (data.name !== undefined) updateData.name = data.name;
    if (data.description !== undefined) updateData.description = data.description;
    
    const response = await put(`/api/asset-account/${accountId}`, updateData);
    
    if (response.statusCode === 200) {
      return {
        id: response.data.id,
        name: response.data.name,
        icon: response.data.icon || '',
        totalAsset: response.data.totalAsset || 0.00,
        dailyReturn: response.data.dailyReturn || 0.00,
        dailyReturnRate: response.data.dailyReturnRate || 0.00,
        totalReturn: response.data.totalReturn || 0.00,
        totalReturnRate: response.data.totalReturnRate || 0.00,
        sortOrder: response.data.sortOrder ?? 9999,
        navUpdatedAt: response.data.navUpdatedAt || null
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
 * 清空账户的所有持仓
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const clearAccountHoldings = async (accountId) => {
  // 如果未登录，清空本地存储中的持仓
  if (!isLoggedIn()) {
    console.log('未登录状态，清空本地账户持仓');
    clearLocalAccountHoldings(accountId);
    return;
  }
  
  await clearAccountHoldingsServer(accountId);
};

/**
 * 清空账户的所有持仓
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const clearAccountHoldingsServer = async (accountId) => {
  try {
    const response = await del(`/api/asset-account/${accountId}/holdings/clear`);
    
    if (response.statusCode === 204 || response.statusCode === 200) {
      return;
    } else if (response.statusCode === 404) {
      throw new Error('账户不存在');
    } else if (response.statusCode === 403) {
      throw new Error('无权清空此账户持仓');
    } else {
      throw new Error(response.data?.message || '清空账户持仓失败');
    }
  } catch (error) {
    console.error('清空账户持仓错误:', error);
    
    throw error;
  }
};

/**
 * 重新排序资产账户
 * @param {Array<number>} accountIds - 资产账户ID数组，按新顺序排列
 * @returns {Promise<void>}
 */
export const reorderAccounts = async (accountIds) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，重新排序本地账户');
    reorderAccountsInLocal(accountIds);
    return;
  }
  
  await reorderAccountsServer(accountIds);
};


/**
 * 重新排序资产账户
 * @param {Array<number>} accountIds - 资产账户ID数组，按新顺序排列
 * @returns {Promise<void>}
 */
export const reorderAccountsServer = async (accountIds) => {
  try {
    const numericAccountIds = accountIds.map(id => Number(id));
    
    const response = await post(`/api/asset-account/reorder`, { accountIds: numericAccountIds });
    
    if (response.statusCode === 200) {
      return;
    } else if (response.statusCode === 400) {
      throw new Error('无效的账户ID列表');
    } else {
      throw new Error(response.data?.message || '重新排序资产账户失败');
    }
  } catch (error) {
    console.error('重新排序资产账户错误:', error);
    
    throw error;
  }
};

/**
 * 删除资产账户
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const deleteAssetAccount = async (accountId) => {
  if (!isLoggedIn()) {
    console.log('未登录状态，删除本地账户');
    deleteLocalAccount(accountId);
    return;
  }
  
  await deleteAssetAccountServer(accountId);
};

/**
 * 删除资产账户
 * @param {number} accountId - 资产账户ID
 * @returns {Promise<void>}
 */
export const deleteAssetAccountServer = async (accountId) => {
  try {
    const response = await del(`/api/asset-account/${accountId}`);
    
    if (response.statusCode === 200 || response.statusCode === 204) {
      return;
    } else if (response.statusCode === 404) {
      throw new Error('账户不存在');
    } else if (response.statusCode === 403) {
      throw new Error('无权删除此账户');
    } else {
      throw new Error(response.data?.message || '删除资产账户失败');
    }
  } catch (error) {
    console.error('删除资产账户错误:', error);
    
    throw error;
  }
};

/**
 * 删除持仓项
 * @param {number} accountId - 资产账户ID
 * @param {number} itemId - 持仓项ID
 * @returns {Promise<void>}
 */
export const deleteHolding = async (accountId, itemId) => {
  // 如果未登录，从本地存储中删除持仓
  if (!isLoggedIn()) {
    console.log('未登录状态，删除本地持仓');
    const success = deleteHoldingFromLocal(accountId, itemId);
    if (!success) {
      throw new Error('删除本地持仓失败');
    }
    return;
  }
  
  await deleteHoldingServer(accountId, itemId);
};

/**
 * 删除持仓项
 * @param {number} accountId - 资产账户ID
 * @param {number} itemId - 持仓项ID
 * @returns {Promise<void>}
 */
export const deleteHoldingServer = async (accountId, itemId) => {
  try {
    const response = await del(`/api/asset-account/${accountId}/holdings/${itemId}`);
    
    if (response.statusCode === 200 || response.statusCode === 204) {
      return;
    } else if (response.statusCode === 404) {
      throw new Error('持仓不存在');
    } else if (response.statusCode === 403) {
      throw new Error('无权删除此持仓');
    } else {
      throw new Error(response.data?.message || '删除持仓失败');
    }
  } catch (error) {
    console.error('删除持仓错误:', error);
    
    throw error;
  }
};