<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>员工登录</h2>
        <p>酒店管理系统</p>
      </div>
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { login as authLogin } from '../stores/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }

  loading.value = true
  try {
    const response = await login(form.username, form.password)
    if (response.code === 200) {
      authLogin(response.data.token, response.data.employee)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    if (error && error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败，请检查网络')
    }
  } finally {
    loading.value = false
  }
}
</script>


