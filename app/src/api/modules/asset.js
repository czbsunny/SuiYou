import { get, post, put, del } from '../request'

export const getAssetList = (params) => {
  return get('/api/assets', params)
}

export const getAssetDetail = (id) => {
  return get(`/api/assets/${id}`)
}

export const createAsset = (data) => {
  return post('/api/assets', data)
}

export const updateAsset = (id, data) => {
  return post(`/api/assets/${id}`, data)
}

export const deleteAsset = (id) => {
  return post(`/api/assets/${id}/delete`)
}

export const getAssetCategories = () => {
  return get('/api/assets/categories')
}

export const getInstitutions = () => {
  return get('/api/assets/institutions')
}

export const getAccountList = (params) => {
  return get('/api/accounts', params)
}

export const getAccountById = (id) => {
  return get(`/api/accounts/${id}`)
}

export const createAccount = (data) => {
  return post('/api/accounts', data)
}

export const updateAccount = (data) => {
  return put('/api/accounts', data)
}

export const updateAccountStatus = (id, status) => {
  return put(`/api/accounts/${id}/status`, { status })
}

export const deleteAccount = (id) => {
  return del(`/api/accounts/${id}`)
}

export const batchUpdateAccounts = (data) => {
  return put('/api/accounts/sync', data)
}

export const addAccountModule = (accountId, data) => {
  return post(`/api/accounts/${accountId}/modules`, data)
}

export const removeAccountModule = (accountId, moduleId) => {
  return del(`/api/accounts/${accountId}/modules/${moduleId}`)
}

export const getAssetSummary = () => {
  return get('/api/assets/summary')
}

export const getAssetStructure = () => {
  return get('/api/assets/structure')
}

export const getInstitutionTypes = () => {
  return get('/api/inst/types')
}

export const getAllInstitutions = () => {
  return get('/api/inst/all')
}

export const getHotInstitutions = () => {
  return get('/api/inst/hot')
}

export const getInstitutionsByType = (typeCode) => {
  return get(`/api/inst/type/${typeCode}`)
}


export const getInstitutionDetail = (instCode) => {
  return get(`/api/inst/${instCode}`)
}

export const getAccountModules = (instCode, accountType) => {
  return get(`/api/inst/${instCode}/account-types/${accountType}/modules`)
}
