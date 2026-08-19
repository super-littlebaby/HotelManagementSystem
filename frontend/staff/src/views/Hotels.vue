<template>
  <div class="page">
    <div class="page-header">
      <h2>酒店管理</h2>
      <el-button type="primary" @click="showAddDialog = true" :disabled="!canAddHotel" title="无权添加酒店">添加酒店</el-button>
    </div>
    
    <el-table :data="hotels" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="酒店名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="description" label="简介" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editHotel(row)" :disabled="!canEditHotel(row)" title="无权编辑">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteHotel(row)" :disabled="!canDeleteHotel" title="无权删除">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="添加酒店" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="酒店名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveHotel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHotels, createHotel, updateHotel, deleteHotel as deleteHotelApi } from '../api/hotel'
import { state as authState } from '../stores/auth'

const hotels = ref([])
const showAddDialog = ref(false)

const currentRole = computed(() => authState.staff?.role || '')
const currentHotelId = computed(() => authState.staff?.hotelId)

const isGroupAdmin = computed(() => currentRole.value === 'admin')

const canAddHotel = computed(() => {
  return isGroupAdmin.value
})

const canEditHotel = (row) => {
  if (isGroupAdmin.value) return true
  if (currentRole.value !== 'manager') return false
  return row.id === currentHotelId.value
}

const canDeleteHotel = computed(() => {
  return isGroupAdmin.value
})

const form = reactive({
  id: null,
  name: '',
  address: '',
  phone: '',
  email: '',
  description: ''
})

const loadHotels = async () => {
  try {
    const response = await getHotels()
    hotels.value = response.data || []
  } catch (error) {
    ElMessage.error('加载酒店列表失败')
  }
}

const editHotel = (row) => {
  Object.assign(form, row)
  showAddDialog.value = true
}

const saveHotel = async () => {
  if (!form.name) {
    ElMessage.warning('请填写酒店名称')
    return
  }
  
  try {
    let res
    if (form.id) {
      res = await updateHotel(form.id, form)
    } else {
      res = await createHotel(form)
    }
    
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      showAddDialog.value = false
      loadHotels()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const deleteHotel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该酒店吗？', '提示', {
      type: 'warning'
    })
    const res = await deleteHotelApi(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadHotels()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
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
