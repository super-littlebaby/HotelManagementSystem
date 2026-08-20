<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { state } from '../stores/auth'
import { updateGuest } from '../api/auth'

const router = useRouter()
const isEditing = ref(false)
const form = reactive({
  id: null,
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  idType: '',
  idNumber: '',
  nationality: '',
  gender: '',
  dateOfBirth: '',
  notes: ''
})

const originalForm = reactive({})

const idTypeOptions = [
  { value: 'id_card', label: '身份证' },
  { value: 'passport', label: '护照' },
  { value: 'drivers_license', label: '驾驶证' },
  { value: 'other', label: '其他证件' }
]

const genderOptions = [
  { value: 'male', label: '男' },
  { value: 'female', label: '女' },
  { value: 'secret', label: '保密' }
]

onMounted(() => {
  if (!state.isLoggedIn || !state.guest) {
    router.push('/login')
    return
  }
  
  const guest = state.guest
  form.id = guest.id
  form.firstName = guest.firstName || ''
  form.lastName = guest.lastName || ''
  form.email = guest.email || ''
  form.phone = guest.phone || ''
  form.idType = guest.idType || 'id_card'
  form.idNumber = guest.idNumber || ''
  form.nationality = guest.nationality || ''
  form.gender = guest.gender || 'secret'
  form.dateOfBirth = guest.dateOfBirth || ''
  form.notes = guest.notes || ''
  
  Object.assign(originalForm, form)
})

const toggleEdit = () => {
  if (!isEditing.value) {
    Object.assign(originalForm, form)
  } else {
    Object.assign(form, originalForm)
  }
  isEditing.value = !isEditing.value
}

const handleSave = async () => {
  if (!form.firstName || !form.lastName) {
    ElMessage.warning('请填写姓名')
    return
  }
  
  if (form.phone && !form.phone.includes('*') && !validatePhone(form.phone)) {
    ElMessage.warning('请输入有效的手机号码')
    return
  }
  
  if (form.idNumber && !form.idNumber.includes('*') && !validateIdNumber(form.idType, form.idNumber)) {
    ElMessage.warning('请输入有效的证件号码')
    return
  }
  
  const updateData = {
    id: form.id,
    firstName: form.firstName,
    lastName: form.lastName
  }
  
  if (form.phone && !form.phone.includes('*') && form.phone !== originalForm.phone) {
    updateData.phone = form.phone
  }
  
  if (form.idType !== originalForm.idType) {
    updateData.idType = form.idType
  }
  
  if (form.idNumber && !form.idNumber.includes('*') && form.idNumber !== originalForm.idNumber) {
    updateData.idNumber = form.idNumber
  }
  
  if (form.nationality !== originalForm.nationality) {
    updateData.nationality = form.nationality
  }
  
  if (form.gender !== originalForm.gender) {
    updateData.gender = form.gender
  }
  
  if (form.dateOfBirth !== originalForm.dateOfBirth) {
    updateData.dateOfBirth = form.dateOfBirth
  }
  
  if (form.notes !== originalForm.notes) {
    updateData.notes = form.notes
  }
  
  try {
    const res = await updateGuest(updateData)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      state.guest = res.data
      localStorage.setItem('guest', JSON.stringify(res.data))
      Object.assign(originalForm, form)
      isEditing.value = false
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('更新失败，请检查网络')
  }
}

const validatePhone = (phone) => {
  const cleanedPhone = phone.replace(/[\s\-()]/g, '')
  
  if (cleanedPhone.startsWith('86') || cleanedPhone.startsWith('+86')) {
    const chinaPhone = cleanedPhone.replace(/^\+?86/, '')
    return /^1[3-9]\d{9}$/.test(chinaPhone)
  }
  
  return /^[+]?[1-9]\d{1,14}$/.test(cleanedPhone)
}

const validateIdNumber = (idType, idNumber) => {
  if (!idNumber) return true
  
  switch (idType) {
    case 'id_card':
      const idRegex = /^\d{17}[\dXx]$/
      if (!idRegex.test(idNumber)) return false
      
      const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
      const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
      
      let sum = 0
      for (let i = 0; i < 17; i++) {
        sum += parseInt(idNumber[i]) * factors[i]
      }
      const remainder = sum % 11
      const checkCode = checkCodes[remainder]
      
      return idNumber[17].toUpperCase() === checkCode
    
    case 'passport':
      return /^[A-Za-z0-9]{6,20}$/.test(idNumber)
    
    case 'drivers_license':
      return /^[A-Za-z0-9]{8,20}$/.test(idNumber)
    
    case 'other':
    default:
      return idNumber.length >= 4 && idNumber.length <= 50
  }
}

const getIdTypeLabel = (idType) => {
  const map = { 'id_card': '身份证', 'passport': '护照', 'drivers_license': '驾驶证', 'other': '其他证件' }
  return map[idType] || idType
}

const getGenderText = (gender) => {
  const map = { 'male': '男', 'female': '女', 'secret': '保密' }
  return map[gender] || gender || '未设置'
}
</script>

<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar">
            <span>{{ form.firstName?.charAt(0) || '?' }}</span>
          </div>
          <div class="user-info">
            <h2>{{ form.firstName }} {{ form.lastName }}</h2>
            <p>{{ form.email }}</p>
          </div>
          <button class="edit-btn" @click="toggleEdit">
            {{ isEditing ? '取消' : '编辑资料' }}
          </button>
        </div>
        
        <div class="profile-content">
          <div class="section">
            <h3>基本信息</h3>
            <div class="form-grid">
              <div class="form-item">
                <label>姓</label>
                <input v-model="form.firstName" :disabled="!isEditing" />
              </div>
              <div class="form-item">
                <label>名</label>
                <input v-model="form.lastName" :disabled="!isEditing" />
              </div>
              <div class="form-item">
                <label>邮箱</label>
                <input v-model="form.email" :disabled="true" />
              </div>
              <div class="form-item">
                <label>手机号</label>
                <input v-model="form.phone" :disabled="!isEditing" placeholder="请输入手机号" />
              </div>
              <div class="form-item">
                <label>证件类型</label>
                <select v-model="form.idType" :disabled="!isEditing">
                  <option v-for="option in idTypeOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="form-item">
                <label>证件号码</label>
                <input v-model="form.idNumber" :disabled="!isEditing" :placeholder="form.idType === 'id_card' ? '请输入18位身份证号码' : '请输入证件号码'" />
              </div>
              <div class="form-item">
                <label>国籍</label>
                <input v-model="form.nationality" :disabled="!isEditing" placeholder="请输入国籍" />
              </div>
              <div class="form-item">
                <label>性别</label>
                <select v-model="form.gender" :disabled="!isEditing">
                  <option v-for="option in genderOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="form-item">
                <label>出生日期</label>
                <input type="date" v-model="form.dateOfBirth" :disabled="!isEditing" />
              </div>
              <div class="form-item">
                <label>备注</label>
                <textarea v-model="form.notes" :disabled="!isEditing" placeholder="请输入备注信息" rows="3"></textarea>
              </div>
            </div>
          </div>
          
          <div class="action-buttons" v-if="isEditing">
            <button class="save-btn" @click="handleSave">保存修改</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


