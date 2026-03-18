// 本地数据存储服务，用于在未登录状态下保存数据，登录后同步到服务器
import { isLoggedIn } from './apiService.js';

// 本地存储键名前缀
const STORAGE_PREFIX = 'local_data_';
const ACCOUNTS_KEY = `${STORAGE_PREFIX}accounts`;
const HOLDINGS_KEY = `${STORAGE_PREFIX}holdings`;
const SYNC_STATUS_KEY = `${STORAGE_PREFIX}sync_status`;
const LAST_SYNC_TIME_KEY = `${STORAGE_PREFIX}last_sync_time`;

/**
 * 保存账户数据到本地存储
 * @param {Array} accounts - 账户列表
 */
export const saveAccountsToLocal = (accounts) => {
  try {
    uni.setStorageSync(ACCOUNTS_KEY, JSON.stringify(accounts));
    console.log('账户数据已保存到本地存储');
  } catch (error) {
    console.error('保存账户数据到本地存储失败:', error);
  }
};

/**
 * 从本地存储获取账户数据
 * @returns {Array} 账户列表
 */
export const getAccountsFromLocal = () => {
  try {
    const accountsData = uni.getStorageSync(ACCOUNTS_KEY);
    return accountsData ? JSON.parse(accountsData) : [];
  } catch (error) {
    console.error('从本地存储获取账户数据失败:', error);
    return [];
  }
};

/**
 * 保存持仓数据到本地存储
 * @param {number} accountId - 账户ID
 * @param {Array} holdings - 持仓列表
 */
export const saveHoldingsToLocal = (accountId, holdings) => {
  try {
    const allHoldings = getHoldingsFromLocal();
    allHoldings[accountId] = holdings;
    uni.setStorageSync(HOLDINGS_KEY, JSON.stringify(allHoldings));
    console.log(`账户${accountId}的持仓数据已保存到本地存储`);
  } catch (error) {
    console.error('保存持仓数据到本地存储失败:', error);
  }
};

/**
 * 从本地存储获取持仓数据
 * @param {number} accountId - 账户ID，如果不提供则返回所有持仓数据
 * @returns {Array|Object} 持仓列表或所有持仓数据
 */
export const getHoldingsFromLocal = (accountId) => {
  try {
    const holdingsData = uni.getStorageSync(HOLDINGS_KEY);
    const allHoldings = holdingsData ? JSON.parse(holdingsData) : {};
    
    if (accountId !== undefined) {
      return allHoldings[accountId] || [];
    }
    
    return allHoldings;
  } catch (error) {
    console.error('从本地存储获取持仓数据失败:', error);
    return accountId !== undefined ? [] : {};
  }
};

/**
 * 获取下一个可用的账户ID
 * @returns {number} 下一个可用的账户ID
 */
export const getNextAccountId = () => {
  try {
    const accounts = getAccountsFromLocal();
    if (accounts.length === 0) return 0;
    
    // 找出最大的ID值
    const maxId = Math.max(...accounts.map(account => {
      // 如果是数字ID，直接返回；如果是字符串ID，尝试提取数字部分
      if (typeof account.id === 'number') return account.id;
      
      // 尝试从字符串ID中提取数字部分
      const numMatch = account.id.toString().match(/\d+/);
      return numMatch ? parseInt(numMatch[0]) : 0;
    }));
    
    return maxId + 1;
  } catch (error) {
    console.error('获取下一个账户ID失败:', error);
    return 0;
  }
};

/**
 * 获取下一个可用的持仓ID
 * @param {number} accountId - 账户ID
 * @returns {number} 下一个可用的持仓ID
 */
export const getNextHoldingId = (accountId) => {
  try {
    const holdings = getHoldingsFromLocal(accountId);
    if (holdings.length === 0) return 0;
    
    // 找出最大的ID值
    const maxId = Math.max(...holdings.map(holding => {
      // 如果是数字ID，直接返回；如果是字符串ID，尝试提取数字部分
      if (typeof holding.id === 'number') return holding.id;
      
      // 尝试从字符串ID中提取数字部分
      const numMatch = holding.id.toString().match(/\d+/);
      return numMatch ? parseInt(numMatch[0]) : 0;
    }));
    
    return maxId + 1;
  } catch (error) {
    console.error('获取下一个持仓ID失败:', error);
    return 0;
  }
};

/**
 * 计算账户的总资产
 * @param {number} accountId - 账户ID
 * @returns {number} 账户总资产
 */
export const calculateAccountTotalAsset = (accountId) => {
  try {
    const holdings = getHoldingsFromLocal(accountId);
    let totalAsset = 0;
    
    for (const holding of holdings) {
      totalAsset += Number(holding.amount || 0);
    }
    
    return totalAsset;
  } catch (error) {
    console.error(`计算账户${accountId}总资产失败:`, error);
    return 0;
  }
};

