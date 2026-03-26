import { post, put, del, get } from './apiService.js';

const TRANSACTION_API_BASE = '/api/transactions';

// 创建交易记录
export const createTransaction = async (transactionData) => {
  try {
    const response = await post(TRANSACTION_API_BASE, transactionData);
    
    if (response.statusCode === 201 || response.statusCode === 200) {
      return response.data;
    } else if (response.statusCode === 400) {
      throw new Error(response.data?.message || '参数错误');
    } else {
      throw new Error(response.data?.message || '创建交易记录失败');
    }
  } catch (error) {
    console.error('创建交易记录失败:', error);
    throw error;
  }
};

// 更新交易记录
export const updateTransaction = async (id, transactionData) => {
  try {
    const response = await put(`${TRANSACTION_API_BASE}/${id}`, transactionData);
    return response.data;
  } catch (error) {
    console.error('更新交易记录失败:', error);
    throw error;
  }
};

// 删除交易记录
export const deleteTransaction = async (id) => {
  try {
    const response = await del(`${TRANSACTION_API_BASE}/${id}`);
    return response.data;
  } catch (error) {
    console.error('删除交易记录失败:', error);
    throw error;
  }
};

// 获取交易记录详情
export const getTransactionById = async (id) => {
  try {
    const response = await get(`${TRANSACTION_API_BASE}/${id}`);
    return response.data;
  } catch (error) {
    console.error('获取交易记录详情失败:', error);
    throw error;
  }
};

// 获取交易记录列表
export const getTransactions = async (params = {}) => {
  try {
    const response = await get(TRANSACTION_API_BASE, params);
    
    if (response.statusCode === 200) {
      return response.data;
    } else {
      throw new Error(response.data?.message || '获取交易记录列表失败');
    }
  } catch (error) {
    console.error('获取交易记录列表失败:', error);
    throw error;
  }
};

export default {
  createTransaction,
  updateTransaction,
  deleteTransaction,
  getTransactionById,
  getTransactions
};