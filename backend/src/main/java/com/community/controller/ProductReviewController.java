package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.ProductReview;
import com.community.service.ProductReviewService;
import com.community.vo.ProductReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品评价控制器
 */
@RestController
@RequestMapping("/product/review")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    /**
     * 分页查询商品评价列表
     */
    @GetMapping("/page")
    public R<Page<ProductReviewVO>> page(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(productReviewService.page(productId, userId, page, size));
    }

    /**
     * 发表评价
     */
    @PostMapping
    public R<Long> publish(@RequestBody ReviewRequest reviewRequest) {
        // 设置用户ID
        reviewRequest.getReview().setUserId(StpUtil.getLoginIdAsLong());

        Long reviewId = productReviewService.publish(reviewRequest.getReview(), reviewRequest.getImageUrls());
        return R.ok(reviewId);
    }

    /**
     * 评价请求包装类
     */
    public static class ReviewRequest {
        private ProductReview review;
        private List<String> imageUrls;

        public ProductReview getReview() {
            return review;
        }

        public void setReview(ProductReview review) {
            this.review = review;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    public R<ProductReviewVO> getDetail(@PathVariable Long id) {
        ProductReviewVO reviewVO = productReviewService.getDetail(id);
        if (reviewVO == null) {
            return R.failed("评价不存在");
        }
        return R.ok(reviewVO);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验是否有权限删除
        ProductReview review = productReviewService.getById(id);
        if (review == null) {
            return R.failed("评价不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!review.getUserId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        productReviewService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除评价（管理员接口）
     */
    @PostMapping("/batch/delete")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        // 校验权限
        StpUtil.checkRole("admin");

        productReviewService.batchDelete(ids);
        return R.ok();
    }
}
