<template>
  <div class="gesture-container">
    <el-card class="gesture-card">
      <template #header>
        <div class="card-header">
          <h2>手势交互示例</h2>
        </div>
      </template>
      
      <div class="gesture-content">
        <div class="gesture-area" ref="gestureArea">
          <div class="gesture-hint">
            <el-icon v-if="!isDrawing"><TouchScreen /></el-icon>
            <span>{{ hintText }}</span>
          </div>
          
          <canvas ref="canvas" class="gesture-canvas"></canvas>
          
          <div class="gesture-result" v-if="gestureResult">
            <div class="result-title">识别结果：</div>
            <div class="result-content">
              <el-tag :type="getTagType(gestureResult.type)">{{ getGestureName(gestureResult.type) }}</el-tag>
              <div class="result-confidence">置信度：{{ (gestureResult.confidence * 100).toFixed(0) }}%</div>
            </div>
          </div>
        </div>
        
        <div class="gesture-info">
          <div class="info-title">支持的手势：</div>
          <div class="gesture-list">
            <div v-for="(gesture, index) in supportedGestures" :key="index" class="gesture-item">
              <el-tag :type="getTagType(gesture.type)">{{ gesture.name }}</el-tag>
              <div class="gesture-desc">{{ gesture.description }}</div>
              <div class="gesture-action">动作：{{ gesture.action }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="demo-card">
      <template #header>
        <div class="card-header">
          <h2>手势应用示例</h2>
        </div>
      </template>
      
      <div class="demo-content">
        <div class="demo-area" ref="demoArea">
          <div class="demo-hint">
            <el-icon><TouchScreen /></el-icon>
            <span>在此区域尝试手势操作</span>
          </div>
          
          <div class="demo-element" ref="demoElement" :style="demoElementStyle">
            <img src="../../assets/images/logo.png" alt="Logo" />
          </div>
        </div>
        
        <div class="demo-info">
          <div class="info-title">手势操作说明：</div>
          <ul class="operation-list">
            <li><strong>左右滑动：</strong> 水平移动元素</li>
            <li><strong>上下滑动：</strong> 垂直移动元素</li>
            <li><strong>画圆：</strong> 旋转元素</li>
            <li><strong>Z形：</strong> 重置元素位置</li>
            <li><strong>双击：</strong> 放大/缩小元素</li>
            <li><strong>长按：</strong> 显示操作菜单</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { TouchScreen } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import GestureRecognizer from '../../utils/gesture'

// 状态
const gestureArea = ref(null)
const canvas = ref(null)
const demoArea = ref(null)
const demoElement = ref(null)
const isDrawing = ref(false)
const hintText = ref('在此区域绘制手势')
const gestureResult = ref(null)
const ctx = ref(null)
const points = ref([])
const gestureRecognizer = ref(null)
const demoGestureRecognizer = ref(null)

// 元素样式
const demoElementStyle = reactive({
  left: '50%',
  top: '50%',
  transform: 'translate(-50%, -50%) rotate(0deg)',
  scale: '1'
})

// 支持的手势
const supportedGestures = [
  { type: 'SWIPE_LEFT', name: '左滑', description: '从右向左滑动', action: '下一页' },
  { type: 'SWIPE_RIGHT', name: '右滑', description: '从左向右滑动', action: '上一页' },
  { type: 'SWIPE_UP', name: '上滑', description: '从下向上滑动', action: '刷新' },
  { type: 'SWIPE_DOWN', name: '下滑', description: '从上向下滑动', action: '加载更多' },
  { type: 'CIRCLE', name: '画圆', description: '画一个圆形', action: '旋转' },
  { type: 'Z_SHAPE', name: 'Z形', description: '画一个Z形', action: '撤销' }
]

// 方法
function initCanvas() {
  const canvasEl = canvas.value
  const container = gestureArea.value
  
  // 设置画布大小
  canvasEl.width = container.clientWidth
  canvasEl.height = container.clientHeight
  
  // 获取上下文
  ctx.value = canvasEl.getContext('2d')
  ctx.value.lineWidth = 3
  ctx.value.lineCap = 'round'
  ctx.value.lineJoin = 'round'
  ctx.value.strokeStyle = '#409EFF'
}

// 开始绘制
function startDrawing(x, y) {
  isDrawing.value = true
  hintText.value = '正在绘制...'
  gestureResult.value = null
  
  // 清空画布
  clearCanvas()
  
  // 记录起点
  points.value = [{
    x,
    y,
    timestamp: Date.now()
  }]
  
  // 开始绘制
  ctx.value.beginPath()
  ctx.value.moveTo(x, y)
}

// 绘制中
function draw(x, y) {
  if (!isDrawing.value) return
  
  // 记录点
  points.value.push({
    x,
    y,
    timestamp: Date.now()
  })
  
  // 绘制线条
  ctx.value.lineTo(x, y)
  ctx.value.stroke()
}

// 结束绘制
function stopDrawing() {
  if (!isDrawing.value) return
  
  isDrawing.value = false
  hintText.value = '在此区域绘制手势'
  
  // 识别手势
  recognizeGesture()
}

// 清空画布
function clearCanvas() {
  ctx.value.clearRect(0, 0, canvas.value.width, canvas.value.height)
}

// 识别手势
async function recognizeGesture() {
  if (points.value.length < 2) return
  
  try {
    // 调用后端API识别手势
    // 这里模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 根据轨迹特征判断手势类型
    const result = simulateGestureRecognition(points.value)
    
    gestureResult.value = result
  } catch (error) {
    console.error('手势识别失败:', error)
    ElMessage.error('手势识别失败')
  }
}

// 模拟手势识别
function simulateGestureRecognition(points) {
  // 计算起点和终点
  const start = points[0]
  const end = points[points.length - 1]
  
  // 计算轨迹长度
  let pathLength = 0
  for (let i = 1; i < points.length; i++) {
    pathLength += getDistance(points[i-1].x, points[i-1].y, points[i].x, points[i].y)
  }
  
  // 计算直线距离
  const directDistance = getDistance(start.x, start.y, end.x, end.y)
  
  // 计算曲直比（轨迹长度 / 直线距离）
  const curvature = pathLength / directDistance
  
  // 计算方向
  const angle = getAngle(start.x, start.y, end.x, end.y)
  
  // 判断手势类型
  if (curvature < 1.5) {
    // 滑动手势
    if (angle >= 45 && angle < 135) {
      return { type: 'SWIPE_DOWN', confidence: 0.9 }
    } else if (angle >= 135 && angle < 225) {
      return { type: 'SWIPE_LEFT', confidence: 0.9 }
    } else if (angle >= 225 && angle < 315) {
      return { type: 'SWIPE_UP', confidence: 0.9 }
    } else {
      return { type: 'SWIPE_RIGHT', confidence: 0.9 }
    }
  } else if (curvature > 3.0 && directDistance / pathLength < 0.3) {
    // 圆形手势
    return { type: 'CIRCLE', confidence: 0.8 }
  } else if (curvature > 1.5 && curvature < 3.0) {
    // Z形手势
    return { type: 'Z_SHAPE', confidence: 0.7 }
  } else {
    // 未知手势
    return { type: 'UNKNOWN', confidence: 0.5 }
  }
}

// 计算两点之间的距离
function getDistance(x1, y1, x2, y2) {
  return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
}

// 计算角度
function getAngle(x1, y1, x2, y2) {
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

// 获取手势名称
function getGestureName(type) {
  const gesture = supportedGestures.find(g => g.type === type)
  return gesture ? gesture.name : '未知手势'
}

// 获取标签类型
function getTagType(type) {
  switch (type) {
    case 'SWIPE_LEFT':
    case 'SWIPE_RIGHT':
      return 'primary'
    case 'SWIPE_UP':
    case 'SWIPE_DOWN':
      return 'success'
    case 'CIRCLE':
      return 'warning'
    case 'Z_SHAPE':
      return 'danger'
    default:
      return 'info'
  }
}

// 初始化手势识别器
function initGestureRecognizer() {
  // 初始化手势绘制区域
  gestureArea.value.addEventListener('mousedown', handleMouseDown)
  gestureArea.value.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  
  gestureArea.value.addEventListener('touchstart', handleTouchStart, { passive: false })
  gestureArea.value.addEventListener('touchmove', handleTouchMove, { passive: false })
  gestureArea.value.addEventListener('touchend', handleTouchEnd)
  
  // 初始化演示区域手势识别器
  demoGestureRecognizer.value = new GestureRecognizer()
  demoGestureRecognizer.value.init(demoArea.value)
    .on('swipeLeft', handleSwipeLeft)
    .on('swipeRight', handleSwipeRight)
    .on('swipeUp', handleSwipeUp)
    .on('swipeDown', handleSwipeDown)
    .on('circle', handleCircle)
    .on('zShape', handleZShape)
    .on('doubleTap', handleDoubleTap)
    .on('longPress', handleLongPress)
}

// 鼠标事件处理
function handleMouseDown(event) {
  const rect = canvas.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  startDrawing(x, y)
}

function handleMouseMove(event) {
  const rect = canvas.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  draw(x, y)
}

function handleMouseUp() {
  stopDrawing()
}

// 触摸事件处理
function handleTouchStart(event) {
  event.preventDefault()
  const touch = event.touches[0]
  const rect = canvas.value.getBoundingClientRect()
  const x = touch.clientX - rect.left
  const y = touch.clientY - rect.top
  startDrawing(x, y)
}

function handleTouchMove(event) {
  event.preventDefault()
  const touch = event.touches[0]
  const rect = canvas.value.getBoundingClientRect()
  const x = touch.clientX - rect.left
  const y = touch.clientY - rect.top
  draw(x, y)
}

function handleTouchEnd() {
  stopDrawing()
}

// 演示区域手势处理
function handleSwipeLeft() {
  // 向左移动元素
  const currentLeft = parseFloat(demoElementStyle.left)
  demoElementStyle.left = Math.max(10, currentLeft - 50) + '%'
  showGestureToast('左滑')
}

function handleSwipeRight() {
  // 向右移动元素
  const currentLeft = parseFloat(demoElementStyle.left)
  demoElementStyle.left = Math.min(90, currentLeft + 50) + '%'
  showGestureToast('右滑')
}

function handleSwipeUp() {
  // 向上移动元素
  const currentTop = parseFloat(demoElementStyle.top)
  demoElementStyle.top = Math.max(10, currentTop - 50) + '%'
  showGestureToast('上滑')
}

function handleSwipeDown() {
  // 向下移动元素
  const currentTop = parseFloat(demoElementStyle.top)
  demoElementStyle.top = Math.min(90, currentTop + 50) + '%'
  showGestureToast('下滑')
}

function handleCircle() {
  // 旋转元素
  const transform = demoElementStyle.transform
  const currentRotation = transform.match(/rotate\(([^)]+)\)/)
  let rotation = 0
  
  if (currentRotation) {
    rotation = parseFloat(currentRotation[1]) || 0
  }
  
  rotation += 90
  
  demoElementStyle.transform = transform.replace(/rotate\([^)]*\)/, `rotate(${rotation}deg)`)
  showGestureToast('画圆')
}

