import request from '../utils/request'

// 获取私信列表
export function getMessageList(params) {
  return request({
    url: '/message/page',
    method: 'get',
    params
  })
}

// 发送私信
export function sendMessage(toUserId, content) {
  return request({
    url: '/message/send',
    method: 'post',
    params: { toUserId, content }
  })
}

// 标记私信为已读
export function markMessageAsRead(id) {
  return request({
    url: `/message/read/${id}`,
    method: 'post'
  })
}

// 标记会话为已读
export function markConversationAsRead(targetUserId) {
  return request({
    url: `/message/read/conversation/${targetUserId}`,
    method: 'post'
  })
}

// 获取会话列表
export function getConversationList() {
  return request({
    url: '/message/conversation/list',
    method: 'get'
  })
}

// 获取未读私信数量
export function getUnreadMessageCount() {
  return request({
    url: '/message/unread/count',
    method: 'get'
  })
}

// 获取通知列表
export function getNotificationList(params) {
  return request({
    url: '/notification/page',
    method: 'get',
    params
  })
}

// 标记通知为已读
export function markNotificationAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'post'
  })
}

// 标记所有通知为已读
export function markAllNotificationsAsRead() {
  return request({
    url: '/notification/read/all',
    method: 'post'
  })
}

// 获取未读通知数量
export function getUnreadNotificationCount(type) {
  return request({
    url: '/notification/unread/count',
    method: 'get',
    params: { type }
  })
}

// 删除私信会话
export function deleteConversation(targetUserId) {
  return request({
    url: `/message/conversation/${targetUserId}`,
    method: 'delete'
  })
}
