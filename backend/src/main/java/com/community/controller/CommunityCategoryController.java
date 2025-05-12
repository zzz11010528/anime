package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.result.R;
import com.community.entity.CommunityCategory;
import com.community.entity.Post;
import com.community.mapper.PostMapper;
import com.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区分类控制器
 */
@RestController
@RequestMapping("/community/category")
@RequiredArgsConstructor
public class CommunityCategoryController {

    private final CommunityCategoryService communityCategoryService;
    private final PostMapper postMapper;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public R<List<CommunityCategory>> list() {
        // 只返回状态正常的分类
        List<CommunityCategory> categories = communityCategoryService.list(
            new LambdaQueryWrapper<CommunityCategory>()
                .eq(CommunityCategory::getStatus, 1)
                .orderByAsc(CommunityCategory::getSort)
        );

        // 查询每个分类下的帖子数量
        for (CommunityCategory category : categories) {
            // 只统计状态正常的帖子
            Long count = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                    .eq(Post::getCategoryId, category.getId())
                    .eq(Post::getStatus, 1)
            );
            category.setPostCount(count.intValue());
        }

        return R.ok(categories);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public R<CommunityCategory> getById(@PathVariable Long id) {
        return R.ok(communityCategoryService.getById(id));
    }

    /**
     * 添加分类（管理员接口）
     */
    @PostMapping
    public R<Void> add(@RequestBody CommunityCategory category) {
        // 校验权限
        StpUtil.checkRole("admin");
        communityCategoryService.save(category);
        return R.ok();
    }

    /**
     * 更新分类（管理员接口）
     */
    @PutMapping
    public R<Void> update(@RequestBody CommunityCategory category) {
        // 校验权限
        StpUtil.checkRole("admin");
        communityCategoryService.updateById(category);
        return R.ok();
    }

    /**
     * 删除分类（管理员接口）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验权限
        StpUtil.checkRole("admin");
        communityCategoryService.removeById(id);
        return R.ok();
    }
}
