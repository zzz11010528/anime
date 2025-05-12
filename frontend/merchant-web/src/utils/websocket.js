/**
 * WebSocket工具类
 * 用于管理WebSocket连接和消息处理
 */
class WebSocketClient {
  constructor() {
    this.socket = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.listeners = {
      message: [],
      notification: [],
      connect: [],
      disconnect: []
    }
  }

  /**
   * 初始化WebSocket连接
   * @param {string} token 用户认证token
   */
  connect(token) {
    if (this.socket && this.connected) {
      console.log('WebSocket已连接')
      return
    }

    // 获取WebSocket服务器地址
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = '127.0.0.1:8080'  // 直接使用后端服务器地址
    const wsUrl = `${protocol}//${host}/api/websocket/${token}`

    console.log('正在连接WebSocket:', wsUrl)

    try {
      this.socket = new WebSocket(wsUrl)

      // 连接建立
      this.socket.onopen = () => {
        console.log('WebSocket连接已建立')
        this.connected = true
        this.reconnectAttempts = 0
        this.startHeartbeat()
        this.notifyListeners('connect')
      }

      // 接收消息
      this.socket.onmessage = (event) => {
        try {
          // 检查是否是心跳响应
          if (event.data === 'pong') {
            console.log('收到心跳响应')
            return
          }

          // 尝试解析JSON
          const data = JSON.parse(event.data)
          console.log('收到WebSocket消息:', data)

          // 处理不同类型的消息
          if (data.type === 'message') {
            this.notifyListeners('message', data)
          } else if (data.type === 'notification') {
            this.notifyListeners('notification', data)
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error, event.data)
        }
      }

      // 连接关闭
      this.socket.onclose = (event) => {
        console.log('WebSocket连接已关闭:', event.code, event.reason)
        this.connected = false
        this.stopHeartbeat()
        this.notifyListeners('disconnect')

        // 尝试重新连接
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
          setTimeout(() => this.connect(token), this.reconnectInterval)
        }
      }

      // 连接错误
      this.socket.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
      }
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
    }
  }

  /**
   * 关闭WebSocket连接
   */
  disconnect() {
    if (this.socket) {
      this.stopHeartbeat()
      this.socket.close()
      this.socket = null
      this.connected = false
      console.log('WebSocket连接已断开')
    }
  }

  /**
   * 发送消息
   * @param {Object} data 要发送的数据
   */
  send(data) {
    if (!this.socket) {
      console.warn('WebSocket实例不存在，无法发送消息')
      return false
    }

    if (!this.connected) {
      console.warn('WebSocket未连接，消息将在连接后发送')
      // 可以选择将消息加入队列，在连接成功后发送
      return false
    }

    try {
      this.socket.send(JSON.stringify(data))
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  /**
   * 发送私信
   * @param {number} toUserId 接收者用户ID
   * @param {string} content 消息内容
   */
  sendMessage(toUserId, content) {
    return this.send({
      type: 'message',
      toUserId,
      content
    })
  }

  /**
   * 发送心跳包
   */
  sendHeartbeat() {
    this.send({ type: 'heartbeat' })
  }

  /**
   * 开始心跳检测
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      this.sendHeartbeat()
    }, 30000) // 每30秒发送一次心跳
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * 添加事件监听器
   * @param {string} event 事件类型 (message, notification, connect, disconnect)
   * @param {Function} callback 回调函数
   */
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
    return this
  }

  /**
   * 移除事件监听器
   * @param {string} event 事件类型
   * @param {Function} callback 回调函数
   */
  off(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
    return this
  }

  /**
   * 通知所有监听器
   * @param {string} event 事件类型
   * @param {*} data 事件数据
   */
  notifyListeners(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`执行${event}事件监听器失败:`, error)
        }
      })
    }
  }
}

// 创建单例
const wsClient = new WebSocketClient()
export default wsClient
