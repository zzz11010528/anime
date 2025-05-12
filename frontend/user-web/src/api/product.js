import request from '../utils/request'

// 获取商品列表
export function getProductList(params) {
  return request({
    url: '/product/page',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 获取商品分类列表
export function getCategoryList() {
  return request({
    url: '/product/category/list',
    method: 'get'
  })
}

// 获取IP列表
export function getIpList() {
  return request({
    url: '/ip/list',
    method: 'get'
  })
}

// 获取热门商品
export function getHotProducts(params) {
  return request({
    url: '/stat/product/hot',
    method: 'get',
    params
  })
}
