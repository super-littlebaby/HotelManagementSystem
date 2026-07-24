import request from '../utils/request'

export const getRoomTypes = (hotelId) => {
  return request.get(`/room-types/search/byHotelId?hotelId=${hotelId}`)
}

export const getRoomsByType = (roomTypeId) => {
  return request.get(`/rooms/search/byRoomTypeId?roomTypeId=${roomTypeId}`)
}

export const getRoomById = (id) => {
  return request.get(`/rooms/${id}`)
}

export const getFacilities = () => {
  return request.get('/facilities')
}
