import request from '../utils/request'

/**
 * 获取商家订单列表
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
 * 获取订单列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getOrderList(params) {
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
 * @param {Object} data 发货信息
 * @returns {Promise}
 */
export function shipOrder(data) {
  return request({
    url: '/order/ship',
    method: 'post',
    data
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

/**
 * 申请退款
 * @param {Object} data 退款信息
 * @returns {Promise}
 */
export function refundOrder(data) {
  return request({
    url: '/order/refund/apply',
    method: 'post',
    data
  })
}

/**
 * 提醒付款
 * @param {number} id 订单ID
 * @returns {Promise}
 */
export function remindPayment(id) {
  return request({
    url: `/order/remind/${id}`,
    method: 'post'
  })
}

/**
 * 导出订单
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportOrders(params) {
  return request({
    url: '/order/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取订单统计数据
 * @returns {Promise}
 */
export function getOrderStats() {
  return request({
    url: '/order/stats',
    method: 'get'
  })
}

/**
 * 取消订单
 * @param {number} id 订单ID
 * @returns {Promise}
 */
export function cancelOrder(id) {
  return request({
    url: '/order/cancel',
    method: 'post',
    params: { id }
  })
}

/**
 * 更新订单备注
 * @param {Object} data 备注信息
 * @returns {Promise}
 */
export function updateOrderRemark(data) {
  return request({
    url: '/order/remark',
    method: 'post',
    data
  })
}



/**
 * 获取物流信息
 * @param {string} company 物流公司编码
 * @param {string} number 物流单号
 * @returns {Promise}
 */
export function getLogisticsInfo(company, number) {
  return request({
    url: '/order/logistics',
    method: 'get',
    params: { company, number }
  })
}
