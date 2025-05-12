package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.*;
import com.community.mapper.OrderMapper;
import com.community.service.*;
import com.community.vo.CartVO;
import com.community.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final PaymentService paymentService;
    private final RefundService refundService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, String shippingName, String shippingPhone, String shippingAddress) {
        // 获取购物车中选中的商品
        List<CartVO> cartList = cartService.list(userId);
        if (cartList.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 过滤出选中的商品
        List<CartVO> selectedCartList = cartList.stream()
                .filter(cart -> cart.getSelected() == 1)
                .collect(Collectors.toList());
        if (selectedCartList.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        // 校验商品状态和库存
        for (CartVO cart : selectedCartList) {
            if (cart.getStatus() != 1) {
                throw new BusinessException("商品" + cart.getProductName() + "已下架");
            }
            if (cart.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品" + cart.getProductName() + "库存不足");
            }
        }

        // 计算订单总金额
        BigDecimal totalAmount = selectedCartList.stream()
                .map(CartVO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSellerId(selectedCartList.get(0).getProductId()); // 暂时只支持单个商家的订单
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingAddress(shippingAddress);
        save(order);

        // 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartVO cart : selectedCartList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(cart.getProductName());
            orderItem.setProductImage(cart.getProductImage());
            orderItem.setPrice(cart.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(cart.getTotalPrice());
            orderItems.add(orderItem);

            // 更新商品库存和销量
            Product product = productService.getById(cart.getProductId());
            product.setStock(product.getStock() - cart.getQuantity());
            product.setSales(product.getSales() + cart.getQuantity());
            productService.updateById(product);

            // 删除购物车中已购买的商品
            cartService.removeById(cart.getId());
        }
        orderItemService.saveBatch(orderItems);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDirect(Long userId, Long productId, Integer quantity, String shippingName, String shippingPhone, String shippingAddress) {
        // 校验商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品状态和库存
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        // 计算订单总金额
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(quantity));

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSellerId(product.getUserId()); // 商品所属商家
        order.setTotalAmount(totalPrice);
        order.setPayAmount(totalPrice);
        order.setStatus(0); // 待付款
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingAddress(shippingAddress);
        save(order);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.setTotalPrice(totalPrice);
        orderItemService.save(orderItem);

        // 更新商品库存和销量
        product.setStock(product.getStock() - quantity);
        product.setSales(product.getSales() + quantity);
        productService.updateById(product);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        order.setCloseTime(LocalDateTime.now());
        boolean result = updateById(order);

        // 恢复商品库存和销量
        List<OrderItem> orderItems = orderItemService.getByOrderId(order.getId());
        for (OrderItem orderItem : orderItems) {
            Product product = productService.getById(orderItem.getProductId());
            product.setStock(product.getStock() + orderItem.getQuantity());
            product.setSales(product.getSales() - orderItem.getQuantity());
            productService.updateById(product);
        }

        return result;
    }

    private final com.community.common.util.AlipayUtil alipayUtil;

    @Override
    public String pay(Long id, Integer payType) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }

        // 更新订单支付方式
        order.setPayType(payType);
        updateById(order);

        // 创建支付记录
        paymentService.create(order.getId(), "alipay", order.getPayAmount().toString());

        // 调用支付宝接口，生成支付链接
        if (payType == 1) {
            // 支付宝支付
            return alipayUtil.generatePayUrl(
                    order.getOrderNo(),
                    order.getPayAmount().toString(),
                    "订单支付-" + order.getOrderNo()
            );
        } else {
            throw new BusinessException("不支持的支付方式");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payCallback(String orderNo, String paymentNo, String amount) {
        // 校验订单是否存在
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = getOne(wrapper);
        if (order == null) {
            System.out.println("支付回调 - 订单不存在: " + orderNo);
            return false;
        }

        // 校验订单状态 - 如果已经是已支付状态，直接返回成功
        if (order.getStatus() != 0) {
            // 如果订单已经是已支付或更高状态，说明已经处理过，直接返回成功
            if (order.getStatus() >= 1) {
                System.out.println("支付回调 - 订单已处理: " + orderNo + ", 状态: " + order.getStatus());
                return true;
            }
            System.out.println("支付回调 - 订单状态异常: " + orderNo + ", 状态: " + order.getStatus());
            return false;
        }

        // 校验支付金额
        if (!order.getPayAmount().toString().equals(amount)) {
            System.out.println("支付回调 - 金额不匹配: " + orderNo +
                ", 订单金额: " + order.getPayAmount() + ", 支付金额: " + amount);
            return false;
        }

        // 检查是否已有支付记录
        Payment payment = paymentService.getByOrderId(order.getId());
        if (payment != null && payment.getStatus() == 1) {
            // 已有成功的支付记录，直接返回成功
            System.out.println("支付回调 - 已有支付记录: " + orderNo);
            return true;
        }

        // 更新订单状态
        order.setStatus(1); // 待发货
        order.setPaymentTime(LocalDateTime.now());
        boolean result = updateById(order);
        System.out.println("支付回调 - 更新订单状态: " + orderNo + ", 结果: " + result);

        // 更新支付记录
        if (payment != null) {
            payment.setPaymentNo(paymentNo);
            payment.setStatus(1); // 已支付
            payment.setPaymentTime(LocalDateTime.now());
            boolean updateResult = paymentService.updateById(payment);
            System.out.println("支付回调 - 更新支付记录: " + orderNo + ", 结果: " + updateResult);
        } else {
            // 创建新的支付记录
            Long paymentId = paymentService.create(order.getId(), "alipay", amount);
            boolean updateResult = paymentService.updateStatus(order.getId(), paymentNo, 1);
            System.out.println("支付回调 - 创建支付记录: " + orderNo + ", ID: " + paymentId + ", 更新结果: " + updateResult);
        }

        return result;
    }

    @Override
    public boolean ship(Long id) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不允许发货");
        }

        // 更新订单状态
        order.setStatus(2); // 待收货
        order.setShippingTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean receive(Long id) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许收货");
        }

        // 更新订单状态
        order.setStatus(3); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(Long id, String reason) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许申请退款");
        }

        // 更新订单状态
        order.setStatus(5); // 申请退款
        boolean result = updateById(order);

        // 创建退款记录
        refundService.create(order.getId(), order.getPayAmount().toString(), reason);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long id, Integer status) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 5) {
            throw new BusinessException("订单状态不允许处理退款");
        }

        // 获取退款记录
        Refund refund = refundService.getByOrderId(order.getId());
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }

        if (status == 1) {
            // 同意退款
            // 调用支付宝退款接口
            boolean refundResult = alipayUtil.refund(
                    order.getOrderNo(),
                    refund.getRefundNo(),
                    refund.getRefundAmount().toString(),
                    refund.getReason()
            );

            if (!refundResult) {
                throw new BusinessException("退款失败");
            }

            // 更新订单状态
            order.setStatus(6); // 已退款
            updateById(order);
            refundService.updateStatus(refund.getId(), 1);

            // 恢复商品库存和销量
            List<OrderItem> orderItems = orderItemService.getByOrderId(order.getId());
            for (OrderItem orderItem : orderItems) {
                Product product = productService.getById(orderItem.getProductId());
                product.setStock(product.getStock() + orderItem.getQuantity());
                product.setSales(product.getSales() - orderItem.getQuantity());
                productService.updateById(product);
            }

            // 更新支付记录
            Payment payment = paymentService.getByOrderId(order.getId());
            if (payment != null) {
                payment.setStatus(3); // 已退款
                paymentService.updateById(payment);
            }
        } else {
            // 拒绝退款
            order.setStatus(order.getShippingTime() == null ? 1 : 2); // 恢复原状态
            updateById(order);
            refundService.updateStatus(refund.getId(), 2);
        }

        return true;
    }

    @Override
    public Page<OrderVO> page(Long userId, Integer status, Integer page, Integer size) {
        Page<OrderVO> pageParam = new Page<>(page, size);
        return baseMapper.selectOrderPage(pageParam, userId, status);
    }

    @Override
    public OrderVO getDetail(Long id) {
        return baseMapper.selectOrderDetail(id);
    }

    @Override
    public Page<OrderVO> getSellerOrders(Long sellerId, Integer status, Integer page, Integer size) {
        Page<OrderVO> pageParam = new Page<>(page, size);
        return baseMapper.selectSellerOrderPage(pageParam, sellerId, status);
    }

    @Override
    public Page<OrderVO> getSellerOrdersWithFilter(Long sellerId, String orderNo, String customerInfo, Integer status,
                                                 String startDate, String endDate, BigDecimal minAmount,
                                                 BigDecimal maxAmount, Integer page, Integer size) {
        Page<OrderVO> pageParam = new Page<>(page, size);

        // 转换日期字符串为LocalDate对象
        LocalDate startLocalDate = null;
        LocalDate endLocalDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            startLocalDate = LocalDate.parse(startDate);
        }

        if (endDate != null && !endDate.isEmpty()) {
            endLocalDate = LocalDate.parse(endDate);
        }

        return baseMapper.selectSellerOrdersWithFilter(pageParam, sellerId, orderNo, customerInfo, status,
                                                     startLocalDate, endLocalDate, minAmount, maxAmount);
    }

    @Override
    public Page<OrderVO> getAllOrders(Page<OrderVO> page, String orderNo, Long userId, Long sellerId, Integer status) {
        // 查询所有订单，根据参数过滤
        return baseMapper.selectAllOrders(page, orderNo, userId, sellerId, status);
    }

    @Override
    public List<OrderVO> getOrdersByGroupOrderId(Long groupOrderId) {
        // 查询拼团订单关联的所有订单
        return baseMapper.selectOrdersByGroupOrderId(groupOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long id, Integer status, String reason) {
        // 校验订单是否存在
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 5) {
            throw new BusinessException("订单状态不允许处理退款");
        }

        // 获取退款记录
        Refund refund = refundService.getByOrderId(order.getId());
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }

        if (status == 1) {
            // 同意退款
            // 调用支付宝退款接口
            boolean refundResult = alipayUtil.refund(
                    order.getOrderNo(),
                    refund.getRefundNo(),
                    refund.getRefundAmount().toString(),
                    refund.getReason()
            );

            if (!refundResult) {
                throw new BusinessException("退款失败");
            }

            // 更新订单状态
            order.setStatus(6); // 已退款
            updateById(order);
            refundService.updateStatus(refund.getId(), 1);

            // 恢复商品库存和销量
            List<OrderItem> orderItems = orderItemService.getByOrderId(order.getId());
            for (OrderItem orderItem : orderItems) {
                Product product = productService.getById(orderItem.getProductId());
                product.setStock(product.getStock() + orderItem.getQuantity());
                product.setSales(product.getSales() - orderItem.getQuantity());
                productService.updateById(product);
            }

            // 更新支付记录
            Payment payment = paymentService.getByOrderId(order.getId());
            if (payment != null) {
                payment.setStatus(3); // 已退款
                paymentService.updateById(payment);
            }
        } else {
            // 拒绝退款
            order.setStatus(order.getShippingTime() == null ? 1 : 2); // 恢复原状态
            updateById(order);
            refundService.updateStatus(refund.getId(), 2);

            // 不使用rejectReason字段，因为数据库中没有这个字段
        }

        return true;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) +
                UUID.randomUUID().toString().substring(0, 6);
    }
}
