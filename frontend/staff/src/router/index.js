import { createRouter, createWebHistory } from 'vue-router'
import { state } from '../stores/auth'

const rolePermissions = {
  admin: ['employees', 'hotels', 'rooms', 'roomTypes', 'facilities', 'roomTypeFacilities', 'roomStatusLogs', 'consumableItems', 'consumableOrder', 'reservations', 'checkins', 'bills', 'facilityDamage'],
  manager: ['employees', 'hotels', 'rooms', 'roomTypes', 'facilities', 'roomTypeFacilities', 'roomStatusLogs', 'consumableItems', 'consumableOrder', 'reservations', 'checkins', 'bills', 'facilityDamage'],
  front_desk: ['consumableItems', 'consumableOrder', 'reservations', 'checkins', 'bills', 'facilityDamage'],
  housekeeping: ['rooms', 'roomTypes', 'facilities', 'roomTypeFacilities', 'facilityDamage'],
  finance: ['roomTypes', 'roomTypeFacilities', 'facilities', 'consumableItems', 'checkins', 'bills', 'facilityDamage']
}

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
    meta: { requiresAuth: true, permission: 'employees' }
  },
  {
    path: '/hotels',
    name: 'Hotels',
    component: () => import('../views/Hotels.vue'),
    meta: { requiresAuth: true, permission: 'hotels' }
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: () => import('../views/Rooms.vue'),
    meta: { requiresAuth: true, permission: 'rooms' }
  },
  {
    path: '/room-types',
    name: 'RoomTypes',
    component: () => import('../views/RoomTypes.vue'),
    meta: { requiresAuth: true, permission: 'roomTypes' }
  },
  {
    path: '/facilities',
    name: 'Facilities',
    component: () => import('../views/Facilities.vue'),
    meta: { requiresAuth: true, permission: 'facilities' }
  },
  {
    path: '/room-type-facilities',
    name: 'RoomTypeFacilities',
    component: () => import('../views/RoomTypeFacilities.vue'),
    meta: { requiresAuth: true, permission: 'roomTypeFacilities' }
  },
  {
    path: '/room-status-logs',
    name: 'RoomStatusLogs',
    component: () => import('../views/RoomStatusLogs.vue'),
    meta: { requiresAuth: true, permission: 'roomStatusLogs' }
  },
  {
    path: '/consumable-items',
    name: 'ConsumableItems',
    component: () => import('../views/ConsumableItems.vue'),
    meta: { requiresAuth: true, permission: 'consumableItems' }
  },
  {
    path: '/consumable-order',
    name: 'ConsumableOrder',
    component: () => import('../views/ConsumableOrder.vue'),
    meta: { requiresAuth: true, permission: 'consumableOrder' }
  },
  {
    path: '/reservations',
    name: 'Reservations',
    component: () => import('../views/Reservations.vue'),
    meta: { requiresAuth: true, permission: 'reservations' }
  },
  {
    path: '/checkins',
    name: 'CheckIns',
    component: () => import('../views/CheckIns.vue'),
    meta: { requiresAuth: true, permission: 'checkins' }
  },
  {
    path: '/bills',
    name: 'Bills',
    component: () => import('../views/Bills.vue'),
    meta: { requiresAuth: true, permission: 'bills' }
  },
  {
    path: '/facility-damage',
    name: 'FacilityDamage',
    component: () => import('../views/FacilityDamage.vue'),
    meta: { requiresAuth: true, permission: 'facilityDamage' }
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
  } else if (to.meta.permission && state.isLoggedIn) {
    const role = state.staff?.role
    const permissions = rolePermissions[role] || []
    if (permissions.includes(to.meta.permission)) {
      next()
    } else {
      next({ name: 'Dashboard' })
    }
  } else {
    next()
  }
})

export default router
