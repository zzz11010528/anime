<template>
  <div class="gesture-image-viewer">
    <div class="controls">
      <div class="camera-status">
        <el-tag v-if="cameraReady" type="success">摄像头已就绪</el-tag>
        <el-tag v-else type="warning">摄像头未就绪</el-tag>
        <el-switch
          v-model="cameraEnabled"
          active-text="摄像头开启"
          inactive-text="摄像头关闭"
        />
        <el-switch
          v-model="showHandMarkers"
          active-text="显示手部标记"
          inactive-text="隐藏手部标记"
        />
      </div>
      <div class="control-options">
        <el-button type="primary" @click="resetImage">
          <el-icon><Refresh /></el-icon> 重置图片
        </el-button>
        <el-button type="danger" @click="$emit('close')">
          <el-icon><Close /></el-icon> 关闭
        </el-button>
      </div>
    </div>

    <div class="viewer-container">
      <div ref="imageContainer" class="image-container">
        <img
          ref="viewerImage"
          :src="currentImage"
          :style="imageStyle"
          @load="onImageLoad"
        />
      </div>

      <div v-if="cameraEnabled" class="camera-container">
        <video
          ref="videoElement"
          class="camera-feed"
          autoplay
          muted
          playsinline
        ></video>
        <canvas ref="canvasElement" class="hand-canvas"></canvas>

        <!-- 手势状态指示器 -->
        <div class="gesture-indicator" v-if="handState.gestureDetected">
          <div class="gesture-name">
            <span v-if="handState.gestureDetected === 'ok'">👌 点击</span>
            <span v-else-if="handState.gestureDetected === 'open_palm'">✋ 移动</span>
            <span v-else-if="handState.gestureDetected === 'pinch'">✌️ 缩放</span>
            <span v-else-if="handState.gestureDetected === 'rotate'">👆 旋转</span>
            <span v-else-if="handState.gestureDetected === 'swipe'">👉 滑动</span>
          </div>
        </div>
      </div>
    </div>

    <div class="gesture-guide">
      <h3>手势指南</h3>
      <ul>
        <li>
          <div class="gesture-icon">👌</div>
          <div class="gesture-desc">
            <strong>OK手势</strong>
            <span>拇指和食指形成圆圈，触发点击</span>
          </div>
        </li>
        <li>
          <div class="gesture-icon">✋</div>
          <div class="gesture-desc">
            <strong>张开手掌</strong>
            <span>五指张开，移动手掌来平移图片</span>
          </div>
        </li>
        <li>
          <div class="gesture-icon">✌️</div>
          <div class="gesture-desc">
            <strong>两指张开</strong>
            <span>食指和中指伸直，其他手指弯曲，调整两指距离来缩放图片</span>
          </div>
        </li>
        <li>
          <div class="gesture-icon">👆</div>
          <div class="gesture-desc">
            <strong>食指指向</strong>
            <span>只伸出食指，左右移动来旋转图片</span>
          </div>
        </li>
        <li>
          <div class="gesture-icon">👉</div>
          <div class="gesture-desc">
            <strong>食指横向</strong>
            <span>食指水平伸出，向左/右滑动切换图片</span>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { Hands, drawConnectors, drawLandmarks, createHands, createCamera } from '../utils/mediapipe';
import { ElMessage } from 'element-plus';

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['update:index', 'click', 'close']);

// 状态变量
const cameraEnabled = ref(true); // 默认开启摄像头
const showHandMarkers = ref(true);
const cameraReady = ref(false); // 摄像头是否已就绪
const currentIndex = ref(props.initialIndex);
const videoElement = ref(null);
const canvasElement = ref(null);
const imageContainer = ref(null);
const viewerImage = ref(null);

// 图片变换状态
const imageTransform = ref({
  scale: 1,
  rotation: 0,
  translateX: 0,
  translateY: 0
});

// 手势状态
const handState = ref({
  previousLandmarks: null,
  gestureDetected: null,
  gestureStartTime: null,
  pinchStartDistance: 0,
  rotateStartAngle: 0,
  moveStartPosition: { x: 0, y: 0 },
  isGrabbing: false
});

// 计算当前图片URL
const currentImage = computed(() => {
  if (!props.images.length) return '';
  return props.images[currentIndex.value];
});

