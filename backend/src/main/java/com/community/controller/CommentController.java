package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Comment;
import com.community.service.CommentService;
import com.community.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询评论列表
     */
    @GetMapping("/page")
    public R<Page<CommentVO>> page(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "0") Long parentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(commentService.page(postId, parentId, page, size));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public R<Long> publish(@RequestBody Comment comment) {
        // 设置用户ID
        comment.setUserId(StpUtil.getLoginIdAsLong());

        Long commentId = commentService.publish(comment);
        return R.ok(commentId);
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public R<CommentVO> getDetail(@PathVariable Long id) {
        CommentVO commentVO = commentService.getDetail(id);
        if (commentVO == null) {
            return R.failed("评论不存在");
        }
        return R.ok(commentVO);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验是否有权限删除
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return R.failed("评论不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!comment.getUserId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        commentService.delete(id);
        return R.ok();
    }
}
