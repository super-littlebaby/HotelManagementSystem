<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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

const roomDefaultImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20modern%20hotel%20bedroom%20interior%20with%20comfortable%20bed%20and%20warm%20lighting&image_size=landscape_4_3'

const goToReservation = () => {
  const isLoggedIn = localStorage.getItem('token')
  if (!isLoggedIn) {
    ElMessage.warning('请先登录或注册')
    router.push('/login')
  } else {
    router.push(`/reservation?hotelId=${hotel.value.id}`)
  }
}

const bookRoomType = (roomType) => {
  if (!roomType) return
  const isLoggedIn = localStorage.getItem('token')
  const query = {
    hotelId: hotel.value.id,
    roomTypeId: roomType.id
  }
  if (!isLoggedIn) {
    ElMessage.warning('请先登录或注册')
    router.push({ path: '/login', query: { redirect: '/reservation', ...query } })
  } else {
    router.push({ path: '/reservation', query })
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
              class="room-type-card clickable" 
              v-for="roomType in roomTypes" 
              :key="roomType.id"
              @click="bookRoomType(roomType)"
              :title="`点击预订「${roomType.name}」`"
            >
              <div class="room-image">
                <img :src="roomDefaultImage" :alt="roomType.name" />
              </div>
              <div class="room-info">
                <h4>{{ roomType.name }}</h4>
                <p class="bed-type">🛏️ {{ roomType.bedType }}</p>
                <p class="area">📐 {{ roomType.area }} 平方米</p>
                <p class="capacity">👥 最多 {{ roomType.maxAdults }} 成人 / {{ roomType.maxChildren }} 儿童</p>
                <p class="description">{{ roomType.description }}</p>
                <div class="price">
                  <span class="currency">¥</span>
                  <span class="amount">{{ roomType.basePrice }}</span>
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

