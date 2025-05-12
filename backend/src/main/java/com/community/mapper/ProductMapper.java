package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Product;
import com.community.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询商品列表
     * @param page 分页参数
     * @param categoryId 分类ID
     * @param ipId IP ID
     * @param keyword 关键词
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param userId 用户ID
     * @param status 商品状态
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @return 商品列表
     */
    Page<ProductVO> selectProductPage(Page<ProductVO> page,
                                     @Param("categoryId") Long categoryId,
                                     @Param("ipId") Long ipId,
                                     @Param("keyword") String keyword,
                                     @Param("minPrice") Integer minPrice,
                                     @Param("maxPrice") Integer maxPrice,
                                     @Param("userId") Long userId,
                                     @Param("status") Integer status,
                                     @Param("orderBy") String orderBy,
                                     @Param("isAsc") Boolean isAsc);

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO selectProductDetail(@Param("id") Long id);

    /**
     * 根据ID列表查询商品列表
     * @param page 分页参数
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    Page<ProductVO> selectProductsByIds(Page<ProductVO> page, @Param("productIds") List<Long> productIds);
}
