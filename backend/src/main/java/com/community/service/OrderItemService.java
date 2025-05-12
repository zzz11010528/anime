package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.OrderItem;

import java.util.List;

/**
 * 订单项服务接口
 */
public interface OrderItemService extends IService<OrderItem> {

    /**
     * 根据订单ID获取订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> getByOrderId(Long orderId);
}
