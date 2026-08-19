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
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editRoom(row)" :disabled="!canModifyRoom" title="无权编辑">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRoom(row)" :disabled="!canModifyRoom" title="无权删除">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
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
import { getRooms, createRoom, updateRoom, deleteRoom as deleteRoomApi } from '../api/room'
import { getRoomTypes } from '../api/roomType'
import { getHotels } from '../api/hotel'
import { state as authState } from '../stores/auth'
import HotelSelect from '../components/HotelSelect.vue'

const rooms = ref([])
const roomTypes = ref([])
const hotels = ref([])
const showAddDialog = ref(false)

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
  notes: ''
})

const filteredRoomTypes = computed(() => {
  if (!form.hotelId) return roomTypes.value
  return roomTypes.value.filter(rt => rt.hotelId === form.hotelId)
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
    notes: row.notes
  })
  showAddDialog.value = true
}

const saveRoom = async () => {
  if (!form.hotelId || !form.roomNumber || !form.roomTypeId) {
    ElMessage.warning('请填写所属酒店、房间号和房型')
    return
  }
  
  try {
    if (form.id) {
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

onMounted(() => {
  loadRooms()
  loadRoomTypes()
  loadHotels()
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}
</style>