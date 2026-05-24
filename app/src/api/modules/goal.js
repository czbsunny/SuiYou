import { get, post } from '../request'

export const getGoalList = (params) => {
  return get('/api/goals', params)
}

export const getGoalDetail = (id) => {
  return get(`/api/goals/${id}`)
}

export const createGoal = (data) => {
  return post('/api/goals', data)
}

export const updateGoal = (id, data) => {
  return post(`/api/goals/${id}`, data)
}

export const deleteGoal = (id) => {
  return post(`/api/goals/${id}/delete`)
}

export const completeGoal = (id) => {
  return post(`/api/goals/${id}/complete`)
}

export const getGoalCategories = () => {
  return get('/api/goals/categories')
}

export const getGoalTemplates = () => {
  return get('/api/goals/templates')
}

export const addGoalMilestone = (goalId, data) => {
  return post(`/api/goals/${goalId}/milestones`, data)
}

export const updateMilestone = (goalId, milestoneId, data) => {
  return post(`/api/goals/${goalId}/milestones/${milestoneId}`, data)
}

export const deleteMilestone = (goalId, milestoneId) => {
  return post(`/api/goals/${goalId}/milestones/${milestoneId}/delete`)
}

export const getGoalProgress = (id) => {
  return get(`/api/goals/${id}/progress`)
}
