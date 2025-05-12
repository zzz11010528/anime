package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("`order`") // order是MySQL关键字，需要加上反引号
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式：1-支付宝
     */
    private Integer payType;

    /**
     * 订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-申请退款，6-已退款
     */
    private Integer status;

    /**
     * 收货人姓名
     */
    private String shippingName;

    /**
     * 收货人电话
     */
    private String shippingPhone;

    /**
     * 收货地址
     */
    private String shippingAddress;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 拼团订单ID，非拼团为null
     */
    private Long groupOrderId;

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
