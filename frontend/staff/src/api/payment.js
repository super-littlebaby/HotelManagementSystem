import request from '../utils/request'

export const getPayments = () => {
  return request.get('/payments')
}

export const getPaymentById = (id) => {
  return request.get(`/payments/${id}`)
}

export const getPaymentsByBillId = (billId) => {
  return request.get(`/payments/search/byBillId`, { params: { billId } })
}

export const createPayment = (data) => {
  return request.post('/payments', data)
}

export const updatePayment = (id, data) => {
  return request.put(`/payments/${id}`, data)
}

export const deletePayment = (id) => {
  return request.delete(`/payments/${id}`)
}
