/**
 * MediaPipe 库的统一导入和配置
 * 使用 unpkg.com CDN 加载所有 MediaPipe 资源
 */

// 导入 MediaPipe 库
import { Hands } from '@mediapipe/hands';
import { Camera } from '@mediapipe/camera_utils';
import { drawConnectors, drawLandmarks } from '@mediapipe/drawing_utils';

// 配置 MediaPipe 资源加载器
const createResourceLoader = (packageName, version) => {
  return (file) => {
    return `https://unpkg.com/@mediapipe/${packageName}@${version}/${file}`;
  };
};

// 版本配置
const versions = {
  hands: '0.4.1646424915',
  camera_utils: '0.3.1675466862',
  drawing_utils: '0.3.1675466124'
};

// 创建 Hands 实例
const createHands = (options = {}) => {
  const hands = new Hands({
    locateFile: createResourceLoader('hands', versions.hands)
  });

  // 默认配置
  const defaultOptions = {
    maxNumHands: 1,
    modelComplexity: 1,
    minDetectionConfidence: 0.3,
    minTrackingConfidence: 0.3
  };

  // 合并配置
  hands.setOptions({ ...defaultOptions, ...options });

  return hands;
};

// 创建 Camera 实例
const createCamera = (videoElement, options = {}) => {
  // 默认配置
  const defaultOptions = {
    width: 640,
    height: 480
  };

  // 合并配置
  const mergedOptions = { ...defaultOptions, ...options };

  return new Camera(videoElement, mergedOptions);
};

// 导出工具函数
export {
  Hands,
  Camera,
  drawConnectors,
  drawLandmarks,
  createHands,
  createCamera
};
