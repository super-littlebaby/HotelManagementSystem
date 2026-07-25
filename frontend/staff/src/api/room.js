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
