import request from '../utils/request'

/**
 * 根据客人ID查询历史入住记录
 * @param {Number} guestId - 客人ID
 * @returns {Promise} 入住记录列表
 */
export const getMyCheckIns = (guestId) => {
  return request.get(`/check-ins/search/byGuestId?guestId=${guestId}`)
}
