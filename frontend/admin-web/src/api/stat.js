import request from '../utils/request'

/**
 * 获取系统概览数据
 * @returns {Promise}
 */
export function getSystemOverview() {
  return request({
    url: '/stat/overview',
    method: 'get'
  })
}

/**
 * 获取商品销售统计
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getProductSalesStat(params) {
  return request({
    url: '/stat/product/sales',
    method: 'get',
    params
  })
}

/**
 * 获取帖子统计
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getPostStat(params) {
  return request({
    url: '/stat/post',
    method: 'get',
    params
  })
}

/**
 * 获取用户行为统计
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getUserBehaviorStat(params) {
  return request({
    url: '/stat/user/behavior',
    method: 'get',
    params
  })
}

/**
 * 获取活跃用户
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getActiveUsers(params) {
  return request({
    url: '/stat/user/active',
    method: 'get',
    params
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

/**
 * 获取热门商品
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getHotProducts(params) {
  return request({
    url: '/stat/product/hot',
    method: 'get',
    params
  })
}

/**
 * 获取热门帖子
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getHotPosts(params) {
  return request({
    url: '/stat/post/hot',
    method: 'get',
    params
  })
}


