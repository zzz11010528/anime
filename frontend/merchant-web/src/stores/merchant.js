import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '../api/user'
import wsClient from '../utils/websocket'

export const useMerchantStore = defineStore('merchant', () => {
  // 商家信息
  const merchantInfo = ref({
    id: null,
    username: '',
    nickname: '',
    avatar: '',
    email: '',
    phone: '',
    role: 1, // 1-商家
    status: 1, // 1-正常
    merchantName: '',
    logo: '',
    certificationStatus: 0, // 0-未认证，1-已认证，2-认证失败
    companyName: '',
    businessLicense: '',
    contactName: '',
    contactPhone: '',
    description: ''
  })

  // 设置商家信息
  function setMerchantInfo(info) {
    merchantInfo.value = {
      ...merchantInfo.value,
      ...info
    }

    // 保存到localStorage
    localStorage.setItem('merchantInfo', JSON.stringify(merchantInfo.value))
  }

  // 清除商家信息
  function clearMerchantInfo() {
    merchantInfo.value = {
      id: null,
      username: '',
      nickname: '',
      avatar: '',
      email: '',
      phone: '',
      role: 1,
      status: 1,
      merchantName: '',
      logo: '',
      certificationStatus: 0,
      companyName: '',
      businessLicense: '',
      contactName: '',
      contactPhone: '',
      description: ''
    }

    // 从localStorage中移除
    localStorage.removeItem('merchantInfo')
  }

  // 登录
  async function loginAction(loginData) {
    try {
      const res = await login(loginData)
      if (res.code === 200 && res.data) {
        // 保存token
        const token = res.data.token
        localStorage.setItem('token', token)

        // 获取用户信息
        await fetchUserInfo()

        // 建立WebSocket连接
        wsClient.connect(token)

        return res.data
      } else {
        throw new Error(res.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      if (res.code === 200 && res.data) {
        setMerchantInfo(res.data)
        return res.data
      } else {
        throw new Error(res.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  // 退出登录
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除token
      localStorage.removeItem('token')
      // 清除用户信息
      clearMerchantInfo()

      // 断开WebSocket连接
      wsClient.disconnect()

      // 导入router
      const router = window._router
      if (router) {
        // 跳转到登录页面
        router.push('/login')
      } else {
        // 如果router不可用，使用window.location
        window.location.href = '/login'
      }
    }
  }

  return {
    merchantInfo,
    setMerchantInfo,
    clearMerchantInfo,
    loginAction,
    fetchUserInfo,
    logoutAction
  }
})
