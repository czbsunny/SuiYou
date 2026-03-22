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
 * 4. 批量更新账户（排序+归档+恢复）
 * @param {Object} payload - 批量更新载荷
 * @param {Array<Number>} payload.activeAccountIds - 活跃账户ID列表（按排序顺序）
 * @param {Array<Number>} payload.archivedAccountIds - 需要归档的账户ID列表
 * @returns {Promise<Object>} 更新结果
 */
export const batchUpdateAccounts = async (payload) => {
  try {
    const res = await api.put(`${ACCOUNT_API_BASE}/sync`, payload);
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
    if (res.statusCode !== 200) {
      throw new Error(`获取账户失败，状态码: ${res.statusCode}`);
    }

    return res.data;
  } catch (error) {
    throw error;
  }
};


/**
 * 6. 获取指定账户详情
 * @param {Number} accountId - 账户ID
 * @returns {Promise<Object>} 账户详情
 */
export const getAccountById = async (accountId) => {
  try {
    const res = await api.get(`${ACCOUNT_API_BASE}/${accountId}`);
    if (res.statusCode !== 200) {
      throw new Error(`获取账户失败，状态码: ${res.statusCode}`);
    }

    return res.data;
  } catch (error) {
    throw error;
  }
};

export default {
  updateAccount,
  updateAccountStatus,
  deleteAccount,
  batchUpdateAccounts,
  getAccounts,
  getAccountById
};