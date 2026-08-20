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
          <span class="nav-link cursor-pointer" @click="goToReservation">在线预订</span>
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


