<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHotels } from '../api/hotel'
import { getRoomTypes } from '../api/room'
import { createReservation } from '../api/reservation'

const router = useRouter()
const route = useRoute()

const hotels = ref([])
const roomTypes = ref([])
const selectedHotelId = ref(null)
const checkInDate = ref('')
const checkOutDate = ref('')
const specialRequests = ref('')
const channel = ref('online')
const submitting = ref(false)

// 房间列表（支持多房间）
const rooms = ref([
  {
    roomTypeId: null,
    adults: 1,
    children: 0
  }
])

onMounted(async () => {
  await loadHotels()
  if (route.query.hotelId) {
    selectedHotelId.value = Number(route.query.hotelId)
    await loadRoomTypes(route.query.hotelId)
  }
  // 预选房型：在房型列表加载完成后回填第一间房
  if (route.query.roomTypeId && selectedHotelId.value) {
    const targetId = Number(route.query.roomTypeId)
    const matched = roomTypes.value.find(rt => rt.id === targetId)
    if (matched && rooms.value.length > 0) {
      rooms.value[0].roomTypeId = targetId
      // 按房型最大容纳推荐人数，但不低于当前默认值
      if (matched.maxAdults && rooms.value[0].adults > matched.maxAdults) {
        rooms.value[0].adults = matched.maxAdults
      }
      if (matched.maxChildren !== undefined && rooms.value[0].children > matched.maxChildren) {
        rooms.value[0].children = matched.maxChildren
      }
    }
  }
  // 预选日期（支持页面间传参）
  if (route.query.checkIn) {
    checkInDate.value = String(route.query.checkIn)
  }
  if (route.query.checkOut) {
    checkOutDate.value = String(route.query.checkOut)
  }
})

// 加载酒店列表
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

