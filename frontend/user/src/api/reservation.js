import request from '../utils/request'

export const createReservation = (data) => {
  return request.post('/reservations', data)
}

export const getMyReservations = (guestId) => {
  return request.get(`/reservations/search/byGuestId?guestId=${guestId}`)
}

export const getReservationById = (id) => {
  return request.get(`/reservations/${id}`)
}

export const cancelReservation = (id) => {
  return request.put(`/reservations/${id}/cancel`)
}
