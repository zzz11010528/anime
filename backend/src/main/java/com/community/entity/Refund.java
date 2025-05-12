package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款信息实体类
 */
@Data
@TableName("refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 退款编号
     */
    private String refundNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款状态：0-申请中，1-已退款，2-已拒绝
     */
    private Integer status;

    /**
     * 拒绝原因 - 数据库中不存在此字段，仅用于程序内部
     */
    @TableField(exist = false)
    private String rejectReason;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

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
