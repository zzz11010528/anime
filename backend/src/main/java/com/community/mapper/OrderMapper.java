package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Order;
import com.community.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    Page<OrderVO> selectOrderPage(Page<OrderVO> page, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderVO selectOrderDetail(@Param("id") Long id);

    /**
     * 分页查询商家订单列表
     * @param page 分页参数
     * @param sellerId 商家ID
     * @param status 订单状态
     * @return 订单列表
     */
    Page<OrderVO> selectSellerOrderPage(Page<OrderVO> page, @Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 根据拼团订单ID获取订单列表
     * @param groupOrderId 拼团订单ID
     * @return 订单列表
     */
    List<OrderVO> selectOrdersByGroupOrderId(@Param("groupOrderId") Long groupOrderId);

    /**
     * 分页查询所有订单列表（管理员接口）
     * @param page 分页参数
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @param sellerId 商家ID
     * @param status 订单状态
     * @return 订单列表
     */
    Page<OrderVO> selectAllOrders(Page<OrderVO> page,
                                 @Param("orderNo") String orderNo,
                                 @Param("userId") Long userId,
                                 @Param("sellerId") Long sellerId,
                                 @Param("status") Integer status);

    /**
     * 分页查询商家订单列表（带高级筛选）
     * @param page 分页参数
     * @param sellerId 商家ID
     * @param orderNo 订单号
     * @param customerInfo 客户信息（姓名/手机号）
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @return 订单列表
     */
    Page<OrderVO> selectSellerOrdersWithFilter(Page<OrderVO> page,
                                             @Param("sellerId") Long sellerId,
                                             @Param("orderNo") String orderNo,
                                             @Param("customerInfo") String customerInfo,
                                             @Param("status") Integer status,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("minAmount") BigDecimal minAmount,
                                             @Param("maxAmount") BigDecimal maxAmount);
}
