package com.community.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团活动VO
 */
@Data
public class GroupBuyingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 拼团ID
     */
    private Long id;

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
     * 商品原价
     */
    private BigDecimal productPrice;

    /**
     * 拼团价格
     */
    private BigDecimal groupPrice;

    /**
     * 最小成团人数
     */
    private Integer minGroupSize;

    /**
     * 最大成团人数
     */
    private Integer maxGroupSize;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-已结束，1-进行中，2-未开始
     */
    private Integer status;

    /**
     * 已成团数量
     */
    private Integer groupedCount;

    /**
     * 拼团中数量
     */
    private Integer groupingCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 拼团中的订单列表
     */
    private List<GroupOrderVO> groupingOrders;
}
