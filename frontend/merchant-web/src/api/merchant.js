import request from '../utils/request'

/**
 * 商家注册
 * @param {Object} data 注册信息
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/merchant/register',
    method: 'post',
    data
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
    data
  })
}

/**
 * 获取认证信息
 * @returns {Promise}
 */
export function getCertificationInfo() {
  return request({
    url: '/merchant/certification/info',
    method: 'get'
  })
}

/**
 * 获取商家统计概览
 * @returns {Promise}
 */
export function getMerchantOverview() {
  return request({
    url: '/stat/merchant/overview',
    method: 'get'
  })
}
