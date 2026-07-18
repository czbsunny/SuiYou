import { get, post, del } from '../request'

export const getGoalList = (params) => {
  return get('/api/goals', params)
}

export const getGoalDetail = (id) => {
  return get(`/api/goals/${id}`)
}

export const createGoal = (data) => {
  return post('/api/goals', data)
}

export const deleteGoal = (id) => {
  return del(`/api/goals/${id}`)
}

export const getGoalCategories = () => {
  return get('/api/goals/categories')
}

export const getGoalTemplates = (categoryCode) => {
  return get('/api/goals/templates', { categoryCode })
}
