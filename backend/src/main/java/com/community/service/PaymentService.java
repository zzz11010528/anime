package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Payment;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付记录
     * @param orderId 订单ID
     * @param paymentPlatform 支付平台
     * @param amount 支付金额
     * @return 支付记录ID
     */
    Long create(Long orderId, String paymentPlatform, String amount);

    /**
     * 更新支付状态
     * @param orderId 订单ID
     * @param paymentNo 支付流水号
     * @param status 支付状态
     * @return 是否成功
     */
    boolean updateStatus(Long orderId, String paymentNo, Integer status);

    /**
     * 根据订单ID获取支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payment getByOrderId(Long orderId);
}
