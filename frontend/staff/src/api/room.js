import request from '../utils/request'

export const getRooms = () => {
  return request.get('/rooms')
}

export const getRoomById = (id) => {
  return request.get(`/rooms/${id}`)
}

export const createRoom = (data) => {
  return request.post('/rooms', data)
}

export const updateRoom = (id, data) => {
  return request.put(`/rooms/${id}`, data)
}

export const deleteRoom = (id) => {
  return request.delete(`/rooms/${id}`)
}

/**
 * 按房型查询房间列表
 * @param {Number} roomTypeId - 房型ID
 * @returns {Promise} 房间列表
 */
export const getRoomsByType = (roomTypeId) => {
  return request.get(`/rooms/search/byRoomTypeId`, {
    params: { roomTypeId }
  })
}

/**
 * 按酒店查询房间列表
 * @param {Number} hotelId - 酒店ID
 * @returns {Promise} 房间列表
 */
export const getRoomsByHotel = (hotelId) => {
  return request.get(`/rooms/search/byHotelId`, {
    params: { hotelId }
  })
}
