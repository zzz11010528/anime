import request from '../utils/request'

/**
 * 获取IP列表
 * @returns {Promise}
 */
export function getIpList() {
  return request({
    url: '/ip/list',
    method: 'get'
  })
}

/**
 * 获取IP详情
 * @param {number} id IP ID
 * @returns {Promise}
 */
export function getIpDetail(id) {
  return request({
    url: `/ip/${id}`,
    method: 'get'
  })
}
