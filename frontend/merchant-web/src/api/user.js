import request from '../utils/request'

/**
 * 用户登录
 * @param {Object} data 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    params: {
      username: data.username,
      password: data.password
    }
  })
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 * @returns {Promise}
 */
export function register(data) {
  // 尝试使用完整的URL
  const fullUrl = '/user/register';
  console.log('完整注册URL:', fullUrl);

  return request({
    url: fullUrl,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(response => {
    console.log('注册请求成功:', response)
    return response
  }).catch(error => {
    console.error('注册请求失败:', error)
    throw error
  })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data 密码信息
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    params: data
  })
}

/**
 * 退出登录
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}
