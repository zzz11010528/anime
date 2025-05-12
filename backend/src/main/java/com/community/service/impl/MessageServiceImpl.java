package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.Message;
import com.community.entity.User;
import com.community.mapper.MessageMapper;
import com.community.service.MessageService;
import com.community.service.UserService;
import com.community.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 私信服务实现类
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;

    @Override
    public Long send(Long fromUserId, Long toUserId, String content) {
        // 校验发送者是否存在
        User fromUser = userService.getById(fromUserId);
        if (fromUser == null) {
            throw new BusinessException("发送者不存在");
        }

        // 校验接收者是否存在
        User toUser = userService.getById(toUserId);
        if (toUser == null) {
            throw new BusinessException("接收者不存在");
        }

        // 不能给自己发私信
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能给自己发私信");
        }

        // 创建私信
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        save(message);

        return message.getId();
    }

    @Override
    public boolean markAsRead(Long id) {
        // 校验私信是否存在
        Message message = getById(id);
        if (message == null) {
            return false;
        }

        // 标记为已读
        message.setIsRead(1);
        return updateById(message);
    }

    @Override
    public boolean markConversationAsRead(Long userId, Long targetUserId) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getToUserId, userId);
        wrapper.eq(Message::getFromUserId, targetUserId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.set(Message::getIsRead, 1);
        return update(wrapper);
    }

    @Override
    public Page<MessageVO> page(Long userId, Long targetUserId, Integer page, Integer size) {
        Page<MessageVO> pageParam = new Page<>(page, size);
        return baseMapper.selectMessagePage(pageParam, userId, targetUserId);
    }

    @Override
    public List<MessageVO> getConversationList(Long userId) {
        return baseMapper.selectConversationList(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.selectUnreadCount(userId);
    }

    @Override
    public boolean deleteConversation(Long userId, Long targetUserId) {
        // 删除与目标用户的所有私信（包括发送和接收的）
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(inner -> inner.eq(Message::getFromUserId, userId).eq(Message::getToUserId, targetUserId))
                .or(inner -> inner.eq(Message::getFromUserId, targetUserId).eq(Message::getToUserId, userId))
        );
        return remove(wrapper);
    }
}
