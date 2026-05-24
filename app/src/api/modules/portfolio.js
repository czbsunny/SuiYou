import { get, post } from '../request'

export const getPortfolioList = (params) => {
  return get('/api/portfolios', params)
}

export const getPortfolioDetail = (id) => {
  return get(`/api/portfolios/${id}`)
}

export const createPortfolio = (data) => {
  return post('/api/portfolios', data)
}

export const updatePortfolio = (id, data) => {
  return post(`/api/portfolios/${id}`, data)
}

export const deletePortfolio = (id) => {
  return post(`/api/portfolios/${id}/delete`)
}

export const getPortfolioHoldings = (portfolioId) => {
  return get(`/api/portfolios/${portfolioId}/holdings`)
}

export const addHolding = (portfolioId, data) => {
  return post(`/api/portfolios/${portfolioId}/holdings`, data)
}

export const updateHolding = (portfolioId, holdingId, data) => {
  return post(`/api/portfolios/${portfolioId}/holdings/${holdingId}`, data)
}

export const deleteHolding = (portfolioId, holdingId) => {
  return post(`/api/portfolios/${portfolioId}/holdings/${holdingId}/delete`)
}

export const getPortfolioAnalysis = (id) => {
  return get(`/api/portfolios/${id}/analysis`)
}

export const getPortfolioRebalance = (id) => {
  return get(`/api/portfolios/${id}/rebalance`)
}

export const searchFunds = (keyword) => {
  return get('/api/funds/search', { keyword })
}

export const getFundInfo = (code) => {
  return get(`/api/funds/${code}`)
}
