<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
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
    if (res.code === 200) {
      setLogin(res.data.token, res.data.guest)
      ElMessage.success('登录成功')
      // 如果存在 redirect 参数，跳回来源页面并保留附带参数（如 hotelId / roomTypeId）
      const redirect = route.query.redirect
      if (redirect && typeof redirect === 'string') {
        const extra = { ...route.query }
        delete extra.redirect
        router.push({ path: redirect, query: Object.keys(extra).length ? extra : undefined })
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查网络')
  }
}

const validateAccount = (account) => {
  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  // 手机号格式验证（支持国际格式）
  const phoneRegex = /^[+]?[1-9]\d{1,14}$/
  
  return emailRegex.test(account) || phoneRegex.test(account)
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
          </div>
        </form>
      </div>
    </div>
  </div>
</template>


