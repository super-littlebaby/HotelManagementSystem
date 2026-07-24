import { reactive } from 'vue'

const state = reactive({
  isLoggedIn: false,
  guest: null,
  token: null
})

const loadAuthState = () => {
  const token = localStorage.getItem('token')
  const guestData = localStorage.getItem('guest')
  state.isLoggedIn = !!token
  state.token = token
  if (guestData) {
    state.guest = JSON.parse(guestData)
  }
}

const login = (token, guest) => {
  state.isLoggedIn = true
  state.token = token
  state.guest = guest
  localStorage.setItem('token', token)
  localStorage.setItem('guest', JSON.stringify(guest))
}

const logout = () => {
  state.isLoggedIn = false
  state.token = null
  state.guest = null
  localStorage.removeItem('token')
  localStorage.removeItem('guest')
}

loadAuthState()

export { state, login, logout, loadAuthState }
