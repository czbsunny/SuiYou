/**
 * 基金服务，处理基金相关的API调用
 */
import { post } from './apiService';

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
