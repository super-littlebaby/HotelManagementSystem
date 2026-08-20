import request from '../utils/request'

/**
 * 查询预订列表（按酒店过滤）
 * @param {Number} hotelId - 酒店ID（可选）
 * @returns {Promise} 预订列表
 */
export const getReservations = (hotelId) => {
  const params = hotelId ? `?hotelId=${hotelId}` : ''
  return request.get(`/reservations${params}`)
}

/**
 * 查询预订详情
 * @param {Number} id - 预订ID
 * @returns {Promise} 预订详情
 */
export const getReservationById = (id) => {
  return request.get(`/reservations/${id}`)
}

/**
 * 创建预订
 * @param {Object} data - 预订信息
 * @returns {Promise} 创建结果
 */
export const createReservation = (data) => {
  return request.post('/reservations/create', data)
}

/**
 * 确认预订
 * @param {Number} id - 预订ID
 * @param {Number} roomId - 房间ID（可选）
 * @returns {Promise} 确认结果
 */
export const confirmReservation = (id, roomId) => {
  return request.put(`/reservations/${id}/confirm`, { roomId })
}

/**
 * 取消预订
 * @param {Number} id - 预订ID
 * @returns {Promise} 取消结果
 */
export const cancelReservation = (id) => {
  return request.put(`/reservations/${id}/cancel`)
}

/**
 * 办理入住（按房间录入实际入住人信息）
 * @param {Number} id - 预订ID
 * @param {Object} data - 按房间分组的入住人信息 { rooms: [{ reservationRoomId, primaryGuestName, primaryIdType, primaryIdNumber, primaryPhone, stayGuests }] }
 * @returns {Promise} 入住结果
 */
export const checkInReservation = (id, data) => {
  return request.put(`/reservations/${id}/check-in`, data || {})
}

/**
 * 办理退房
 * @param {Number} id - 预订ID
 * @returns {Promise} 退房结果
 */
export const checkOutReservation = (id) => {
  return request.put(`/reservations/${id}/check-out`)
}

/**
 * 分配房间
 * @param {Number} id - 预订ID
 * @param {Number} roomId - 房间ID
 * @param {Number} roomTypeId - 房型ID（可选，换房型时使用）
 * @returns {Promise} 分配结果
 */
export const assignRoom = (id, roomId, roomTypeId) => {
  const data = { roomId }
  if (roomTypeId) {
    data.roomTypeId = roomTypeId
  }
  return request.put(`/reservations/${id}/assign-room`, data)
}

/**
 * 通过客人手机号查询预订
 * @param {String} phone - 手机号
 * @returns {Promise} 预订列表
 */
export const searchByGuestPhone = (phone) => {
  return request.get(`/reservations/search/byGuestPhone?phone=${phone}`)
}

/**
 * 通过客人邮箱查询预订
 * @param {String} email - 邮箱
 * @returns {Promise} 预订列表
 */
export const searchByGuestEmail = (email) => {
  return request.get(`/reservations/search/byGuestEmail?email=${email}`)
}

/**
 * 通过客人姓名查询预订
 * @param {String} name - 姓名
 * @returns {Promise} 预订列表
 */
export const searchByGuestName = (name) => {
  return request.get(`/reservations/search/byGuestName?name=${name}`)
}
