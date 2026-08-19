import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('staff_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(response => {
  return response.data
}, error => {
  if (error.response && error.response.status === 401) {
    localStorage.removeItem('staff_token')
    localStorage.removeItem('staff')
    window.location.href = '/login'
  }
  if (error.response && error.response.data) {
    return Promise.reject({
      code: error.response.data.code,
      message: error.response.data.message,
      data: error.response.data.data
    })
  }
  return Promise.reject(error)
})

export default request
