package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.ProductReview;
import com.community.vo.ProductReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品评价Mapper接口
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    /**
     * 分页查询商品评价列表
     * @param page 分页参数
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 评价列表
     */
    Page<ProductReviewVO> selectProductReviewPage(Page<ProductReviewVO> page,
                                                @Param("productId") Long productId,
                                                @Param("userId") Long userId);

    /**
     * 获取评价详情
     * @param id 评价ID
     * @return 评价详情
     */
    ProductReviewVO selectProductReviewDetail(@Param("id") Long id);
}
