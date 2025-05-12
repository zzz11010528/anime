package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.Product;
import com.community.entity.ProductDetail;
import com.community.entity.ProductImage;
import com.community.service.ProductService;
import com.community.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public R<Page<ProductVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ipId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "created_at") String orderBy,
            @RequestParam(defaultValue = "false") Boolean isAsc) {
        return R.ok(productService.page(page, size, categoryId, ipId, keyword, minPrice, maxPrice, userId, status, orderBy, isAsc));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public R<ProductVO> getDetail(@PathVariable Long id) {
        return R.ok(productService.getDetail(id));
    }

    /**
     * 发布商品
     */
    @PostMapping
    public R<Void> publish(@RequestBody Map<String, Object> params) {
        Product product = new Product();
        ProductDetail detail = new ProductDetail();
        List<ProductImage> images = null;

        // 设置用户ID
        product.setUserId(StpUtil.getLoginIdAsLong());

        // 解析商品基本信息
        if (params.containsKey("name")) {
            product.setName((String) params.get("name"));
        }
        if (params.containsKey("description")) {
            product.setDescription((String) params.get("description"));
        }
        if (params.containsKey("price")) {
            product.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        }
        if (params.containsKey("stock")) {
            product.setStock(Integer.valueOf(params.get("stock").toString()));
        }
        if (params.containsKey("categoryId")) {
            product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.containsKey("ipId")) {
            product.setIpId(Long.valueOf(params.get("ipId").toString()));
        }
        if (params.containsKey("mainImage")) {
            product.setMainImage((String) params.get("mainImage"));
        }

        // 初始化销量
        product.setSales(0);

        // 解析商品详情
        if (params.containsKey("content")) {
            detail.setContent((String) params.get("content"));
        }

        // 解析商品图片
        if (params.containsKey("images") && params.get("images") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) params.get("images");
            if (!imageList.isEmpty()) {
                images = new java.util.ArrayList<>();
                for (int i = 0; i < imageList.size(); i++) {
                    Map<String, Object> imageMap = imageList.get(i);
                    ProductImage image = new ProductImage();
                    image.setImageUrl((String) imageMap.get("imageUrl"));
                    image.setSort(i + 1);
                    images.add(image);
                }
            }
        }

        productService.publish(product, detail, images);
        return R.ok();
    }

    /**
     * 更新商品
     */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> params) {
        Product product = new Product();
        ProductDetail detail = new ProductDetail();
        List<ProductImage> images = null;

        // 校验是否有权限修改
        Long userId = StpUtil.getLoginIdAsLong();
        product.setUserId(userId);

        // 解析商品ID
        if (params.containsKey("id")) {
            product.setId(Long.valueOf(params.get("id").toString()));
            detail.setProductId(product.getId());
        } else {
            return R.failed("商品ID不能为空");
        }

        // 解析商品基本信息
        if (params.containsKey("name")) {
            product.setName((String) params.get("name"));
        }
        if (params.containsKey("description")) {
            product.setDescription((String) params.get("description"));
        }
        if (params.containsKey("price")) {
            product.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        }
        if (params.containsKey("stock")) {
            product.setStock(Integer.valueOf(params.get("stock").toString()));
        }
        if (params.containsKey("categoryId")) {
            product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.containsKey("ipId")) {
            product.setIpId(Long.valueOf(params.get("ipId").toString()));
        }
        if (params.containsKey("mainImage")) {
            product.setMainImage((String) params.get("mainImage"));
        }

        // 解析商品详情
        if (params.containsKey("content")) {
            detail.setContent((String) params.get("content"));
        }

        // 解析商品图片
        if (params.containsKey("images") && params.get("images") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) params.get("images");
            if (!imageList.isEmpty()) {
                images = new java.util.ArrayList<>();
                for (int i = 0; i < imageList.size(); i++) {
                    Map<String, Object> imageMap = imageList.get(i);
                    ProductImage image = new ProductImage();
                    image.setImageUrl((String) imageMap.get("imageUrl"));
                    image.setSort(i + 1);
                    images.add(image);
                }
            }
        }

        productService.update(product, detail, images);
        return R.ok();
    }

    /**
     * 审核商品（管理员接口）
     */
    @PostMapping("/audit")
    public R<Void> audit(@RequestParam Long id, @RequestParam Integer status) {
        // 校验权限
        StpUtil.checkRole("admin");
        productService.audit(id, status);
        return R.ok();
    }

    /**
     * 上下架商品
     */
    @PostMapping("/status")
    public R<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        // 校验是否有权限修改
        Product product = productService.getById(id);
        if (product == null) {
            return R.failed("商品不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!product.getUserId().equals(userId) && !StpUtil.hasRole("admin")) {
            return R.failed("无权操作");
        }

        productService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 获取用户发布的商品列表
     */
    @GetMapping("/user")
    public R<Page<Product>> getUserProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(productService.getUserProducts(userId, page, size));
    }
}
