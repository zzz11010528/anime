package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.GroupOrder;
import com.community.vo.GroupOrderVO;

import java.util.List;

/**
 * 拼团订单服务接口
 */
public interface GroupOrderService extends IService<GroupOrder> {

    /**
     * 创建拼团订单
     * @param groupBuyingId 拼团活动ID
     * @param userId 用户ID
     * @return 拼团订单ID
     */
    Long create(Long groupBuyingId, Long userId);

    /**
     * 加入拼团
     * @param groupOrderId 拼团订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean join(Long groupOrderId, Long userId);

    /**
     * 检查拼团是否成功
     * @param groupOrderId 拼团订单ID
     * @return 是否成功
     */
    boolean checkGroupSuccess(Long groupOrderId);

    /**
     * 解散拼团
     * @param groupOrderId 拼团订单ID
     * @return 是否成功
     */
    boolean dissolve(Long groupOrderId);

    /**
     * 获取拼团订单详情
     * @param id 拼团订单ID
     * @return 拼团订单详情
     */
    GroupOrderVO getDetail(Long id);

    /**
     * 获取拼团中的订单列表
     * @param groupBuyingId 拼团活动ID
     * @return 拼团订单列表
     */
    List<GroupOrderVO> getGroupingOrders(Long groupBuyingId);

    /**
     * 获取已成团的订单列表
     * @param groupBuyingId 拼团活动ID
     * @return 拼团订单列表
     */
    List<GroupOrderVO> getGroupedOrders(Long groupBuyingId);

    /**
     * 检查并更新过期的拼团订单
     * 定时任务调用，检查是否有过期的拼团订单，如果有则解散
     * @return 更新数量
     */
    int checkAndUpdateExpiredOrders();

    /**
     * 获取用户参与的拼团订单列表
     * @param userId 用户ID
     * @param status 拼团状态
     * @param page 页码
     * @param size 每页大小
     * @return 拼团订单列表
     */
    Page<GroupOrderVO> getUserOrders(Long userId, Integer status, Integer page, Integer size);
}
