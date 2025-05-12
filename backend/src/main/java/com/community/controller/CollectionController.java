package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.service.CollectionService;
import com.community.vo.PostVO;
import com.community.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 收藏
     */
    @PostMapping
    public R<Void> collect(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        collectionService.collect(userId, targetId, type);
        return R.ok();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public R<Void> uncollect(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        collectionService.uncollect(userId, targetId, type);
        return R.ok();
    }

    /**
     * 是否已收藏
     */
    @GetMapping("/status")
    public R<Boolean> isCollected(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(collectionService.isCollected(userId, targetId, type));
    }

    /**
     * 获取用户收藏的帖子列表
     */
    @GetMapping("/post")
    public R<Page<PostVO>> getCollectedPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(collectionService.getCollectedPosts(userId, page, size));
    }

    /**
     * 获取用户收藏的商品列表
     */
    @GetMapping("/product")
    public R<Page<ProductVO>> getCollectedProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(collectionService.getCollectedProducts(userId, page, size));
    }
}
