<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { getMyReservations } from '../api/reservation'
import { getHotelById } from '../api/hotel'

const reservations = ref([])
const hotels = ref({})
const loading = ref(false)

onMounted(() => {
  loadReservations()
})

const loadReservations = async () => {
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  if (!guest) {
    ElMessage.error('请先登录')
    return
  }
  
  loading.value = true
  try {
    const res = await getMyReservations(guest.id)
    if (res.code === 200) {
      reservations.value = res.data
      await loadHotelsInfo(res.data)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('加载预订列表失败')
  } finally {
    loading.value = false
  }
}

const loadHotelsInfo = async (reservationsList) => {
  const hotelIds = [...new Set(reservationsList.map(r => r.hotelId))]
  for (const hotelId of hotelIds) {
    try {
      const res = await getHotelById(hotelId)
      if (res.code === 200) {
        hotels.value[hotelId] = res.data
      }
    } catch (error) {
      console.error('加载酒店信息失败')
    }
  }
}

const getHotelName = (hotelId) => {
  return hotels.value[hotelId]?.name || '未知酒店'
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待确认',
    'confirmed': '已确认',
    'checked_in': '已入住',
    'checked_out': '已退房',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    'pending': '#f39c12',
    'confirmed': '#3498db',
    'checked_in': '#27ae60',
    'checked_out': '#95a5a6',
    'cancelled': '#e74c3c'
  }
  return colorMap[status] || '#333'
}
</script>

<template>
  <div class="my-reservations-page">
    <div class="container">
      <h2>我的预订</h2>
      
      <div class="reservation-list" v-loading="loading">
        <div 
          class="reservation-card" 
          v-for="reservation in reservations" 
          :key="reservation.id"
        >
          <div class="reservation-header">
            <h3>{{ getHotelName(reservation.hotelId) }}</h3>
            <span 
              class="status" 
              :style="{ color: getStatusColor(reservation.status) }"
            >
              {{ getStatusText(reservation.status) }}
            </span>
          </div>
          
          <div class="reservation-info">
            <div class="info-item">
              <span class="label">预订编号</span>
              <span class="value">{{ reservation.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">入住日期</span>
              <span class="value">{{ reservation.checkInDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">退房日期</span>
              <span class="value">{{ reservation.checkOutDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">入住人数</span>
              <span class="value">{{ reservation.guestCount }} 人</span>
            </div>
          </div>
          
          <div class="reservation-footer">
            <span class="create-time">创建时间: {{ reservation.createdAt }}</span>
          </div>
        </div>
        
        <div v-if="reservations.length === 0 && !loading" class="empty-state">
          <p>暂无预订记录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-reservations-page {
  min-height: calc(100vh - 140px);
}

.container h2 {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.reservation-list {
  max-width: 800px;
  margin: 0 auto;
}

.reservation-card {
  background: #fff;
  border-radius: 10px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.reservation-header h3 {
  font-size: 20px;
  color: #333;
}

.status {
  font-size: 14px;
  font-weight: bold;
  padding: 5px 15px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.05);
}

.reservation-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 14px;
  color: #888;
  margin-bottom: 5px;
}

.value {
  font-size: 16px;
  color: #333;
}

.reservation-footer {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.create-time {
  font-size: 14px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #999;
}
</style>
