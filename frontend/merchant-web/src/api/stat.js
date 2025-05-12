import request from '../utils/request'

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
 * 获取销售趋势
 * @param {string} period 周期：week-近7天，month-近30天
 * @returns {Promise}
 */
export function getSalesTrend(period = 'week') {
  return request({
    url: '/stat/sales/trend',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取订单趋势
 * @param {string} period 周期：week-近7天，month-近30天
 * @returns {Promise}
 */
export function getOrderTrend(period = 'week') {
  return request({
    url: '/stat/order/trend',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取商品分类销售分布
 * @returns {Promise}
 */
export function getProductCategorySales() {
  return request({
    url: '/stat/product/category/sales',
    method: 'get'
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
