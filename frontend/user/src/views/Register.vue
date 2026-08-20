<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  idType: 'id_card',
  idNumber: '',
  nationality: '',
  gender: '',
  dateOfBirth: '',
  password: '',
  confirmPassword: ''
})

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

const handleRegister = async () => {
  if (!form.value.firstName || !form.value.lastName || !form.value.phone || !form.value.password) {
    ElMessage.warning('请填写必填信息')
    return
  }
  
  if (!validatePhone(form.value.phone)) {
    ElMessage.warning('请输入有效的手机号码')
    return
  }
  
  if (form.value.email && !validateEmail(form.value.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }
  
  if (form.value.idNumber && !validateIdNumber(form.value.idType, form.value.idNumber)) {
    ElMessage.warning('请输入有效的证件号码')
    return
  }
  
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度至少为6位')
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  
  try {
    const res = await register(form.value)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('注册失败，请检查网络')
  }
}

const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
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
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="logo">
          <h2>🏨 酒店管理系统</h2>
          <p>创建账号</p>
        </div>
        
        <form class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">姓 <span class="required">*</span></label>
              <input 
                id="firstName"
                v-model="form.firstName" 
                type="text" 
                placeholder="姓"
                maxlength="50"
              />
            </div>
            <div class="form-group">
              <label for="lastName">名 <span class="required">*</span></label>
              <input 
                id="lastName"
                v-model="form.lastName" 
                type="text" 
                placeholder="名"
                maxlength="50"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="phone">手机号 <span class="required">*</span></label>
            <input 
              id="phone"
              v-model="form.phone" 
              type="tel" 
              placeholder="请输入手机号（支持国际格式，如 +8613800138000）"
              maxlength="30"
            />
            <span class="hint">将作为登录账号，请确保输入正确</span>
          </div>
          
          <div class="form-group">
            <label for="email">邮箱</label>
            <input 
              id="email"
              v-model="form.email" 
              type="email" 
              placeholder="请输入邮箱（选填）"
              maxlength="100"
            />
            <span class="hint">用于接收预订通知，选填</span>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="idType">证件类型</label>
              <select id="idType" v-model="form.idType">
                <option v-for="option in idTypeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="idNumber">证件号码</label>
              <input 
                id="idNumber"
                v-model="form.idNumber" 
                type="text" 
                :placeholder="form.idType === 'id_card' ? '请输入18位身份证号码' : '请输入证件号码'"
                maxlength="50"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="nationality">国籍</label>
              <input 
                id="nationality"
                v-model="form.nationality" 
                type="text" 
                placeholder="请输入国籍"
                maxlength="50"
              />
            </div>
            <div class="form-group">
              <label for="gender">性别</label>
              <select id="gender" v-model="form.gender">
                <option v-for="option in genderOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label for="dateOfBirth">出生日期</label>
            <input 
              id="dateOfBirth"
              v-model="form.dateOfBirth" 
              type="date" 
            />
          </div>
          
          <div class="form-group">
            <label for="password">密码 <span class="required">*</span></label>
            <input 
              id="password"
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              minlength="6"
              maxlength="50"
            />
            <span class="hint">密码长度至少为6位</span>
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">确认密码 <span class="required">*</span></label>
            <input 
              id="confirmPassword"
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码"
            />
          </div>
          
          <button class="register-btn" @click.prevent="handleRegister">注册</button>
          
          <div class="link-section">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>


