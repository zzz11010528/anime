package com.community.gesture;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 手势识别器
 */
@Component
public class GestureRecognizer {

    /**
     * 识别手势
     * @param points 手势轨迹点
     * @return 识别结果
     */
    public GestureResult recognize(List<Point> points) {
        if (points == null || points.size() < 2) {
            return new GestureResult(GestureType.UNKNOWN, 0.0);
        }

        // 计算手势特征
        GestureFeature feature = calculateFeature(points);

        // 根据特征识别手势类型
        return identifyGesture(feature);
    }

    /**
     * 计算手势特征
     * @param points 手势轨迹点
     * @return 手势特征
     */
    private GestureFeature calculateFeature(List<Point> points) {
        GestureFeature feature = new GestureFeature();

        // 计算起点和终点
        Point start = points.get(0);
        Point end = points.get(points.size() - 1);
        feature.setStartPoint(start);
        feature.setEndPoint(end);

        // 计算轨迹长度
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            length += Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
        }
        feature.setLength(length);

        // 计算直线距离
        double directDistance = Math.sqrt(Math.pow(end.getX() - start.getX(), 2) + Math.pow(end.getY() - start.getY(), 2));
        feature.setDirectDistance(directDistance);

        // 计算曲直比（轨迹长度 / 直线距离）
        feature.setCurvature(length / (directDistance > 0 ? directDistance : 1));

        // 计算方向
        double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        feature.setDirection(angle);

        // 计算速度
        if (points.get(points.size() - 1).getTimestamp() > points.get(0).getTimestamp()) {
            double time = (points.get(points.size() - 1).getTimestamp() - points.get(0).getTimestamp()) / 1000.0; // 转换为秒
            feature.setSpeed(length / (time > 0 ? time : 1));
        }

        return feature;
    }

    /**
     * 根据特征识别手势类型
     * @param feature 手势特征
     * @return 识别结果
     */
    private GestureResult identifyGesture(GestureFeature feature) {
        // 判断是否为滑动手势
        if (feature.getCurvature() < 1.5) {
            // 根据方向判断滑动类型
            double direction = feature.getDirection();
            if (direction >= -Math.PI/4 && direction < Math.PI/4) {
                return new GestureResult(GestureType.SWIPE_RIGHT, 0.9);
            } else if (direction >= Math.PI/4 && direction < 3*Math.PI/4) {
                return new GestureResult(GestureType.SWIPE_DOWN, 0.9);
            } else if (direction >= 3*Math.PI/4 || direction < -3*Math.PI/4) {
                return new GestureResult(GestureType.SWIPE_LEFT, 0.9);
            } else {
                return new GestureResult(GestureType.SWIPE_UP, 0.9);
            }
        }
        
        // 判断是否为圆形手势
        if (feature.getCurvature() > 3.0 && 
            feature.getDirectDistance() / feature.getLength() < 0.3) {
            return new GestureResult(GestureType.CIRCLE, 0.8);
        }
        
        // 判断是否为Z形手势
        if (feature.getCurvature() > 1.5 && feature.getCurvature() < 3.0) {
            return new GestureResult(GestureType.Z_SHAPE, 0.7);
        }

        // 无法识别的手势
        return new GestureResult(GestureType.UNKNOWN, 0.5);
    }

    /**
     * 手势类型
     */
    public enum GestureType {
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_UP,
        SWIPE_DOWN,
        CIRCLE,
        Z_SHAPE,
        UNKNOWN
    }

    /**
     * 手势特征
     */
    @Data
    public static class GestureFeature {
        private Point startPoint;
        private Point endPoint;
        private double length;
        private double directDistance;
        private double curvature;
        private double direction;
        private double speed;
    }

    /**
     * 手势识别结果
     */
    @Data
    public static class GestureResult {
        private GestureType type;
        private double confidence;

        public GestureResult(GestureType type, double confidence) {
            this.type = type;
            this.confidence = confidence;
        }
    }

    /**
     * 手势轨迹点
     */
    @Data
    public static class Point {
        private double x;
        private double y;
        private long timestamp;

        public Point(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }
}
