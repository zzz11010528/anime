import request from '../utils/request'

/**
 * 获取评论列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCommentList(params) {
  return request({
    url: '/comment/page',
    method: 'get',
    params
  })
}

/**
 * 获取评论详情
 * @param {number} id 评论ID
 * @returns {Promise}
 */
export function getCommentDetail(id) {
  return request({
    url: `/comment/${id}`,
    method: 'get'
  })
}

/**
 * 删除评论
 * @param {number} id 评论ID
 * @returns {Promise}
 */
export function deleteComment(id) {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除评论
 * @param {Array} ids 评论ID数组
 * @returns {Promise}
 */
export function batchDeleteComment(ids) {
  return request({
    url: '/comment/batch/delete',
    method: 'post',
    data: { ids }
  })
}
