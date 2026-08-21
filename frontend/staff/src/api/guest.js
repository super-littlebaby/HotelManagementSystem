import request from '../utils/request'

/**
 * 按手机号查询客人档案列表
 * @param {String} phone - 手机号
 * @returns {Promise} 客人列表
 */
export const findGuestsByPhone = (phone) => {
  return request.get('/guests/search/byPhone', { params: { phone } })
}

/**
 * 按邮箱查询客人档案
 * @param {String} email - 邮箱
 * @returns {Promise} 客人信息
 */
export const findGuestByEmail = (email) => {
  return request.get('/guests/search/byEmail', { params: { email } })
}

/**
 * 按证件号查询客人档案
 * @param {String} idNumber - 证件号
 * @returns {Promise} 客人信息
 */
export const findGuestByIdNumber = (idNumber) => {
  return request.get('/guests/search/byIdNumber', { params: { idNumber } })
}

/**
 * 新增客人档案（线下客人）
 * @param {Object} data - 客人信息 { firstName, lastName, phone, email, idType, idNumber, ... }
 * @returns {Promise} 创建结果
 */
export const createGuest = (data) => {
  return request.post('/guests', data)
}

/**
 * 根据ID查询客人
 * @param {Number} id - 客人ID
 * @returns {Promise} 客人信息
 */
export const getGuestById = (id) => {
  return request.get(`/guests/${id}`)
}
