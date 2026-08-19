import request from '../utils/request'

export const getRoomTypeFacilities = () => {
  return request.get('/room-type-facilities')
}

export const getRoomTypeFacilityById = (roomTypeId, facilityId) => {
  return request.get(`/room-type-facilities/${roomTypeId}/${facilityId}`)
}

export const createRoomTypeFacility = (data) => {
  return request.post('/room-type-facilities', data)
}

export const updateRoomTypeFacility = (roomTypeId, facilityId, data) => {
  return request.put(`/room-type-facilities/${roomTypeId}/${facilityId}`, data)
}

export const deleteRoomTypeFacility = (roomTypeId, facilityId) => {
  return request.delete(`/room-type-facilities/${roomTypeId}/${facilityId}`)
}

export const getFacilitiesByRoomTypeId = (roomTypeId) => {
  return request.get(`/room-type-facilities/${roomTypeId}/facilities`)
}

export const addFacilitiesToRoomType = (roomTypeId, facilityIds) => {
  return request.post(`/room-type-facilities/${roomTypeId}/facilities`, facilityIds)
}

export const removeFacilitiesFromRoomType = (roomTypeId, facilityIds) => {
  return request.delete(`/room-type-facilities/${roomTypeId}/facilities`, { data: facilityIds })
}

export const replaceFacilitiesForRoomType = (roomTypeId, facilityIds) => {
  return request.put(`/room-type-facilities/${roomTypeId}/facilities`, facilityIds)
}