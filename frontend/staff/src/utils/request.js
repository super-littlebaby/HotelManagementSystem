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
  const res = response.data
  if (res.code !== undefined && res.code !== 200) {
    return Promise.reject({
      code: res.code,
      message: res.message || '请求失败',
      data: res.data
    })
  }
  return res
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
