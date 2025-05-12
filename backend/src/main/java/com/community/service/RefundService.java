package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Refund;

/**
 * 退款服务接口
 */
public interface RefundService extends IService<Refund> {

    /**
     * 创建退款申请
     * @param orderId 订单ID
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款申请ID
     */
    Long create(Long orderId, String refundAmount, String reason);

    /**
     * 更新退款状态
     * @param id 退款ID
     * @param status 退款状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 根据订单ID获取退款记录
     * @param orderId 订单ID
     * @return 退款记录
     */
    Refund getByOrderId(Long orderId);
}
