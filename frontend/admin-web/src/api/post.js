import request from '../utils/request'

/**
 * 获取帖子列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getPostList(params) {
  return request({
    url: '/post/page',
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 * @param {number} id 帖子ID
 * @returns {Promise}
 */
export function getPostDetail(id) {
  return request({
    url: `/post/${id}`,
    method: 'get'
  })
}

/**
 * 审核帖子
 * @param {number} id 帖子ID
 * @param {number} status 状态：1-通过，0-拒绝
 * @param {string} rejectReason 拒绝原因（拒绝时必填）
 * @returns {Promise}
 */
export function auditPost(id, status, rejectReason) {
  const params = { id, status };

  // 如果有拒绝原因，添加到参数中
  if (rejectReason) {
    params.rejectReason = rejectReason;
  }

  return request({
    url: '/post/audit',
    method: 'post',
    params: params
  })
}

/**
 * 删除帖子
 * @param {number} id 帖子ID
 * @returns {Promise}
 */
export function deletePost(id) {
  return request({
    url: `/post/${id}`,
    method: 'delete'
  })
}

/**
 * 获取热门帖子
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getHotPosts(limit = 10) {
  return request({
    url: '/stat/post/hot',
    method: 'get',
    params: { limit }
  })
}
