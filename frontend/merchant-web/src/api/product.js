import request from '../utils/request'

/**
 * 获取商品列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getProductList(params) {
  // 确保查询的是当前商家的商品
  const merchantInfo = JSON.parse(localStorage.getItem('merchantInfo') || '{}');
  const userId = merchantInfo.id;
  console.log('当前商家ID:', userId);

  const newParams = { ...params, userId };
  return request({
    url: '/product/page',
    method: 'get',
    params: newParams
  })
}

/**
 * 获取商品详情
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function getProductDetail(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

/**
 * 添加商品
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function addProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {number} id 商品ID
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function updateProduct(id, data) {
  // 确保data中包含id
  const submitData = { ...data, id };
  return request({
    url: '/product',
    method: 'put',
    data: submitData
  })
}

/**
 * 删除商品
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
  return request({
    url: '/product/status',
    method: 'post',
    params: { id, status: -1 }  // 使用status=-1表示删除
  })
}

/**
 * 上架商品
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function publishProduct(id) {
  return request({
    url: '/product/status',
    method: 'post',
    params: { id, status: 1 }
  })
}

/**
 * 下架商品
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function unpublishProduct(id) {
  return request({
    url: '/product/status',
    method: 'post',
    params: { id, status: 0 }
  })
}

/**
 * 批量删除商品
 * @param {Array} ids 商品ID数组
 * @returns {Promise}
 */
export function batchDeleteProduct(ids) {
  return request({
    url: '/product/batch/status',
    method: 'post',
    data: { ids, status: -1 }  // 使用status=-1表示删除
  })
}

/**
 * 批量上架商品
 * @param {Array} ids 商品ID数组
 * @returns {Promise}
 */
export function batchPublishProduct(ids) {
  return request({
    url: '/product/batch/status',
    method: 'post',
    data: { ids, status: 1 }
  })
}

/**
 * 批量下架商品
 * @param {Array} ids 商品ID数组
 * @returns {Promise}
 */
export function batchUnpublishProduct(ids) {
  return request({
    url: '/product/batch/status',
    method: 'post',
    data: { ids, status: 0 }
  })
}

/**
 * 获取商品分类列表
 * @returns {Promise}
 */
export function getCategoryList() {
  return request({
    url: '/product/category/list',
    method: 'get'
  })
}

/**
 * 获取商品分类树
 * @returns {Promise}
 */
export function getCategoryTree() {
  return request({
    url: '/product/category/tree',
    method: 'get'
  })
}

/**
 * 获取商品分类树VO
 * @returns {Promise}
 */
export function getCategoryTreeVO() {
  return request({
    url: '/product/category/tree/vo',
    method: 'get'
  })
}

/**
 * 获取IP列表
 * @returns {Promise}
 */
export function getIpList() {
  return request({
    url: '/ip/list',
    method: 'get'
  })
}

/**
 * 获取运费模板列表
 * @returns {Promise}
 */
export function getFreightTemplateList() {
  return request({
    url: '/freight/template/list',
    method: 'get'
  })
}

/**
 * 导出商品
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportProduct(params) {
  return request({
    url: '/product/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 下载导入模板
 * @returns {Promise}
 */
export function downloadImportTemplate() {
  return request({
    url: '/product/import/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取商品销售数据
 * @param {number} id 商品ID
 * @returns {Promise}
 */
export function getProductSalesData(id) {
  return request({
    url: `/stat/product/sales/${id}`,
    method: 'get'
  })
}
