package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.Payment;
import com.community.mapper.PaymentMapper;
import com.community.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Override
    public Long create(Long orderId, String paymentPlatform, String amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentPlatform(paymentPlatform);
        payment.setAmount(new BigDecimal(amount));
        payment.setStatus(0); // 未支付
        save(payment);
        return payment.getId();
    }

    @Override
    public boolean updateStatus(Long orderId, String paymentNo, Integer status) {
        Payment payment = getByOrderId(orderId);
        if (payment == null) {
            return false;
        }
        payment.setPaymentNo(paymentNo);
        payment.setStatus(status);
        if (status == 1) {
            payment.setPaymentTime(LocalDateTime.now());
        }
        return updateById(payment);
    }

    @Override
    public Payment getByOrderId(Long orderId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId);
        return getOne(wrapper);
    }
}
