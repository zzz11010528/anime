package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.Refund;
import com.community.mapper.RefundMapper;
import com.community.service.RefundService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 退款服务实现类
 */
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    @Override
    public Long create(Long orderId, String refundAmount, String reason) {
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setRefundNo(UUID.randomUUID().toString().replaceAll("-", ""));
        refund.setRefundAmount(new BigDecimal(refundAmount));
        refund.setReason(reason);
        refund.setStatus(0); // 申请中
        save(refund);
        return refund.getId();
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Refund refund = getById(id);
        if (refund == null) {
            return false;
        }
        refund.setStatus(status);
        if (status == 1) {
            refund.setRefundTime(LocalDateTime.now());
        }
        return updateById(refund);
    }

    @Override
    public Refund getByOrderId(Long orderId) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getOrderId, orderId);
        return getOne(wrapper);
    }
}
