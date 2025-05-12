/**
 * 手势识别工具类
 */
export default class GestureRecognizer {
  constructor(options = {}) {
    // 配置项
    this.options = {
      minDistance: 50, // 最小滑动距离
      maxTime: 1000, // 最大滑动时间
      ...options
    }
    
    // 手势状态
    this.state = {
      startX: 0,
      startY: 0,
      endX: 0,
      endY: 0,
      startTime: 0,
      endTime: 0,
      points: []
    }
    
    // 手势回调
    this.callbacks = {
      swipeLeft: null,
      swipeRight: null,
      swipeUp: null,
      swipeDown: null,
      circle: null,
      zShape: null,
      tap: null,
      doubleTap: null,
      longPress: null
    }
    
    // 长按定时器
    this.longPressTimer = null
    
    // 双击计时器
    this.doubleTapTimer = null
    this.lastTapTime = 0
  }
  
  /**
   * 初始化手势识别
   * @param {HTMLElement} element 要绑定手势的元素
   */
  init(element) {
    if (!element) {
      console.error('GestureRecognizer: 未提供有效的DOM元素')
      return
    }
    
    this.element = element
    
    // 绑定事件
    this.bindEvents()
    
    return this
  }
  
  /**
   * 绑定事件
   */
  bindEvents() {
    // 触摸事件
    this.element.addEventListener('touchstart', this.handleTouchStart.bind(this), false)
    this.element.addEventListener('touchmove', this.handleTouchMove.bind(this), false)
    this.element.addEventListener('touchend', this.handleTouchEnd.bind(this), false)
    
    // 鼠标事件（兼容PC）
    this.element.addEventListener('mousedown', this.handleMouseDown.bind(this), false)
    document.addEventListener('mousemove', this.handleMouseMove.bind(this), false)
    document.addEventListener('mouseup', this.handleMouseUp.bind(this), false)
  }
  
  /**
   * 解绑事件
   */
  unbindEvents() {
    // 触摸事件
    this.element.removeEventListener('touchstart', this.handleTouchStart.bind(this), false)
    this.element.removeEventListener('touchmove', this.handleTouchMove.bind(this), false)
    this.element.removeEventListener('touchend', this.handleTouchEnd.bind(this), false)
    
    // 鼠标事件
    this.element.removeEventListener('mousedown', this.handleMouseDown.bind(this), false)
    document.removeEventListener('mousemove', this.handleMouseMove.bind(this), false)
    document.removeEventListener('mouseup', this.handleMouseUp.bind(this), false)
  }
  
  /**
   * 处理触摸开始事件
   */
  handleTouchStart(event) {
    const touch = event.touches[0]
    this.startGesture(touch.clientX, touch.clientY)
  }
  
  /**
   * 处理触摸移动事件
   */
  handleTouchMove(event) {
    if (this.state.startTime === 0) return
    
    const touch = event.touches[0]
    this.moveGesture(touch.clientX, touch.clientY)
    
    // 如果是长按，移动超过一定距离则取消长按
    if (this.longPressTimer) {
      const distance = this.getDistance(this.state.startX, this.state.startY, touch.clientX, touch.clientY)
      if (distance > 10) {
        this.cancelLongPress()
      }
    }
  }
  
  /**
   * 处理触摸结束事件
   */
  handleTouchEnd(event) {
    if (this.state.startTime === 0) return
    
    const touch = event.changedTouches[0]
    this.endGesture(touch.clientX, touch.clientY)
  }
  
  /**
   * 处理鼠标按下事件
   */
  handleMouseDown(event) {
    this.isMouseDown = true
    this.startGesture(event.clientX, event.clientY)
  }
  
  /**
   * 处理鼠标移动事件
   */
  handleMouseMove(event) {
    if (!this.isMouseDown || this.state.startTime === 0) return
    
    this.moveGesture(event.clientX, event.clientY)
    
    // 如果是长按，移动超过一定距离则取消长按
    if (this.longPressTimer) {
      const distance = this.getDistance(this.state.startX, this.state.startY, event.clientX, event.clientY)
      if (distance > 10) {
        this.cancelLongPress()
      }
    }
  }
  
  /**
   * 处理鼠标松开事件
   */
  handleMouseUp(event) {
    if (!this.isMouseDown || this.state.startTime === 0) return
    
    this.isMouseDown = false
    this.endGesture(event.clientX, event.clientY)
  }
  
  /**
   * 开始手势
   */
  startGesture(x, y) {
    this.state.startX = x
    this.state.startY = y
    this.state.startTime = Date.now()
    this.state.points = [{
      x,
      y,
      timestamp: this.state.startTime
    }]
    
    // 设置长按定时器
    this.longPressTimer = setTimeout(() => {
      this.triggerCallback('longPress', {
        x: this.state.startX,
        y: this.state.startY
      })
      this.longPressTimer = null
    }, 800)
  }
  
  /**
   * 移动手势
   */
  moveGesture(x, y) {
    this.state.endX = x
    this.state.endY = y
    this.state.endTime = Date.now()
    
    // 记录轨迹点
    this.state.points.push({
      x,
      y,
      timestamp: this.state.endTime
    })
  }
  
