<template>
  <div class="page">
    <h2>个人中心</h2>
    
    <el-form :model="form" label-width="100px" style="margin-top: 30px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="名">
        <el-input v-model="form.firstName" />
      </el-form-item>
      <el-form-item label="姓">
        <el-input v-model="form.lastName" />
      </el-form-item>
      <el-form-item label="角色">
        <el-tag>{{ getRoleLabel(form.role) }}</el-tag>
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="入职日期">
        <el-date-picker v-model="form.hireDate" type="date" style="width: 100%" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存修改</el-button>
      </el-form-item>
    </el-form>
    
    <div style="margin-top: 30px; padding-top: 30px; border-top: 1px solid #e2e8f0">
      <h3>修改密码</h3>
      <el-form :model="passwordForm" label-width="100px" style="margin-top: 20px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.currentPassword" type="password" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { state } from '../stores/auth'
import { updateEmployee } from '../api/employee'

const router = useRouter()

const form = reactive({
  id: null,
  username: '',
  firstName: '',
  lastName: '',
  role: '',
  phone: '',
  email: '',
  hireDate: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
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

const loadProfile = () => {
  if (state.staff) {
    Object.assign(form, {
      id: state.staff.id,
      username: state.staff.username,
      firstName: state.staff.firstName || '',
      lastName: state.staff.lastName || '',
      role: state.staff.role || '',
      phone: state.staff.phone || '',
      email: state.staff.email || '',
      hireDate: state.staff.hireDate || ''
    })
  }
}

const saveProfile = async () => {
  if (!form.firstName || !form.lastName) {
    ElMessage.warning('请填写姓名')
    return
  }
  
  try {
    await updateEmployee(form.id, form)
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const changePassword = () => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写所有密码字段')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  ElMessage.info('密码修改功能开发中')
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.page h2 {
  font-size: 24px;
  color: #333;
}

.page h3 {
  font-size: 18px;
  color: #333;
}
</style>
