import request from '../utils/request'

/**
 * 获取基本设置
 * @returns {Promise}
 */
export function getBasicSettings() {
  return request({
    url: '/system/settings/basic',
    method: 'get'
  })
}

/**
 * 保存基本设置
 * @param {Object} data 基本设置数据
 * @returns {Promise}
 */
export function saveBasicSettings(data) {
  return request({
    url: '/system/settings/basic',
    method: 'post',
    data
  })
}

/**
 * 获取注册设置
 * @returns {Promise}
 */
export function getRegisterSettings() {
  return request({
    url: '/system/settings/register',
    method: 'get'
  })
}

/**
 * 保存注册设置
 * @param {Object} data 注册设置数据
 * @returns {Promise}
 */
export function saveRegisterSettings(data) {
  return request({
    url: '/system/settings/register',
    method: 'post',
    data
  })
}

/**
 * 获取支付设置
 * @returns {Promise}
 */
export function getPaymentSettings() {
  return request({
    url: '/system/settings/payment',
    method: 'get'
  })
}

/**
 * 保存支付设置
 * @param {Object} data 支付设置数据
 * @returns {Promise}
 */
export function savePaymentSettings(data) {
  return request({
    url: '/system/settings/payment',
    method: 'post',
    data
  })
}

/**
 * 获取存储设置
 * @returns {Promise}
 */
export function getStorageSettings() {
  return request({
    url: '/system/settings/storage',
    method: 'get'
  })
}

/**
 * 保存存储设置
 * @param {Object} data 存储设置数据
 * @returns {Promise}
 */
export function saveStorageSettings(data) {
  return request({
    url: '/system/settings/storage',
    method: 'post',
    data
  })
}

/**
 * 测试支付宝配置
 * @param {Object} data 支付宝配置数据
 * @returns {Promise}
 */
export function testAlipayConfig(data) {
  return request({
    url: '/system/settings/payment/test-alipay',
    method: 'post',
    data
  })
}

/**
 * 测试存储配置
 * @param {Object} data 存储配置数据
 * @returns {Promise}
 */
export function testStorageConfig(data) {
  return request({
    url: '/system/settings/storage/test',
    method: 'post',
    data
  })
}

/**
 * 获取系统日志
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getSystemLogs(params) {
  return request({
    url: '/system/logs',
    method: 'get',
    params
  })
}

/**
 * 清空系统日志
 * @returns {Promise}
 */
export function clearSystemLogs() {
  return request({
    url: '/system/logs/clear',
    method: 'delete'
  })
}

/**
 * 获取系统信息
 * @returns {Promise}
 */
export function getSystemInfo() {
  return request({
    url: '/system/info',
    method: 'get'
  })
}
