package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.Notification;
import com.community.entity.User;
import com.community.mapper.NotificationMapper;
import com.community.service.NotificationService;
import com.community.service.UserService;
import com.community.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final UserService userService;

    @Override
    public Long create(Long userId, Integer type, Long targetId, String content) {
        // 校验用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }

        // 创建通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notification.setIsRead(0);
        save(notification);

        return notification.getId();
    }

    @Override
    public boolean markAsRead(Long id) {
        // 校验通知是否存在
        Notification notification = getById(id);
        if (notification == null) {
            return false;
        }

        // 标记为已读
        notification.setIsRead(1);
        return updateById(notification);
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getIsRead, 0);
        wrapper.set(Notification::getIsRead, 1);
        return update(wrapper);
    }

    @Override
    public Page<NotificationVO> page(Long userId, Integer type, Integer page, Integer size) {
        Page<NotificationVO> pageParam = new Page<>(page, size);
        return baseMapper.selectNotificationPage(pageParam, userId, type);
    }

    @Override
    public int getUnreadCount(Long userId, Integer type) {
        return baseMapper.selectUnreadCount(userId, type);
    }
}
