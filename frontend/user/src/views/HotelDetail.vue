<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { getHotelById } from '../api/hotel'
import { getRoomTypes } from '../api/room'

const router = useRouter()
const route = useRoute()
const hotel = ref(null)
const roomTypes = ref([])
const loading = ref(false)

onMounted(() => {
  loadHotel()
})

const loadHotel = async () => {
  const id = route.params.id
  loading.value = true
  try {
    const hotelRes = await getHotelById(id)
    if (hotelRes.code === 200) {
      hotel.value = hotelRes.data
      loadRoomTypes(id)
    } else {
      ElMessage.error(hotelRes.message)
    }
  } catch (error) {
    ElMessage.error('加载酒店信息失败')
  } finally {
    loading.value = false
  }
}

const loadRoomTypes = async (hotelId) => {
  try {
    const res = await getRoomTypes(hotelId)
    if (res.code === 200) {
      roomTypes.value = res.data
    }
  } catch (error) {
    console.error('加载房型失败')
  }
}

const goToReservation = () => {
  const isLoggedIn = localStorage.getItem('token')
  if (!isLoggedIn) {
    ElMessage.warning('请先登录或注册')
    router.push('/login')
  } else {
    router.push(`/reservation?hotelId=${hotel.value.id}`)
  }
}
</script>

<template>
  <div class="hotel-detail-page" v-loading="loading">
    <template v-if="hotel">
      <div class="hero-section" :style="{ background: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)` }">
        <div class="container">
          <h2>{{ hotel.name }}</h2>
          <p>{{ hotel.address }}</p>
        </div>
      </div>
      
      <div class="container">
        <div class="hotel-info">
          <div class="basic-info">
            <div class="info-item">
              <span class="label">电话</span>
              <span class="value">{{ hotel.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ hotel.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">星级</span>
              <span class="value">{{ hotel.starRating }} 星</span>
            </div>
          </div>
          <div class="description">
            <h3>酒店介绍</h3>
            <p>{{ hotel.description }}</p>
          </div>
        </div>
        
        <div class="room-types">
          <h3>房型列表</h3>
          <div class="room-type-grid">
            <div 
              class="room-type-card" 
              v-for="roomType in roomTypes" 
              :key="roomType.id"
            >
              <div class="room-image">
                <img :src="`https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20room%20interior%20${roomType.name}&image_size=landscape_4_3`" alt="房型图片" />
              </div>
              <div class="room-info">
                <h4>{{ roomType.name }}</h4>
                <p class="bed-type">🛏️ {{ roomType.bedType }}</p>
                <p class="area">📐 {{ roomType.area }} 平方米</p>
                <p class="capacity">👥 最多 {{ roomType.maxOccupancy }} 人</p>
                <p class="description">{{ roomType.description }}</p>
                <div class="price">
                  <span class="currency">¥</span>
                  <span class="amount">{{ roomType.pricePerNight }}</span>
                  <span class="unit">/晚</span>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="roomTypes.length === 0" class="empty-state">
            <p>暂无房型信息</p>
          </div>
        </div>
        
        <div class="action-section">
          <button class="book-btn" @click="goToReservation">立即预订</button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.hotel-detail-page {
  min-height: calc(100vh - 140px);
}

.hero-section {
  color: #fff;
  padding: 40px 0;
  text-align: center;
}

.hero-section h2 {
  font-size: 32px;
  margin-bottom: 10px;
}

.hero-section p {
  font-size: 16px;
}

.hotel-info {
  margin: 30px 0;
}

.basic-info {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
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

.description h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.description p {
  font-size: 16px;
  line-height: 1.8;
  color: #666;
}

.room-types {
  margin: 30px 0;
}

.room-types h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.room-type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.room-type-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.room-image img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.room-info {
  padding: 20px;
}

.room-info h4 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.bed-type, .area, .capacity {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.room-info .description {
  font-size: 14px;
  color: #888;
  margin-bottom: 15px;
}

.price {
  font-size: 22px;
  color: #e74c3c;
  font-weight: bold;
}

.currency {
  font-size: 14px;
}

.amount {
  font-size: 24px;
}

.unit {
  font-size: 14px;
  font-weight: normal;
  color: #888;
}

.action-section {
  text-align: center;
  margin: 40px 0;
}

.book-btn {
  padding: 15px 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 30px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.book-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #999;
}
</style>
