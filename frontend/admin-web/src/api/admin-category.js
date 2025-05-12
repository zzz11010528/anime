import request from '../utils/request'

/**
 * 获取所有商品分类列表（包括禁用的）
 * @returns {Promise}
 */
export function getAllCategoryList() {
  return request({
    url: '/admin/product/category/list',
    method: 'get'
  })
}

/**
 * 获取所有分类树（包括禁用的）
 * @returns {Promise}
 */
export function getAllCategoryTree() {
  return request({
    url: '/admin/product/category/tree',
    method: 'get'
  })
}

/**
 * 获取所有分类树VO（包括禁用的）
 * @returns {Promise}
 */
export function getAllCategoryTreeVO() {
  return request({
    url: '/admin/product/category/tree/vo',
    method: 'get'
  })
}
