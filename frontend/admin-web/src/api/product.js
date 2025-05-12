import request from '../utils/request'

/**
 * 获取商品列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getProductList(params) {
  return request({
    url: '/product/page',
    method: 'get',
    params
  })
}

/**
 * 获取热门商品
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getHotProducts(limit = 10) {
  return request({
    url: '/stat/product/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取商品详情
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function getProductDetail(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

/**
 * 审核商品
 * @param {number} id 商品ID
 * @param {number} status 状态：0-拒绝，1-通过
 * @returns {Promise}
 */
export function auditProduct(id, status) {
  return request({
    url: '/product/audit',
    method: 'post',
    params: { id, status }
  })
}

/**
 * 更新商品状态
 * @param {number} id 商品ID
 * @param {number} status 状态：0-下架，1-上架
 * @returns {Promise}
 */
export function updateProductStatus(id, status) {
  return request({
    url: '/product/status',
    method: 'post',
    params: { id, status }
  })
}
