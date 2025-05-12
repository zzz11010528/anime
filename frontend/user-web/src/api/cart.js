import request from '../utils/request'

// 获取购物车列表
export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

// 添加商品到购物车
export function addToCart(productId, quantity) {
  return request({
    url: '/cart/add',
    method: 'post',
    params: { productId, quantity }
  })
}

// 更新购物车商品数量
export function updateCartQuantity(id, quantity) {
  return request({
    url: '/cart/update/quantity',
    method: 'post',
    params: { id, quantity }
  })
}

// 更新购物车商品选中状态
export function updateCartSelected(id, selected) {
  return request({
    url: '/cart/update/selected',
    method: 'post',
    params: { id, selected }
  })
}

// 全选/取消全选
export function selectAllCart(selected) {
  return request({
    url: '/cart/select/all',
    method: 'post',
    params: { selected }
  })
}

// 删除购物车商品
export function deleteCartItem(id) {
  return request({
    url: `/cart/${id}`,
    method: 'delete'
  })
}

// 获取购物车商品数量
export function getCartCount() {
  return request({
    url: '/cart/count',
    method: 'get'
  })
}
