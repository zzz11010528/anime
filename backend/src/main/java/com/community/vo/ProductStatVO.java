package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品统计VO
 */
@Data
public class ProductStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 销售量
     */
    private Integer salesCount;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 总浏览量
     */
    private Integer totalViewCount;

    /**
     * 总销售量
     */
    private Integer totalSalesCount;

    /**
     * 总销售额
     */
    private BigDecimal totalSalesAmount;
}
