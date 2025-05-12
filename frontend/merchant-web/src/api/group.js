import request from '@/utils/request'

// 获取拼团活动列表
export function getGroupBuyingList(params) {
  return request({
    url: '/merchant/group-buying/page',
    method: 'get',
    params
  })
}

// 获取拼团活动详情
export function getGroupBuyingDetail(id) {
  return request({
    url: `/merchant/group-buying/${id}`,
    method: 'get'
  })
}

// 创建拼团活动
export function createGroupBuying(data) {
  return request({
    url: '/merchant/group-buying',
    method: 'post',
    data
  })
}

// 更新拼团活动
export function updateGroupBuying(data) {
  return request({
    url: '/merchant/group-buying',
    method: 'put',
    data
  })
}

// 结束拼团活动
export function endGroupBuying(id) {
  return request({
    url: `/merchant/group-buying/end/${id}`,
    method: 'post'
  })
}

// 获取拼团中的订单列表
export function getGroupingOrders(groupBuyingId) {
  return request({
    url: `/merchant/group-buying/orders/grouping/${groupBuyingId}`,
    method: 'get'
  })
}

// 获取已成团的订单列表
export function getGroupedOrders(groupBuyingId) {
  return request({
    url: `/merchant/group-buying/orders/grouped/${groupBuyingId}`,
    method: 'get'
  })
}

// 获取拼团订单详情
export function getGroupOrderDetail(id) {
  return request({
    url: `/merchant/group-buying/order/${id}`,
    method: 'get'
  })
}
