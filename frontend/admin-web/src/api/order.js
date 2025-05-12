import request from '../utils/request'

/**
 * 获取订单列表（管理员专用）
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getOrderList(params) {
  console.log('订单查询参数:', params);

  // 直接使用原始参数，不做处理
  return request({
    url: '/order/admin/list',
    method: 'get',
    params: params
  });
}

/**
 * 获取订单详情
 * @param {number} id 订单ID
 * @returns {Promise}
 */
export function getOrderDetail(id) {
  return request({
    url: `/order/admin/${id}`,
    method: 'get'
  })
}

/**
 * 处理退款申请
 * @param {number} id 订单ID
 * @param {number} status 状态：1-同意，2-拒绝
 * @param {string} reason 拒绝原因（拒绝时必填）
 * @returns {Promise}
 */
export function processRefund(id, status, reason) {
  return request({
    url: '/order/admin/refund/handle',
    method: 'post',
    params: { id, status, reason }
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
