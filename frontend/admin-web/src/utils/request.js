import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('adminToken')

    // 如果有token，添加到请求头
    if (token) {
      config.headers['Authorization'] = token
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，说明接口请求有误
    if (res.code !== undefined && res.code !== 200) {
      // 显示错误消息
      ElMessage.error(res.message || res.msg || '请求失败')

      // 如果是未登录状态，跳转到登录页
      if (res.code === 401) {
        // 清除token
        localStorage.removeItem('adminToken')

        // 跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      }

      return Promise.reject(new Error(res.message || res.msg || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)

    // 显示错误消息
    ElMessage.error(error.message || '请求失败')

    return Promise.reject(error)
  }
)

export default service
