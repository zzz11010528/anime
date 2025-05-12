import request from '../utils/request'

/**
 * 获取社区分类列表
 * @returns {Promise}
 */
export function getCategoryList() {
  return request({
    url: '/community/category/list',
    method: 'get'
  })
}

/**
 * 获取社区分类树
 * @returns {Promise}
 */
export function getCategoryTree() {
  return request({
    url: '/community/category/tree',
    method: 'get'
  })
}

/**
 * 添加社区分类
 * @param {Object} data 分类数据
 * @returns {Promise}
 */
export function addCategory(data) {
  return request({
    url: '/community/category',
    method: 'post',
    data
  })
}

/**
 * 更新社区分类
 * @param {Object} data 分类数据
 * @returns {Promise}
 */
export function updateCategory(data) {
  return request({
    url: '/community/category',
    method: 'put',
    data
  })
}

/**
 * 删除社区分类
 * @param {number} id 分类ID
 * @returns {Promise}
 */
export function deleteCategory(id) {
  return request({
    url: `/community/category/${id}`,
    method: 'delete'
  })
}

/**
 * 获取社区分类详情
 * @param {number} id 分类ID
 * @returns {Promise}
 */
export function getCategoryDetail(id) {
  return request({
    url: `/community/category/${id}`,
    method: 'get'
  })
}
