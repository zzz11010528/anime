package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Notification;
import com.community.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询通知列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param type 通知类型
     * @return 通知列表
     */
    Page<NotificationVO> selectNotificationPage(Page<NotificationVO> page,
                                               @Param("userId") Long userId,
                                               @Param("type") Integer type);

    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @param type 通知类型
     * @return 未读通知数量
     */
    int selectUnreadCount(@Param("userId") Long userId, @Param("type") Integer type);
}
