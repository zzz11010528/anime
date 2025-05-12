import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getInfo } from '../api/admin'

export const useAdminStore = defineStore('admin', () => {
  // 状态
  const token = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref({})
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  
  // 方法
  async function loginAction(loginData) {
    try {
      const res = await login(loginData)
      const { token: newToken } = res.data
      
      // 保存token
      token.value = newToken
      localStorage.setItem('adminToken', newToken)
      
      // 获取管理员信息
      await getAdminInfo()
      
      return res
    } catch (error) {
      // 清除token
      token.value = ''
      localStorage.removeItem('adminToken')
      throw error
    }
  }
  
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除token和用户信息
      token.value = ''
      adminInfo.value = {}
      localStorage.removeItem('adminToken')
    }
  }
  
  async function getAdminInfo() {
    try {
      const res = await getInfo()
      adminInfo.value = res.data
      return res.data
    } catch (error) {
      console.error('获取管理员信息失败:', error)
      throw error
    }
  }
  
  return {
    token,
    adminInfo,
    isLoggedIn,
    loginAction,
    logoutAction,
    getAdminInfo
  }
})
