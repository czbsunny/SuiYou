// 账户相关API服务模块
import { get, post } from './apiService';

/**
 * 创建新的资产账户
 * @param {Object} assetData - 资产数据
 * @param {string} assetData.groupType - 资产组类型 (ASSET/LIABILITY)
 * @param {string} assetData.category - 资产分类 (LIQUID/INVEST等)
 * @param {string} assetData.subCategory - 资产子分类 (BANK_CARD/STOCK等)
 * @param {string} assetData.institution - 机构代码 (如"1234")
 * @param {string} assetData.institutionIdentifier - 机构账号末4位
 * @param {string} assetData.name - 资产名称
 * @param {string} [assetData.currency] - 币种，默认CNY
 * @param {number} assetData.balance - 初始余额(正数)
 * @param {string} [assetData.visibleScope] - 可见范围 (PRIVATE/FAMILY)，默认PRIVATE
 * @param {Object} [assetData.attributes] - 扩展属性
 * @returns {Promise<Object>} 创建的资产账户信息
 */
export const createAsset = async (assetData) => {
  try {
    const response = await post('/api/assets', assetData);
    
    if (response.statusCode === 201 || response.statusCode === 200) {
      return response.data;
    } else if (response.statusCode === 400) {
      throw new Error(response.data?.message || '参数错误');
    } else {
      throw new Error(response.data?.message || '创建资产账户失败');
    }
  } catch (error) {
    console.error('创建资产项错误:', error);
    throw error;
  }
};

/**
 * 获取当前用户的所有资产项
 * @returns {Promise<Object>} 资产项列表和总数
 */
export const getAssets = async () => {
  try {
    const response = await get('/api/assets');
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '获取资产项失败');
    }
  } catch (error) {
    console.error('获取资产项错误:', error);
    throw error;
  }
};

