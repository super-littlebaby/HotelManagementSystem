import request from '../utils/request'

export const getFacilities = () => {
  return request.get('/facilities')
}

export const getFacilityById = (id) => {
  return request.get(`/facilities/${id}`)
}

export const createFacility = (data) => {
  return request.post('/facilities', data)
}

export const updateFacility = (id, data) => {
  return request.put(`/facilities/${id}`, data)
}

export const deleteFacility = (id) => {
  return request.delete(`/facilities/${id}`)
}