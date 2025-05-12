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
 * 发表商品评价
 * @param {Object} data 评价数据，包含review对象和imageUrls数组
 * @returns {Promise}
 */
export function publishReview(data) {
  return request({
    url: '/product/review',
    method: 'post',
    data: data
  })
}

/**
 * 删除商品评价
 * @param {number} id 评价ID
 * @returns {Promise}
 */
export function deleteReview(id) {
  return request({
    url: `/product/review/${id}`,
    method: 'delete'
  })
}
