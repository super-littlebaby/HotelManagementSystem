import request from '../utils/request'

export const getBills = () => {
  return request.get('/bills')
}

export const getBillById = (id) => {
  return request.get(`/bills/${id}`)
}

export const createBill = (data) => {
  return request.post('/bills', data)
}

export const updateBill = (id, data) => {
  return request.put(`/bills/${id}`, data)
}

export const deleteBill = (id) => {
  return request.delete(`/bills/${id}`)
}

export const settleBill = (id, paymentMethod) => {
  return request.put(`/bills/${id}/settle`, null, { params: { paymentMethod } })
}

export const voidBill = (id) => {
  return request.put(`/bills/${id}/void`)
}

export const getMonthlyClosedRevenue = () => {
  return request.get('/bills/stats/monthly-revenue')
}