function handleZShape() {
  // 重置元素位置
  demoElementStyle.left = '50%'
  demoElementStyle.top = '50%'
  demoElementStyle.transform = 'translate(-50%, -50%) rotate(0deg)'
  demoElementStyle.scale = '1'
  showGestureToast('Z形')
}

function handleDoubleTap() {
  // 放大/缩小元素
  const currentScale = parseFloat(demoElementStyle.scale) || 1
  demoElementStyle.scale = currentScale === 1 ? '1.5' : '1'
  showGestureToast('双击')
}

function handleLongPress() {
  // 显示操作菜单
  ElMessageBox.alert('这是一个长按操作菜单示例', '长按菜单', {
    confirmButtonText: '确定'
  })
  showGestureToast('长按')
}

// 显示手势提示
function showGestureToast(gestureName) {
  ElMessage({
    message: `检测到${gestureName}手势`,
    type: 'success',
    duration: 1000
  })
}

// 生命周期钩子
onMounted(() => {
  initCanvas()
  initGestureRecognizer()
  
  // 监听窗口大小变化
  window.addEventListener('resize', initCanvas)
})

onUnmounted(() => {
  // 移除事件监听
  gestureArea.value.removeEventListener('mousedown', handleMouseDown)
  gestureArea.value.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  gestureArea.value.removeEventListener('touchstart', handleTouchStart)
  gestureArea.value.removeEventListener('touchmove', handleTouchMove)
  gestureArea.value.removeEventListener('touchend', handleTouchEnd)
  
  // 销毁手势识别器
  if (demoGestureRecognizer.value) {
    demoGestureRecognizer.value.destroy()
  }
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', initCanvas)
})
</script>

