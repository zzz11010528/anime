package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.Like;
import com.community.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 点赞
     */
    @PostMapping
    public R<Void> like(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.like(userId, targetId, type);
        return R.ok();
    }

    /**
     * 取消点赞
     */
    @DeleteMapping
    public R<Void> unlike(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.unlike(userId, targetId, type);
        return R.ok();
    }

    /**
     * 是否已点赞
     */
    @GetMapping("/status")
    public R<Boolean> isLiked(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(likeService.isLiked(userId, targetId, type));
    }

    /**
     * 获取点赞详情
     */
    @GetMapping("/{id}")
    public R<Like> getDetail(@PathVariable Long id) {
        Like like = likeService.getById(id);
        if (like == null) {
            return R.failed("点赞记录不存在");
        }
        return R.ok(like);
    }
}
