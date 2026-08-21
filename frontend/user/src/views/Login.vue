<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, resetPassword } from '../api/auth'
import { login as setLogin } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const form = ref({
  account: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.value.account || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (!validateAccount(form.value.account)) {
    ElMessage.warning('请输入有效的手机号或邮箱地址')
    return
  }
  
  try {
    const res = await login(form.value)
    setLogin(res.data.token, res.data.guest)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      const extra = { ...route.query }
      delete extra.redirect
      router.push({ path: redirect, query: Object.keys(extra).length ? extra : undefined })
    } else {
      router.push('/')
    }
  } catch (error) {
    const msg = error?.message || error?.response?.data?.message || '登录失败，请检查网络'
    ElMessage.error(msg)
  }
}

const validateAccount = (account) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const phoneRegex = /^[+]?[1-9]\d{1,14}$/
  return emailRegex.test(account) || phoneRegex.test(account)
}

// 忘记密码功能
const showResetDialog = ref(false)
const resetForm = ref({
  account: '',
  idType: 'id_card',
  idNumber: '',
  newPassword: '',
  confirmPassword: ''
})
const resetLoading = ref(false)

const openResetDialog = () => {
  resetForm.value = {
    account: '',
    idType: 'id_card',
    idNumber: '',
    newPassword: '',
    confirmPassword: ''
  }
  showResetDialog.value = true
}

const handleResetPassword = async () => {
  if (!resetForm.value.account || !resetForm.value.idNumber || 
      !resetForm.value.newPassword || !resetForm.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (!validateAccount(resetForm.value.account)) {
    ElMessage.warning('请输入有效的手机号或邮箱地址')
    return
  }
  
  if (resetForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  
  if (resetForm.value.newPassword !== resetForm.value.confirmPassword) {
    ElMessage.warning('输入的密码不同')
    return
  }
  
  resetLoading.value = true
  try {
    const res = await resetPassword({
      account: resetForm.value.account,
      idType: resetForm.value.idType,
      idNumber: resetForm.value.idNumber,
      newPassword: resetForm.value.newPassword
    })
    
    ElMessage.success('密码重置成功，请登录')
    showResetDialog.value = false
  } catch (error) {
    const msg = error?.message || error?.response?.data?.message || '重置密码失败，请检查网络'
    ElMessage.error(msg)
  } finally {
    resetLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="logo">
          <h2>🏨 酒店管理系统</h2>
          <p>欢迎回来</p>
        </div>
        
        <form class="login-form">
          <div class="form-group">
            <label for="account">手机号/邮箱 <span class="required">*</span></label>
            <input 
              id="account"
              v-model="form.account" 
              type="text" 
              placeholder="请输入手机号或邮箱"
            />
          </div>
          
          <div class="form-group">
            <label for="password">密码 <span class="required">*</span></label>
            <input 
              id="password"
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
            />
          </div>
          
          <button class="login-btn" @click.prevent="handleLogin">登录</button>
          
          <div class="link-section">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
            <span class="divider">|</span>
            <a href="#" class="forgot-link" @click.prevent="openResetDialog">忘记密码？</a>
          </div>
        </form>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showResetDialog" title="重置密码" width="450px" :close-on-click-modal="false">
      <div class="reset-tips">
        <p>通过手机号/邮箱 + 证件号验证身份后重置密码</p>
      </div>
      <el-form :model="resetForm" label-width="100px">
        <el-form-item label="手机号/邮箱">
          <el-input v-model="resetForm.account" placeholder="请输入注册时使用的手机号或邮箱" />
        </el-form-item>
        <el-form-item label="证件类型">
          <el-select v-model="resetForm.idType" style="width: 100%">
            <el-option label="身份证" value="id_card" />
            <el-option label="护照" value="passport" />
            <el-option label="驾驶证" value="drivers_license" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号码">
          <el-input v-model="resetForm.idNumber" placeholder="请输入证件号码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>
