package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.GroupBuying;
import com.community.vo.GroupBuyingVO;

import java.util.List;

/**
 * 拼团活动服务接口
 */
public interface GroupBuyingService extends IService<GroupBuying> {

    /**
     * 创建拼团活动
     * @param groupBuying 拼团活动信息
     * @return 拼团活动ID
     */
    Long create(GroupBuying groupBuying);

    /**
     * 更新拼团活动
     * @param groupBuying 拼团活动信息
     * @return 是否成功
     */
    boolean update(GroupBuying groupBuying);

    /**
     * 结束拼团活动
     * @param id 拼团活动ID
     * @return 是否成功
     */
    boolean end(Long id);

    /**
     * 分页查询拼团活动列表
     * @param sellerId 商家ID
     * @param keyword 关键词
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 拼团活动列表
     */
    Page<GroupBuyingVO> page(Long sellerId, String keyword, Integer status, Integer page, Integer size);

    /**
     * 获取拼团活动详情
     * @param id 拼团活动ID
     * @return 拼团活动详情
     */
    GroupBuyingVO getDetail(Long id);

    /**
     * 检查并更新拼团活动状态
     * 定时任务调用，检查未开始的拼团是否到了开始时间，进行中的拼团是否到了结束时间
     * @return 更新数量
     */
    int checkAndUpdateStatus();

    /**
     * 获取正在进行中的拼团活动列表
     * @param limit 数量限制
     * @return 拼团活动列表
     */
    List<GroupBuyingVO> getActiveGroupBuyings(Integer limit);
}
