import request from '../utils/request'

export const getEmployees = () => {
  return request.get('/employees')
}

export const getEmployeeById = (id) => {
  return request.get(`/employees/${id}`)
}

export const createEmployee = (data) => {
  return request.post('/employees', data)
}

export const updateEmployee = (id, data) => {
  return request.put(`/employees/${id}`, data)
}

export const deleteEmployee = (id) => {
  return request.delete(`/employees/${id}`)
}

export const searchByUsername = (username) => {
  return request.get('/employees/search/byUsername', { params: { username } })
}

export const searchByHotelId = (hotelId) => {
  return request.get('/employees/search/byHotelId', { params: { hotelId } })
}

export const searchByRole = (role) => {
  return request.get('/employees/search/byRole', { params: { role } })
}

export const getProfile = () => {
  return request.get('/employees/profile')
}

export const updateProfile = (data) => {
  return request.put('/employees/profile', data)
}

export const changePassword = (data) => {
  return request.put('/employees/password', data)
}

export const toggleEmployeeStatus = (id, isActive) => {
  return request.patch(`/employees/${id}/status`, null, { params: { isActive } })
}
