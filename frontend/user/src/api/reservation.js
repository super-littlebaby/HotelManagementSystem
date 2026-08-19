import request from '../utils/request'

/**
 * 创建预订
 * @param {Object} data - 预订信息
 * @returns {Promise} 预订详情
 */
export const createReservation = (data) => {
  return request.post('/reservations/create', data)
}

/**
 * 查询我的预订
 * @param {Number} guestId - 客人ID
 * @returns {Promise} 预订列表
 */
export const getMyReservations = (guestId) => {
  return request.get(`/reservations/my/${guestId}`)
}

/**
 * 通过手机号查询预订
 * @param {String} phone - 客人手机号
 * @returns {Promise} 预订列表
 */
export const searchByPhone = (phone) => {
  return request.get(`/reservations/search/byGuestPhone?phone=${phone}`)
}

/**
 * 通过邮箱查询预订
 * @param {String} email - 客人邮箱
 * @returns {Promise} 预订列表
 */
export const searchByEmail = (email) => {
  return request.get(`/reservations/search/byGuestEmail?email=${email}`)
}

/**
 * 通过姓名查询预订
 * @param {String} name - 客人姓名
 * @returns {Promise} 预订列表
 */
export const searchByName = (name) => {
  return request.get(`/reservations/search/byGuestName?name=${name}`)
}

/**
 * 查询预订详情
 * @param {Number} id - 预订ID
 * @returns {Promise} 预订详情
 */
export const getReservationDetail = (id) => {
  return request.get(`/reservations/${id}`)
}

/**
 * 取消预订
 * @param {Number} id - 预订ID
 * @returns {Promise} 取消结果
 */
export const cancelReservation = (id) => {
  return request.put(`/reservations/${id}/cancel`)
}