/**
 * 更新账户的总资产
 * @param {number} accountId - 账户ID
 * @returns {boolean} 是否更新成功
 */
export const updateAccountTotalAsset = (accountId) => {
  try {
    const accounts = getAccountsFromLocal();
    const accountIndex = accounts.findIndex(acc => acc.id === Number(accountId));
    
    if (accountIndex !== -1) {
      const totalAsset = calculateAccountTotalAsset(accountId);
      accounts[accountIndex].asset = totalAsset;
      saveAccountsToLocal(accounts);
      console.log(`账户${accountId}的总资产已更新为: ${totalAsset}`);
      return true;
    }
    
    console.warn(`未找到ID为${accountId}的账户`);
    return false;
  } catch (error) {
    console.error(`更新账户${accountId}总资产失败:`, error);
    return false;
  }
};

/**
 * 添加持仓到本地存储
 * @param {number} accountId - 账户ID
 * @param {Object} holding - 持仓数据
 */
export const addHoldingToLocal = (accountId, holding) => {
  try {
    const holdings = getHoldingsFromLocal(accountId);
    const newId = getNextHoldingId(accountId);
    holding.id = newId;
    holdings.push(holding);
    saveHoldingsToLocal(accountId, holdings);
    
    // 更新账户的总资产
    updateAccountTotalAsset(accountId);
    
    console.log(`持仓已添加到本地存储，账户ID: ${accountId}, 持仓ID: ${newId}`);
    return holding;
  } catch (error) {
    console.error('添加持仓到本地存储失败:', error);
    return null;
  }
};

/**
 * 更新本地存储中的持仓
 * @param {number} accountId - 账户ID
 * @param {string} holdingId - 持仓ID
 * @param {Object} updates - 更新数据
 */
