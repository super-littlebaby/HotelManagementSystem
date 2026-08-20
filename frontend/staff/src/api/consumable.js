import request from '../utils/request'

export const getConsumableItems = () => {
  return request.get('/consumable-items')
}

export const getConsumableItemById = (id) => {
  return request.get(`/consumable-items/${id}`)
}

export const createConsumableItem = (data) => {
  return request.post('/consumable-items', data)
}

export const updateConsumableItem = (id, data) => {
  return request.put(`/consumable-items/${id}`, data)
}

export const deleteConsumableItem = (id) => {
  return request.delete(`/consumable-items/${id}`)
}

export const getConsumableItemsByHotelId = (hotelId) => {
  return request.get('/consumable-items/search/byHotelId', { params: { hotelId } })
}

export const getConsumableItemsByCategory = (category) => {
  return request.get('/consumable-items/search/byCategory', { params: { category } })
}

export const getConsumableItemsByIsActive = (isActive) => {
  return request.get('/consumable-items/search/byIsActive', { params: { isActive } })
}

export const getConsumableItemsByHotelAndCategory = (hotelId, category) => {
  return request.get('/consumable-items/search/byHotelIdAndCategory', { params: { hotelId, category } })
}

export const getConsumableItemsByHotelAndIsActive = (hotelId, isActive) => {
  return request.get('/consumable-items/search/byHotelIdAndIsActive', { params: { hotelId, isActive } })
}
