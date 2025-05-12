import request from '../utils/request'

// 获取正在进行中的拼团活动列表
export function getActiveGroupBuyings(params) {
  return request({
    url: '/user/group-buying/active',
    method: 'get',
    params
  })
}

// 获取拼团活动详情
export function getGroupBuyingDetail(id) {
  return request({
    url: `/user/group-buying/${id}`,
    method: 'get'
  })
}

// 创建拼团
export function createGroup(groupBuyingId) {
  return request({
    url: `/user/group-buying/create/${groupBuyingId}`,
    method: 'post'
  })
}

// 加入拼团
export function joinGroup(groupOrderId) {
  return request({
    url: `/user/group-buying/join/${groupOrderId}`,
    method: 'post'
  })
}

// 获取拼团订单详情
export function getGroupOrderDetail(id) {
  return request({
    url: `/user/group-buying/order/${id}`,
    method: 'get'
  })
}

// 取消拼团
export function cancelGroup(groupOrderId) {
  return request({
    url: `/user/group-buying/cancel/${groupOrderId}`,
    method: 'post'
  })
}

// 获取用户拼团订单列表
export function getUserGroupOrders(params) {
  return request({
    url: '/user/group-buying/user-orders',
    method: 'get',
    params
  }).then(res => {
    console.log('拼团订单API响应:', res)
    return res
  }).catch(err => {
    console.error('拼团订单API错误:', err)
    throw err
  })
}
