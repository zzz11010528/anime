import request from '../utils/request'

/**
 * 管理员登录
 * @param {Object} data 登录数据
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
 * 管理员登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取管理员信息
 * @returns {Promise}
 */
export function getInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {Object} data 密码数据
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    params: {
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }
  })
}

/**
 * 获取管理员列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getAdminList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 添加管理员
 * @param {Object} data 管理员数据
 * @returns {Promise}
 */
export function addAdmin(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  })
}

/**
 * 更新管理员
 * @param {Object} data 管理员数据
 * @returns {Promise}
 */
export function updateAdmin(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}

/**
 * 删除管理员
 * @param {number} id 管理员ID
 * @returns {Promise}
 */
export function deleteAdmin(id) {
  return request({
    url: `/admin/user/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 重置管理员密码
 * @param {number} id 管理员ID
 * @param {string} password 新密码
 * @returns {Promise}
 */
export function resetPassword(id, password) {
  return request({
    url: '/admin/user/reset-password',
    method: 'put',
    data: { id, password }
  })
}

/**
 * 更新管理员状态
 * @param {number} id 管理员ID
 * @param {number} status 状态：0-禁用，1-启用
 * @returns {Promise}
 */
export function updateAdminStatus(id, status) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    data: { id, status }
  })
}

/**
 * 更新管理员个人信息
 * @param {Object} data 个人信息数据
 * @returns {Promise}
 */
export function updateAdminInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}


