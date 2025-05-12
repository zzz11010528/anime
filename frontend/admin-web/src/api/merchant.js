import request from '../utils/request'

/**
 * 获取商家认证列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCertificationList(params) {
  return request({
    url: '/merchant/certification/list',
    method: 'get',
    params
  })
}

/**
 * 审核通过商家认证
 * @param {number} id 认证ID
 * @returns {Promise}
 */
export function approveCertification(id) {
  return request({
    url: '/merchant/certification/audit',
    method: 'post',
    data: {
      id,
      status: 1
    }
  })
}

/**
 * 拒绝商家认证
 * @param {number} id 认证ID
 * @param {string} rejectReason 拒绝原因
 * @returns {Promise}
 */
export function rejectCertification(id, rejectReason) {
  return request({
    url: '/merchant/certification/audit',
    method: 'post',
    data: {
      id,
      status: 2,
      rejectReason
    }
  })
}

/**
 * 获取商家列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getMerchantList(params) {
  return request({
    url: '/admin/merchant/list',
    method: 'get',
    params
  })
}

/**
 * 获取商家详情
 * @param {number} id 商家ID
 * @returns {Promise}
 */
export function getMerchantDetail(id) {
  return request({
    url: `/admin/merchant/${id}`,
    method: 'get'
  })
}

/**
 * 更新商家状态
 * @param {number} id 商家ID
 * @param {number} status 状态：0-禁用，1-正常
 * @returns {Promise}
 */
export function updateMerchantStatus(id, status) {
  return request({
    url: '/admin/merchant/status',
    method: 'put',
    data: { id, status }
  })
}
