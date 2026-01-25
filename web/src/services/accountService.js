// 账户（物理容器）管理服务模块
import api from './apiService';

const ACCOUNT_API_BASE = '/api/accounts';

/**
 * 1. 创建账户
 * @param {Object} data - 包含机构代码、标识码、名称、是否计入净值
 */
export const createAccount = async (data) => {
  const payload = {
    institution: data.instCode,              // 对应 DTO 的 institution
    institutionIdentifier: data.identifier,  // 对应 DTO 的 institutionIdentifier
    accountName: data.accountName,           // 对应 DTO 的 accountName
    includeInNetWorth: data.includeInNetWorth, // 对应 DTO 的 includeInNetWorth
    themeColor: data.themeColor, // 对应 DTO 的 themeColor
  };

  try {
    return await api.post(ACCOUNT_API_BASE, payload);
  } catch (error) {
    throw error;
  }
};

/**
 * 2. 修改账户信息
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
 * 3. 更新账户状态
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
 * 4. 删除账户
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
 * 5. 获取所有账户
 * @returns {Promise<Array>} 账户列表
 */
export const getAccounts = async () => {
  try {
    const res = await api.get(ACCOUNT_API_BASE);
    return res.data;
  } catch (error) {
    throw error;
  }
};

export default {
  createAccount,
  updateAccount,
  updateAccountStatus,
  deleteAccount,
  getAccounts
};