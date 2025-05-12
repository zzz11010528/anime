package com.community.vo;

import com.community.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
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
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 卖家用户名
     */
    private String sellerUsername;

    /**
     * 卖家昵称
     */
    private String sellerNickname;

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
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 订单项列表
     */
    private List<OrderItem> orderItems;
}
