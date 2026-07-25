import { createRouter, createWebHistory } from 'vue-router'
import { state } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/employees',
    name: 'Employees',
    component: () => import('../views/Employees.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/hotels',
    name: 'Hotels',
    component: () => import('../views/Hotels.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: () => import('../views/Rooms.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/room-types',
    name: 'RoomTypes',
    component: () => import('../views/RoomTypes.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/facilities',
    name: 'Facilities',
    component: () => import('../views/Facilities.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservations',
    name: 'Reservations',
    component: () => import('../views/Reservations.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkins',
    name: 'CheckIns',
    component: () => import('../views/CheckIns.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bills',
    name: 'Bills',
    component: () => import('../views/Bills.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !state.isLoggedIn) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && state.isLoggedIn) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