// 加载房型列表
const loadRoomTypes = async (hotelId) => {
  if (!hotelId) return
  try {
    const res = await getRoomTypes(hotelId)
    if (res.code === 200) {
      roomTypes.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载房型失败')
  }
}

// 酒店选择变化
const handleHotelChange = (event) => {
  const hotelId = event.target.value
  selectedHotelId.value = hotelId ? Number(hotelId) : null
  loadRoomTypes(hotelId)
  // 重置所有房间的房型选择
  rooms.value.forEach(room => {
    room.roomTypeId = null
  })
}

// 添加房间
const addRoom = () => {
  if (rooms.value.length >= 5) {
    ElMessage.warning('最多只能预订5间房')
    return
  }
  rooms.value.push({
    roomTypeId: null,
    adults: 1,
    children: 0
  })
}

// 删除房间
const removeRoom = (index) => {
  if (rooms.value.length <= 1) {
    ElMessage.warning('至少需要一间房')
    return
  }
  rooms.value.splice(index, 1)
}

// 计算入住天数
const nights = computed(() => {
  if (!checkInDate.value || !checkOutDate.value) return 0
  const inDate = new Date(checkInDate.value)
  const outDate = new Date(checkOutDate.value)
  const diff = outDate - inDate
  return Math.max(0, diff / (1000 * 60 * 60 * 24))
})

// 获取房型信息
const getRoomTypeInfo = (roomTypeId) => {
  return roomTypes.value.find(rt => rt.id === roomTypeId) || null
}

// 计算单个房间的价格
const getRoomPrice = (room) => {
  if (!room.roomTypeId || nights.value <= 0) return 0
  const roomType = getRoomTypeInfo(room.roomTypeId)
  if (!roomType) return 0
  return Number(roomType.basePrice) * nights.value
}

// 计算总价
const totalAmount = computed(() => {
  return rooms.value.reduce((sum, room) => sum + getRoomPrice(room), 0)
})

// 房型选择变化
const handleRoomTypeChange = (index, event) => {
  rooms.value[index].roomTypeId = event.target.value ? Number(event.target.value) : null
}

// 成人数量调整
const adjustAdults = (index, delta) => {
  const room = rooms.value[index]
  const roomType = getRoomTypeInfo(room.roomTypeId)
  const maxAdults = roomType?.maxAdults || 10
  const newValue = room.adults + delta
  if (newValue < 1) {
    ElMessage.warning('至少需要1位成人')
    return
  }
  if (newValue > maxAdults) {
    ElMessage.warning(`该房型最多容纳${maxAdults}位成人`)
    return
  }
  room.adults = newValue
}

// 儿童数量调整
const adjustChildren = (index, delta) => {
  const room = rooms.value[index]
  const roomType = getRoomTypeInfo(room.roomTypeId)
  const maxChildren = roomType?.maxChildren !== undefined ? roomType.maxChildren : 5
  const newValue = room.children + delta
  if (newValue < 0) return
  if (newValue > maxChildren) {
    ElMessage.warning(`该房型最多容纳${maxChildren}位儿童`)
    return
  }
  room.children = newValue
}

// 提交预订
const handleSubmit = async () => {
  // 校验酒店
  if (!selectedHotelId.value) {
    ElMessage.warning('请选择酒店')
    return
  }

  // 校验日期
  if (!checkInDate.value || !checkOutDate.value) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  if (nights.value <= 0) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }

  // 校验房间
  for (let i = 0; i < rooms.value.length; i++) {
    const room = rooms.value[i]
    if (!room.roomTypeId) {
      ElMessage.warning(`请选择第${i + 1}间房的房型`)
      return
    }
  }

  // 获取登录用户信息
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  if (!guest || !guest.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    const data = {
      guestId: guest.id,
      checkInDate: checkInDate.value,
      checkOutDate: checkOutDate.value,
      specialRequests: specialRequests.value,
      channel: channel.value,
      rooms: rooms.value.map(room => ({
        roomTypeId: room.roomTypeId,
        adults: room.adults,
        children: room.children
      }))
    }

    const res = await createReservation(data)
    if (res.code === 200) {
      ElMessage.success('预订成功')
      router.push('/my-reservations')
    } else {
      ElMessage.error(res.message || '预订失败')
    }
  } catch (error) {
    ElMessage.error('预订失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="reservation-page">
    <div class="container">
      <h2>在线预订</h2>

      <div class="reservation-content">
        <!-- 左侧表单 -->
        <div class="reservation-form">
          <!-- 选择酒店 -->
          <div class="form-section">
            <h3>选择酒店</h3>
            <div class="hotel-select">
              <select :value="selectedHotelId" @change="handleHotelChange">
                <option value="">请选择酒店</option>
                <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
                  {{ hotel.name }} - {{ hotel.address }}
                </option>
              </select>
            </div>
          </div>

          <!-- 选择日期 -->
          <div class="form-section">
            <h3>选择日期</h3>
            <div class="date-row">
              <div class="date-item">
                <label>入住日期</label>
                <input v-model="checkInDate" type="date" :min="new Date().toISOString().split('T')[0]" />
              </div>
              <div class="date-item">
                <label>退房日期</label>
                <input v-model="checkOutDate" type="date" :min="checkInDate || new Date().toISOString().split('T')[0]" />
              </div>
            </div>
            <div v-if="nights > 0" class="nights-info">
              共 <strong>{{ nights }}</strong> 晚
            </div>
          </div>

          <!-- 房间选择 -->
          <div class="form-section">
            <div class="section-header">
              <h3>房间信息</h3>
              <button class="add-room-btn" @click="addRoom" :disabled="!selectedHotelId">
                + 添加房间
              </button>
            </div>

            <div class="room-list">
              <div v-for="(room, index) in rooms" :key="index" class="room-item">
                <div class="room-header">
                  <span class="room-label">第 {{ index + 1 }} 间房</span>
                  <button v-if="rooms.length > 1" class="remove-btn" @click="removeRoom(index)">
                    删除
                  </button>
                </div>

                <div class="room-type-select">
                  <label>房型</label>
                  <select :value="room.roomTypeId" @change="handleRoomTypeChange(index, $event)" :disabled="!selectedHotelId">
                    <option value="">请选择房型</option>
                    <option v-for="roomType in roomTypes" :key="roomType.id" :value="roomType.id">
                      {{ roomType.name }} - ¥{{ roomType.basePrice }}/晚 (成人{{ roomType.maxAdults || 0 }}人, 儿童{{ roomType.maxChildren || 0 }}人)
                    </option>
                  </select>
                  <div v-if="room.roomTypeId" class="room-type-info">
                    最大容纳: 成人 {{ getRoomTypeInfo(room.roomTypeId)?.maxAdults || 0 }} 人, 儿童 {{ getRoomTypeInfo(room.roomTypeId)?.maxChildren || 0 }} 人
                  </div>
                </div>

                <div class="guest-counts" :class="{ disabled: !room.roomTypeId }">
                  <div class="count-item">
                    <label>成人</label>
                    <div class="count-controls" :class="{ disabled: !room.roomTypeId }">
                      <button @click="adjustAdults(index, -1)" :disabled="!room.roomTypeId">-</button>
                      <span>{{ room.adults }}</span>
                      <button @click="adjustAdults(index, 1)" :disabled="!room.roomTypeId">+</button>
                    </div>
                  </div>
                  <div class="count-item">
                    <label>儿童</label>
                    <div class="count-controls" :class="{ disabled: !room.roomTypeId }">
                      <button @click="adjustChildren(index, -1)" :disabled="!room.roomTypeId">-</button>
                      <span>{{ room.children }}</span>
                      <button @click="adjustChildren(index, 1)" :disabled="!room.roomTypeId">+</button>
                    </div>
                  </div>
                </div>

                <div v-if="room.roomTypeId && nights > 0" class="room-price">
                  房间小计: <strong>¥{{ getRoomPrice(room).toFixed(2) }}</strong>
                </div>
              </div>
            </div>
          </div>

          <!-- 特殊要求 -->
          <div class="form-section">
            <h3>特殊要求</h3>
            <textarea
              v-model="specialRequests"
              placeholder="如有特殊要求请在此填写（如：高楼层、无烟房、加床等）"
              rows="3"
            ></textarea>
          </div>
        </div>

        <!-- 右侧预订摘要 -->
        <div class="reservation-summary">
          <div class="summary-card">
            <h3>预订摘要</h3>

            <div class="summary-item" v-if="selectedHotelId">
              <span class="label">酒店</span>
              <span class="value">{{ hotels.find(h => h.id === selectedHotelId)?.name || '-' }}</span>
            </div>

            <div class="summary-item">
              <span class="label">入住日期</span>
              <span class="value">{{ checkInDate || '未选择' }}</span>
            </div>

            <div class="summary-item">
              <span class="label">退房日期</span>
              <span class="value">{{ checkOutDate || '未选择' }}</span>
            </div>

            <div class="summary-item" v-if="nights > 0">
              <span class="label">入住天数</span>
              <span class="value">{{ nights }} 晚</span>
            </div>

            <div class="summary-item">
              <span class="label">房间数</span>
              <span class="value">{{ rooms.length }} 间</span>
            </div>

            <template v-for="(room, index) in rooms" :key="index">
              <div class="summary-item" v-if="room.roomTypeId">
                <span class="label">房间 {{ index + 1 }}</span>
                <span class="value">{{ getRoomTypeInfo(room.roomTypeId)?.name }} × {{ nights }}</span>
              </div>
            </template>

            <div class="summary-total">
              <span class="label">总价</span>
              <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <button
              class="submit-btn"
              @click="handleSubmit"
              :disabled="submitting || !selectedHotelId || nights <= 0 || rooms.some(r => !r.roomTypeId)"
            >
              {{ submitting ? '提交中...' : '确认预订' }}
            </button>

            <p class="tip">* 预订成功后，您可以在"我的预订"中查看订单详情</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


