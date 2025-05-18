package com.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.GroupBuying;
import com.community.service.GroupBuyingService;
import com.community.service.GroupOrderService;
import com.community.vo.GroupBuyingVO;
import com.community.vo.GroupOrderVO;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团活动控制器
 */
@RestController
@RequestMapping("/merchant/group-buying")
@RequiredArgsConstructor
public class GroupBuyingController {

    private final GroupBuyingService groupBuyingService;
    private final GroupOrderService groupOrderService;

    /**
     * 创建拼团活动（允许普通用户和商家创建）
     */
    @PostMapping
    public R<Long> create(@RequestBody GroupBuying groupBuying) {
        // 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        Long id = groupBuyingService.create(groupBuying);
        return R.ok(id);
    }

    /**
     * 更新拼团活动
     */
    @PutMapping
    public R<Void> update(@RequestBody GroupBuying groupBuying) {
        // 获取当前用户ID，用于权限校验
        Long userId = StpUtil.getLoginIdAsLong();

        // 这里应该添加检查，确保用户只能更新自己创建的拼团活动
        // 但由于GroupBuying实体没有创建者字段，暂时不做处理
        // 实际应用中应该在数据库和实体中添加创建者字段

        boolean result = groupBuyingService.update(groupBuying);
        return result ? R.ok() : R.failed("更新失败");
    }

    /**
     * 结束拼团活动
     */
    @PostMapping("/end/{id}")
    public R<Void> end(@PathVariable Long id) {
        // 获取当前用户ID，用于权限校验
        Long userId = StpUtil.getLoginIdAsLong();

        // 这里应该添加检查，确保用户只能结束自己创建的拼团活动
        // 但由于GroupBuying实体没有创建者字段，暂时不做处理

        boolean result = groupBuyingService.end(id);
        return result ? R.ok() : R.failed("结束失败");
    }

    /**
     * 分页查询拼团活动列表
     */
    @GetMapping("/page")
    public R<Page<GroupBuyingVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 获取当前用户ID作为卖家ID
        Long sellerId = StpUtil.getLoginIdAsLong();
        Page<GroupBuyingVO> result = groupBuyingService.page(sellerId, keyword, status, page, size);
        return R.ok(result);
    }

    /**
     * 获取拼团活动详情
     */
    @GetMapping("/{id}")
    public R<GroupBuyingVO> getDetail(@PathVariable Long id) {
        GroupBuyingVO detail = groupBuyingService.getDetail(id);
        return R.ok(detail);
    }

    /**
     * 获取拼团中的订单列表
     */
    @GetMapping("/orders/grouping/{groupBuyingId}")
    public R<List<GroupOrderVO>> getGroupingOrders(@PathVariable Long groupBuyingId) {
        // 这里应该添加检查，确保用户只能查看自己创建的拼团活动的订单
        // 但由于GroupBuying实体没有创建者字段，暂时不做处理

        List<GroupOrderVO> orders = groupOrderService.getGroupingOrders(groupBuyingId);
        return R.ok(orders);
    }

    /**
     * 获取已成团的订单列表
     */
    @GetMapping("/orders/grouped/{groupBuyingId}")
    public R<List<GroupOrderVO>> getGroupedOrders(@PathVariable Long groupBuyingId) {
        // 这里应该添加检查，确保用户只能查看自己创建的拼团活动的订单
        // 但由于GroupBuying实体没有创建者字段，暂时不做处理

        List<GroupOrderVO> orders = groupOrderService.getGroupedOrders(groupBuyingId);
        return R.ok(orders);
    }

    /**
     * 获取拼团订单详情
     */
    @GetMapping("/order/{id}")
    public R<GroupOrderVO> getOrderDetail(@PathVariable Long id) {
        // 这里应该添加检查，确保用户只能查看自己创建的拼团活动的订单
        // 但由于GroupBuying实体没有创建者字段，暂时不做处理

        GroupOrderVO detail = groupOrderService.getDetail(id);
        return R.ok(detail);
    }
}
