import request from '../utils/request'

export const getHotels = () => {
  return request.get('/hotels')
}

export const getHotelById = (id) => {
  return request.get(`/hotels/${id}`)
}

export const searchHotelsByName = (name) => {
  return request.get(`/hotels/search/byName?name=${name}`)
}

export const searchHotelsByAddress = (address) => {
  return request.get(`/hotels/search/byAddress?address=${address}`)
}
