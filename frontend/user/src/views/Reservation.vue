<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElDatePicker } from 'element-plus'
import { getHotels } from '../api/hotel'
import { getRoomTypes } from '../api/room'
import { createReservation } from '../api/reservation'

const router = useRouter()
const route = useRoute()
const hotels = ref([])
const roomTypes = ref([])
const selectedHotelId = ref('')
const selectedRoomTypeId = ref('')
const checkInDate = ref('')
const checkOutDate = ref('')
const guestCount = ref(1)

onMounted(() => {
  loadHotels()
  if (route.query.hotelId) {
    selectedHotelId.value = route.query.hotelId
    loadRoomTypes(route.query.hotelId)
  }
})

const loadHotels = async () => {
  try {
    const res = await getHotels()
    if (res.code === 200) {
      hotels.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载酒店列表失败')
  }
}

const loadRoomTypes = async (hotelId) => {
  try {
    const res = await getRoomTypes(hotelId)
    if (res.code === 200) {
      roomTypes.value = res.data
      selectedRoomTypeId.value = ''
    }
  } catch (error) {
    ElMessage.error('加载房型失败')
  }
}

const handleHotelChange = (hotelId) => {
  selectedHotelId.value = hotelId
  loadRoomTypes(hotelId)
}

const handleSubmit = async () => {
  if (!selectedHotelId.value) {
    ElMessage.warning('请选择酒店')
    return
  }
  if (!selectedRoomTypeId.value) {
    ElMessage.warning('请选择房型')
    return
  }
  if (!checkInDate.value || !checkOutDate.value) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  
  try {
    const data = {
      hotelId: selectedHotelId.value,
      roomTypeId: selectedRoomTypeId.value,
      guestId: guest.id,
      checkInDate: checkInDate.value,
      checkOutDate: checkOutDate.value,
      guestCount: guestCount.value,
      status: 'pending'
    }
    
    const res = await createReservation(data)
    if (res.code === 200) {
      ElMessage.success('预订成功')
      router.push('/my-reservations')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('预订失败')
  }
}
</script>

<template>
  <div class="reservation-page">
    <div class="container">
      <h2>在线预订</h2>
      
      <div class="reservation-form">
        <div class="form-section">
          <h3>选择酒店</h3>
          <div class="hotel-select">
            <select v-model="selectedHotelId" @change="handleHotelChange($event.target.value)">
              <option value="">请选择酒店</option>
              <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
                {{ hotel.name }} - {{ hotel.address }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-section">
          <h3>选择房型</h3>
          <div class="room-type-select">
            <select v-model="selectedRoomTypeId">
              <option value="">请选择房型</option>
              <option v-for="roomType in roomTypes" :key="roomType.id" :value="roomType.id">
                {{ roomType.name }} - ¥{{ roomType.pricePerNight }}/晚
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-section">
          <h3>选择日期</h3>
          <div class="date-row">
            <div class="date-item">
              <label>入住日期</label>
              <input v-model="checkInDate" type="date" />
            </div>
            <div class="date-item">
              <label>退房日期</label>
              <input v-model="checkOutDate" type="date" />
            </div>
          </div>
        </div>
        
        <div class="form-section">
          <h3>入住人数</h3>
          <div class="guest-count">
            <button @click="guestCount = Math.max(1, guestCount - 1)">-</button>
            <span>{{ guestCount }}</span>
            <button @click="guestCount = Math.min(10, guestCount + 1)">+</button>
          </div>
        </div>
        
        <div class="action-section">
          <button class="submit-btn" @click="handleSubmit">确认预订</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reservation-page {
  min-height: calc(100vh - 140px);
}

.container h2 {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.reservation-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-section {
  background: #fff;
  padding: 25px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.form-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.hotel-select select, .room-type-select select {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background: #fff;
  cursor: pointer;
}

.hotel-select select:focus, .room-type-select select:focus {
  outline: none;
  border-color: #667eea;
}

.date-row {
  display: flex;
  gap: 20px;
}

.date-item {
  flex: 1;
}

.date-item label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.date-item input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
}

.date-item input:focus {
  outline: none;
  border-color: #667eea;
}

.guest-count {
  display: flex;
  align-items: center;
  gap: 20px;
}

.guest-count button {
  width: 40px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 20px;
  cursor: pointer;
  background: #fff;
}

.guest-count button:hover {
  background: #f5f5f5;
}

.guest-count span {
  font-size: 24px;
  font-weight: bold;
  min-width: 40px;
  text-align: center;
}

.action-section {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  padding: 15px 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 30px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}
</style>