// 计算图片样式
const imageStyle = computed(() => {
  const { scale, rotation, translateX, translateY } = imageTransform.value;
  return {
    transform: `translate(${translateX}px, ${translateY}px) scale(${scale}) rotate(${rotation}deg)`,
    transition: 'transform 0.1s ease'
  };
});

// 监听图片索引变化
watch(() => currentIndex.value, (newIndex) => {
  emit('update:index', newIndex);
});

// 重置图片变换
function resetImage() {
  imageTransform.value = {
    scale: 1,
    rotation: 0,
    translateX: 0,
    translateY: 0
  };
}

// 图片加载完成
function onImageLoad() {
  resetImage();
}

// 切换到下一张图片
function nextImage() {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++;
  }
}

// 切换到上一张图片
function prevImage() {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
}

// 手势处理函数
function onHandResults(results) {
  if (!canvasElement.value) return;

  const canvasCtx = canvasElement.value.getContext('2d');
  if (!canvasCtx) return;

  // 清除画布
  canvasCtx.clearRect(0, 0, canvasElement.value.width, canvasElement.value.height);

  try {
    // 如果没有检测到手，重置状态
    if (!results.multiHandLandmarks || results.multiHandLandmarks.length === 0) {
      handState.value.previousLandmarks = null;
      handState.value.gestureDetected = null;
      return;
    }

    // 绘制手部标记
    if (showHandMarkers.value) {
      try {
        for (const landmarks of results.multiHandLandmarks) {
          // 使用更明显的颜色和更粗的线条
          drawConnectors(canvasCtx, landmarks, Hands.HAND_CONNECTIONS, { color: '#00FF00', lineWidth: 8 });
          drawLandmarks(canvasCtx, landmarks, { color: '#FF0000', lineWidth: 4 });
        }
        console.log('手部标记已绘制');
      } catch (error) {
        console.error('绘制手部标记失败:', error);
      }
    }

    // 处理第一只检测到的手
    const landmarks = results.multiHandLandmarks[0];
    if (!landmarks) return;

    // 识别手势并执行相应操作
    detectAndHandleGestures(landmarks);

    // 更新上一帧的手部标记
    handState.value.previousLandmarks = landmarks;
  } catch (error) {
    console.error('手势处理错误:', error);
  }
}

