import request from '../utils/request'

// 创建订单
export function createOrder(data) {
  return request({
    url: '/user/order/create',
    method: 'post',
    data: data // 使用data作为请求体，因为后端使用@RequestBody
  })
}

// 直接购买创建订单
export function createDirectOrder(data) {
  return request({
    url: '/user/order/direct',
    method: 'post',
    data: data
  })
}

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/user/order/list',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/user/order/${id}`,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/user/order/cancel/${id}`,
    method: 'post'
  })
}

// 支付订单
export function payOrder(id, payType) {
  return request({
    url: `/user/order/pay/${id}`,
    method: 'post',
    data: { payType }
  })
}

// 确认收货
export function receiveOrder(id) {
  return request({
    url: `/user/order/receive/${id}`,
    method: 'post'
  })
}

// 申请退款
export function applyRefund(id, reason) {
  return request({
    url: `/user/order/refund/${id}`,
    method: 'post',
    data: { reason }
  })
}

// 删除订单
export function deleteOrder(id) {
  return request({
    url: `/user/order/delete/${id}`,
    method: 'post'
  })
}
