<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReservations, searchByPhone, searchByEmail, cancelReservation } from '../api/reservation'
import { state as authState } from '../stores/auth'

const reservations = ref([])
const loading = ref(false)
const searchType = ref('phone')
const searchValue = ref('')
const searching = ref(false)
const activeTab = ref('list') // list | detail
const selectedReservation = ref(null)

onMounted(() => {
  if (authState.isLoggedIn) {
    loadMyReservations()
  }
})

// 加载我的预订
const loadMyReservations = async () => {
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  if (!guest || !guest.id) {
    return
  }

  loading.value = true
  try {
    const res = await getMyReservations(guest.id)
    if (res.code === 200) {
      reservations.value = res.data
    } else {
      ElMessage.error(res.message || '加载预订列表失败')
    }
  } catch (error) {
    ElMessage.error('加载预订列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索预订
const handleSearch = async () => {
  if (!searchValue.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  searching.value = true
  try {
    let res
    if (searchType.value === 'phone') {
      res = await searchByPhone(searchValue.value.trim())
    } else if (searchType.value === 'email') {
      res = await searchByEmail(searchValue.value.trim())
    }

    if (res.code === 200) {
      reservations.value = res.data
      if (res.data.length === 0) {
        ElMessage.info('未找到相关预订记录')
      }
    } else {
      ElMessage.error(res.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    searching.value = false
  }
}

// 查看详情
const viewDetail = (reservation) => {
  selectedReservation.value = reservation
  activeTab.value = 'detail'
}

// 返回列表
const backToList = () => {
  activeTab.value = 'list'
  selectedReservation.value = null
}

// 取消预订
const handleCancel = async (reservation) => {
  // 确保已登录用户才能取消
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  if (!guest || !guest.id) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要取消该预订吗？取消后不可恢复。',
      '取消预订',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    const res = await cancelReservation(reservation.id)
    if (res.code === 200) {
      ElMessage.success('预订已取消')
      // 更新列表中的状态
      const idx = reservations.value.findIndex(r => r.id === reservation.id)
      if (idx !== -1) {
        reservations.value[idx] = res.data
      }
      if (selectedReservation.value && selectedReservation.value.id === reservation.id) {
        selectedReservation.value = res.data
      }
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待确认',
    'confirmed': '已确认',
    'checked_in': '已入住',
    'checked_out': '已退房',
    'cancelled': '已取消',
    'no_show': '未入住'
  }
  return statusMap[status] || status
}

// 状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'pending': '#f39c12',
    'confirmed': '#3498db',
    'checked_in': '#27ae60',
    'checked_out': '#95a5a6',
    'cancelled': '#e74c3c',
    'no_show': '#9b59b6'
  }
  return colorMap[status] || '#333'
}

// 是否可取消
const canCancel = (status) => {
  return status === 'pending' || status === 'confirmed'
}

// 渠道文本
const getChannelText = (channel) => {
  const channelMap = {
    'online': '在线预订',
    'phone': '电话预订',
    'walk_in': '到店预订',
    'ota': 'OTA平台'
  }
  return channelMap[channel] || channel
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr
}

// 格式化时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <div class="my-reservations-page">
    <div class="container">
      <h2>我的预订</h2>

      <!-- 未登录状态：显示搜索框 -->
      <div v-if="!authState.isLoggedIn" class="search-section">
        <div class="search-card">
          <h3>查询预订</h3>
          <p class="subtitle">未登录？使用手机号或邮箱查询您的预订</p>

          <div class="search-tabs">
            <button
              :class="{ active: searchType === 'phone' }"
              @click="searchType = 'phone'"
            >
              手机号查询
            </button>
            <button
              :class="{ active: searchType === 'email' }"
              @click="searchType = 'email'"
            >
              邮箱查询
            </button>
          </div>

          <div class="search-input-row">
            <input
              v-model="searchValue"
              :type="searchType === 'phone' ? 'tel' : 'email'"
              :placeholder="searchType === 'phone' ? '请输入手机号' : '请输入邮箱'"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch" :disabled="searching">
              {{ searching ? '搜索中...' : '查询' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 预订列表视图 -->
      <div v-if="activeTab === 'list'">
        <div v-if="authState.isLoggedIn" class="action-bar">
          <span>共 {{ reservations.length }} 条预订记录</span>
          <button class="refresh-btn" @click="loadMyReservations" :disabled="loading">
            刷新
          </button>
        </div>

        <div class="reservation-list" v-loading="loading || searching">
          <div
            class="reservation-card"
            v-for="reservation in reservations"
            :key="reservation.id"
          >
            <div class="reservation-header">
              <div class="hotel-info">
                <h3>{{ reservation.hotelName || '酒店' }}</h3>
                <span class="booking-id">订单号: {{ reservation.id }}</span>
              </div>
              <span
                class="status"
                :style="{ color: getStatusColor(reservation.status), borderColor: getStatusColor(reservation.status) }"
              >
                {{ getStatusText(reservation.status) }}
              </span>
            </div>

            <div class="reservation-info">
              <div class="info-item">
                <span class="label">入住日期</span>
                <span class="value">{{ formatDate(reservation.checkInDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">退房日期</span>
                <span class="value">{{ formatDate(reservation.checkOutDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">房型</span>
                <span class="value">
                  <template v-if="reservation.rooms && reservation.rooms.length > 0">
                    {{ reservation.rooms.length }}间 · {{ reservation.rooms[0].roomTypeName || '-' }}
                  </template>
                  <template v-else>-</template>
                </span>
              </div>
              <div class="info-item">
                <span class="label">金额</span>
                <span class="value price">¥{{ Number(reservation.totalAmount).toFixed(2) }}</span>
              </div>
            </div>

            <div class="reservation-footer">
              <span class="channel">{{ getChannelText(reservation.channel) }}</span>
              <div class="actions">
                <button class="btn-detail" @click="viewDetail(reservation)">
                  查看详情
                </button>
                <button
                  v-if="canCancel(reservation.status)"
                  class="btn-cancel"
                  @click="handleCancel(reservation)"
                >
                  取消预订
                </button>
              </div>
            </div>
          </div>

          <div v-if="reservations.length === 0 && !loading && !searching" class="empty-state">
            <div class="empty-icon">📋</div>
            <p>暂无预订记录</p>
            <p class="empty-tip" v-if="authState.isLoggedIn">去挑选心仪的酒店开始您的旅程吧</p>
          </div>
        </div>
      </div>

      <!-- 预订详情视图 -->
      <div v-if="activeTab === 'detail' && selectedReservation" class="detail-view">
        <button class="back-btn" @click="backToList">
          ← 返回列表
        </button>

        <div class="detail-card">
          <div class="detail-header">
            <div>
              <h3>{{ selectedReservation.hotelName || '酒店' }}</h3>
              <span class="booking-id">订单号: {{ selectedReservation.id }}</span>
            </div>
            <span
              class="status"
              :style="{ color: getStatusColor(selectedReservation.status), borderColor: getStatusColor(selectedReservation.status) }"
            >
              {{ getStatusText(selectedReservation.status) }}
            </span>
          </div>

          <div class="detail-section">
            <h4>预订信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">客人姓名</span>
                <span class="value">{{ selectedReservation.guestName || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">预订渠道</span>
                <span class="value">{{ getChannelText(selectedReservation.channel) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">入住日期</span>
                <span class="value">{{ formatDate(selectedReservation.checkInDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">退房日期</span>
                <span class="value">{{ formatDate(selectedReservation.checkOutDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">预订时间</span>
                <span class="value">{{ formatDateTime(selectedReservation.bookingDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">总金额</span>
                <span class="value price">¥{{ Number(selectedReservation.totalAmount).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>房间明细</h4>
            <div class="room-list-detail">
              <div
                v-for="(room, index) in selectedReservation.rooms"
                :key="room.id"
                class="room-item-detail"
              >
                <div class="room-header">
                  <span class="room-name">房间 {{ index + 1 }}: {{ room.roomTypeName || '-' }}</span>
                  <span class="room-rate">¥{{ Number(room.ratePerNight).toFixed(2) }}/晚</span>
                </div>
                <div class="room-details">
                  <span>{{ room.adults }}位成人</span>
                  <span v-if="room.children > 0">{{ room.children }}位儿童</span>
                  <span v-if="room.roomNumber">房间号: {{ room.roomNumber }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="selectedReservation.specialRequests" class="detail-section">
            <h4>特殊要求</h4>
            <p class="special-requests">{{ selectedReservation.specialRequests }}</p>
          </div>

          <div v-if="canCancel(selectedReservation.status)" class="detail-actions">
            <button class="btn-cancel-large" @click="handleCancel(selectedReservation)">
              取消预订
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-reservations-page {
  min-height: calc(100vh - 140px);
  background: #f5f6fa;
  padding: 30px 0;
}

.container h2 {
  font-size: 28px;
  margin-bottom: 24px;
  color: #333;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 30px;
}

.search-card {
  background: #fff;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  max-width: 600px;
  margin: 0 auto;
}

.search-card h3 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #333;
}

.subtitle {
  color: #888;
  margin-bottom: 20px;
  font-size: 14px;
}

.search-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-tabs button {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-tabs button.active {
  border-color: #667eea;
  background: #667eea;
  color: #fff;
}

.search-input-row {
  display: flex;
  gap: 10px;
}

.search-input-row input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

.search-input-row input:focus {
  outline: none;
  border-color: #667eea;
}

.search-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
}

.search-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.refresh-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 预订列表 */
.reservation-list {
  max-width: 900px;
  margin: 0 auto;
}

.reservation-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.reservation-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.hotel-info h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 6px;
}

.booking-id {
  font-size: 13px;
  color: #999;
}

.status {
  font-size: 13px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid;
  background: rgba(255, 255, 255, 0.9);
}

.reservation-info {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item .label {
  font-size: 13px;
  color: #999;
}

.info-item .value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.info-item .price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.reservation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.channel {
  font-size: 13px;
  color: #999;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-detail {
  padding: 8px 18px;
  background: #fff;
  border: 1px solid #667eea;
  color: #667eea;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-detail:hover {
  background: #667eea;
  color: #fff;
}

.btn-cancel {
  padding: 8px 18px;
  background: #fff;
  border: 1px solid #e74c3c;
  color: #e74c3c;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel:hover {
  background: #e74c3c;
  color: #fff;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 8px;
}

.empty-tip {
  font-size: 14px;
  color: #bbb;
}

/* 详情视图 */
.detail-view {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  background: none;
  border: none;
  color: #667eea;
  font-size: 15px;
  cursor: pointer;
  margin-bottom: 16px;
  padding: 4px 0;
}

.back-btn:hover {
  text-decoration: underline;
}

.detail-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.detail-header h3 {
  font-size: 22px;
  margin-bottom: 6px;
  color: #fff;
}

.detail-header .booking-id {
  color: rgba(255, 255, 255, 0.8);
}

.detail-header .status {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff !important;
}

.detail-section {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-of-type {
  border-bottom: none;
}

.detail-section h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 13px;
  color: #999;
}

.detail-item .value {
  font-size: 15px;
  color: #333;
}

.detail-item .price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.room-list-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-item-detail {
  background: #f9f9fb;
  padding: 16px;
  border-radius: 8px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.room-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.room-rate {
  font-size: 14px;
  color: #e67e22;
  font-weight: 500;
}

.room-details {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.special-requests {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  padding: 12px;
  background: #fff9e6;
  border-radius: 6px;
}

.detail-actions {
  padding: 20px 24px;
  text-align: center;
}

.btn-cancel-large {
  padding: 12px 40px;
  background: #fff;
  border: 1px solid #e74c3c;
  color: #e74c3c;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
}

.btn-cancel-large:hover {
  background: #e74c3c;
  color: #fff;
}

/* 响应式 */
@media (max-width: 640px) {
  .reservation-info {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
