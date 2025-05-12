package com.community.controller;

import com.community.common.result.R;
import com.community.gesture.GestureRecognizer;
import com.community.service.GestureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 手势控制器
 */
@RestController
@RequestMapping("/gesture")
@RequiredArgsConstructor
public class GestureController {

    private final GestureService gestureService;

    /**
     * 识别手势
     */
    @PostMapping("/recognize")
    public R<GestureRecognizer.GestureResult> recognize(@RequestBody List<Map<String, Object>> pointsData) {
        // 将请求数据转换为手势轨迹点
        List<GestureRecognizer.Point> points = pointsData.stream()
                .map(data -> new GestureRecognizer.Point(
                        Double.parseDouble(data.get("x").toString()),
                        Double.parseDouble(data.get("y").toString()),
                        Long.parseLong(data.get("timestamp").toString())
                ))
                .collect(java.util.stream.Collectors.toList());
        
        // 识别手势
        GestureRecognizer.GestureResult result = gestureService.recognize(points);
        return R.ok(result);
    }

    /**
     * 执行手势操作
     */
    @PostMapping("/execute")
    public R<Map<String, Object>> executeGestureAction(
            @RequestParam String gestureType,
            @RequestBody(required = false) Map<String, Object> params) {
        // 将字符串转换为手势类型枚举
        GestureRecognizer.GestureType type;
        try {
            type = GestureRecognizer.GestureType.valueOf(gestureType);
        } catch (IllegalArgumentException e) {
            return R.failed("不支持的手势类型");
        }
        
        // 执行手势操作
        Map<String, Object> result = gestureService.executeGestureAction(type, params);
        return R.ok(result);
    }

    /**
     * 获取支持的手势列表
     */
    @GetMapping("/supported")
    public R<List<Map<String, Object>>> getSupportedGestures() {
        return R.ok(gestureService.getSupportedGestures());
    }
}
