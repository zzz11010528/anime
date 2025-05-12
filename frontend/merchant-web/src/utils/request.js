import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    // 如果有token，添加到请求头
    if (token) {
      // 使用Authorization作为头名称，这是Sa-Token框架配置的头名称
      config.headers['Authorization'] = token
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
      // 如果是未登录或token失效
      if (res.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        // 清除token
        localStorage.removeItem('token')
        // 跳转到登录页
        router.push('/login')
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }

      // 其他错误
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  error => {
    console.error('Response error:', error)

    // 处理网络错误
    if (error.response) {
      const status = error.response.status

      // 未登录或token失效
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        // 清除token
        localStorage.removeItem('token')
        // 跳转到登录页
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('请求无响应:', error.request)
      ElMessage.error('服务器无响应，请检查后端服务是否启动')
    } else {
      // 请求配置有误
      console.error('请求配置错误:', error.message)
      ElMessage.error('请求配置错误: ' + error.message)
    }

    return Promise.reject(error)
  }
)

export default service
