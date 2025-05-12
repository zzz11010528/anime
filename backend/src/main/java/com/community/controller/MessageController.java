package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.service.MessageService;
import com.community.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信控制器
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public R<Long> send(@RequestParam Long toUserId, @RequestParam String content) {
        Long fromUserId = StpUtil.getLoginIdAsLong();
        Long messageId = messageService.send(fromUserId, toUserId, content);
        return R.ok(messageId);
    }

    /**
     * 标记为已读
     */
    @PostMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return R.ok();
    }

    /**
     * 标记会话为已读
     */
    @PostMapping("/read/conversation/{targetUserId}")
    public R<Void> markConversationAsRead(@PathVariable Long targetUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        messageService.markConversationAsRead(userId, targetUserId);
        return R.ok();
    }

    /**
     * 分页查询私信列表
     */
    @GetMapping("/page")
    public R<Page<MessageVO>> page(
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(messageService.page(userId, targetUserId, page, size));
    }

    /**
     * 获取用户的私信会话列表
     */
    @GetMapping("/conversation/list")
    public R<List<MessageVO>> getConversationList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(messageService.getConversationList(userId));
    }

    /**
     * 获取未读私信数量
     */
    @GetMapping("/unread/count")
    public R<Integer> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(messageService.getUnreadCount(userId));
    }

    /**
     * 删除私信会话
     */
    @DeleteMapping("/conversation/{targetUserId}")
    public R<Void> deleteConversation(@PathVariable Long targetUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = messageService.deleteConversation(userId, targetUserId);
        return success ? R.ok() : R.failed("删除失败");
    }
}
