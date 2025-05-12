package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.ProductCategory;
import com.community.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 获取分类树（只包含状态正常的分类）
     * @return 分类树
     */
    List<ProductCategory> getCategoryTree();

    /**
     * 获取分类树VO（只包含状态正常的分类）
     * @return 分类树VO
     */
    List<ProductCategoryVO> getCategoryTreeVO();

    /**
     * 获取所有分类树（包括禁用的分类）
     * @return 所有分类树
     */
    List<ProductCategory> getAllCategoryTree();

    /**
     * 获取所有分类树VO（包括禁用的分类）
     * @return 所有分类树VO
     */
    List<ProductCategoryVO> getAllCategoryTreeVO();

    /**
     * 获取子分类ID列表
     * @param categoryId 分类ID
     * @return 子分类ID列表（包含自身）
     */
    List<Long> getChildrenIds(Long categoryId);
}
