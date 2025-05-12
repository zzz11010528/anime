package com.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Order;
import com.community.service.OrderService;
import com.community.vo.OrderVO;
import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户订单控制器
 */
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;

    /**
     * 创建订单（从购物车）
     */
    @PostMapping("/create")
    public R<Long> create(@RequestBody Order order) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long orderId = orderService.create(userId, order.getShippingName(), order.getShippingPhone(), order.getShippingAddress());
        return R.ok(orderId);
    }

    /**
     * 直接购买创建订单
     */
    @PostMapping("/direct")
    public R<Long> createDirect(@RequestBody DirectOrderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long orderId = orderService.createDirect(
                userId,
                request.getProductId(),
                request.getQuantity(),
                request.getShippingName(),
                request.getShippingPhone(),
                request.getShippingAddress());
        return R.ok(orderId);
    }

    /**
     * 直接购买请求
     */
    @Data
    public static class DirectOrderRequest {
        private Long productId;
        private Integer quantity;
        private String shippingName;
        private String shippingPhone;
        private String shippingAddress;
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        orderService.cancel(id);
        return R.ok();
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay/{id}")
    public R<String> pay(@PathVariable Long id, @RequestBody(required = false) PayRequest payRequest) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        Integer payType = payRequest != null ? payRequest.getPayType() : 1; // 默认支付方式为1
        String payUrl = orderService.pay(id, payType);
        return R.ok(payUrl);
    }

    /**
     * 确认收货
     */
    @PostMapping("/receive/{id}")
    public R<Void> receive(@PathVariable Long id) {
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
    @PostMapping("/refund/{id}")
    public R<Void> applyRefund(@PathVariable Long id, @RequestBody RefundRequest refundRequest) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        orderService.applyRefund(id, refundRequest.getReason());
        return R.ok();
    }

    /**
     * 删除订单
     */
    @PostMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            return R.failed("无权操作");
        }

        // 只有已完成或已取消的订单才能删除
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            return R.failed("只有已完成或已取消的订单才能删除");
        }

        orderService.removeById(id);
        return R.ok();
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
        // 校验是否有权限
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        return R.ok(orderService.getDetail(id));
    }

    /**
     * 支付请求参数
     */
    static class PayRequest {
        private Integer payType;

        public Integer getPayType() {
            return payType;
        }

        public void setPayType(Integer payType) {
            this.payType = payType;
        }
    }

    /**
     * 退款请求参数
     */
    static class RefundRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
