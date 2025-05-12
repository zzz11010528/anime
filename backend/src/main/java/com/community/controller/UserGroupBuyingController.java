package com.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.service.GroupBuyingService;
import com.community.service.GroupOrderService;
import com.community.service.OrderService;
import com.community.vo.GroupBuyingVO;
import com.community.vo.GroupOrderVO;
import com.community.vo.OrderVO;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户拼团控制器
 */
@RestController
@RequestMapping("/user/group-buying")
@RequiredArgsConstructor
public class UserGroupBuyingController {

    private final GroupBuyingService groupBuyingService;
    private final GroupOrderService groupOrderService;
    private final OrderService orderService;

    /**
     * 获取正在进行中的拼团活动列表
     */
    @GetMapping("/active")
    public R<List<GroupBuyingVO>> getActiveGroupBuyings(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<GroupBuyingVO> list = groupBuyingService.getActiveGroupBuyings(limit);
        return R.ok(list);
    }

    /**
     * 获取拼团活动详情
     */
    @GetMapping("/{id}")
    public R<GroupBuyingVO> getDetail(@PathVariable Long id) {
        GroupBuyingVO detail = groupBuyingService.getDetail(id);

        // 获取拼团中的订单
        if (detail != null) {
            List<GroupOrderVO> groupingOrders = groupOrderService.getGroupingOrders(id);
            detail.setGroupingOrders(groupingOrders);
        }

        return R.ok(detail);
    }

    /**
     * 创建拼团
     */
    @PostMapping("/create/{groupBuyingId}")
    public R<Long> createGroup(@PathVariable Long groupBuyingId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long groupOrderId = groupOrderService.create(groupBuyingId, userId);
        return R.ok(groupOrderId);
    }

    /**
     * 加入拼团
     */
    @PostMapping("/join/{groupOrderId}")
    public R<Void> joinGroup(@PathVariable Long groupOrderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupOrderService.join(groupOrderId, userId);
        return result ? R.ok() : R.failed("加入拼团失败");
    }

    /**
     * 获取拼团订单详情
     */
    @GetMapping("/order/{id}")
    public R<GroupOrderVO> getOrderDetail(@PathVariable Long id) {
        GroupOrderVO detail = groupOrderService.getDetail(id);

        // 获取参与用户的订单信息
        if (detail != null) {
            List<OrderVO> orders = orderService.getOrdersByGroupOrderId(id);
            detail.setOrders(orders);
        }

        return R.ok(detail);
    }

    /**
     * 取消拼团
     */
    @PostMapping("/cancel/{groupOrderId}")
    public R<Void> cancelGroup(@PathVariable Long groupOrderId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验是否有权限
        GroupOrderVO groupOrder = groupOrderService.getDetail(groupOrderId);
        if (groupOrder == null) {
            return R.failed("拼团订单不存在");
        }

        // 只有团长才能取消拼团
        if (!groupOrder.getLeaderUserId().equals(userId)) {
            return R.failed("只有团长才能取消拼团");
        }

        boolean result = groupOrderService.dissolve(groupOrderId);
        return result ? R.ok() : R.failed("取消拼团失败");
    }

    /**
     * 获取用户拼团订单列表
     */
    @GetMapping("/user-orders")
    public R<Page<GroupOrderVO>> getUserOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<GroupOrderVO> result = groupOrderService.getUserOrders(userId, status, page, size);
        return R.ok(result);
    }
}
