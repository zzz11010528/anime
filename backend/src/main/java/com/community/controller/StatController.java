package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    /**
     * 获取商品销售统计
     */
    @GetMapping("/product/sales")
    public R<Map<String, Object>> getProductSalesStat(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 校验权限
        if (!StpUtil.hasRole("merchant") && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        return R.ok(statService.getProductSalesStat(productId, startDate, endDate));
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/product/hot")
    public R<Map<String, Object>> getHotProducts(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(statService.getHotProducts(startDate, endDate, limit));
    }

    /**
     * 获取帖子统计
     */
    @GetMapping("/post")
    public R<Map<String, Object>> getPostStat(
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 校验权限
        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        return R.ok(statService.getPostStat(postId, startDate, endDate));
    }

    /**
     * 获取热门帖子
     */
    @GetMapping("/post/hot")
    public R<Map<String, Object>> getHotPosts(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(statService.getHotPosts(startDate, endDate, limit));
    }

    /**
     * 获取用户行为统计
     */
    @GetMapping("/user/behavior")
    public R<Map<String, Object>> getUserBehaviorStat(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 校验权限
        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        return R.ok(statService.getUserBehaviorStat(userId, startDate, endDate));
    }

    /**
     * 获取活跃用户
     */
    @GetMapping("/user/active")
    public R<Map<String, Object>> getActiveUsers(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return R.ok(statService.getActiveUsers(startDate, endDate, limit));
    }

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> getSystemOverview() {
        return R.ok(statService.getSystemOverview());
    }

    /**
     * 获取商家统计概览
     */
    @GetMapping("/merchant/overview")
    public R<Map<String, Object>> getMerchantOverview() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取商家的商品销售统计
        Map<String, Object> salesStat = statService.getProductSalesStat(null, LocalDate.now().minusDays(30), LocalDate.now());

        // 获取商家的热门商品
        Map<String, Object> hotProducts = statService.getHotProducts(LocalDate.now().minusDays(30), LocalDate.now(), 5);

        // 合并结果
        Map<String, Object> result = new HashMap<String, Object>();
        result.putAll(salesStat);
        result.putAll(hotProducts);
        result.put("userId", userId); // 添加用户ID到结果中

        return R.ok(result);
    }
}
