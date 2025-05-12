package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.ProductCategory;
import com.community.service.ProductCategoryService;
import com.community.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public R<List<ProductCategory>> getCategoryTree() {
        return R.ok(productCategoryService.getCategoryTree());
    }

    /**
     * 获取分类树VO（带子分类）
     */
    @GetMapping("/tree/vo")
    public R<List<ProductCategoryVO>> getCategoryTreeVO() {
        return R.ok(productCategoryService.getCategoryTreeVO());
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public R<List<ProductCategory>> list() {
        // 只返回状态正常的分类
        return R.ok(productCategoryService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort)
        ));
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public R<ProductCategory> getById(@PathVariable Long id) {
        return R.ok(productCategoryService.getById(id));
    }

    /**
     * 添加分类（管理员接口）
     */
    @PostMapping
    public R<Void> add(@RequestBody ProductCategory category) {
        // 校验权限
        StpUtil.checkRole("admin");
        productCategoryService.save(category);
        return R.ok();
    }

    /**
     * 更新分类（管理员接口）
     */
    @PutMapping
    public R<Void> update(@RequestBody ProductCategory category) {
        // 校验权限
        StpUtil.checkRole("admin");
        productCategoryService.updateById(category);
        return R.ok();
    }

    /**
     * 删除分类（管理员接口）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验权限
        StpUtil.checkRole("admin");
        productCategoryService.removeById(id);
        return R.ok();
    }

    /**
     * 更新分类状态（管理员接口）
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        // 校验权限
        StpUtil.checkRole("admin");
        ProductCategory category = productCategoryService.getById(id);
        if (category == null) {
            return R.failed("分类不存在");
        }
        category.setStatus(status);
        productCategoryService.updateById(category);
        return R.ok();
    }
}
