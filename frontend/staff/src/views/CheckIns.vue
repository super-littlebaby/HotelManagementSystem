<template>
  <div class="page">
    <div class="page-header">
      <h2>入住管理</h2>
      <el-button type="primary" @click="showAddDialog = true">办理入住</el-button>
    </div>
    
    <el-table :data="checkIns" border>
      <el-table-column prop="id" label="登记ID" width="80" />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column prop="roomNumber" label="房间号" />
      <el-table-column prop="adults" label="成人" width="60" />
      <el-table-column prop="children" label="儿童" width="60" />
      <el-table-column prop="checkInTime" label="入住时间" />
      <el-table-column prop="expectedCheckOutTime" label="预计退房" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalCharge" label="总费用">
        <template #default="{ row }">¥{{ row.totalCharge || 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editCheckIn(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="checkOut(row)" v-if="row.status === 'in_house'">退房</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="办理入住" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="预订ID">
          <el-input v-model.number="form.reservationId" type="number" />
        </el-form-item>
        <el-form-item label="客人ID" required>
          <el-input v-model.number="form.guestId" type="number" />
        </el-form-item>
        <el-form-item label="房间ID" required>
          <el-select v-model="form.roomId">
            <el-option v-for="room in availableRooms" :key="room.id" :label="room.roomNumber" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成人">
          <el-input v-model.number="form.adults" type="number" :min="1" />
        </el-form-item>
        <el-form-item label="儿童">
          <el-input v-model.number="form.children" type="number" :min="0" />
        </el-form-item>
        <el-form-item label="预计退房时间">
          <el-date-picker v-model="form.expectedCheckOutTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCheckIn">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckIns, createCheckIn, updateCheckIn } from '../api/checkin'
import { getRooms } from '../api/room'

const checkIns = ref([])
const availableRooms = ref([])
const showAddDialog = ref(false)

const form = reactive({
  id: null,
  reservationId: null,
  guestId: null,
  roomId: null,
  adults: 1,
  children: 0,
  checkInTime: new Date().toISOString().slice(0, 16),
  expectedCheckOutTime: '',
  status: 'in_house',
  notes: ''
})

const getStatusType = (status) => {
  const types = {
    in_house: 'danger',
    checked_out: 'success',
    early_check_out: 'warning'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    in_house: '在住',
    checked_out: '已退房',
    early_check_out: '提前退房'
  }
  return labels[status] || status
}

const loadCheckIns = async () => {
  try {
    const response = await getCheckIns()
    checkIns.value = (response.data || []).map(c => ({
      ...c,
      guestName: `${c.guest?.firstName} ${c.guest?.lastName}`,
      roomNumber: c.room?.roomNumber
    }))
  } catch (error) {
    ElMessage.error('加载入住列表失败')
  }
}

const loadAvailableRooms = async () => {
  try {
    const response = await getRooms()
    availableRooms.value = (response.data || []).filter(r => r.status === 'vacant')
  } catch (error) {
    console.error('加载房间失败', error)
  }
}

const editCheckIn = (row) => {
  Object.assign(form, row)
  showAddDialog.value = true
}

const saveCheckIn = async () => {
  if (!form.guestId || !form.roomId) {
    ElMessage.warning('请填写客人ID和房间号')
    return
  }
  
  try {
    if (form.id) {
      await updateCheckIn(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createCheckIn(form)
      ElMessage.success('入住成功')
    }
    showAddDialog.value = false
    loadCheckIns()
    loadAvailableRooms()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const checkOut = async (row) => {
  try {
    await ElMessageBox.confirm('确定要办理退房吗？', '提示', {
      type: 'warning'
    })
    await updateCheckIn(row.id, { ...row, status: 'checked_out', actualCheckOutTime: new Date().toISOString() })
    ElMessage.success('退房成功')
    loadCheckIns()
    loadAvailableRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadCheckIns()
  loadAvailableRooms()
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
