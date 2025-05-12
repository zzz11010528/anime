package com.community.vo;

import com.community.entity.ProductImage;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品VO
 */
@Data
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 发布者用户ID
     */
    private Long userId;

    /**
     * 发布者用户名
     */
    private String username;

    /**
     * 发布者昵称
     */
    private String nickname;

    /**
     * 发布者头像
     */
    private String avatar;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * IP ID
     */
    private Long ipId;

    /**
     * IP名称
     */
    private String ipName;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 主图URL
     */
    private String mainImage;

    /**
     * 是否认证商家发布：0-否，1-是
     */
    private Integer isCertified;

    /**
     * 状态：0-下架，1-上架，2-审核中
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
     * 商品图片列表
     */
    private List<ProductImage> images;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 是否收藏
     */
    private Boolean isCollected;
}
