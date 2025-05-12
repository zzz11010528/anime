package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.GroupBuying;
import com.community.vo.GroupBuyingVO;
import org.apache.ibatis.annotations.Param;

/**
 * 拼团活动Mapper接口
 */
public interface GroupBuyingMapper extends BaseMapper<GroupBuying> {

    /**
     * 分页查询拼团活动列表
     * @param page 分页参数
     * @param sellerId 商家ID
     * @param keyword 关键词
     * @param status 状态
     * @return 拼团活动列表
     */
    Page<GroupBuyingVO> selectGroupBuyingPage(
            Page<GroupBuyingVO> page,
            @Param("sellerId") Long sellerId,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    /**
     * 获取拼团活动详情
     * @param id 拼团活动ID
     * @return 拼团活动详情
     */
    GroupBuyingVO selectGroupBuyingDetail(@Param("id") Long id);
}
