import request from '../utils/request'

/**
 * 获取商品评价列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getProductReviews(params) {
  return request({
    url: '/product/review/page',
    method: 'get',
    params
  })
}

/**
 * 获取评价详情
 * @param {number} id 评价ID
 * @returns {Promise}
 */
export function getProductReviewDetail(id) {
  return request({
    url: `/product/review/${id}`,
    method: 'get'
  })
}

/**
 * 删除商品评价
 * @param {number} id 评价ID
 * @returns {Promise}
 */
export function deleteProductReview(id) {
  return request({
    url: `/product/review/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除商品评价
 * @param {Array} ids 评价ID数组
 * @returns {Promise}
 */
export function batchDeleteProductReview(ids) {
  return request({
    url: '/product/review/batch/delete',
    method: 'post',
    data: { ids }
  })
}
