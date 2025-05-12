package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.ProductCategory;
import com.community.mapper.ProductCategoryMapper;
import com.community.service.ProductCategoryService;
import com.community.vo.ProductCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Override
    public List<ProductCategory> getCategoryTree() {
        // 只查询状态正常的分类
        List<ProductCategory> allCategories = list(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort));

        // 按父ID分组
        Map<Long, List<ProductCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        // 获取一级分类
        List<ProductCategory> rootCategories = parentMap.getOrDefault(0L, new ArrayList<>());

        // 递归设置子分类
        rootCategories.forEach(category -> setCategoryChildren(category, parentMap));

        return rootCategories;
    }

    /**
     * 递归设置子分类（针对ProductCategory实体）
     */
    private void setCategoryChildren(ProductCategory parent, Map<Long, List<ProductCategory>> parentMap) {
        List<ProductCategory> children = parentMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        children.forEach(child -> setCategoryChildren(child, parentMap));
    }

    @Override
    public List<ProductCategoryVO> getCategoryTreeVO() {
        // 只查询状态正常的分类
        List<ProductCategory> allCategories = list(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort));

        // 转换为VO对象
        List<ProductCategoryVO> categoryVOList = allCategories.stream().map(category -> {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        // 按父ID分组
        Map<Long, List<ProductCategoryVO>> parentMap = categoryVOList.stream()
                .collect(Collectors.groupingBy(ProductCategoryVO::getParentId));

        // 获取一级分类
        List<ProductCategoryVO> rootCategories = parentMap.getOrDefault(0L, new ArrayList<>());

        // 递归设置子分类
        rootCategories.forEach(category -> setChildren(category, parentMap));

        return rootCategories;
    }

    @Override
    public List<ProductCategory> getAllCategoryTree() {
        // 查询所有分类，包括禁用的
        List<ProductCategory> allCategories = list(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort));

        // 按父ID分组
        Map<Long, List<ProductCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        // 获取一级分类
        List<ProductCategory> rootCategories = parentMap.getOrDefault(0L, new ArrayList<>());

        // 递归设置子分类
        rootCategories.forEach(category -> setCategoryChildren(category, parentMap));

        return rootCategories;
    }

    @Override
    public List<ProductCategoryVO> getAllCategoryTreeVO() {
        // 查询所有分类，包括禁用的
        List<ProductCategory> allCategories = list(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort));

        // 转换为VO对象
        List<ProductCategoryVO> categoryVOList = allCategories.stream().map(category -> {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        // 按父ID分组
        Map<Long, List<ProductCategoryVO>> parentMap = categoryVOList.stream()
                .collect(Collectors.groupingBy(ProductCategoryVO::getParentId));

        // 获取一级分类
        List<ProductCategoryVO> rootCategories = parentMap.getOrDefault(0L, new ArrayList<>());

        // 递归设置子分类
        rootCategories.forEach(category -> setChildren(category, parentMap));

        return rootCategories;
    }

    @Override
    public List<Long> getChildrenIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        result.add(categoryId);

        // 查询所有分类
        List<ProductCategory> allCategories = list();

        // 递归获取子分类ID
        getChildrenIdsRecursive(categoryId, allCategories, result);

        return result;
    }

    /**
     * 递归设置子分类
     */
    private void setChildren(ProductCategoryVO parent, Map<Long, List<ProductCategoryVO>> parentMap) {
        List<ProductCategoryVO> children = parentMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        children.forEach(child -> setChildren(child, parentMap));
    }

    /**
     * 递归获取子分类ID
     */
    private void getChildrenIdsRecursive(Long parentId, List<ProductCategory> allCategories, List<Long> result) {
        allCategories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .forEach(category -> {
                    result.add(category.getId());
                    getChildrenIdsRecursive(category.getId(), allCategories, result);
                });
    }
}
