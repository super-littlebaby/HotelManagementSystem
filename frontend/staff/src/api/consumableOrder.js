import request from '../utils/request'

export const getCheckInInfoByRoomNumber = (roomNumber) => {
  return request.get('/consumable-orders/check-in-info', { params: { roomNumber } })
}

export const addConsumableToBill = (data) => {
  return request.post('/consumable-orders/add', data)
}

export const getActiveConsumableItems = () => {
  return request.get('/consumable-orders/consumable-items')
}
