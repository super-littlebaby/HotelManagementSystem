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
            <div class="stat-value">{{ stats.checkins }}</div>
            <div class="stat-label">在住人数</div>
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
        <el-table-column prop="checkInDate" label="入住日期" />
        <el-table-column prop="checkOutDate" label="退房日期" />
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
        <el-table-column prop="checkInTime" label="入住时间" />
        <el-table-column prop="expectedCheckOut" label="预计退房" />
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
import { state as authState } from '../stores/auth'

const currentHotelId = computed(() => authState.staff?.hotelId || null)
const currentHotelName = ref(null)

const stats = reactive({
  hotels: 0,
  rooms: 0,
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

    if (hotelId) {
      const roomsRes = await getRoomsByHotel(hotelId)
      if (roomsRes.code === 200) {
        stats.rooms = (roomsRes.data || []).length
      }
    } else {
      const roomsRes = await getRooms()
      if (roomsRes.code === 200) {
        stats.rooms = (roomsRes.data || []).length
      }
    }

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

      stats.checkins = checkins.filter(c => c.status === 'in_house').length

      const totalRevenue = checkins
        .filter(c => c.status === 'checked_out' && c.totalCharge != null)
        .reduce((sum, c) => sum + Number(c.totalCharge), 0)
      stats.revenue = '¥' + totalRevenue.toFixed(2)
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

