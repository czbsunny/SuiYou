import { get, post } from '../request'

export const getTransactionList = (params) => {
  return get('/api/transactions', params)
}

export const getTransactionDetail = (id) => {
  return get(`/api/transactions/${id}`)
}

export const createTransaction = (data) => {
  return post('/api/transactions', data)
}

export const updateTransaction = (id, data) => {
  return post(`/api/transactions/${id}`, data)
}

export const deleteTransaction = (id) => {
  return post(`/api/transactions/${id}/delete`)
}

export const getTransactionCategories = () => {
  return get('/api/transactions/categories')
}

export const getTransactionSummary = (params) => {
  return get('/api/transactions/summary', params)
}