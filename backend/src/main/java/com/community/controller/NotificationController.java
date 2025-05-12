package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.service.NotificationService;
import com.community.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 标记为已读
     */
    @PostMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return R.ok();
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/read/all")
    public R<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllAsRead(userId);
        return R.ok();
    }

    /**
     * 分页查询通知列表
     */
    @GetMapping("/page")
    public R<Page<NotificationVO>> page(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(notificationService.page(userId, type, page, size));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public R<Integer> getUnreadCount(@RequestParam(required = false) Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(notificationService.getUnreadCount(userId, type));
    }
}
