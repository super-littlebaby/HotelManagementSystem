<template>
  <div class="page">
    <div class="page-header">
      <h2>员工管理</h2>
      <el-button type="primary" @click="openAddDialog" :disabled="!canAddEmployee">添加员工</el-button>
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
      <el-table-column prop="hireDate" label="入职时间" width="120">
        <template #default="{ row }">
          <span>{{ formatDate(row.hireDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="isActive" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'">{{ row.isActive ? '在职' : '离职' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="editEmployee(row)" :disabled="!canEditEmployee(row)" title="无权编辑">编辑</el-button>
          <el-button
            size="small"
            :type="row.isActive ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
            :disabled="!canToggleStatus(row)"
            :title="canToggleStatus(row) ? (row.isActive ? '设置离职' : '设置在职') : '无权修改状态'"
          >
            {{ row.isActive ? '离职' : '在职' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteEmployee(row)" :disabled="!canDeleteEmployee(row)" title="无权删除">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAddDialog" :title="form.id ? '编辑员工' : '添加员工'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item v-else label="密码">
          <el-input v-model="form.password" type="password" placeholder="留空表示不修改密码" />
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
        <el-form-item v-if="form.id" label="入职时间">
          <el-input v-model="form.hireDate" disabled placeholder="创建账号时自动记录" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.isActive" :disabled="isGroupAdmin ? false : (form.role === 'admin')">
            <el-option label="在职" :value="true" />
            <el-option label="离职" :value="false" />
          </el-select>
          <span v-if="form.role === 'admin' && !isGroupAdmin" style="color:#909399;font-size:12px;margin-left:8px;">管理员状态不可修改</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetForm">取消</el-button>
        <el-button type="primary" @click="saveEmployee">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployees, createEmployee, updateEmployee, deleteEmployee as deleteEmployeeApi, toggleEmployeeStatus } from '../api/employee'
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

const canToggleStatus = (row) => {
  if (isGroupAdmin.value) {
    return !(row.role === 'admin' && row.isActive)
  }
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
  isActive: true,
  hireDate: ''
})

const formatDate = (value) => {
  if (!value) return ''
  const s = String(value)
  // ISO: yyyy-MM-dd
  if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10)
  return s
}

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
    isActive: row.isActive,
    hireDate: formatDate(row.hireDate)
  })
  showAddDialog.value = true
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    role: 'front_desk',
    hotelId: null,
    phone: '',
    email: '',
    isActive: true,
    hireDate: ''
  })
  showAddDialog.value = false
}

const openAddDialog = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    role: 'front_desk',
    hotelId: null,
    phone: '',
    email: '',
    isActive: true,
    hireDate: ''
  })
  showAddDialog.value = true
}

const handleToggleStatus = async (row) => {
  const targetStatus = !row.isActive
  const actionText = targetStatus ? '设置为在职' : '设置为离职'
  try {
    await ElMessageBox.confirm(`确定要将员工 "${row.username}" ${actionText} 吗？`, '提示', {
      type: 'warning'
    })
    await toggleEmployeeStatus(row.id, targetStatus)
    ElMessage.success(actionText + '成功')
    loadEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(actionText + '失败')
    }
  }
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
  // 新增时入职时间由后端按服务端当天自动写入，前端不传
  // 编辑时若为空，交给后端继承原记录，避免清空
  if (!form.id || !form.hireDate) {
    delete data.hireDate
  }

  try {
    if (form.id) {
      await updateEmployee(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createEmployee(data)
      ElMessage.success('创建成功')
    }
    resetForm()
    loadEmployees()
  } catch (error) {
    const msg = error && error.message ? error.message : '操作失败'
    ElMessage.error(msg)
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


