package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Notification;
import com.community.vo.NotificationVO;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知
     * @param userId 用户ID
     * @param type 类型：1-点赞，2-评论，3-回复，4-系统通知
     * @param targetId 目标ID
     * @param content 通知内容
     * @return 通知ID
     */
    Long create(Long userId, Integer type, Long targetId, String content);

    /**
     * 标记为已读
     * @param id 通知ID
     * @return 是否成功
     */
    boolean markAsRead(Long id);

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);

    /**
     * 分页查询通知列表
     * @param userId 用户ID
     * @param type 通知类型
     * @param page 页码
     * @param size 每页大小
     * @return 通知列表
     */
    Page<NotificationVO> page(Long userId, Integer type, Integer page, Integer size);

    /**
     * 获取未读通知数量
     * @param userId 用户ID
     * @param type 通知类型
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId, Integer type);
}
