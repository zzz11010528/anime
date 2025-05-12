package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.result.R;
import com.community.entity.CommunityCategory;
import com.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员-社区分类控制器
 */
@RestController
@RequestMapping("/admin/community/category")
@RequiredArgsConstructor
public class AdminCommunityCategoryController {

    private final CommunityCategoryService communityCategoryService;

    /**
     * 获取所有分类列表（包括禁用的）
     */
    @GetMapping("/list")
    public R<List<CommunityCategory>> list() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有分类，包括禁用的
        return R.ok(communityCategoryService.list(
            new LambdaQueryWrapper<CommunityCategory>()
                .orderByAsc(CommunityCategory::getSort)
        ));
    }
}
