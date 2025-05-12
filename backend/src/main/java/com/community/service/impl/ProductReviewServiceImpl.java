package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.entity.ProductReview;
import com.community.entity.ProductReviewImage;
import com.community.common.exception.BusinessException;
import com.community.mapper.ProductReviewImageMapper;
import com.community.mapper.ProductReviewMapper;
import com.community.service.ProductReviewService;
import com.community.service.ProductService;
import com.community.vo.ProductReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商品评价服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    private final ProductReviewImageMapper productReviewImageMapper;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ProductReview productReview, List<String> imageUrls) {
        // 校验商品是否存在
        if (productService.getById(productReview.getProductId()) == null) {
            throw new BusinessException("商品不存在");
        }

        // 设置状态
        productReview.setStatus(1);

        // 保存评价
        save(productReview);

        // 保存评价图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<ProductReviewImage> images = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductReviewImage image = new ProductReviewImage();
                image.setReviewId(productReview.getId());
                image.setImageUrl(imageUrls.get(i));
                image.setSort(i);
                images.add(image);
            }
            for (ProductReviewImage image : images) {
                productReviewImageMapper.insert(image);
            }
        }

        return productReview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 校验评价是否存在
        ProductReview review = getById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 删除评价（逻辑删除）
        review.setStatus(0);
        updateById(review);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 批量更新状态为删除
        List<ProductReview> reviews = listByIds(ids);
        for (ProductReview review : reviews) {
            review.setStatus(0);
        }
        updateBatchById(reviews);

        return true;
    }

    @Override
    public Page<ProductReviewVO> page(Long productId, Long userId, Integer page, Integer size) {
        Page<ProductReviewVO> pageParam = new Page<>(page, size);
        Page<ProductReviewVO> result = baseMapper.selectProductReviewPage(pageParam, productId, userId);

        // 处理评价图片
        for (ProductReviewVO review : result.getRecords()) {
            processImagesConcat(review);
        }

        return result;
    }

    /**
     * 处理评价图片字符串
     */
    private void processImagesConcat(ProductReviewVO review) {
        String imagesConcat = review.getImagesConcat();
        if (imagesConcat != null && !imagesConcat.isEmpty()) {
            review.setImages(Arrays.asList(imagesConcat.split(",")));
        } else {
            review.setImages(new ArrayList<>());
        }
    }

    @Override
    public ProductReviewVO getDetail(Long id) {
        // 查询评价详情
        ProductReviewVO review = baseMapper.selectProductReviewDetail(id);
        if (review == null) {
            return null;
        }

        // 处理评价图片
        processImagesConcat(review);

        return review;
    }
}
