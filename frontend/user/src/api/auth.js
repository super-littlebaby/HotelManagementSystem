import request from '../utils/request'

export const login = (data) => {
  return request.post('/guests/login', data)
}

export const register = (data) => {
  return request.post('/guests/register', data)
}

export const getGuestInfo = () => {
  return request.get('/guests/info')
}

export const updateGuest = (data) => {
  return request.put('/guests/update', data)
}

export const resetPassword = (data) => {
  return request.post('/guests/reset-password', data)
}
