import request from '../utils/request'

/**
 * 获取商品分类列表
 * @returns {Promise}
 */
export function getCategoryList() {
  return request({
    url: '/product/category/list',
    method: 'get'
  })
}

/**
 * 获取分类树
 * @returns {Promise}
 */
export function getCategoryTree() {
  return request({
    url: '/product/category/tree',
    method: 'get'
  })
}

/**
 * 获取分类树VO
 * @returns {Promise}
 */
export function getCategoryTreeVO() {
  return request({
    url: '/product/category/tree/vo',
    method: 'get'
  })
}

/**
 * 获取分类详情
 * @param {number} id 分类ID
 * @returns {Promise}
 */
export function getCategoryDetail(id) {
  return request({
    url: `/product/category/${id}`,
    method: 'get'
  })
}

/**
 * 添加分类
 * @param {Object} data 分类数据
 * @returns {Promise}
 */
export function addCategory(data) {
  return request({
    url: '/product/category',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 * @param {Object} data 分类数据
 * @returns {Promise}
 */
export function updateCategory(data) {
  return request({
    url: '/product/category',
    method: 'put',
    data
  })
}

/**
 * 删除分类
 * @param {number} id 分类ID
 * @returns {Promise}
 */
export function deleteCategory(id) {
  return request({
    url: `/product/category/${id}`,
    method: 'delete'
  })
}

/**
 * 更新分类状态
 * @param {number} id 分类ID
 * @param {number} status 状态：0-禁用，1-启用
 * @returns {Promise}
 */
export function updateCategoryStatus(id, status) {
  return request({
    url: '/product/category/status',
    method: 'put',
    params: { id, status }
  })
}

/**
 * 获取社区分类列表
 * @returns {Promise}
 */
export function getCommunityCategories() {
  return request({
    url: '/community/category/list',
    method: 'get'
  })
}

/**
 * 获取社区分类详情
 * @param {number} id 分类ID
 * @returns {Promise}
 */
export function getCommunityCategory(id) {
  return request({
    url: `/community/category/${id}`,
    method: 'get'
  })
}

/**
 * 添加社区分类
 * @param {Object} data 分类数据
 * @returns {Promise}
 */
export function addCommunityCategory(data) {
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
export function updateCommunityCategory(data) {
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
export function deleteCommunityCategory(id) {
  return request({
    url: `/community/category/${id}`,
    method: 'delete'
  })
}
