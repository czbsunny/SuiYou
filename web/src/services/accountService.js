// 账户相关API服务模块
import { get, post, put, del } from './apiService';

/**
 * 创建新的资产账户
 * @param {Object} accountData - 账户数据
 * @param {string} accountData.groupType - 账户组类型 (ASSET/LIABILITY)
 * @param {string} accountData.category - 账户分类 (LIQUID/INVEST等)
 * @param {string} accountData.subCategory - 账户子分类 (BANK_CARD/STOCK等)
 * @param {string} [accountData.institution] - 机构代码
 * @param {string} accountData.name - 账户名称
 * @param {string} [accountData.currency] - 币种，默认CNY
 * @param {number} accountData.balance - 初始余额(正数)
 * @param {string} [accountData.visibleScope] - 可见范围 (PRIVATE/FAMILY)，默认PRIVATE
 * @param {Object} [accountData.attributes] - 扩展属性
 * @returns {Promise<Object>} 创建的账户信息
 */
export const createAccount = async (accountData) => {
  try {
    const response = await post('/api/accounts', accountData);
    
    if (response.statusCode === 201 || response.statusCode === 200) {
      return response.data;
    } else if (response.statusCode === 400) {
      throw new Error(response.data?.message || '参数错误');
    } else {
      throw new Error(response.data?.message || '创建资产账户失败');
    }
  } catch (error) {
    console.error('创建资产账户错误:', error);
    throw error;
  }
};

/**
 * 获取当前用户的所有资产账户
 * @returns {Promise<Object>} 账户列表和总数
 */
export const getAccounts = async () => {
  try {
    const response = await get('/api/accounts');
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '获取资产账户失败');
    }
  } catch (error) {
    console.error('获取资产账户错误:', error);
    throw error;
  }
};

