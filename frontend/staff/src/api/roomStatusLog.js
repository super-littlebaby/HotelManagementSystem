import request from '../utils/request'

/**
 * 查询所有房间状态变更日志
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogs = () => {
  return request.get('/room-status-logs')
}

/**
 * 根据ID查询单条日志
 * @param {Number} id - 日志ID
 * @returns {Promise} 日志详情
 */
export const getRoomStatusLogById = (id) => {
  return request.get(`/room-status-logs/${id}`)
}

/**
 * 按房间ID查询日志列表
 * @param {Number} roomId - 房间ID
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogsByRoomId = (roomId) => {
  return request.get('/room-status-logs/search/byRoomId', { params: { roomId } })
}

/**
 * 按操作人ID查询日志列表
 * @param {Number} changedBy - 操作人ID
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogsByChangedBy = (changedBy) => {
  return request.get('/room-status-logs/search/byChangedBy', { params: { changedBy } })
}

/**
 * 按酒店ID查询日志列表
 * @param {Number} hotelId - 酒店ID
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogsByHotelId = (hotelId) => {
  return request.get('/room-status-logs/search/byHotelId', { params: { hotelId } })
}

/**
 * 按新状态查询日志列表
 * @param {String} newStatus - 新状态
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogsByNewStatus = (newStatus) => {
  return request.get('/room-status-logs/search/byNewStatus', { params: { newStatus } })
}

/**
 * 按时间范围查询日志列表
 * @param {String} startTime - 开始时间(yyyy-MM-ddTHH:mm:ss)
 * @param {String} endTime - 结束时间(yyyy-MM-ddTHH:mm:ss)
 * @returns {Promise} 日志列表
 */
export const getRoomStatusLogsByTimeRange = (startTime, endTime) => {
  return request.get('/room-status-logs/search/byTimeRange', { params: { startTime, endTime } })
}

/**
 * 多条件组合分页查询
 * @param {Object} params - 查询参数 { hotelId, roomId, newStatus, changedBy, startTime, endTime, page, size }
 * @returns {Promise} 分页结果
 */
export const searchRoomStatusLogs = (params) => {
  return request.get('/room-status-logs/search', { params })
}

/**
 * 新增日志（仅手动补录场景）
 * @param {Object} data - 日志信息
 * @returns {Promise} 创建结果
 */
export const createRoomStatusLog = (data) => {
  return request.post('/room-status-logs', data)
}

/**
 * 更新日志
 * @param {Number} id - 日志ID
 * @param {Object} data - 日志信息
 * @returns {Promise} 更新结果
 */
export const updateRoomStatusLog = (id, data) => {
  return request.put(`/room-status-logs/${id}`, data)
}

/**
 * 删除日志
 * @param {Number} id - 日志ID
 * @returns {Promise} 删除结果
 */
export const deleteRoomStatusLog = (id) => {
  return request.delete(`/room-status-logs/${id}`)
}
