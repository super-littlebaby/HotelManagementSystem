<template>
  <div class="page">
    <div class="page-header">
      <h2>房间状态变更日志</h2>
    </div>

    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="酒店">
        <el-select
          v-if="isGroupAdmin"
          v-model="query.hotelId"
          placeholder="全部酒店"
          clearable
          style="width: 160px"
        >
          <el-option v-for="h in hotels" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
        <el-tag v-else type="info" effect="plain" class="hotel-tag">
          {{ currentHotelName }}
        </el-tag>
      </el-form-item>
      <el-form-item label="房间号">
        <el-input v-model="query.roomNumber" placeholder="按房间号过滤" clearable style="width: 140px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="新状态">
        <el-select v-model="query.newStatus" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="空闲" value="vacant" />
          <el-option label="入住中" value="occupied" />
          <el-option label="待打扫" value="dirty" />
          <el-option label="维修中" value="out_of_order" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人ID">
        <el-input v-model.number="query.changedBy" placeholder="员工ID" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="query.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="logs" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="roomNumber" label="房间号" width="100" />
      <el-table-column prop="hotelName" label="所属酒店" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态变更" width="240">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.oldStatus)">{{ getStatusLabel(row.oldStatus) }}</el-tag>
          <el-icon style="margin: 0 4px"><ArrowRight /></el-icon>
          <el-tag :type="getStatusType(row.newStatus)">{{ getStatusLabel(row.newStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人" width="160">
        <template #default="{ row }">
          <span>{{ row.changedByName || row.changedByUsername || '-' }}</span>
          <span v-if="row.changedBy" style="color: #999; margin-left: 4px">#{{ row.changedBy }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="changedAt" label="变更时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.changedAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" show-overflow-tooltip min-width="160" />
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { searchRoomStatusLogs } from '../api/roomStatusLog'
import { getHotels } from '../api/hotel'
import { getRooms } from '../api/room'
import { state as authState } from '../stores/auth'

const logs = ref([])
const hotels = ref([])
const rooms = ref([])
const loading = ref(false)
const total = ref(0)

const query = reactive({
  hotelId: null,
  roomId: null,
  roomNumber: '',
  newStatus: '',
  changedBy: null,
  timeRange: null,
  page: 1,
  size: 20
})

const currentRole = computed(() => authState.staff?.role || '')
const isGroupAdmin = computed(() => currentRole.value === 'admin')
const currentUserHotelId = computed(() => authState.staff?.hotelId)
const currentHotelName = computed(() => {
  const hid = currentUserHotelId.value
  if (!hid) return '全部酒店'
  const hotel = hotels.value.find(h => h.id === hid)
  return hotel ? hotel.name : `酒店 #${hid}`
})

const getStatusType = (status) => {
  const types = {
    vacant: 'success',
    occupied: 'danger',
    dirty: 'warning',
    out_of_order: 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    vacant: '空闲',
    occupied: '入住中',
    dirty: '待打扫',
    out_of_order: '维修中'
  }
  return labels[status] || (status || '-')
}

const formatDateTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const buildParams = () => {
  const params = {
    page: query.page - 1,
    size: query.size
  }
  if (query.hotelId) params.hotelId = query.hotelId
  if (query.roomId) params.roomId = query.roomId
  if (query.newStatus) params.newStatus = query.newStatus
  if (query.changedBy) params.changedBy = query.changedBy
  if (Array.isArray(query.timeRange) && query.timeRange.length === 2) {
    params.startTime = query.timeRange[0]
    params.endTime = query.timeRange[1]
  }
  return params
}

const resolveRoomId = () => {
  if (!query.roomNumber) {
    query.roomId = null
    return Promise.resolve()
  }
  const matched = rooms.value.find(r => r.roomNumber === query.roomNumber.trim())
  if (matched) {
    query.roomId = matched.id
    return Promise.resolve()
  }
  // 房间号未在已加载列表中找到，提示但不阻断查询
  query.roomId = null
  return Promise.resolve()
}

const loadLogs = async () => {
  loading.value = true
  try {
    await resolveRoomId()
    const params = buildParams()
    if (query.roomNumber && !query.roomId) {
      // 房间号无法匹配到ID时，按当前条件查询，结果可能为空
      params.roomId = -1
    }
    const response = await searchRoomStatusLogs(params)
    const data = response.data || {}
    logs.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载状态变更日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadLogs()
}

const handleReset = () => {
  query.hotelId = isGroupAdmin.value ? null : (currentUserHotelId.value || null)
  query.roomId = null
  query.roomNumber = ''
  query.newStatus = ''
  query.changedBy = null
  query.timeRange = null
  query.page = 1
  loadLogs()
}

const handlePageChange = (p) => {
  query.page = p
  loadLogs()
}

const handleSizeChange = (s) => {
  query.size = s
  query.page = 1
  loadLogs()
}

const loadHotels = async () => {
  try {
    const response = await getHotels()
    hotels.value = response.data || []
  } catch (error) {
    console.error('加载酒店失败', error)
  }
}

const loadRooms = async () => {
  try {
    const response = await getRooms()
    rooms.value = response.data || []
  } catch (error) {
    console.error('加载房间失败', error)
  }
}

onMounted(async () => {
  await loadHotels()
  loadRooms()
  // 非集团管理员自动锁定当前酒店
  if (!isGroupAdmin.value && currentUserHotelId.value) {
    query.hotelId = currentUserHotelId.value
  }
  loadLogs()
})
</script>
