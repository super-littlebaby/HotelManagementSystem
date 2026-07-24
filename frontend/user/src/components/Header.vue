<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { state, logout } from '../stores/auth'

const router = useRouter()

const goToReservation = () => {
  if (!state.isLoggedIn) {
    ElMessage.warning('请先登录或注册')
    router.push('/login')
  } else {
    router.push('/reservation')
  }
}

const handleLogout = () => {
  logout()
  router.push('/')
}
</script>

<template>
  <header class="header">
    <div class="container">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <h1>🏨 酒店管理系统</h1>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/reservation" class="nav-link" @click.prevent="goToReservation">在线预订</router-link>
          <router-link to="/my-reservations" class="nav-link" v-if="state.isLoggedIn">我的预订</router-link>
          <router-link to="/profile" class="nav-link" v-if="state.isLoggedIn">个人中心</router-link>
        </nav>
        <div class="auth">
          <template v-if="state.isLoggedIn">
            <span class="welcome">欢迎, {{ state.guest?.firstName }}</span>
            <button class="logout-btn" @click="handleLogout">退出登录</button>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 15px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
  cursor: pointer;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: #fff;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.auth {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome {
  font-size: 14px;
}

.login-btn, .register-btn, .logout-btn {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.login-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.register-btn {
  background: #fff;
  color: #667eea;
}

.register-btn:hover {
  background: #f0f0f0;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
