<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { login as setLogin } from '../stores/auth'

const router = useRouter()
const form = ref({
  email: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.value.email || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (!validateEmail(form.value.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }
  
  try {
    const res = await login(form.value)
    if (res.code === 200) {
      setLogin(res.data.token, res.data.guest)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查网络')
  }
}

const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
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
            <label for="email">邮箱 <span class="required">*</span></label>
            <input 
              id="email"
              v-model="form.email" 
              type="email" 
              placeholder="请输入邮箱"
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

<style scoped>
.login-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 15px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo h2 {
  font-size: 28px;
  margin-bottom: 10px;
  color: #333;
}

.logo p {
  font-size: 16px;
  color: #888;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.required {
  color: #e74c3c;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.link-section {
  text-align: center;
  font-size: 14px;
  color: #888;
}

.link-section a {
  color: #667eea;
  margin-left: 5px;
}

.link-section a:hover {
  text-decoration: underline;
}
</style>
