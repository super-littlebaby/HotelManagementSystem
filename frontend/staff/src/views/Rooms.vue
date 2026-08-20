<template>
  <div class="page">
    <div class="page-header">
      <h2>房间管理</h2>
      <el-button type="primary" @click="showAddDialog = true" :disabled="!canModifyRoom" title="无权添加房间">添加房间</el-button>
    </div>
    
    <el-table :data="rooms" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="hotelName" label="所属酒店" />
      <el-table-column prop="roomNumber" label="房间号" />
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column prop="roomTypeName" label="房型" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="editRoom(row)" :disabled="!canModifyRoom" title="无权编辑">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRoom(row)" :disabled="!canModifyRoom" title="无权删除">删除</el-button>
          <el-button size="small" type="info" @click="openLogDrawer(row)">状态日志</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="showLogDrawer" :title="`房间 ${currentLogRoom?.roomNumber || ''} 的状态变更日志`" size="60%">
      <el-table :data="roomLogs" border v-loading="loadingLogs">
        <el-table-column prop="changedAt" label="变更时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.changedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态变更" width="220">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.oldStatus)">{{ getStatusLabel(row.oldStatus) }}</el-tag>
            <el-icon style="margin: 0 4px"><ArrowRight /></el-icon>
            <el-tag :type="getStatusType(row.newStatus)">{{ getStatusLabel(row.newStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changedByName" label="操作人" width="120">
          <template #default="{ row }">
            {{ row.changedByName || row.changedByUsername || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!loadingLogs && roomLogs.length === 0" description="暂无状态变更记录" />
    </el-drawer>

    <el-dialog v-model="showAddDialog" title="添加房间" width="500px">
      <el-form :model="form" label-width="80px">
        <HotelSelect v-model="form.hotelId" label="所属酒店" :required="true" @change="onHotelChange" />
        <el-form-item label="房间号" required>
          <el-input v-model="form.roomNumber" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="form.floor" :min="1" :max="99" controls-position="right" />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="form.roomTypeId">
            <el-option v-for="rt in filteredRoomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="空闲" value="vacant" />
            <el-option label="入住中" value="occupied" />
            <el-option label="待打扫" value="dirty" />
            <el-option label="维修中" value="out_of_order" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isStatusChanged" label="变更说明" :required="isOutOfOrder">
          <el-input
            v-model="form.statusChangeNote"
            type="textarea"
            :rows="2"
            :placeholder="isOutOfOrder ? '设置为维修中必须填写损坏/维修原因' : '请填写状态变更原因（可选）'"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoom">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { getRooms, createRoom, updateRoom, updateRoomStatus, deleteRoom as deleteRoomApi } from '../api/room'
import { getRoomTypes } from '../api/roomType'
import { getHotels } from '../api/hotel'
import { getRoomStatusLogsByRoomId } from '../api/roomStatusLog'
import { state as authState } from '../stores/auth'
import HotelSelect from '../components/HotelSelect.vue'

const rooms = ref([])
const roomTypes = ref([])
const hotels = ref([])
const showAddDialog = ref(false)
const showLogDrawer = ref(false)
const currentLogRoom = ref(null)
const roomLogs = ref([])
const loadingLogs = ref(false)

const currentRole = computed(() => authState.staff?.role || '')

const canModifyRoom = computed(() => {
  return currentRole.value === 'admin' || currentRole.value === 'manager'
})

const form = reactive({
  id: null,
  hotelId: null,
  roomNumber: '',
  floor: null,
  roomTypeId: null,
  status: 'vacant',
  notes: '',
  originalStatus: null,
  statusChangeNote: ''
})

const filteredRoomTypes = computed(() => {
  if (!form.hotelId) return roomTypes.value
  return roomTypes.value.filter(rt => rt.hotelId === form.hotelId)
})

const isStatusChanged = computed(() => {
  return form.id && form.originalStatus && form.status !== form.originalStatus
})

const isOutOfOrder = computed(() => form.status === 'out_of_order')

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
  return labels[status] || status
}

const loadRooms = async () => {
  try {
    const response = await getRooms()
    rooms.value = response.data || []
  } catch (error) {
    ElMessage.error('加载房间列表失败')
  }
}

const loadRoomTypes = async () => {
  try {
    const response = await getRoomTypes()
    roomTypes.value = response.data || []
  } catch (error) {
    console.error('加载房型失败', error)
  }
}

const loadHotels = async () => {
  try {
    const response = await getHotels()
    hotels.value = response.data || []
  } catch (error) {
    console.error('加载酒店列表失败', error)
  }
}

const onHotelChange = () => {
  form.roomTypeId = null
}

const editRoom = (row) => {
  Object.assign(form, {
    id: row.id,
    hotelId: row.hotelId,
    roomNumber: row.roomNumber,
    floor: row.floor,
    roomTypeId: row.roomTypeId,
    status: row.status,
    notes: row.notes,
    originalStatus: row.status,
    statusChangeNote: ''
  })
  showAddDialog.value = true
}

const saveRoom = async () => {
  if (!form.hotelId || !form.roomNumber || !form.roomTypeId) {
    ElMessage.warning('请填写所属酒店、房间号和房型')
    return
  }

  // 状态变更为维修中时，必须填写变更说明
  if (isStatusChanged.value && isOutOfOrder.value && !form.statusChangeNote.trim()) {
    ElMessage.warning('房间设置为维修中时必须填写损坏/维修原因')
    return
  }

  try {
    if (form.id) {
      // 编辑场景：如果状态发生变化，先调状态变更接口（写入日志），再保存其他字段
      if (isStatusChanged.value) {
        await updateRoomStatus(form.id, {
          status: form.status,
          notes: form.statusChangeNote.trim()
        })
      }
      await updateRoom(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRoom(form)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    loadRooms()
  } catch (error) {
    const message = error.response?.data?.message || '操作失败'
    ElMessage.error(message)
  }
}

const deleteRoom = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该房间吗？', '提示', {
      type: 'warning'
    })
    await deleteRoomApi(row.id)
    ElMessage.success('删除成功')
    loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDateTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const openLogDrawer = async (row) => {
  currentLogRoom.value = row
  showLogDrawer.value = true
  roomLogs.value = []
  loadingLogs.value = true
  try {
    const response = await getRoomStatusLogsByRoomId(row.id)
    roomLogs.value = response.data || []
  } catch (error) {
    ElMessage.error('加载状态日志失败')
  } finally {
    loadingLogs.value = false
  }
}

onMounted(() => {
  loadRooms()
  loadRoomTypes()
  loadHotels()
})
</script>

