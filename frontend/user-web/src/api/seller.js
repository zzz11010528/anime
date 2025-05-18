import request from '../utils/request'

/**
 * 获取我的商品列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getMyProducts(params) {
  // 从localStorage获取用户信息
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return request({
    url: '/product/page',
    method: 'get',
    params: {
      ...params,
      userId: userInfo.id // 确保只查询当前用户的商品
    }
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
 * 添加商品
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function addProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function updateProduct(data) {
  return request({
    url: '/product',
    method: 'put',
    data
  })
}

/**
 * 上下架商品
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

/**
 * 删除商品
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
  return request({
    url: '/product/status',
    method: 'post',
    params: { id, status: -1 } // 使用status=-1表示删除
  })
}

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
 * 获取卖家订单列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getSellerOrders(params) {
  return request({
    url: '/order/seller/list',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {number} id 订单ID
 * @returns {Promise}
 */
export function getOrderDetail(id) {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

/**
 * 发货
 * @param {number} id 订单ID
 * @returns {Promise}
 */
export function shipOrder(id) {
  return request({
    url: '/order/ship',
    method: 'post',
    params: { id }
  })
}

/**
 * 处理退款
 * @param {number} id 订单ID
 * @param {number} status 状态：1-同意，2-拒绝
 * @param {string} reason 拒绝原因（拒绝时必填）
 * @returns {Promise}
 */
export function handleRefund(id, status, reason) {
  return request({
    url: '/order/refund/handle',
    method: 'post',
    params: { id, status, reason }
  })
}
