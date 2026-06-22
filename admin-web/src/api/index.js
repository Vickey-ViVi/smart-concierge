import request from './request'

export const login = (data) => request.post('/login', data)
export const logout = () => request.post('/logout')

export const getOverview = () => request.get('/dashboard/overview')
export const getTopQuestions = () => request.get('/dashboard/questions')
export const getTopComplaints = () => request.get('/dashboard/complaints')
export const getTrend = () => request.get('/dashboard/trend')

export const getComplaintList = (params) => request.get('/complaint/list', { params })
export const getComplaintDetail = (id) => request.get(`/complaint/detail/${id}`)
export const getComplaintPhone = (id) => request.get(`/complaint/phone/${id}`)
export const handleComplaint = (data) => request.post('/complaint/handle', data)

export const addLostFound = (data) => request.post('/lost/add', data)
export const getLostList = () => request.get('/lost/list')
export const matchLost = (complaintId) => request.get(`/lost/match/${complaintId}`)

export const getProposalList = () => request.get('/proposal/list')
export const updateProposalThreshold = (data) => request.put('/proposal/threshold', data)
export const exportProposal = () =>
  request.get('/proposal/export', { responseType: 'blob' })
export const getProposalRecords = (params) => request.get('/proposal/records', { params })

export const getShopPage = (params) => request.get('/shop/page', { params })
export const saveShop = (data) => request.post('/shop/save', data)
export const deleteShop = (id) => request.delete(`/shop/delete/${id}`)

export const getActivityPage = (params) => request.get('/activity/page', { params })
export const saveActivity = (data) => request.post('/activity/save', data)
export const deleteActivity = (id) => request.delete(`/activity/delete/${id}`)

export const getUserList = () => request.get('/user/list')
export const saveUser = (data) => request.post('/user/save', data)
export const deleteUser = (id) => request.delete(`/user/delete/${id}`)
export const resetPassword = (id) => request.post(`/user/reset-password/${id}`)

export const getConfigList = () => request.get('/config/list')
export const updateConfig = (data) => request.put('/config/update', data)

export const getAuditLogs = (params) => request.get('/audit/logs', { params })
export const exportAudit = (params) =>
  request.get('/audit/export', { params, responseType: 'blob' })

export const getMonitorDeepseek = () => request.get('/monitor/deepseek')

export const getQuestionList = (params) => request.get('/question/list', { params })
export const getQuestionDetail = (id) => request.get(`/question/detail/${id}`)

export const uploadFile = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
