package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动实体类
 */
@Data
@TableName("group_buying")
public class GroupBuying implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 拼团ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
