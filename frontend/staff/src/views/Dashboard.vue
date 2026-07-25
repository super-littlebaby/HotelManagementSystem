<template>
  <div class="dashboard">
    <h2>系统概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
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
            <div class="stat-label">今日收入</div>
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
import { ref, reactive, onMounted } from 'vue'
import { OfficeBuilding, Grid, Key, Wallet } from '@element-plus/icons-vue'
import { getReservations } from '../api/reservation'
import { getCheckIns } from '../api/checkin'

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
    const resResponse = await getReservations()
    recentReservations.value = (resResponse.data || []).slice(0, 5).map(r => ({
      id: r.id,
      guestName: `${r.guest?.firstName} ${r.guest?.lastName}`,
      checkInDate: r.checkInDate,
      checkOutDate: r.checkOutDate,
      status: r.status
    }))
    
    const checkinResponse = await getCheckIns()
    const today = new Date().toISOString().split('T')[0]
    todayCheckIns.value = (checkinResponse.data || []).filter(c => 
      c.checkInTime?.startsWith(today)
    ).slice(0, 5).map(c => ({
      id: c.id,
      guestName: `${c.guest?.firstName} ${c.guest?.lastName}`,
      roomNumber: c.room?.roomNumber,
      checkInTime: c.checkInTime,
      expectedCheckOut: c.expectedCheckOutTime
    }))
    
    stats.checkins = (checkinResponse.data || []).filter(c => c.status === 'in_house').length
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.blue { background-color: #409eff; }
.stat-icon.green { background-color: #67c23a; }
.stat-icon.orange { background-color: #e6a23c; }
.stat-icon.purple { background-color: #909399; }

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.section {
  margin-top: 30px;
}

.section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
}
</style>
