package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Order;
import com.community.vo.OrderVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单（从购物车）
     * @param userId 用户ID
     * @param shippingName 收货人姓名
     * @param shippingPhone 收货人电话
     * @param shippingAddress 收货地址
     * @return 订单ID
     */
    Long create(Long userId, String shippingName, String shippingPhone, String shippingAddress);

    /**
     * 直接购买创建订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 购买数量
     * @param shippingName 收货人姓名
     * @param shippingPhone 收货人电话
     * @param shippingAddress 收货地址
     * @return 订单ID
     */
    Long createDirect(Long userId, Long productId, Integer quantity, String shippingName, String shippingPhone, String shippingAddress);

    /**
     * 取消订单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean cancel(Long id);

    /**
     * 支付订单
     * @param id 订单ID
     * @param payType 支付方式：1-支付宝
     * @return 支付信息
     */
    String pay(Long id, Integer payType);

    /**
     * 支付回调
     * @param orderNo 订单编号
     * @param paymentNo 支付流水号
     * @param amount 支付金额
     * @return 是否成功
     */
    boolean payCallback(String orderNo, String paymentNo, String amount);

    /**
     * 发货
     * @param id 订单ID
     * @return 是否成功
     */
    boolean ship(Long id);

    /**
     * 确认收货
     * @param id 订单ID
     * @return 是否成功
     */
    boolean receive(Long id);

    /**
     * 申请退款
     * @param id 订单ID
     * @param reason 退款原因
     * @return 是否成功
     */
    boolean applyRefund(Long id, String reason);

    /**
     * 退款处理
     * @param id 订单ID
     * @param status 处理结果：1-同意，2-拒绝
     * @return 是否成功
     */
    boolean handleRefund(Long id, Integer status);

    /**
     * 分页查询订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderVO> page(Long userId, Integer status, Integer page, Integer size);

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderVO getDetail(Long id);

    /**
     * 分页查询商家订单列表
     * @param sellerId 商家ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderVO> getSellerOrders(Long sellerId, Integer status, Integer page, Integer size);

    /**
     * 分页查询商家订单列表（带高级筛选）
     * @param sellerId 商家ID
     * @param orderNo 订单号
     * @param customerInfo 客户信息（姓名/手机号）
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderVO> getSellerOrdersWithFilter(Long sellerId, String orderNo, String customerInfo, Integer status,
                                           String startDate, String endDate, BigDecimal minAmount,
                                           BigDecimal maxAmount, Integer page, Integer size);

    /**
     * 分页查询所有订单列表（管理员接口）
     * @param page 分页参数
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @param sellerId 商家ID
     * @param status 订单状态
     * @return 订单列表
     */
    Page<OrderVO> getAllOrders(Page<OrderVO> page, String orderNo, Long userId, Long sellerId, Integer status);

    /**
     * 处理退款申请（带拒绝原因）
     * @param id 订单ID
     * @param status 处理结果：1-同意，2-拒绝
     * @param reason 拒绝原因
     * @return 是否成功
     */
    boolean handleRefund(Long id, Integer status, String reason);

    /**
     * 根据拼团订单ID获取订单列表
     * @param groupOrderId 拼团订单ID
     * @return 订单列表
     */
    List<OrderVO> getOrdersByGroupOrderId(Long groupOrderId);
}
