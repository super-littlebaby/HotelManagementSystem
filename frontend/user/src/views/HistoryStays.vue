<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCheckIns } from '../api/checkin'
import { state as authState } from '../stores/auth'

const checkIns = ref([])
const loading = ref(false)
const activeTab = ref('list')
const selectedCheckIn = ref(null)

onMounted(() => {
  if (authState.isLoggedIn) {
    loadMyCheckIns()
  }
})

const loadMyCheckIns = async () => {
  const guest = JSON.parse(localStorage.getItem('guest') || 'null')
  if (!guest || !guest.id) {
    return
  }

  loading.value = true
  try {
    const res = await getMyCheckIns(guest.id)
    if (res.code === 200) {
      checkIns.value = res.data.sort((a, b) => {
        const timeA = a.checkInTime ? new Date(a.checkInTime).getTime() : 0
        const timeB = b.checkInTime ? new Date(b.checkInTime).getTime() : 0
        return timeB - timeA
      })
    } else {
      ElMessage.error(res.message || '加载历史住房记录失败')
    }
  } catch (error) {
    ElMessage.error('加载历史住房记录失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (checkIn) => {
  selectedCheckIn.value = checkIn
  activeTab.value = 'detail'
}

const backToList = () => {
  activeTab.value = 'list'
  selectedCheckIn.value = null
}

const getStatusText = (status) => {
  const statusMap = {
    'in_house': '在住中',
    'checked_out': '已退房',
    'no_show': '未入住'
  }
  return statusMap[status] || status
}

const getStatusColor = (status) => {
  const colorMap = {
    'in_house': '#27ae60',
    'checked_out': '#95a5a6',
    'no_show': '#9b59b6'
  }
  return colorMap[status] || '#333'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 10)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.replace('T', ' ').substring(0, 16)
}

const getHotelName = (checkIn) => {
  if (checkIn.hotel) return checkIn.hotel.name || checkIn.hotelName || '酒店'
  return checkIn.hotelName || '酒店'
}

const getRoomNumber = (checkIn) => {
  if (checkIn.room) return checkIn.room.roomNumber || '-'
  return checkIn.roomNumber || '-'
}

const getRoomTypeName = (checkIn) => {
  if (checkIn.room && checkIn.room.roomType) return checkIn.room.roomType.typeName || '-'
  return checkIn.roomTypeName || '-'
}
</script>

<template>
  <div class="history-stays-page">
    <div class="container">
      <h2>历史住房记录</h2>

      <div v-if="activeTab === 'list'">
        <div class="action-bar">
          <span>共 {{ checkIns.length }} 条住房记录</span>
          <button class="refresh-btn" @click="loadMyCheckIns" :disabled="loading">
            刷新
          </button>
        </div>

        <div class="checkin-list" v-loading="loading">
          <div
            class="checkin-card"
            v-for="checkIn in checkIns"
            :key="checkIn.id"
          >
            <div class="checkin-header">
              <div class="hotel-info">
                <h3>{{ getHotelName(checkIn) }}</h3>
                <span class="checkin-id">入住编号: {{ checkIn.id }}</span>
              </div>
              <span
                class="status"
                :style="{ color: getStatusColor(checkIn.status), borderColor: getStatusColor(checkIn.status) }"
              >
                {{ getStatusText(checkIn.status) }}
              </span>
            </div>

            <div class="checkin-info">
              <div class="info-item">
                <span class="label">房间号</span>
                <span class="value">{{ getRoomNumber(checkIn) }}</span>
              </div>
              <div class="info-item">
                <span class="label">房型</span>
                <span class="value">{{ getRoomTypeName(checkIn) }}</span>
              </div>
              <div class="info-item">
                <span class="label">入住时间</span>
                <span class="value">{{ formatDateTime(checkIn.checkInTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">退房时间</span>
                <span class="value">{{ checkIn.actualCheckOutTime ? formatDateTime(checkIn.actualCheckOutTime) : (checkIn.expectedCheckOutTime ? formatDate(checkIn.expectedCheckOutTime) : '-') }}</span>
              </div>
              <div class="info-item">
                <span class="label">人数</span>
                <span class="value">
                  {{ checkIn.adults || 0 }}位成人
                  <span v-if="checkIn.children && checkIn.children > 0">, {{ checkIn.children }}位儿童</span>
                </span>
              </div>
              <div class="info-item">
                <span class="label">房费</span>
                <span class="value price" v-if="checkIn.ratePerNight">¥{{ Number(checkIn.ratePerNight).toFixed(2) }}/晚</span>
                <span class="value" v-else>-</span>
              </div>
            </div>

            <div class="checkin-footer">
              <div class="total-charge" v-if="checkIn.totalCharge">
                总费用: <span class="price">¥{{ Number(checkIn.totalCharge).toFixed(2) }}</span>
              </div>
              <div class="actions">
                <button class="btn-detail" @click="viewDetail(checkIn)">
                  查看详情
                </button>
              </div>
            </div>
          </div>

          <div v-if="checkIns.length === 0 && !loading" class="empty-state">
            <div class="empty-icon">🏨</div>
            <p>暂无历史住房记录</p>
            <p class="empty-tip">去预订一间心仪的房间，开启您的旅程吧</p>
          </div>
        </div>
      </div>

      <!-- 详情视图 -->
      <div v-if="activeTab === 'detail' && selectedCheckIn" class="detail-view">
        <button class="back-btn" @click="backToList">
          ← 返回列表
        </button>

        <div class="detail-card">
          <div class="detail-header">
            <div>
              <h3>{{ getHotelName(selectedCheckIn) }}</h3>
              <span class="checkin-id">入住编号: {{ selectedCheckIn.id }}</span>
            </div>
            <span
              class="status"
              :style="{ color: getStatusColor(selectedCheckIn.status), borderColor: getStatusColor(selectedCheckIn.status) }"
            >
              {{ getStatusText(selectedCheckIn.status) }}
            </span>
          </div>

          <div class="detail-section">
            <h4>入住信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">客人姓名</span>
                <span class="value">{{ selectedCheckIn.guestName || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">联系电话</span>
                <span class="value">{{ selectedCheckIn.phone || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房间号</span>
                <span class="value">{{ getRoomNumber(selectedCheckIn) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">房型</span>
                <span class="value">{{ getRoomTypeName(selectedCheckIn) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">入住时间</span>
                <span class="value">{{ formatDateTime(selectedCheckIn.checkInTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">预计退房</span>
                <span class="value">{{ selectedCheckIn.expectedCheckOutTime ? formatDate(selectedCheckIn.expectedCheckOutTime) : '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">实际退房</span>
                <span class="value">{{ selectedCheckIn.actualCheckOutTime ? formatDateTime(selectedCheckIn.actualCheckOutTime) : '在住中' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">入住人数</span>
                <span class="value">
                  {{ selectedCheckIn.adults || 0 }}位成人
                  <span v-if="selectedCheckIn.children && selectedCheckIn.children > 0">, {{ selectedCheckIn.children }}位儿童</span>
                </span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>费用信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">房间单价</span>
                <span class="value" v-if="selectedCheckIn.ratePerNight">¥{{ Number(selectedCheckIn.ratePerNight).toFixed(2) }}/晚</span>
                <span class="value" v-else>-</span>
              </div>
              <div class="detail-item">
                <span class="label">总费用</span>
                <span class="value price" v-if="selectedCheckIn.totalCharge">¥{{ Number(selectedCheckIn.totalCharge).toFixed(2) }}</span>
                <span class="value" v-else>-</span>
              </div>
            </div>
          </div>

          <div v-if="selectedCheckIn.notes" class="detail-section">
            <h4>备注信息</h4>
            <p class="notes">{{ selectedCheckIn.notes }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