<style lang="scss" scoped>
.gesture-container {
  max-width: 1200px;
  margin: 0 auto;
  
  .gesture-card,
  .demo-card {
    margin-bottom: 20px;
    
    .card-header {
      h2 {
        margin: 0;
        font-size: 20px;
      }
    }
  }
  
  .gesture-content,
  .demo-content {
    display: flex;
    gap: 20px;
    
    @media (max-width: 768px) {
      flex-direction: column;
    }
  }
  
  .gesture-area,
  .demo-area {
    flex: 1;
    height: 300px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    position: relative;
    overflow: hidden;
  }
  
  .gesture-hint,
  .demo-hint {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: #909399;
    pointer-events: none;
    
    .el-icon {
      font-size: 24px;
      margin-bottom: 10px;
      display: block;
    }
  }
  
  .gesture-canvas {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
  
  .gesture-result {
    position: absolute;
    bottom: 10px;
    left: 10px;
    background-color: rgba(255, 255, 255, 0.9);
    padding: 10px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .result-title {
      font-weight: bold;
      margin-bottom: 5px;
    }
    
    .result-content {
      display: flex;
      align-items: center;
      
      .result-confidence {
        margin-left: 10px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .gesture-info,
  .demo-info {
    width: 300px;
    
    @media (max-width: 768px) {
      width: 100%;
    }
    
    .info-title {
      font-weight: bold;
      margin-bottom: 15px;
    }
    
    .gesture-list {
      .gesture-item {
        margin-bottom: 15px;
        
        .gesture-desc {
          margin: 5px 0;
          font-size: 14px;
          color: #606266;
        }
        
        .gesture-action {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .operation-list {
      padding-left: 20px;
      
      li {
        margin-bottom: 10px;
      }
    }
  }
  
  .demo-element {
    position: absolute;
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
}
</style>
