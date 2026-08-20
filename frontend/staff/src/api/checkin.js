import request from '../utils/request'

export const getCheckIns = () => {
  return request.get('/check-ins')
}

export const getCheckInById = (id) => {
  return request.get(`/check-ins/${id}`)
}

export const createCheckIn = (data) => {
  return request.post('/check-ins', data)
}

export const updateCheckIn = (id, data) => {
  return request.put(`/check-ins/${id}`, data)
}

export const deleteCheckIn = (id) => {
  return request.delete(`/check-ins/${id}`)
}

export const preCheckOut = (id) => {
  return request.get(`/check-ins/${id}/pre-check-out`)
}

export const checkOut = (id, data) => {
  return request.put(`/check-ins/${id}/check-out`, data || {})
}
