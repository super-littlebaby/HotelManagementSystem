import request from '../utils/request'

export const login = (username, password) => {
  return request.post('/employees/login', { username, password })
}

export const getProfile = () => {
  return request.get('/employees/profile')
}
