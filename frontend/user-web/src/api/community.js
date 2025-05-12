import request from '../utils/request'

// 获取帖子列表
export function getPostList(params) {
  return request({
    url: '/post/page',
    method: 'get',
    params
  })
}

// 获取帖子详情
export function getPostDetail(id) {
  return request({
    url: `/post/${id}`,
    method: 'get'
  })
}

// 发布帖子
export function publishPost(data) {
  return request({
    url: '/post',
    method: 'post',
    data
  })
}

// 更新帖子
export function updatePost(data) {
  return request({
    url: '/post',
    method: 'put',
    data
  })
}

// 删除帖子
export function deletePost(id) {
  return request({
    url: `/post/${id}`,
    method: 'delete'
  })
}

// 获取评论列表
export function getCommentList(params) {
  return request({
    url: '/comment/page',
    method: 'get',
    params
  })
}

// 发表评论
export function publishComment(data) {
  return request({
    url: '/comment',
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(id) {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

// 获取评论详情
export function getCommentDetail(id) {
  return request({
    url: `/comment/${id}`,
    method: 'get'
  })
}

// 点赞
export function like(targetId, type) {
  return request({
    url: '/like',
    method: 'post',
    params: { targetId, type }
  })
}

// 取消点赞
export function unlike(targetId, type) {
  return request({
    url: '/like',
    method: 'delete',
    params: { targetId, type }
  })
}

// 是否已点赞
export function isLiked(targetId, type) {
  return request({
    url: '/like/status',
    method: 'get',
    params: { targetId, type }
  })
}

// 检查是否已点赞（别名，避免命名冲突）
export function checkIsLiked(targetId, type) {
  return request({
    url: '/like/status',
    method: 'get',
    params: { targetId, type }
  })
}

// 收藏
export function collect(targetId, type) {
  return request({
    url: '/collection',
    method: 'post',
    params: { targetId, type }
  })
}

// 取消收藏
export function uncollect(targetId, type) {
  return request({
    url: '/collection',
    method: 'delete',
    params: { targetId, type }
  })
}

// 是否已收藏
export function isCollected(targetId, type) {
  return request({
    url: '/collection/status',
    method: 'get',
    params: { targetId, type }
  })
}

// 检查是否已收藏（别名，避免命名冲突）
export function checkIsCollected(targetId, type) {
  return request({
    url: '/collection/status',
    method: 'get',
    params: { targetId, type }
  })
}

// 获取社区分类列表
export function getCategoryList() {
  return request({
    url: '/community/category/list',
    method: 'get'
  })
}

// 获取话题列表
export function getTopicList() {
  return request({
    url: '/topic/list',
    method: 'get'
  })
}

// 获取热门帖子
export function getHotPosts(params) {
  return request({
    url: '/stat/post/hot',
    method: 'get',
    params
  })
}

// 获取用户帖子列表
export function getUserPosts(params) {
  return request({
    url: '/post/user',
    method: 'get',
    params
  })
}

// 获取用户评论列表
export function getUserComments(params) {
  return request({
    url: '/user/profile/comment/list',
    method: 'get',
    params
  })
}

// 获取用户点赞列表
export function getUserLikes(params) {
  return request({
    url: '/user/profile/like/list',
    method: 'get',
    params
  })
}
