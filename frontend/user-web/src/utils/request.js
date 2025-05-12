import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 使用正确的格式设置Authorization头
      config.headers['Authorization'] = `${token}`
      console.log('设置请求头:', config.headers)
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，说明接口请求失败
    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 401: 未登录或token过期
      if (res.code === 401) {
        // 清除本地token和用户信息
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')

        // 跳转到登录页
        router.replace({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
      }

      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.error('Response error:', error)

    // 获取错误信息
    let errorMessage = '请求失败'

    if (error.response && error.response.data) {
      // 优先使用后端返回的错误信息
      errorMessage = error.response.data.message || error.response.data.msg || errorMessage
    } else if (error.message) {
      errorMessage = error.message
    }

    // 不在拦截器中显示错误消息，让调用方自行处理
    // 这样可以避免重复显示错误消息，也可以让调用方根据具体情况处理错误

    // 创建一个包含错误信息的Error对象
    const enhancedError = new Error(errorMessage)
    enhancedError.originalError = error
    enhancedError.response = error.response

    return Promise.reject(enhancedError)
  }
)

export default service
