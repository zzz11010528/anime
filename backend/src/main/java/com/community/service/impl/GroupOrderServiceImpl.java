package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.GroupBuying;
import com.community.entity.GroupOrder;
import com.community.entity.Order;
import com.community.entity.OrderItem;
import com.community.entity.Product;
import com.community.mapper.GroupOrderMapper;
import com.community.service.GroupBuyingService;
import com.community.service.GroupOrderService;
import com.community.service.OrderItemService;
import com.community.service.OrderService;
import com.community.service.ProductService;
import com.community.vo.GroupOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 拼团订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class GroupOrderServiceImpl extends ServiceImpl<GroupOrderMapper, GroupOrder> implements GroupOrderService {

    private final GroupBuyingService groupBuyingService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long groupBuyingId, Long userId) {
        // 校验拼团活动是否存在
        GroupBuying groupBuying = groupBuyingService.getById(groupBuyingId);
        if (groupBuying == null) {
            throw new BusinessException("拼团活动不存在");
        }

        // 校验拼团活动状态
        if (groupBuying.getStatus() != 1) {
            throw new BusinessException("拼团活动未开始或已结束");
        }

        // 创建拼团订单
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setGroupBuyingId(groupBuyingId);
        groupOrder.setLeaderUserId(userId);
        groupOrder.setCurrentSize(1);
        groupOrder.setStatus(0);
        // 设置过期时间为24小时后
        groupOrder.setExpireTime(LocalDateTime.now().plusHours(24));

        // 保存拼团订单
        boolean result = save(groupOrder);
        if (!result) {
            throw new BusinessException("创建拼团订单失败");
        }

        // 为团长创建普通订单
        Product product = productService.getById(groupBuying.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 获取商品所属商家
        Long sellerId = product.getUserId();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSellerId(sellerId);
        order.setTotalAmount(groupBuying.getGroupPrice());
        order.setPayAmount(groupBuying.getGroupPrice());
        order.setStatus(0); // 待付款
        order.setGroupOrderId(groupOrder.getId()); // 关联拼团订单
        orderService.save(order);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setPrice(groupBuying.getGroupPrice());
        orderItem.setQuantity(1);
        orderItem.setTotalPrice(groupBuying.getGroupPrice());
        orderItemService.save(orderItem);

        return groupOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean join(Long groupOrderId, Long userId) {
        // 校验拼团订单是否存在
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团订单不存在");
        }

        // 校验拼团订单状态
        if (groupOrder.getStatus() != 0) {
            throw new BusinessException("拼团已成功或已解散");
        }

        // 校验拼团活动是否存在
        GroupBuying groupBuying = groupBuyingService.getById(groupOrder.getGroupBuyingId());
        if (groupBuying == null) {
            throw new BusinessException("拼团活动不存在");
        }

        // 校验拼团活动状态
        if (groupBuying.getStatus() != 1) {
            throw new BusinessException("拼团活动未开始或已结束");
        }

        // 校验是否已过期
        if (groupOrder.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("拼团已过期");
        }

        // 校验是否已满
        if (groupOrder.getCurrentSize() >= groupBuying.getMaxGroupSize()) {
            throw new BusinessException("拼团人数已满");
        }

        // 检查是否是团长自己
        if (groupOrder.getLeaderUserId().equals(userId)) {
            throw new BusinessException("不能参与自己发起的拼团");
        }

        // 检查用户是否已经参与了该拼团
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getGroupOrderId, groupOrderId)
                .eq(Order::getUserId, userId);
        long count = orderService.count(orderWrapper);
        if (count > 0) {
            throw new BusinessException("您已经参与了该拼团");
        }

        // 创建订单
        Product product = productService.getById(groupBuying.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 获取商品所属商家
        Long sellerId = product.getUserId();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSellerId(sellerId);
        order.setTotalAmount(groupBuying.getGroupPrice());
        order.setPayAmount(groupBuying.getGroupPrice());
        order.setStatus(0); // 待付款
        order.setGroupOrderId(groupOrderId); // 关联拼团订单
        orderService.save(order);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setPrice(groupBuying.getGroupPrice());
        orderItem.setQuantity(1);
        orderItem.setTotalPrice(groupBuying.getGroupPrice());
        orderItemService.save(orderItem);

        // 增加拼团人数
        groupOrder.setCurrentSize(groupOrder.getCurrentSize() + 1);
        boolean result = updateById(groupOrder);
        if (!result) {
            throw new BusinessException("加入拼团失败");
        }

        // 检查是否达到最小成团人数
        if (groupOrder.getCurrentSize() >= groupBuying.getMinGroupSize()) {
            // 更新拼团状态为已成团
            groupOrder.setStatus(1);
            updateById(groupOrder);
        }

        return true;
    }

    // 生成订单编号
    private String generateOrderNo() {
        // 生成订单编号：年月日时分秒+6位随机数
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkGroupSuccess(Long groupOrderId) {
        // 校验拼团订单是否存在
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团订单不存在");
        }

        // 校验拼团订单状态
        if (groupOrder.getStatus() != 0) {
            return groupOrder.getStatus() == 1;
        }

        // 校验拼团活动是否存在
        GroupBuying groupBuying = groupBuyingService.getById(groupOrder.getGroupBuyingId());
        if (groupBuying == null) {
            throw new BusinessException("拼团活动不存在");
        }

        // 检查是否达到最小成团人数
        if (groupOrder.getCurrentSize() >= groupBuying.getMinGroupSize()) {
            // 更新拼团状态为已成团
            groupOrder.setStatus(1);
            updateById(groupOrder);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dissolve(Long groupOrderId) {
        // 校验拼团订单是否存在
        GroupOrder groupOrder = getById(groupOrderId);
        if (groupOrder == null) {
            throw new BusinessException("拼团订单不存在");
        }

        // 校验拼团订单状态
        if (groupOrder.getStatus() != 0) {
            throw new BusinessException("拼团已成功或已解散");
        }

        // 更新拼团状态为已解散
        groupOrder.setStatus(2);
        boolean result = updateById(groupOrder);
        if (!result) {
            throw new BusinessException("解散拼团失败");
        }

        // 查询关联的订单，取消未支付的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getGroupOrderId, groupOrderId)
                .eq(Order::getStatus, 0);
        List<Order> orders = orderService.list(wrapper);
        for (Order order : orders) {
            orderService.cancel(order.getId());
        }

        return true;
    }

    @Override
    public GroupOrderVO getDetail(Long id) {
        return baseMapper.selectGroupOrderDetail(id);
    }

    @Override
    public List<GroupOrderVO> getGroupingOrders(Long groupBuyingId) {
        return baseMapper.selectGroupingOrders(groupBuyingId);
    }

    @Override
    public List<GroupOrderVO> getGroupedOrders(Long groupBuyingId) {
        return baseMapper.selectGroupedOrders(groupBuyingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndUpdateExpiredOrders() {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        // 查询过期的拼团订单
        LambdaQueryWrapper<GroupOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupOrder::getStatus, 0)
                .le(GroupOrder::getExpireTime, now);
        List<GroupOrder> list = list(wrapper);

        for (GroupOrder groupOrder : list) {
            // 解散拼团
            groupOrder.setStatus(2);
            updateById(groupOrder);

            // 查询关联的订单，取消未支付的订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getGroupOrderId, groupOrder.getId())
                    .eq(Order::getStatus, 0);
            List<Order> orders = orderService.list(orderWrapper);
            for (Order order : orders) {
                orderService.cancel(order.getId());
            }

            count++;
        }

        return count;
    }

    @Override
    public Page<GroupOrderVO> getUserOrders(Long userId, Integer status, Integer page, Integer size) {
        // 创建分页参数
        Page<GroupOrderVO> pageParam = new Page<>(page, size);

        // 查询用户参与的拼团订单，包括作为团长和参与者的
        return baseMapper.selectUserGroupOrders(pageParam, userId, status);
    }
}
