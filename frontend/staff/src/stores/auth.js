import { reactive } from 'vue'

const state = reactive({
  isLoggedIn: false,
  staff: null,
  token: null
})

const loadAuthState = () => {
  const token = localStorage.getItem('staff_token')
  const staffData = localStorage.getItem('staff')
  state.isLoggedIn = !!token
  state.token = token
  if (staffData) {
    state.staff = JSON.parse(staffData)
  }
}

const login = (token, staff) => {
  state.isLoggedIn = true
  state.token = token
  state.staff = staff
  localStorage.setItem('staff_token', token)
  localStorage.setItem('staff', JSON.stringify(staff))
}

const logout = () => {
  state.isLoggedIn = false
  state.token = null
  state.staff = null
  localStorage.removeItem('staff_token')
  localStorage.removeItem('staff')
}

loadAuthState()

export { state, login, logout, loadAuthState }