// 识别手势并处理
function detectAndHandleGestures(landmarks) {
  // 检查所有关键点是否存在
  if (!landmarks[0] || !landmarks[4] || !landmarks[8] || !landmarks[12] || !landmarks[16] || !landmarks[20]) {
    console.warn('手部关键点不完整');
    return;
  }

  // 提取关键点
  const thumb = landmarks[4];  // 拇指尖
  const indexFinger = landmarks[8];  // 食指尖
  const middleFinger = landmarks[12];  // 中指尖
  const ringFinger = landmarks[16];  // 无名指尖
  const pinky = landmarks[20];  // 小指尖
  const wrist = landmarks[0];  // 手腕

  // 计算手指间距离
  const thumbIndexDistance = getDistance(thumb, indexFinger);
  const indexMiddleDistance = getDistance(indexFinger, middleFinger);

  // 检测OK手势（拇指和食指形成圆圈）
  if (thumbIndexDistance < 0.15) { // 增大阈值，使OK手势更容易被检测到
    if (handState.value.gestureDetected !== 'ok') {
      handState.value.gestureDetected = 'ok';
      handState.value.gestureStartTime = Date.now();
    } else if (Date.now() - handState.value.gestureStartTime > 300) { // 减少持续时间要求
      // 持续OK手势超过300ms，触发点击
      emit('click');
      handState.value.gestureStartTime = Date.now(); // 重置计时器
    }
    return;
  }

  // 检测张开手掌（所有手指伸直）
  const fingersExtended =
    indexFinger.y < wrist.y &&
    middleFinger.y < wrist.y &&
    (ringFinger.y < wrist.y || pinky.y < wrist.y); // 放宽条件，只要食指、中指和至少一个其他手指伸直即可

  if (fingersExtended) {
    if (handState.value.gestureDetected !== 'open_palm') {
      handState.value.gestureDetected = 'open_palm';
      handState.value.moveStartPosition = { x: wrist.x, y: wrist.y };
    } else if (handState.value.previousLandmarks) {
      // 计算手腕移动距离，增强移动效果
      const deltaX = (wrist.x - handState.value.moveStartPosition.x) * 1500; // 增强水平移动效果
      const deltaY = (wrist.y - handState.value.moveStartPosition.y) * 1500; // 增强垂直移动效果

      // 更新图片位置
      imageTransform.value.translateX += deltaX;
      imageTransform.value.translateY += deltaY;

      // 更新起始位置
      handState.value.moveStartPosition = { x: wrist.x, y: wrist.y };
    }
    return;
  }

  // 检测食指指向（旋转手势）- 完全重写旋转检测逻辑
  // 只检测食指伸出，其他手指弯曲的情况
  const isOnlyIndexFingerUp =
    indexFinger.y < wrist.y - 0.05 && // 确保食指明显伸出
    middleFinger.y > indexFinger.y + 0.03 && // 确保中指明显低于食指
    ringFinger.y > indexFinger.y + 0.03 && // 确保无名指明显低于食指
    pinky.y > indexFinger.y + 0.03; // 确保小指明显低于食指

  // 检测食指是否水平伸出（与旋转区分）
  const isIndexFingerVertical = Math.abs(indexFinger.x - wrist.x) < 0.15;

  if (isOnlyIndexFingerUp && isIndexFingerVertical) {
    console.log('检测到旋转手势 - 食指指向');

    if (handState.value.gestureDetected !== 'rotate') {
      // 首次检测到旋转手势
      handState.value.gestureDetected = 'rotate';
      handState.value.rotateStartAngle = Math.atan2(indexFinger.y - wrist.y, indexFinger.x - wrist.x);
      console.log('旋转手势初始化', handState.value.rotateStartAngle);

      // 显示提示
      ElMessage.info('检测到旋转手势');
    } else {
      // 持续检测到旋转手势
      // 计算角度变化
      const currentAngle = Math.atan2(indexFinger.y - wrist.y, indexFinger.x - wrist.x);
      const angleDiff = (currentAngle - handState.value.rotateStartAngle) * (180 / Math.PI);

      console.log('旋转角度差:', angleDiff);

      // 使用更小的阈值检测旋转
      if (Math.abs(angleDiff) > 0.2) { // 进一步降低阈值
        // 增强旋转效果
        const enhancedAngleDiff = angleDiff * 3.0; // 大幅增强旋转效果
        console.log('应用旋转:', enhancedAngleDiff);

        imageTransform.value.rotation += enhancedAngleDiff;
        handState.value.rotateStartAngle = currentAngle;
      }
    }
    return;
  }

  // 检测两指张开（缩放手势）- 完全重写缩放检测逻辑
  const isTwoFingersExtended =
    indexFinger.y < wrist.y &&
    middleFinger.y < wrist.y &&
    Math.abs(indexFinger.y - middleFinger.y) < 0.1; // 食指和中指高度接近

  const isOtherFingersCurled =
    ringFinger.y > middleFinger.y * 1.1 ||
    pinky.y > middleFinger.y * 1.1;

  // 检测"剪刀手"姿势
  if (isTwoFingersExtended && isOtherFingersCurled) {
    console.log('检测到缩放手势', indexMiddleDistance);

    if (handState.value.gestureDetected !== 'pinch') {
      // 首次检测到缩放手势
      handState.value.gestureDetected = 'pinch';
      handState.value.pinchStartDistance = indexMiddleDistance;
      console.log('缩放手势初始化', indexMiddleDistance);

      // 显示提示
      ElMessage.info('检测到缩放手势');
    } else if (handState.value.previousLandmarks) {
      // 持续检测到缩放手势
      // 计算缩放比例
      const scaleFactor = indexMiddleDistance / handState.value.pinchStartDistance;

      console.log('缩放比例:', scaleFactor, '当前距离:', indexMiddleDistance, '起始距离:', handState.value.pinchStartDistance);

      // 使用更小的阈值检测缩放
      if (Math.abs(scaleFactor - 1) > 0.003) { // 进一步降低阈值
        // 增强缩放效果
        const enhancedScaleFactor = 1 + (scaleFactor - 1) * 2.5; // 大幅增强缩放效果
        console.log('应用缩放:', enhancedScaleFactor);

        imageTransform.value.scale *= enhancedScaleFactor;
        // 限制缩放范围
        imageTransform.value.scale = Math.max(0.2, Math.min(8, imageTransform.value.scale));

        // 更新起始距离
        handState.value.pinchStartDistance = indexMiddleDistance;
      }
    }
    return;
  }

  // 检测食指横向（滑动手势）- 完全重写滑动检测逻辑
  // 检测食指水平伸出，其他手指弯曲的情况
  const isIndexFingerHorizontal =
    indexFinger.y < wrist.y && // 食指伸出
    Math.abs(indexFinger.x - wrist.x) > 0.15 && // 食指明显偏向一侧
    Math.abs(indexFinger.y - wrist.y) < 0.15; // 食指不要太高

  // 确保其他手指弯曲
  const areOtherFingersCurledForSwipe =
    middleFinger.y > indexFinger.y + 0.02 &&
    ringFinger.y > indexFinger.y + 0.02 &&
    pinky.y > indexFinger.y + 0.02;

  if (isIndexFingerHorizontal && areOtherFingersCurledForSwipe) {
    console.log('检测到滑动手势 - 食指横向');

    if (handState.value.gestureDetected !== 'swipe') {
      // 首次检测到滑动手势
      handState.value.gestureDetected = 'swipe';
      handState.value.moveStartPosition = { x: indexFinger.x, y: indexFinger.y };
      handState.value.gestureStartTime = Date.now();
      console.log('滑动手势初始化', indexFinger.x);

      // 显示提示
      ElMessage.info('检测到滑动手势');
    } else {
      // 持续检测到滑动手势
      const currentTime = Date.now();
      const deltaTime = currentTime - handState.value.gestureStartTime;

      // 减少等待时间，使滑动更快响应
      if (deltaTime > 100) {
        // 计算水平移动距离
        const startX = handState.value.moveStartPosition.x;
        const currentX = indexFinger.x;
        const deltaX = currentX - startX;

        console.log('滑动距离:', deltaX, '开始位置:', startX, '当前位置:', currentX);

        // 使用更小的阈值检测滑动
        if (Math.abs(deltaX) > 0.02) { // 进一步降低阈值
          if (deltaX > 0) {
            console.log('向右滑动 - 下一张');
            nextImage();
            ElMessage.success('下一张');
          } else {
            console.log('向左滑动 - 上一张');
            prevImage();
            ElMessage.success('上一张');
          }

          // 重置状态，防止连续触发
          handState.value.gestureStartTime = currentTime;
          handState.value.moveStartPosition = { x: indexFinger.x, y: indexFinger.y };

          // 添加短暂延迟，防止连续触发
          setTimeout(() => {
            handState.value.gestureDetected = 'swipe_cooldown';
            setTimeout(() => {
              if (handState.value.gestureDetected === 'swipe_cooldown') {
                handState.value.gestureDetected = null;
              }
            }, 500);
          }, 0);
        }
      }
    }
    return;
  }

  // 如果没有检测到任何已知手势，重置状态
  handState.value.gestureDetected = null;
}

