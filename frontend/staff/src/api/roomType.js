import request from '../utils/request'

export const getRoomTypes = () => {
  return request.get('/room-types')
}

export const getRoomTypeById = (id) => {
  return request.get(`/room-types/${id}`)
}

export const createRoomType = (data) => {
  return request.post('/room-types', data)
}

export const updateRoomType = (id, data) => {
  return request.put(`/room-types/${id}`, data)
}

export const deleteRoomType = (id) => {
  return request.delete(`/room-types/${id}`)
}
