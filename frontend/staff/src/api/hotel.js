import request from '../utils/request'

export const getHotels = () => {
  return request.get('/hotels')
}

export const getHotelById = (id) => {
  return request.get(`/hotels/${id}`)
}

export const createHotel = (data) => {
  return request.post('/hotels', data)
}

export const updateHotel = (id, data) => {
  return request.put(`/hotels/${id}`, data)
}

export const deleteHotel = (id) => {
  return request.delete(`/hotels/${id}`)
}
