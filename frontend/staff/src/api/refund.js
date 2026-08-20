import request from '../utils/request'

export const getRefunds = () => {
  return request.get('/refunds')
}

export const getRefundById = (id) => {
  return request.get(`/refunds/${id}`)
}

export const getRefundsByBillId = (billId) => {
  return request.get(`/refunds/search/byBillId`, { params: { billId } })
}

export const createRefund = (data) => {
  return request.post('/refunds', data)
}

export const updateRefund = (id, data) => {
  return request.put(`/refunds/${id}`, data)
}

export const deleteRefund = (id) => {
  return request.delete(`/refunds/${id}`)
}
