import request from '../utils/request'

/**
 * 点赞
 * @param {number} targetId 目标ID
 * @param {number} type 类型：1-帖子，2-评论
 * @returns {Promise}
 */
export function like(targetId, type) {
  return request({
    url: '/like',
    method: 'post',
    params: { targetId, type }
  })
}

/**
 * 取消点赞
 * @param {number} targetId 目标ID
 * @param {number} type 类型：1-帖子，2-评论
 * @returns {Promise}
 */
export function unlike(targetId, type) {
  return request({
    url: '/like',
    method: 'delete',
    params: { targetId, type }
  })
}

/**
 * 是否已点赞
 * @param {number} targetId 目标ID
 * @param {number} type 类型：1-帖子，2-评论
 * @returns {Promise}
 */
export function isLiked(targetId, type) {
  return request({
    url: '/like/status',
    method: 'get',
    params: { targetId, type }
  })
}

/**
 * 获取点赞详情
 * @param {number} id 点赞ID
 * @returns {Promise}
 */
export function getLikeDetail(id) {
  return request({
    url: `/like/${id}`,
    method: 'get'
  })
}
