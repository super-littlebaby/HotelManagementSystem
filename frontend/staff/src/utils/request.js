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
  return Promise.reject(error)
})

export default request
