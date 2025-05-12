package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.GroupOrder;
import com.community.vo.GroupOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拼团订单Mapper接口
 */
public interface GroupOrderMapper extends BaseMapper<GroupOrder> {

    /**
     * 获取拼团订单详情
     * @param id 拼团订单ID
     * @return 拼团订单详情
     */
    GroupOrderVO selectGroupOrderDetail(@Param("id") Long id);

    /**
     * 获取拼团中的订单列表
     * @param groupBuyingId 拼团活动ID
     * @return 拼团订单列表
     */
    List<GroupOrderVO> selectGroupingOrders(@Param("groupBuyingId") Long groupBuyingId);

    /**
     * 获取已成团的订单列表
     * @param groupBuyingId 拼团活动ID
     * @return 拼团订单列表
     */
    List<GroupOrderVO> selectGroupedOrders(@Param("groupBuyingId") Long groupBuyingId);

    /**
     * 统计拼团活动的拼团中订单数量
     * @param groupBuyingId 拼团活动ID
     * @return 拼团中订单数量
     */
    int countGroupingOrders(@Param("groupBuyingId") Long groupBuyingId);

    /**
     * 获取用户参与的拼团订单列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 拼团状态
     * @return 拼团订单列表
     */
    Page<GroupOrderVO> selectUserGroupOrders(Page<GroupOrderVO> page, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计拼团活动的已成团订单数量
     * @param groupBuyingId 拼团活动ID
     * @return 已成团订单数量
     */
    int countGroupedOrders(@Param("groupBuyingId") Long groupBuyingId);
}
