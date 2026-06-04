import { get, post } from '../request'

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
  return get('/api/assets/accounts', params)
}

export const createAccount = (data) => {
  return post('/api/assets/accounts', data)
}

export const updateAccount = (id, data) => {
  return post(`/api/assets/accounts/${id}`, data)
}

export const deleteAccount = (id) => {
  return post(`/api/assets/accounts/${id}/delete`)
}

export const getAssetSummary = () => {
  return get('/api/assets/summary')
}

export const getAssetStructure = () => {
  return get('/api/assets/structure')
}

export const getInstitutionTypes = () => {
  return get('/api/institution-types')
}

export const getAllInstitutions = () => {
  return get('/api/institutions')
}

export const getInstitutionsByType = (typeCode) => {
  return get(`/api/institutions/type/${typeCode}`)
}

export const getHotInstitutions = () => {
  return get('/api/institutions/hot')
}

export const getInstitutionDetail = (instCode) => {
  return get(`/api/institutions/${instCode}`)
}

export const getInstitutionCategories = (instCode) => {
  return get(`/api/institutions/${instCode}/categories`)
}
