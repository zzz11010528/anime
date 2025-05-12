import request from '../utils/request'

/**
 * 获取认证信息 - 普通用户可访问
 * @returns {Promise}
 */
export function getUserCertificationInfo() {
  return request({
    url: '/user/certification/info',
    method: 'get',
    headers: {
      'satoken': localStorage.getItem('token')
    }
  })
}

/**
 * 申请商家认证 - 普通用户可访问
 * @param {Object} data 认证信息
 * @returns {Promise}
 */
export function applyUserCertification(data) {
  return request({
    url: '/user/certification/apply',
    method: 'post',
    data,
    headers: {
      'satoken': localStorage.getItem('token')
    }
  })
}
