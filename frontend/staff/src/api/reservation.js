import request from '../utils/request'

export const getReservations = () => {
  return request.get('/reservations')
}

export const getReservationById = (id) => {
  return request.get(`/reservations/${id}`)
}

export const createReservation = (data) => {
  return request.post('/reservations', data)
}

export const updateReservation = (id, data) => {
  return request.put(`/reservations/${id}`, data)
}

export const deleteReservation = (id) => {
  return request.delete(`/reservations/${id}`)
}
