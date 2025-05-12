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

/**
 * 添加IP
 * @param {Object} data IP数据
 * @returns {Promise}
 */
export function addIp(data) {
  return request({
    url: '/ip',
    method: 'post',
    data
  })
}

/**
 * 更新IP
 * @param {Object} data IP数据
 * @returns {Promise}
 */
export function updateIp(data) {
  return request({
    url: '/ip',
    method: 'put',
    data
  })
}

/**
 * 删除IP
 * @param {number} id IP ID
 * @returns {Promise}
 */
export function deleteIp(id) {
  return request({
    url: `/ip/${id}`,
    method: 'delete'
  })
}

/**
 * 更新IP状态
 * @param {number} id IP ID
 * @param {number} status 状态：0-禁用，1-启用
 * @returns {Promise}
 */
export function updateIpStatus(id, status) {
  return request({
    url: '/ip/status',
    method: 'put',
    data: { id, status }
  })
}
