// 账户（物理容器）管理服务模块
import api from './apiService';

const ACCOUNT_API_BASE = '/api/accounts';
/**
 * 1. 修改账户信息
 * @param {Object} data - 更新的字段
 */
export const updateAccount = async (data) => {
  const payload = {
    accountId: data.accountId,
    institution: data.instCode,
    institutionIdentifier: data.identifier,
    accountName: data.accountName,
    includeInNetWorth: data.includeInNetWorth,
    themeColor: data.themeColor
  };

  try {
    const res = await api.put(ACCOUNT_API_BASE, payload);
    return res.data;
  } catch (error) {
    throw error;
  }
};

/**
 * 2. 更新账户状态
 * @param {String|Number} id 账户ID
 * @param {Number} status 状态值 (1: 活跃, 0: 归档)
 */
export const updateAccountStatus = async (id, status) => {
  try {
    const res = await api.put(`${ACCOUNT_API_BASE}/${id}/status`, { status });
    return res.data;
  } catch (error) {
    throw error;
  }
};

/**
 * 3. 删除账户
 * @param {String|Number} id 
 */
export const deleteAccount = async (id) => {
  try {
    const res = await api.del(`${ACCOUNT_API_BASE}/${id}`);
    return res.statusCode === 200 || res.statusCode === 204;
  } catch (error) {
    throw error;
  }
};

/**
 * 4. 同步账户
 * @param {String|Number} id 账户ID
 * @param {Number} status 状态值 (1: 活跃, 0: 归档)
 */
export const batchUpdateAccounts = async (syncAccounts) => {
  try {
    const res = await api.put(`${ACCOUNT_API_BASE}/sync`, syncAccounts);
    return res.data;
  } catch (error) {
    throw error;
  }
};


/**
 * 5. 获取所有账户
 * @param {Object} params - 查询参数，如 { institution: 'alipay' }
 * @returns {Promise<Array>} 账户列表
 */
export const getAccounts = async (params = {}) => {
  try {
    const res = await api.get(ACCOUNT_API_BASE, params);
    // 兼容后端返回格式 { accounts: [], count: 0 } 或直接 []
    return res.data.accounts || res.data;
  } catch (error) {
    throw error;
  }
};

export default {
  updateAccount,
  updateAccountStatus,
  deleteAccount,
  batchUpdateAccounts,
  getAccounts
};