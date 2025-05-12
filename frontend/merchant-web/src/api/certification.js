import request from '../utils/request'

/**
 * 获取认证信息
 * @returns {Promise}
 */
export function getCertificationInfo() {
  return request({
    url: '/merchant/certification/info',
    method: 'get',
    headers: {
      'satoken': localStorage.getItem('token')
    }
  })
}

/**
 * 申请商家认证
 * @param {Object} data 认证信息
 * @returns {Promise}
 */
export function applyCertification(data) {
  return request({
    url: '/merchant/certification/apply',
    method: 'post',
    data,
    headers: {
      'satoken': localStorage.getItem('token')
    }
  })
}