// 计算两点之间的距离
function getDistance(a, b) {
  return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
}

// 初始化摄像头和手势识别
function initCamera() {
  if (!videoElement.value) return;

  // 设置画布尺寸
  canvasElement.value.width = videoElement.value.width;
  canvasElement.value.height = videoElement.value.height;

  // 初始化手势识别 - 使用统一的 MediaPipe 工具
  const hands = createHands({
    maxNumHands: 1,
    modelComplexity: 1,
    minDetectionConfidence: 0.3, // 降低检测置信度阈值，使手势更容易被检测到
    minTrackingConfidence: 0.3   // 降低跟踪置信度阈值，使手势跟踪更连续
  });

  hands.onResults(onHandResults);

  // 初始化摄像头 - 使用统一的 MediaPipe 工具
  const camera = createCamera(videoElement.value, {
    onFrame: async () => {
      try {
        await hands.send({ image: videoElement.value });
      } catch (error) {
        console.error('手势识别错误:', error);
      }
    },
    width: 640,
    height: 480
  });

  // 启动摄像头
  camera.start()
    .then(() => {
      console.log('摄像头启动成功');
      cameraReady.value = true;
    })
    .catch(error => {
      console.error('摄像头启动失败:', error);
      cameraEnabled.value = false;
      ElMessage.error('摄像头启动失败，请检查权限设置');
    });

  return { hands, camera };
}

