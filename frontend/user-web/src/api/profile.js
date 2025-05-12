import request from '../utils/request'

/**
 * 获取用户资料
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function getUserProfile(userId) {
  return request({
    url: `/user/profile/${userId}`,
    method: 'get'
  })
}
