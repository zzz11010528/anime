package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.ProductReview;
import com.community.vo.ProductReviewVO;

import java.util.List;

/**
 * 商品评价服务接口
 */
public interface ProductReviewService extends IService<ProductReview> {

    /**
     * 发表评价
     * @param productReview 评价信息
     * @param imageUrls 评价图片URL列表
     * @return 评价ID
     */
    Long publish(ProductReview productReview, List<String> imageUrls);

    /**
     * 删除评价
     * @param id 评价ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 批量删除评价
     * @param ids 评价ID列表
     * @return 是否成功
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 分页查询商品评价列表
     * @param productId 商品ID
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 评价列表
     */
    Page<ProductReviewVO> page(Long productId, Long userId, Integer page, Integer size);

    /**
     * 获取评价详情
     * @param id 评价ID
     * @return 评价详情
     */
    ProductReviewVO getDetail(Long id);
}
