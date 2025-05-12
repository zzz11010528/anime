package com.community.service;

import com.community.gesture.GestureRecognizer;

import java.util.List;
import java.util.Map;

/**
 * 手势服务接口
 */
public interface GestureService {

    /**
     * 识别手势
     * @param points 手势轨迹点
     * @return 识别结果
     */
    GestureRecognizer.GestureResult recognize(List<GestureRecognizer.Point> points);

    /**
     * 执行手势操作
     * @param gestureType 手势类型
     * @param params 操作参数
     * @return 操作结果
     */
    Map<String, Object> executeGestureAction(GestureRecognizer.GestureType gestureType, Map<String, Object> params);

    /**
     * 获取支持的手势列表
     * @return 支持的手势列表
     */
    List<Map<String, Object>> getSupportedGestures();
}
