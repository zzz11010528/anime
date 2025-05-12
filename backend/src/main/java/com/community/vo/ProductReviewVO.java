package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价VO
 */
@Data
public class ProductReviewVO {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单项ID
     */
    private Long orderItemId;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评分：1-5
     */
    private Integer rating;

    /**
     * 状态：0-删除，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 评价图片列表
     */
    private List<String> images;

    /**
     * 评价图片字符串（逗号分隔）
     */
    private String imagesConcat;
}
