<template>
  <div class="app-container" v-if="state.isLoggedIn">
    <el-container class="main-container">
      <el-aside width="200px" class="sidebar">
        <div class="logo">酒店管理系统</div>
        <div class="role-tag">{{ getRoleLabel(state.staff?.role) }}</div>
        <el-menu :default-active="activeMenu" class="menu" router>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/employees" v-if="hasPermission('employees')">
            <el-icon><User /></el-icon>
            <span>员工管理</span>
          </el-menu-item>
          <el-menu-item index="/hotels" v-if="hasPermission('hotels')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>酒店管理</span>
          </el-menu-item>
          <el-menu-item index="/rooms" v-if="hasPermission('rooms')">
            <el-icon><Grid /></el-icon>
            <span>房间管理</span>
          </el-menu-item>
          <el-menu-item index="/room-types" v-if="hasPermission('roomTypes')">
            <el-icon><Grid /></el-icon>
            <span>房型管理</span>
          </el-menu-item>
          <el-menu-item index="/facilities" v-if="hasPermission('facilities')">
            <el-icon><Monitor /></el-icon>
            <span>设施管理</span>
          </el-menu-item>
          <el-menu-item index="/room-type-facilities" v-if="hasPermission('roomTypeFacilities')">
            <el-icon><Connection /></el-icon>
            <span>房型设施关联</span>
          </el-menu-item>
          <el-menu-item index="/reservations" v-if="hasPermission('reservations')">
            <el-icon><Calendar /></el-icon>
            <span>预订管理</span>
          </el-menu-item>
          <el-menu-item index="/checkins" v-if="hasPermission('checkins')">
            <el-icon><Key /></el-icon>
            <span>入住管理</span>
          </el-menu-item>
          <el-menu-item index="/bills" v-if="hasPermission('bills')">
            <el-icon><Wallet /></el-icon>
            <span>账单管理</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-right">
            <el-avatar :size="36" :style="{ backgroundColor: avatarColor }">
              {{ avatarText }}
            </el-avatar>
            <span class="welcome">欢迎, {{ displayName }}</span>
            <el-button type="primary" @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        <el-main class="content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, User, OfficeBuilding, Grid, Calendar, Key, Wallet, UserFilled, Monitor, Connection } from '@element-plus/icons-vue'
import { state, logout } from './stores/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const displayName = computed(() => {
  const last = state.staff?.lastName || ''
  const first = state.staff?.firstName || ''
  return (last + first) || state.staff?.username || '员工'
})

const avatarColor = computed(() => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#8b5cf6', '#ec4899']
  const index = (state.staff?.username?.charCodeAt(0) || 0) % colors.length
  return colors[index]
})

const avatarText = computed(() => {
  const name = displayName.value
  return name ? name.charAt(0) : 'U'
})

const rolePermissions = {
  admin: ['employees', 'hotels', 'rooms', 'roomTypes', 'facilities', 'roomTypeFacilities', 'reservations', 'checkins', 'bills'],
  manager: ['employees', 'hotels', 'rooms', 'roomTypes', 'facilities', 'roomTypeFacilities', 'reservations', 'checkins', 'bills'],
  front_desk: ['rooms', 'reservations', 'checkins', 'bills'],
  housekeeping: ['rooms'],
  finance: ['bills']
}

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

const hasPermission = (module) => {
  const role = state.staff?.role
  const permissions = rolePermissions[role] || []
  return permissions.includes(module)
}

const handleLogout = () => {
  logout()
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.main-container {
  height: 100vh;
}

.sidebar {
  background-color: #2d3748;
  color: #fff;
}

.logo {
  padding: 20px;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #4a5568;
}

.role-tag {
  padding: 8px 20px;
  text-align: center;
  font-size: 12px;
  color: #63b3ed;
  background-color: rgba(99, 179, 237, 0.1);
  border-bottom: 1px solid #4a5568;
}

.menu {
  height: calc(100vh - 110px);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome {
  font-size: 14px;
  color: #4a5568;
}

.content {
  padding: 20px;
}
</style>
