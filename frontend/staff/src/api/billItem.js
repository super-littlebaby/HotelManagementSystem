import request from '../utils/request'

export const getBillItems = (billId) => {
  return request.get(`/bill-items/search/byBillId`, { params: { billId } })
}

export const getBillItemById = (id) => {
  return request.get(`/bill-items/${id}`)
}

export const createBillItem = (data) => {
  return request.post('/bill-items', data)
}

export const updateBillItem = (id, data) => {
  return request.put(`/bill-items/${id}`, data)
}

export const deleteBillItem = (id) => {
  return request.delete(`/bill-items/${id}`)
}
