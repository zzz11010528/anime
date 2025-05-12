/**
 * 全局配置文件
 */

// API基础URL
export const BASE_API_URL = '/api'

// 完整的API基础URL（包含域名和端口）
export const FULL_API_URL = 'http://localhost:3002/api'

// 上传文件配置
export const UPLOAD_CONFIG = {
  // 上传文件的基础URL
  baseUrl: FULL_API_URL,
  
  // 获取完整的文件URL
  getFullUrl: (path) => {
    if (!path) return ''
    // 确保路径格式正确
    const formattedPath = path.startsWith('/') ? path : '/' + path
    return FULL_API_URL + formattedPath
  }
}

// 默认分页配置
export const PAGINATION_CONFIG = {
  pageSize: 10,
  pageSizes: [10, 20, 50, 100]
}

export default {
  BASE_API_URL,
  FULL_API_URL,
  UPLOAD_CONFIG,
  PAGINATION_CONFIG
}