  /**
   * 结束手势
   */
  endGesture(x, y) {
    this.state.endX = x
    this.state.endY = y
    this.state.endTime = Date.now()
    
    // 取消长按定时器
    this.cancelLongPress()
    
    // 记录最后一个轨迹点
    this.state.points.push({
      x,
      y,
      timestamp: this.state.endTime
    })
    
    // 识别手势
    this.recognizeGesture()
    
    // 重置状态
    this.resetState()
  }
  
  /**
   * 取消长按
   */
  cancelLongPress() {
    if (this.longPressTimer) {
      clearTimeout(this.longPressTimer)
      this.longPressTimer = null
    }
  }
  
  /**
   * 重置状态
   */
  resetState() {
    this.state.startX = 0
    this.state.startY = 0
    this.state.endX = 0
    this.state.endY = 0
    this.state.startTime = 0
    this.state.endTime = 0
    this.state.points = []
  }
  
  /**
   * 识别手势
   */
  recognizeGesture() {
    const { startX, startY, endX, endY, startTime, endTime, points } = this.state
    const duration = endTime - startTime
    const distance = this.getDistance(startX, startY, endX, endY)
    
    // 如果时间太长或距离太短，可能不是滑动手势
    if (duration > this.options.maxTime || distance < this.options.minDistance) {
      // 可能是点击
      if (distance < 10 && duration < 300) {
        // 检查是否是双击
        const now = Date.now()
        if (now - this.lastTapTime < 300) {
          // 双击
          this.triggerCallback('doubleTap', {
            x: endX,
            y: endY
          })
          this.lastTapTime = 0 // 重置，避免连续触发
        } else {
          // 单击
          this.lastTapTime = now
          this.triggerCallback('tap', {
            x: endX,
            y: endY
          })
        }
      }
      return
    }
    
    // 计算角度
    const angle = this.getAngle(startX, startY, endX, endY)
    
    // 判断滑动方向
    if (angle >= 45 && angle < 135) {
      // 向下滑动
      this.triggerCallback('swipeDown', {
        distance,
        duration
      })
    } else if (angle >= 135 && angle < 225) {
      // 向左滑动
      this.triggerCallback('swipeLeft', {
        distance,
        duration
      })
    } else if (angle >= 225 && angle < 315) {
      // 向上滑动
      this.triggerCallback('swipeUp', {
        distance,
        duration
      })
    } else {
      // 向右滑动
      this.triggerCallback('swipeRight', {
        distance,
        duration
      })
    }
    
    // 检查是否是复杂手势
    this.recognizeComplexGesture(points)
  }
  
  /**
   * 识别复杂手势
   */
  recognizeComplexGesture(points) {
    if (points.length < 10) return
    
    // 计算轨迹长度
    let pathLength = 0
    for (let i = 1; i < points.length; i++) {
      pathLength += this.getDistance(points[i-1].x, points[i-1].y, points[i].x, points[i].y)
    }
    
    // 计算起点和终点的直线距离
    const directDistance = this.getDistance(points[0].x, points[0].y, points[points.length-1].x, points[points.length-1].y)
    
    // 计算曲直比（轨迹长度 / 直线距离）
    const curvature = pathLength / directDistance
    
    // 判断是否为圆形手势
    if (curvature > 3.0 && directDistance / pathLength < 0.3) {
      this.triggerCallback('circle', {
        points,
        curvature
      })
      return
    }
    
    // 判断是否为Z形手势
    if (curvature > 1.5 && curvature < 3.0) {
      // 检查方向变化
      const directions = this.getDirectionChanges(points)
      if (directions >= 2) {
        this.triggerCallback('zShape', {
          points,
          curvature
        })
      }
    }
  }
  
  /**
   * 获取方向变化次数
   */
  getDirectionChanges(points) {
    if (points.length < 3) return 0
    
    let changes = 0
    let prevDirection = null
    
    for (let i = 1; i < points.length; i++) {
      const dx = points[i].x - points[i-1].x
      const dy = points[i].y - points[i-1].y
      
      let direction
      if (Math.abs(dx) > Math.abs(dy)) {
        direction = dx > 0 ? 'right' : 'left'
      } else {
        direction = dy > 0 ? 'down' : 'up'
      }
      
      if (prevDirection && direction !== prevDirection) {
        changes++
      }
      
      prevDirection = direction
    }
    
    return changes
  }
  
  /**
   * 计算两点之间的距离
   */
  getDistance(x1, y1, x2, y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
  }
  
  /**
   * 计算角度
   */
  getAngle(x1, y1, x2, y2) {
    const dx = x2 - x1
    const dy = y2 - y1
    
    // 计算角度（弧度）
    let angle = Math.atan2(dy, dx)
    
    // 转换为角度
    angle = angle * 180 / Math.PI
    
    // 将角度转换为0-360度
    if (angle < 0) {
      angle += 360
    }
    
    return angle
  }
  
  /**
   * 触发回调
   */
  triggerCallback(type, data) {
    if (this.callbacks[type]) {
      this.callbacks[type](data)
    }
  }
  
  /**
   * 注册手势回调
   */
  on(type, callback) {
    if (typeof callback === 'function' && this.callbacks.hasOwnProperty(type)) {
      this.callbacks[type] = callback
    }
    return this
  }
  
  /**
   * 移除手势回调
   */
  off(type) {
    if (this.callbacks.hasOwnProperty(type)) {
      this.callbacks[type] = null
    }
    return this
  }
  
  /**
   * 销毁实例
   */
  destroy() {
    this.unbindEvents()
    this.cancelLongPress()
    this.callbacks = {}
  }
}
