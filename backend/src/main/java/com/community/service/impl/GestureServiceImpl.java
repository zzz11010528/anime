package com.community.service.impl;

import com.community.gesture.GestureRecognizer;
import com.community.service.GestureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手势服务实现类
 */
@Service
@RequiredArgsConstructor
public class GestureServiceImpl implements GestureService {

    private final GestureRecognizer gestureRecognizer;

    @Override
    public GestureRecognizer.GestureResult recognize(List<GestureRecognizer.Point> points) {
        return gestureRecognizer.recognize(points);
    }

    @Override
    public Map<String, Object> executeGestureAction(GestureRecognizer.GestureType gestureType, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 根据手势类型执行不同的操作
        switch (gestureType) {
            case SWIPE_LEFT:
                // 向左滑动，例如：下一页
                result.put("action", "next_page");
                break;
            case SWIPE_RIGHT:
                // 向右滑动，例如：上一页
                result.put("action", "prev_page");
                break;
            case SWIPE_UP:
                // 向上滑动，例如：刷新
                result.put("action", "refresh");
                break;
            case SWIPE_DOWN:
                // 向下滑动，例如：加载更多
                result.put("action", "load_more");
                break;
            case CIRCLE:
                // 画圆，例如：旋转
                result.put("action", "rotate");
                break;
            case Z_SHAPE:
                // Z形，例如：撤销
                result.put("action", "undo");
                break;
            default:
                result.put("action", "none");
                break;
        }
        
        // 添加上下文参数
        if (params != null) {
            result.put("context", params);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSupportedGestures() {
        List<Map<String, Object>> gestures = new ArrayList<>();
        
        // 添加支持的手势及其说明
        addGesture(gestures, "SWIPE_LEFT", "向左滑动", "下一页");
        addGesture(gestures, "SWIPE_RIGHT", "向右滑动", "上一页");
        addGesture(gestures, "SWIPE_UP", "向上滑动", "刷新");
        addGesture(gestures, "SWIPE_DOWN", "向下滑动", "加载更多");
        addGesture(gestures, "CIRCLE", "画圆", "旋转");
        addGesture(gestures, "Z_SHAPE", "Z形", "撤销");
        
        return gestures;
    }
    
    /**
     * 添加手势信息
     */
    private void addGesture(List<Map<String, Object>> gestures, String type, String description, String action) {
        Map<String, Object> gesture = new HashMap<>();
        gesture.put("type", type);
        gesture.put("description", description);
        gesture.put("action", action);
        gestures.add(gesture);
    }
}
