import request from '../utils/request'

export const getRoomDamageInfo = (roomNumber) => {
  return request.get('/facility-damage/info', {
    params: { roomNumber }
  })
}

export const reportDamage = (data) => {
  return request.post('/facility-damage/report', data)
}