export const updateHoldingInLocal = (accountId, holdingId, updates) => {
  try {
    const holdings = getHoldingsFromLocal(accountId);
    const index = holdings.findIndex(h => h.id === holdingId);
    
    if (index !== -1) {
      holdings[index] = { ...holdings[index], ...updates };
      saveHoldingsToLocal(accountId, holdings);
      
      // 更新账户的总资产
      updateAccountTotalAsset(accountId);
      
      console.log(`持仓已更新到本地存储，账户ID: ${accountId}, 持仓ID: ${holdingId}`);
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('更新本地存储中的持仓失败:', error);
    return false;
  }
};

/**
 * 从本地存储删除持仓
 * @param {number} accountId - 账户ID
 * @param {string} holdingId - 持仓ID
 */
export const deleteHoldingFromLocal = (accountId, holdingId) => {
  try {
    const holdings = getHoldingsFromLocal(accountId);
    const filteredHoldings = holdings.filter(h => h.id !== holdingId);
    
    if (filteredHoldings.length !== holdings.length) {
      saveHoldingsToLocal(accountId, filteredHoldings);
      
      // 更新账户的总资产
      updateAccountTotalAsset(accountId);
      
      console.log(`持仓已从本地存储删除，账户ID: ${accountId}, 持仓ID: ${holdingId}`);
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('从本地存储删除持仓失败:', error);
    return false;
  }
};

/**
 * 创建本地账户
 * @param {Object} accountData - 账户数据
 * @returns {Object} 创建的账户
 */
export const createLocalAccount = (accountData) => {
  try {
    const accounts = getAccountsFromLocal();
    const newId = getNextAccountId();
    
    const newAccount = {
      id: newId,
      name: accountData.name,
      icon: accountData.icon || '💰',
      asset: 0.00,
      dailyReturn: 0.00,
      dailyReturnRate: 0.00,
      totalReturn: 0.00,
      totalReturnRate: 0.00,
      sortOrder: accounts.length,
      isLocal: true // 标记为本地账户
    };
    
    accounts.push(newAccount);
    saveAccountsToLocal(accounts);
    console.log(`本地账户已创建，ID: ${newId}`);
    return newAccount;
  } catch (error) {
    console.error('创建本地账户失败:', error);
    return null;
  }
};

/**
 * 更新本地账户
 * @param {number} accountId - 账户ID
 * @param {Object} updates - 更新数据
 * @returns {boolean} 是否更新成功
 */
export const updateLocalAccount = (accountId, updates) => {
  try {
    const accounts = getAccountsFromLocal();
    const index = accounts.findIndex(acc => acc.id === Number(accountId));
    
    if (index !== -1) {
      accounts[index] = { ...accounts[index], ...updates };
      saveAccountsToLocal(accounts);
      console.log(`本地账户已更新，ID: ${accountId}`);
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('更新本地账户失败:', error);
    return false;
  }
};

/**
 * 删除本地账户及其持仓
 * @param {string} accountId - 账户ID
 * @returns {boolean} 是否删除成功
 */
export const deleteLocalAccount = (accountId) => {
  try {
    // 删除账户
    const accounts = getAccountsFromLocal();
    const filteredAccounts = accounts.filter(acc => acc.id !== accountId);
    
    if (filteredAccounts.length !== accounts.length) {
      saveAccountsToLocal(filteredAccounts);
      
      // 删除该账户的所有持仓
      const allHoldings = getHoldingsFromLocal();
      delete allHoldings[accountId];
      uni.setStorageSync(HOLDINGS_KEY, JSON.stringify(allHoldings));
      
      console.log(`本地账户及其持仓已删除，ID: ${accountId}`);
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('删除本地账户失败:', error);
    return false;
  }
};

/**
 * 清空账户的所有持仓
 * @param {string} accountId - 账户ID
 * @returns {boolean} 是否清空成功
 */
export const clearLocalAccountHoldings = (accountId) => {
  try {
    const allHoldings = getHoldingsFromLocal();
    
    // 检查账户是否存在持仓
    if (!allHoldings[accountId] || allHoldings[accountId].length === 0) {
      console.log(`账户${accountId}没有持仓，无需清空`);
      return true;
    }
    
    // 清空该账户的所有持仓
    allHoldings[accountId] = [];
    uni.setStorageSync(HOLDINGS_KEY, JSON.stringify(allHoldings));
    
    // 更新账户的总资产为0
    updateAccountTotalAsset(accountId);
    
    console.log(`账户${accountId}的所有持仓已清空`);
    return true;
  } catch (error) {
    console.error('清空账户持仓失败:', error);
    return false;
  }
};

/**
 * 重新排序本地账户
 * @param {Array<string>} accountIds - 账户ID数组，按新顺序排列
 * @returns {boolean} 是否排序成功
 */
export const reorderAccountsInLocal = (accountIds) => {
  try {
    const accounts = getAccountsFromLocal();
    
    // 创建一个映射，用于快速查找账户
    const accountMap = {};
    accounts.forEach(account => {
      accountMap[account.id] = account;
    });
    
    // 根据提供的ID顺序重新排列账户
    const reorderedAccounts = [];
    accountIds.forEach(id => {
      if (accountMap[id]) {
        // 更新sortOrder为当前索引
        accountMap[id].sortOrder = reorderedAccounts.length;
        reorderedAccounts.push(accountMap[id]);
        delete accountMap[id]; // 从映射中删除已处理的账户
      }
    });
    
    // 添加任何未在ID列表中的账户（保持原有顺序）
    Object.values(accountMap).forEach(account => {
      account.sortOrder = reorderedAccounts.length;
      reorderedAccounts.push(account);
    });
    
    // 保存重新排序后的账户列表
    saveAccountsToLocal(reorderedAccounts);
    console.log(`本地账户已重新排序，共 ${reorderedAccounts.length} 个账户`);
    return true;
  } catch (error) {
    console.error('重新排序本地账户失败:', error);
    return false;
  }
};

/**
 * 检查是否有本地数据需要同步
 * @returns {boolean} 是否有需要同步的数据
 */
export const hasLocalDataToSync = () => {
  const accounts = getAccountsFromLocal();
  const allHoldings = getHoldingsFromLocal();
  
  // 检查是否有本地账户或持仓
  const hasLocalAccounts = accounts.length > 0;
  const hasLocalHoldings = Object.keys(allHoldings).length > 0 && 
    Object.values(allHoldings).some(holdings => holdings.length > 0);
  
  return hasLocalAccounts || hasLocalHoldings;
};

/**
 * 设置同步状态
 * @param {Object} status - 同步状态
 */
export const setSyncStatus = (status) => {
  try {
    uni.setStorageSync(SYNC_STATUS_KEY, JSON.stringify(status));
  } catch (error) {
    console.error('设置同步状态失败:', error);
  }
};

/**
 * 获取同步状态
 * @returns {Object} 同步状态
 */
export const getSyncStatus = () => {
  try {
    const statusData = uni.getStorageSync(SYNC_STATUS_KEY);
    return statusData ? JSON.parse(statusData) : { isSyncing: false, lastSyncTime: null };
  } catch (error) {
    console.error('获取同步状态失败:', error);
    return { isSyncing: false, lastSyncTime: null };
  }
};

/**
 * 记录最后同步时间
 */
export const recordLastSyncTime = () => {
  try {
    uni.setStorageSync(LAST_SYNC_TIME_KEY, Date.now());
  } catch (error) {
    console.error('记录最后同步时间失败:', error);
  }
};

/**
 * 获取最后同步时间
 * @returns {number|null} 最后同步时间戳
 */
export const getLastSyncTime = () => {
  try {
    return uni.getStorageSync(LAST_SYNC_TIME_KEY) || null;
  } catch (error) {
    console.error('获取最后同步时间失败:', error);
    return null;
  }
};

/**
 * 清除所有本地数据
 */
export const clearAllLocalData = () => {
  try {
    uni.removeStorageSync(ACCOUNTS_KEY);
    uni.removeStorageSync(HOLDINGS_KEY);
    uni.removeStorageSync(SYNC_STATUS_KEY);
    uni.removeStorageSync(LAST_SYNC_TIME_KEY);
    console.log('所有本地数据已清除');
  } catch (error) {
    console.error('清除本地数据失败:', error);
  }
};

/**
 * 同步本地数据到服务器
 * @param {Function} createAccountFn - 创建账户的API函数
 * @param {Function} addHoldingFn - 添加持仓的API函数
 * @returns {Promise<Object>} 同步结果
 */
export const syncLocalDataToServer = async (createAccountFn, addHoldingFn) => {
  try {
    if (!hasLocalDataToSync()) {
      return { success: true, message: '没有需要同步的数据', syncedAccounts: 0, syncedHoldings: 0 };
    }
    
    setSyncStatus({ isSyncing: true, lastSyncTime: null });
    
    const localAccounts = getAccountsFromLocal();
    const allLocalHoldings = getHoldingsFromLocal();
    
    let syncedAccounts = 0;
    let syncedHoldings = 0;
    const accountMapping = {}; // 本地ID到服务器ID的映射
    
    // 同步账户
    for (const localAccount of localAccounts) {
      try {
        // 创建服务器账户
        const serverAccount = await createAccountFn({
          name: localAccount.name,
          icon: localAccount.icon,
          description: '',
          platform: ''
        });
        
        // 记录ID映射
        accountMapping[localAccount.id] = serverAccount.id;
        syncedAccounts++;
        
        console.log(`账户已同步到服务器，本地ID: ${localAccount.id}, 服务器ID: ${serverAccount.id}`);
      } catch (error) {
        console.error(`同步账户失败，本地ID: ${localAccount.id}`, error);
      }
    }
    
    // 同步持仓
    for (const [localAccountId, holdings] of Object.entries(allLocalHoldings)) {
      if (holdings.length === 0) continue;
      
      const serverAccountId = accountMapping[localAccountId];
      if (!serverAccountId) {
        console.warn(`账户 ${localAccountId} 未同步到服务器，跳过持仓同步`);
        continue;
      }
      
      for (const holding of holdings) {
        try {
          await addHoldingFn(serverAccountId, {
            symbol: holding.symbol || holding.code,
            amount: holding.amount,
            returnValue: holding.returnValue || 0
          });
          syncedHoldings++;
          
          console.log(`持仓已同步到服务器，账户ID: ${serverAccountId}, 基金代码: ${holding.symbol || holding.code}`);
        } catch (error) {
          console.error(`同步持仓失败，账户ID: ${serverAccountId}, 基金代码: ${holding.symbol || holding.code}`, error);
        }
      }
    }
    
    // 记录同步时间
    recordLastSyncTime();
    setSyncStatus({ isSyncing: false, lastSyncTime: Date.now() });
    
    // 如果同步成功，清除本地数据
    if (syncedAccounts > 0 || syncedHoldings > 0) {
      clearAllLocalData();
      console.log('本地数据同步成功，已清除本地数据');
    }
    
    return {
      success: true,
      message: `同步完成，已同步 ${syncedAccounts} 个账户和 ${syncedHoldings} 个持仓`,
      syncedAccounts,
      syncedHoldings
    };
  } catch (error) {
    console.error('同步本地数据到服务器失败:', error);
    setSyncStatus({ isSyncing: false, lastSyncTime: null });
    return {
      success: false,
      message: `同步失败: ${error.message}`,
      syncedAccounts: 0,
      syncedHoldings: 0
    };
  }
};

export default {
  saveAccountsToLocal,
  getAccountsFromLocal,
  saveHoldingsToLocal,
  getHoldingsFromLocal,
  addHoldingToLocal,
  updateHoldingInLocal,
  deleteHoldingFromLocal,
  createLocalAccount,
  updateLocalAccount,
  deleteLocalAccount,
  clearLocalAccountHoldings,
  reorderAccountsInLocal,
  hasLocalDataToSync,
  setSyncStatus,
  getSyncStatus,
  recordLastSyncTime,
  getLastSyncTime,
  clearAllLocalData,
  syncLocalDataToServer,
  getNextAccountId,
  getNextHoldingId,
  calculateAccountTotalAsset,
  updateAccountTotalAsset
};