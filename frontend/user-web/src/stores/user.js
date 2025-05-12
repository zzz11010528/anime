import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '../api/user'
import wsClient from '../utils/websocket'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  async function loginAction(username, password) {
    try {
      const res = await login(username, password)
      // 从返回数据中提取token
      const tokenValue = res.data.token
      token.value = tokenValue
      localStorage.setItem('token', tokenValue)

      // 如果返回了用户信息，直接保存
      if (res.data.user) {
        userInfo.value = res.data.user
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
      } else {
        // 否则获取用户信息
        await fetchUserInfo()
      }

      // 建立WebSocket连接
      wsClient.connect(tokenValue)

      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function registerAction(userData) {
    try {
      await register(userData)
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return Promise.resolve(res.data)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function logoutAction() {
    try {
      await logout()
      clearUserInfo()
      return Promise.resolve()
    } catch (error) {
      clearUserInfo()
      return Promise.reject(error)
    }
  }

  function clearUserInfo() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')

    // 断开WebSocket连接
    wsClient.disconnect()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    registerAction,
    fetchUserInfo,
    logoutAction,
    clearUserInfo
  }
})
