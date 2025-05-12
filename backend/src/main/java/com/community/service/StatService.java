package com.community.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatService {

    /**
     * 获取商品销售统计
     * @param productId 商品ID，为null时统计所有商品
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 商品销售统计
     */
    Map<String, Object> getProductSalesStat(Long productId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取热门商品
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 数量
     * @return 热门商品
     */
    Map<String, Object> getHotProducts(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取帖子统计
     * @param postId 帖子ID，为null时统计所有帖子
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 帖子统计
     */
    Map<String, Object> getPostStat(Long postId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取热门帖子
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 数量
     * @return 热门帖子
     */
    Map<String, Object> getHotPosts(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取用户行为统计
     * @param userId 用户ID，为null时统计所有用户
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户行为统计
     */
    Map<String, Object> getUserBehaviorStat(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取活跃用户
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 数量
     * @return 活跃用户
     */
    Map<String, Object> getActiveUsers(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取系统概览统计
     * @return 系统概览统计
     */
    Map<String, Object> getSystemOverview();
}
