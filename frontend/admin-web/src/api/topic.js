import request from '../utils/request'

/**
 * 获取话题列表
 * @returns {Promise}
 */
export function getTopicList() {
  return request({
    url: '/topic/list',
    method: 'get'
  })
}

/**
 * 获取话题详情
 * @param {number} id 话题ID
 * @returns {Promise}
 */
export function getTopicDetail(id) {
  return request({
    url: `/topic/${id}`,
    method: 'get'
  })
}

/**
 * 添加话题
 * @param {Object} data 话题数据
 * @returns {Promise}
 */
export function addTopic(data) {
  return request({
    url: '/topic',
    method: 'post',
    data
  })
}

/**
 * 更新话题
 * @param {Object} data 话题数据
 * @returns {Promise}
 */
export function updateTopic(data) {
  return request({
    url: '/topic',
    method: 'put',
    data
  })
}

/**
 * 删除话题
 * @param {number} id 话题ID
 * @returns {Promise}
 */
export function deleteTopic(id) {
  return request({
    url: `/topic/${id}`,
    method: 'delete'
  })
}

/**
 * 更新话题状态
 * @param {number} id 话题ID
 * @param {number} status 状态：0-禁用，1-启用
 * @returns {Promise}
 */
export function updateTopicStatus(id, status) {
  return request({
    url: '/topic/status',
    method: 'put',
    data: { id, status }
  })
}
