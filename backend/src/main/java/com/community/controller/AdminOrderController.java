package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Order;
import com.community.service.OrderService;
import com.community.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/order/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 获取所有订单列表（管理员接口）
     */
    @GetMapping("/list")
    public R<Page<OrderVO>> list(@RequestParam(required = false) String orderNo,
                                 @RequestParam(required = false) Long userId,
                                 @RequestParam(required = false) Long sellerId,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        // 校验是否是管理员
        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        // 调用Service层方法获取所有订单
        Page<OrderVO> pageParam = new Page<>(current, size);
        Page<OrderVO> result = orderService.getAllOrders(pageParam, orderNo, userId, sellerId, status);
        return R.ok(result);
    }

    /**
     * 获取订单详情（管理员接口）
     */
    @GetMapping("/{id}")
    public R<OrderVO> getDetail(@PathVariable Long id) {
        // 校验是否是管理员
        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        return R.ok(orderService.getDetail(id));
    }

    /**
     * 处理退款申请（管理员接口）
     */
    @PostMapping("/refund/handle")
    public R<Void> handleRefund(@RequestParam Long id, @RequestParam Integer status, @RequestParam(required = false) String reason) {
        // 校验是否是管理员
        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        // 校验订单是否存在
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }

        orderService.handleRefund(id, status, reason);
        return R.ok();
    }
}
