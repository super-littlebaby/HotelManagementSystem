<template>
  <div class="dashboard">
    <h2>{{ currentHotelName ? currentHotelName + ' - 系统概览' : '系统概览' }}</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-if="!currentHotelId">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.hotels }}</div>
            <div class="stat-label">酒店数量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.rooms }}</div>
            <div class="stat-label">房间总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Key /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.occupiedRooms }}</div>
            <div class="stat-label">在住房间</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.revenue }}</div>
            <div class="stat-label">累计收入</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <div class="section">
      <h3>最近预订</h3>
      <el-table :data="recentReservations" border>
        <el-table-column prop="id" label="预订ID" />
        <el-table-column prop="guestName" label="客人" />
        <el-table-column label="入住日期">
          <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
        </el-table-column>
        <el-table-column label="退房日期">
          <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="section">
      <h3>今日入住</h3>
      <el-table :data="todayCheckIns" border>
        <el-table-column prop="id" label="登记ID" />
        <el-table-column prop="guestName" label="客人" />
        <el-table-column prop="roomNumber" label="房间号" />
        <el-table-column label="入住时间">
          <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column label="预计退房">
          <template #default="{ row }">{{ formatDateTime(row.expectedCheckOut) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { OfficeBuilding, Grid, Key, Wallet } from '@element-plus/icons-vue'
import { getReservations } from '../api/reservation'
import { getCheckIns } from '../api/checkin'
import { getHotels, getHotelById } from '../api/hotel'
import { getRooms, getRoomsByHotel } from '../api/room'
import { getMonthlyClosedRevenue } from '../api/bill'
import { state as authState } from '../stores/auth'

const currentHotelId = computed(() => authState.staff?.hotelId || null)
const currentHotelName = ref(null)

const stats = reactive({
  hotels: 0,
  rooms: 0,
  occupiedRooms: 0,
  checkins: 0,
  revenue: '¥0.00'
})

const recentReservations = ref([])
const todayCheckIns = ref([])

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'success',
    checked_in: 'primary',
    cancelled: 'danger',
    no_show: 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    pending: '待确认',
    confirmed: '已确认',
    checked_in: '已入住',
    cancelled: '已取消',
    no_show: '未入住'
  }
  return labels[status] || status
}

const pad = (n) => String(n).padStart(2, '0')

const formatDate = (val) => {
  if (!val) return '-'
  if (typeof val === 'string') {
    return val.replace('T', ' ').substring(0, 10)
  }
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const formatDateTime = (val) => {
  if (!val) return '-'
  if (typeof val === 'string') {
    return val.replace('T', ' ').replace(/\.\d+$/, '')
  }
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const loadData = async () => {
  try {
    const hotelId = currentHotelId.value

    if (hotelId) {
      const hotelRes = await getHotelById(hotelId)
      if (hotelRes.code === 200 && hotelRes.data) {
        currentHotelName.value = hotelRes.data.name
      }
    }

    if (!hotelId) {
      const hotelsRes = await getHotels()
      if (hotelsRes.code === 200) {
        stats.hotels = (hotelsRes.data || []).length
      }
    }

    let roomsData = []
    try {
      if (hotelId) {
        const roomsRes = await getRoomsByHotel(hotelId)
        if (roomsRes.code === 200) {
          roomsData = roomsRes.data || []
        }
      } else {
        const roomsRes = await getRooms()
        if (roomsRes.code === 200) {
          roomsData = roomsRes.data || []
        }
      }
    } catch (e) {
      console.error('加载房间数据失败', e)
    }
    stats.rooms = roomsData.length
    stats.occupiedRooms = roomsData.filter(r => r.status === 'occupied').length

    // 累计收入：独立并行请求，不依赖其他接口
    try {
      const revRes = await getMonthlyClosedRevenue()
      if (revRes.code === 200 && revRes.data != null) {
        stats.revenue = '¥' + Number(revRes.data).toFixed(2)
      } else {
        stats.revenue = '¥0.00'
      }
    } catch (e) {
      console.error('加载累计收入失败', e)
      stats.revenue = '¥0.00'
    }

    try {
      const resResponse = await getReservations(hotelId)
      if (resResponse.code === 200) {
        let reservations = resResponse.data || []
        recentReservations.value = reservations.slice(0, 5).map(r => ({
          id: r.id,
          guestName: r.guestName || '线下客户',
          checkInDate: r.checkInDate,
          checkOutDate: r.checkOutDate,
          status: r.status
        }))
      }
    } catch (e) {
      console.error('加载最近预订失败', e)
    }

    try {
      const checkinResponse = await getCheckIns()
      if (checkinResponse.code === 200) {
        const checkins = checkinResponse.data || []
        const today = new Date().toISOString().split('T')[0]

        todayCheckIns.value = checkins
          .filter(c => c.checkInTime?.startsWith(today))
          .slice(0, 5)
          .map(c => ({
            id: c.id,
            guestName: c.guestName || '线下客户',
            roomNumber: c.roomId ? `房间${c.roomId}` : '-',
            checkInTime: c.checkInTime,
            expectedCheckOut: c.expectedCheckOutTime
          }))
      }
    } catch (e) {
      console.error('加载今日入住失败', e)
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

