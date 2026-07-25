<template>
  <div class="page">
    <div class="page-header">
      <h2>预订管理</h2>
      <el-button type="primary" @click="showAddDialog = true">添加预订</el-button>
    </div>
    
    <el-table :data="reservations" border>
      <el-table-column prop="id" label="预订ID" width="80" />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column prop="checkInDate" label="入住日期" />
      <el-table-column prop="checkOutDate" label="退房日期" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="channel" label="渠道">
        <template #default="{ row }">{{ getChannelLabel(row.channel) }}</template>
      </el-table-column>
      <el-table-column prop="specialRequests" label="特殊要求" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editReservation(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="confirmReservation(row)" v-if="row.status === 'pending'">确认</el-button>
          <el-button size="small" type="danger" @click="cancelReservation(row)" v-if="row.status === 'confirmed'">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="添加预订" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客人ID" required>
          <el-input v-model.number="form.guestId" type="number" />
        </el-form-item>
        <el-form-item label="入住日期" required>
          <el-date-picker v-model="form.checkInDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退房日期" required>
          <el-date-picker v-model="form.checkOutDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总金额">
          <el-input v-model.number="form.totalAmount" />
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input v-model="form.specialRequests" type="textarea" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="form.channel">
            <el-option label="在线" value="online" />
            <el-option label="电话" value="phone" />
            <el-option label="到店" value="walk_in" />
            <el-option label="OTA" value="ota" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveReservation">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservations, createReservation, updateReservation } from '../api/reservation'

const reservations = ref([])
const showAddDialog = ref(false)

const form = reactive({
  id: null,
  guestId: null,
  checkInDate: '',
  checkOutDate: '',
  status: 'pending',
  totalAmount: null,
  specialRequests: '',
  channel: 'walk_in'
})

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

const getChannelLabel = (channel) => {
  const labels = {
    online: '在线',
    phone: '电话',
    walk_in: '到店',
    ota: 'OTA'
  }
  return labels[channel] || channel
}

const loadReservations = async () => {
  try {
    const response = await getReservations()
    reservations.value = (response.data || []).map(r => ({
      ...r,
      guestName: `${r.guest?.firstName} ${r.guest?.lastName}`
    }))
  } catch (error) {
    ElMessage.error('加载预订列表失败')
  }
}

const editReservation = (row) => {
  Object.assign(form, row)
  showAddDialog.value = true
}

const saveReservation = async () => {
  if (!form.guestId || !form.checkInDate || !form.checkOutDate) {
    ElMessage.warning('请填写必填字段')
    return
  }
  
  try {
    if (form.id) {
      await updateReservation(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createReservation(form)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    loadReservations()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const confirmReservation = async (row) => {
  try {
    await updateReservation(row.id, { ...row, status: 'confirmed' })
    ElMessage.success('预订已确认')
    loadReservations()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const cancelReservation = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗？', '提示', {
      type: 'warning'
    })
    await updateReservation(row.id, { ...row, status: 'cancelled' })
    ElMessage.success('预订已取消')
    loadReservations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadReservations()
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
