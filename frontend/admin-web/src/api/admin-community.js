import request from '../utils/request'

/**
 * 获取所有社区分类列表（包括禁用的）
 * @returns {Promise}
 */
export function getAllCommunityCategories() {
  return request({
    url: '/admin/community/category/list',
    method: 'get'
  })
}

/**
 * 获取所有话题列表（包括禁用的）
 * @returns {Promise}
 */
export function getAllTopics() {
  return request({
    url: '/admin/topic/list',
    method: 'get'
  })
}

/**
 * 获取所有IP列表（包括禁用的）
 * @returns {Promise}
 */
export function getAllIps() {
  return request({
    url: '/admin/ip/list',
    method: 'get'
  })
}