// 组件挂载时初始化
let cameraInstance = null;
onMounted(() => {
  // 组件挂载后立即初始化摄像头
  setTimeout(() => {
    if (cameraEnabled.value) {
      cameraInstance = initCamera();
    }
  }, 500); // 延迟500ms确保DOM已完全加载

  // 监听摄像头开关
  watch(cameraEnabled, (enabled) => {
    if (enabled && !cameraInstance) {
      cameraInstance = initCamera();
    } else if (!enabled && cameraInstance) {
      // 关闭摄像头
      if (cameraInstance.camera) {
        cameraInstance.camera.stop();
      }
      if (cameraInstance.hands) {
        cameraInstance.hands.close();
      }
      cameraInstance = null;
      cameraReady.value = false; // 重置摄像头就绪状态
    }
  });
});

// 组件卸载时清理资源
onUnmounted(() => {
  if (cameraInstance && cameraInstance.camera) {
    cameraInstance.camera.stop();
  }
  if (cameraInstance && cameraInstance.hands) {
    cameraInstance.hands.close();
  }
});
</script>

<style lang="scss" scoped>
.gesture-image-viewer {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
  padding: 20px;
  box-sizing: border-box;

  .controls {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-bottom: 20px;

    .camera-status {
      display: flex;
      align-items: center;
      gap: 15px;
      justify-content: center;
      padding: 10px;
      background-color: rgba(255, 255, 255, 0.1);
      border-radius: 8px;

      .el-tag {
        padding: 8px 12px;
        font-size: 14px;
      }
    }

    .control-options {
      display: flex;
      gap: 15px;
      justify-content: center;
      flex-wrap: wrap;
    }
  }

  .viewer-container {
    position: relative;
    width: 100%;
    height: calc(100vh - 200px);
    overflow: hidden;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 8px;

    .image-container {
      width: 100%;
      height: 100%;
      display: flex;
      justify-content: center;
      align-items: center;

      img {
        max-width: 90%;
        max-height: 90%;
        object-fit: contain;
        user-select: none;
      }
    }

    .camera-container {
      position: absolute;
      bottom: 20px;
      right: 20px;
      width: 200px;
      height: 150px;
      border-radius: 12px;
      overflow: hidden;
      border: 3px solid #00FF00;
      box-shadow: 0 4px 20px rgba(0, 255, 0, 0.3);
      z-index: 100;

      .camera-feed {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .hand-canvas {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
      }

      .gesture-indicator {
        position: absolute;
        top: 10px;
        left: 10px;
        background-color: rgba(0, 0, 0, 0.7);
        border-radius: 8px;
        padding: 5px 10px;
        color: #fff;
        font-weight: bold;
        font-size: 16px;
        z-index: 101;

        .gesture-name {
          display: flex;
          align-items: center;
          justify-content: center;

          span {
            display: flex;
            align-items: center;

            &:before {
              margin-right: 5px;
              font-size: 20px;
            }
          }
        }
      }
    }
  }

  .gesture-guide {
    margin-top: 20px;
    padding: 20px;
    background-color: rgba(0, 0, 0, 0.7);
    border-radius: 12px;
    color: #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);

    h3 {
      margin-top: 0;
      margin-bottom: 20px;
      text-align: center;
      font-size: 22px;
      color: #00FF00;
      text-shadow: 0 0 10px rgba(0, 255, 0, 0.5);
    }

    ul {
      padding: 0;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 15px;
      list-style-type: none;

      li {
        display: flex;
        align-items: center;
        background-color: rgba(255, 255, 255, 0.1);
        padding: 15px;
        border-radius: 10px;
        transition: all 0.3s ease;

        &:hover {
          background-color: rgba(255, 255, 255, 0.2);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 255, 0, 0.2);
        }

        .gesture-icon {
          font-size: 30px;
          margin-right: 15px;
          background-color: rgba(0, 0, 0, 0.3);
          width: 50px;
          height: 50px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          flex-shrink: 0;
        }

        .gesture-desc {
          display: flex;
          flex-direction: column;

          strong {
            font-size: 16px;
            margin-bottom: 5px;
            color: #00FF00;
          }

          span {
            font-size: 14px;
            color: #ccc;
            line-height: 1.4;
          }
        }
      }
    }
  }
}
</style>
