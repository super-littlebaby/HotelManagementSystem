<template>
  <div class="page">
    <div class="page-header">
      <h2>员工管理</h2>
      <el-button type="primary" @click="showAddDialog = true" :disabled="!canAddEmployee">添加员工</el-button>
    </div>
    
    <el-table :data="employees" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="firstName" label="名" />
      <el-table-column prop="lastName" label="姓" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag>{{ getRoleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hotelId" label="所属酒店">
        <template #default="{ row }">
          <span>{{ getHotelName(row.hotelId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="isActive" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'">{{ row.isActive ? '在职' : '离职' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editEmployee(row)" :disabled="!canEditEmployee(row)" title="无权编辑">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteEmployee(row)" :disabled="!canDeleteEmployee(row)" title="无权删除">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="添加员工" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="名" required>
          <el-input v-model="form.firstName" />
        </el-form-item>
        <el-form-item label="姓" required>
          <el-input v-model="form.lastName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="admin" />
            <el-option label="经理" value="manager" />
            <el-option label="前台" value="front_desk" />
            <el-option label="客房" value="housekeeping" />
            <el-option label="财务" value="finance" />
          </el-select>
        </el-form-item>
        <HotelSelect v-model="form.hotelId" label="所属酒店" :required="false" />
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEmployee">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployees, createEmployee, updateEmployee, deleteEmployee as deleteEmployeeApi } from '../api/employee'
import { getHotels } from '../api/hotel'
import { state as authState } from '../stores/auth'
import HotelSelect from '../components/HotelSelect.vue'

const employees = ref([])
const hotels = ref([])
const showAddDialog = ref(false)

const roleLevels = {
  admin: 0,
  manager: 1,
  front_desk: 2,
  housekeeping: 3,
  finance: 3
}

const currentRole = computed(() => authState.staff?.role || '')
const currentHotelId = computed(() => authState.staff?.hotelId)

const isGroupAdmin = computed(() => currentRole.value === 'admin' && currentHotelId.value === null)

const canAddEmployee = computed(() => {
  return isGroupAdmin.value || currentRole.value === 'manager'
})

const canEditEmployee = (row) => {
  if (isGroupAdmin.value) return true
  if (row.role === 'admin') return false
  if (currentRole.value !== 'manager') return false
  const currentLevel = roleLevels[currentRole.value] || 99
  const targetLevel = roleLevels[row.role] || 99
  return targetLevel > currentLevel
}

const canDeleteEmployee = (row) => {
  if (isGroupAdmin.value) return true
  if (row.role === 'admin') return false
  if (currentRole.value !== 'manager') return false
  const currentLevel = roleLevels[currentRole.value] || 99
  const targetLevel = roleLevels[row.role] || 99
  return targetLevel > currentLevel
}

const form = reactive({
  id: null,
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  role: 'front_desk',
  hotelId: null,
  phone: '',
  email: '',
  isActive: true
})

const getRoleLabel = (role) => {
  const labels = {
    admin: '管理员',
    manager: '经理',
    front_desk: '前台',
    housekeeping: '客房',
    finance: '财务'
  }
  return labels[role] || role
}

const getHotelName = (hotelId) => {
  if (hotelId === null || hotelId === undefined) {
    return '集团权限'
  }
  const hotel = hotels.value.find(h => h.id === hotelId)
  return hotel ? hotel.name : '未知'
}

const loadEmployees = async () => {
  try {
    const response = await getEmployees()
    employees.value = response.data || []
  } catch (error) {
    ElMessage.error('加载员工列表失败')
  }
}

const editEmployee = (row) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    firstName: row.firstName,
    lastName: row.lastName,
    role: row.role,
    hotelId: row.hotelId,
    phone: row.phone,
    email: row.email,
    isActive: row.isActive
  })
  showAddDialog.value = true
}

const saveEmployee = async () => {
  if (!form.username || !form.firstName || !form.lastName) {
    ElMessage.warning('请填写必填字段')
    return
  }
  
  const data = { ...form }
  if (form.password) {
    data.passwordHash = form.password
  }
  delete data.password
  
  try {
    if (form.id) {
      await updateEmployee(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createEmployee(data)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    loadEmployees()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteEmployee = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      type: 'warning'
    })
    await deleteEmployeeApi(row.id)
    ElMessage.success('删除成功')
    loadEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const loadHotels = async () => {
  try {
    const response = await getHotels()
    hotels.value = response.data || []
  } catch (error) {
    ElMessage.error('加载酒店列表失败')
  }
}

onMounted(() => {
  loadEmployees()
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
