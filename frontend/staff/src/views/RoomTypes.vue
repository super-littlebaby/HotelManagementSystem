<template>
  <div class="page">
    <div class="page-header">
      <h2>房型管理</h2>
      <el-button type="primary" @click="showAddDialog = true" :disabled="!canModifyRoomType" title="无权添加房型">添加房型</el-button>
    </div>
    
    <el-table :data="roomTypes" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="hotelName" label="所属酒店" />
      <el-table-column prop="name" label="房型名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="maxAdults" label="最大成人" width="100" />
      <el-table-column prop="maxChildren" label="最大儿童" width="100" />
      <el-table-column prop="basePrice" label="基础价格" width="120">
        <template #default="{ row }">
          ¥{{ row.basePrice }}
        </template>
      </el-table-column>
      <el-table-column prop="area" label="面积(㎡)" width="110" />
      <el-table-column prop="bedType" label="床型">
        <template #default="{ row }">
          <span>{{ getBedTypeLabel(row.bedType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editRoomType(row)" :disabled="!canModifyRoomType" title="无权编辑">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRoomType(row)" :disabled="!canModifyRoomType" title="无权删除">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="添加房型" width="500px">
      <el-form :model="form" label-width="80px">
        <HotelSelect v-model="form.hotelId" label="所属酒店" :required="true" />
        <el-form-item label="房型名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="最大成人">
          <el-input v-model="form.maxAdults" />
        </el-form-item>
        <el-form-item label="最大儿童">
          <el-input v-model="form.maxChildren" />
        </el-form-item>
        <el-form-item label="基础价格" required>
          <el-input v-model="form.basePrice" />
        </el-form-item>
        <el-form-item label="面积(㎡)">
          <el-input v-model="form.area" />
        </el-form-item>
        <el-form-item label="床型">
          <el-select v-model="form.bedType">
            <el-option label="单人床" value="single" />
            <el-option label="双人床" value="double" />
            <el-option label="大床" value="king" />
            <el-option label="特大床" value="queen" />
            <el-option label="榻榻米" value="tatami" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoomType">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomTypes, createRoomType, updateRoomType, deleteRoomType as deleteRoomTypeApi } from '../api/roomType'
import { getHotels } from '../api/hotel'
import { state as authState } from '../stores/auth'
import HotelSelect from '../components/HotelSelect.vue'

const roomTypes = ref([])
const hotels = ref([])
const showAddDialog = ref(false)

const currentRole = computed(() => authState.staff?.role || '')

const canModifyRoomType = computed(() => {
  return currentRole.value === 'admin' || currentRole.value === 'manager'
})

const form = reactive({
  id: null,
  hotelId: null,
  name: '',
  description: '',
  maxAdults: '',
  maxChildren: '',
  basePrice: '',
  area: '',
  bedType: ''
})

const getBedTypeLabel = (bedType) => {
  const labels = {
    single: '单人床',
    double: '双人床',
    king: '大床',
    queen: '特大床',
    tatami: '榻榻米',
    other: '其他'
  }
  return labels[bedType] || bedType
}

const loadRoomTypes = async () => {
  try {
    const response = await getRoomTypes()
    roomTypes.value = response.data || []
  } catch (error) {
    ElMessage.error('加载房型列表失败')
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

const editRoomType = (row) => {
  Object.assign(form, {
    id: row.id,
    hotelId: row.hotelId,
    name: row.name,
    description: row.description,
    maxAdults: row.maxAdults,
    maxChildren: row.maxChildren,
    basePrice: row.basePrice,
    area: row.area,
    bedType: row.bedType
  })
  showAddDialog.value = true
}

const saveRoomType = async () => {
  if (!form.hotelId || !form.name || !form.basePrice) {
    ElMessage.warning('请填写所属酒店、房型名称和基础价格')
    return
  }
  
  const data = {
    hotelId: form.hotelId,
    name: form.name,
    description: form.description,
    maxAdults: form.maxAdults ? parseInt(form.maxAdults) : 0,
    maxChildren: form.maxChildren ? parseInt(form.maxChildren) : 0,
    basePrice: form.basePrice ? parseFloat(form.basePrice) : 0,
    area: form.area ? parseFloat(form.area) : null,
    bedType: form.bedType
  }
  
  try {
    if (form.id) {
      await updateRoomType(form.id, { ...data, id: form.id })
      ElMessage.success('更新成功')
    } else {
      await createRoomType(data)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    Object.assign(form, {
      id: null,
      hotelId: null,
      name: '',
      description: '',
      maxAdults: '',
      maxChildren: '',
      basePrice: '',
      area: '',
      bedType: ''
    })
    loadRoomTypes()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteRoomType = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该房型吗？', '提示', {
      type: 'warning'
    })
    await deleteRoomTypeApi(row.id)
    ElMessage.success('删除成功')
    loadRoomTypes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadRoomTypes()
  loadHotels()
})
</script>

