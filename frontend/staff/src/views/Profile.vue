<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar">
        <el-avatar :size="80" :style="{ backgroundColor: avatarColor }">
          {{ avatarText }}
        </el-avatar>
      </div>
      <div class="profile-title">
        <h2>{{ displayName }}</h2>
        <el-tag :type="getRoleTagType(form.role)">{{ getRoleLabel(form.role) }}</el-tag>
        <span v-if="hotelName" class="hotel-badge">{{ hotelName }}</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="基本信息" name="info">
        <el-form :model="form" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="姓">
            <el-input v-model="form.lastName" placeholder="请输入姓氏" />
          </el-form-item>
          <el-form-item label="名">
            <el-input v-model="form.firstName" placeholder="请输入名字" />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag :type="getRoleTagType(form.role)">{{ getRoleLabel(form.role) }}</el-tag>
          </el-form-item>
          <el-form-item label="所属酒店" v-if="hotelName">
            <el-input :model-value="hotelName" disabled />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="form.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱地址" />
          </el-form-item>
          <el-form-item label="入职日期">
            <el-date-picker v-model="form.hireDate" type="date" disabled style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-form :model="passwordForm" label-width="120px" class="profile-form">
          <el-form-item label="当前密码">
            <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword" :loading="changingPassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { state as authState, login as authLogin } from '../stores/auth'
import { getProfile, updateProfile, changePassword as changePasswordApi } from '../api/employee'
import { getHotelById } from '../api/hotel'

const router = useRouter()

const activeTab = ref('info')
const saving = ref(false)
const changingPassword = ref(false)
const hotelName = ref('')

const form = reactive({
  id: null,
  username: '',
  firstName: '',
  lastName: '',
  role: '',
  phone: '',
  email: '',
  hireDate: '',
  hotelId: null
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const displayName = computed(() => {
  const last = form.lastName || ''
  const first = form.firstName || ''
  return (last + first) || form.username || '员工'
})

const avatarColor = computed(() => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#8b5cf6', '#ec4899']
  const index = (form.username?.charCodeAt(0) || 0) % colors.length
  return colors[index]
})

const avatarText = computed(() => {
  const name = displayName.value
  return name ? name.charAt(0) : 'U'
})

const getRoleLabel = (role) => {
  const labels = {
    admin: '管理员',
    manager: '经理',
    front_desk: '前台',
    housekeeping: '客房',
    finance: '财务'
  }
  return labels[role] || '员工'
}

const getRoleTagType = (role) => {
  const types = {
    admin: 'danger',
    manager: 'warning',
    front_desk: 'primary',
    housekeeping: 'success',
    finance: 'info'
  }
  return types[role] || 'info'
}

const loadProfile = async () => {
  try {
    const res = await getProfile()
    if (res.code === 200 && res.data) {
      Object.assign(form, {
        id: res.data.id,
        username: res.data.username,
        firstName: res.data.firstName || '',
        lastName: res.data.lastName || '',
        role: res.data.role || '',
        phone: res.data.phone || '',
        email: res.data.email || '',
        hireDate: res.data.hireDate || '',
        hotelId: res.data.hotelId || null
      })

      if (res.data.hotelId) {
        try {
          const hotelRes = await getHotelById(res.data.hotelId)
          if (hotelRes.code === 200 && hotelRes.data) {
            hotelName.value = hotelRes.data.name
          }
        } catch (e) {
          hotelName.value = ''
        }
      }
    }
  } catch (error) {
    console.error('加载个人信息失败', error)
    if (authState.staff) {
      Object.assign(form, {
        id: authState.staff.id,
        username: authState.staff.username,
        firstName: authState.staff.firstName || '',
        lastName: authState.staff.lastName || '',
        role: authState.staff.role || '',
        phone: authState.staff.phone || '',
        email: authState.staff.email || '',
        hireDate: authState.staff.hireDate || '',
        hotelId: authState.staff.hotelId || null
      })
      if (authState.staff.hotelId) {
        getHotelById(authState.staff.hotelId).then(res => {
          if (res.code === 200 && res.data) {
            hotelName.value = res.data.name
          }
        }).catch(() => {})
      }
    }
  }
}

const saveProfile = async () => {
  if (!form.lastName && !form.firstName) {
    ElMessage.warning('请至少填写姓名')
    return
  }

  saving.value = true
  try {
    const res = await updateProfile({
      firstName: form.firstName,
      lastName: form.lastName,
      phone: form.phone,
      email: form.email
    })
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      if (res.data) {
        const updatedStaff = { ...authState.staff, ...res.data }
        authLogin(authState.token, updatedStaff)
      }
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败，请检查网络连接')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordForm.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.currentPassword === passwordForm.newPassword) {
    ElMessage.warning('新密码不能与当前密码相同')
    return
  }

  changingPassword.value = true
  try {
    const res = await changePasswordApi({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败，请检查网络连接')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>


