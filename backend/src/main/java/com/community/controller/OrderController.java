package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Order;
import com.community.service.OrderService;
import com.community.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public R<Long> create(@RequestParam String shippingName,
                          @RequestParam String shippingPhone,
                          @RequestParam String shippingAddress) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long orderId = orderService.create(userId, shippingName, shippingPhone, shippingAddress);
        return R.ok(orderId);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public R<Void> cancel(@RequestParam Long id) {
        orderService.cancel(id);
        return R.ok();
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public R<String> pay(@RequestParam Long id, @RequestParam Integer payType) {
        String payUrl = orderService.pay(id, payType);
        return R.ok(payUrl);
    }

    /**
     * 支付回调
     */
    @PostMapping("/pay/callback")
    public R<Void> payCallback(@RequestParam String orderNo,
                               @RequestParam String paymentNo,
                               @RequestParam String amount) {
        orderService.payCallback(orderNo, paymentNo, amount);
        return R.ok();
    }

    /**
     * 发货（商家接口）
     */
    @PostMapping("/ship")
    public R<Void> ship(@RequestParam Long id) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getSellerId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        // 直接更新订单状态为待收货
        order.setStatus(2); // 待收货
        order.setShippingTime(java.time.LocalDateTime.now());
        orderService.updateById(order);
        return R.ok();
    }

    /**
     * 确认收货
     */
    @PostMapping("/receive")
    public R<Void> receive(@RequestParam Long id) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        orderService.receive(id);
        return R.ok();
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund/apply")
    public R<Void> applyRefund(@RequestParam Long id, @RequestParam String reason) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        orderService.applyRefund(id, reason);
        return R.ok();
    }

    /**
     * 处理退款（商家接口）
     */
    @PostMapping("/refund/handle")
    public R<Void> handleRefund(@RequestParam Long id, @RequestParam Integer status, @RequestParam(required = false) String reason) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getSellerId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        try {
            // 调用服务处理退款，包括支付宝退款
            if (reason != null && !reason.isEmpty()) {
                orderService.handleRefund(id, status, reason);
            } else {
                orderService.handleRefund(id, status);
            }
            return R.ok();
        } catch (Exception e) {
            return R.failed(e.getMessage());
        }
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public R<Page<OrderVO>> list(@RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(orderService.page(userId, status, page, size));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public R<OrderVO> getDetail(@PathVariable Long id) {
        return R.ok(orderService.getDetail(id));
    }

    /**
     * 获取商家订单列表（商家接口）
     */
    @GetMapping("/seller/list")
    public R<Page<OrderVO>> getSellerOrders(@RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) String customerInfo,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate,
                                            @RequestParam(required = false) BigDecimal minAmount,
                                            @RequestParam(required = false) BigDecimal maxAmount,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        // 校验是否是商家
        if (!StpUtil.hasRole("merchant") && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        Long sellerId = StpUtil.getLoginIdAsLong();
        return R.ok(orderService.getSellerOrdersWithFilter(sellerId, orderNo, customerInfo, status,
                                                          startDate, endDate, minAmount, maxAmount, page, size));
    }
}
