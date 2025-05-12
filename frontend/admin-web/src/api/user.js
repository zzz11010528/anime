import request from '../utils/request'

/**
 * 获取用户列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 添加用户
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export function addUser(data) {
  return request({
    url: '/admin/user/add',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export function updateUser(data) {
  return request({
    url: '/admin/user/update',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/admin/user/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 更新用户状态
 * @param {number} id 用户ID
 * @param {number} status 状态：0-禁用，1-正常
 * @returns {Promise}
 */
export function updateUserStatus(id, status) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    data: { id, status }
  })
}

/**
 * 重置用户密码
 * @param {number} id 用户ID
 * @param {string} password 新密码
 * @returns {Promise}
 */
export function resetUserPassword(id, password) {
  return request({
    url: '/admin/user/reset-password',
    method: 'put',
    data: { id, password }
  })
}

/**
 * 获取用户详情
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}
