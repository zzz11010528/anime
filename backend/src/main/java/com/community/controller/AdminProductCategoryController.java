package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.result.R;
import com.community.entity.ProductCategory;
import com.community.service.ProductCategoryService;
import com.community.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员-商品分类控制器
 */
@RestController
@RequestMapping("/admin/product/category")
@RequiredArgsConstructor
public class AdminProductCategoryController {

    private final ProductCategoryService productCategoryService;

    /**
     * 获取所有分类列表（包括禁用的）
     */
    @GetMapping("/list")
    public R<List<ProductCategory>> list() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有分类，包括禁用的
        return R.ok(productCategoryService.list(
            new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort)
        ));
    }

    /**
     * 获取所有分类树（包括禁用的）
     */
    @GetMapping("/tree")
    public R<List<ProductCategory>> getCategoryTree() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有分类树，包括禁用的
        return R.ok(productCategoryService.getAllCategoryTree());
    }

    /**
     * 获取所有分类树VO（包括禁用的）
     */
    @GetMapping("/tree/vo")
    public R<List<ProductCategoryVO>> getCategoryTreeVO() {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 返回所有分类树VO，包括禁用的
        return R.ok(productCategoryService.getAllCategoryTreeVO());
    }
}
