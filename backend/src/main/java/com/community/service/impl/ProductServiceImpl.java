package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.*;
import com.community.mapper.ProductDetailMapper;
import com.community.mapper.ProductImageMapper;
import com.community.mapper.ProductMapper;
import com.community.service.ProductCategoryService;
import com.community.service.ProductService;
import com.community.service.UserService;
import com.community.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductDetailMapper productDetailMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductCategoryService productCategoryService;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Product product, ProductDetail detail, List<ProductImage> images) {
        // 校验用户是否存在
        User user = userService.getById(product.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验分类是否存在
        ProductCategory category = productCategoryService.getById(product.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 设置商品状态
        if (user.getRole() == 1) {
            // 商家直接上架
            product.setStatus(1);
            product.setIsCertified(1);
        } else {
            // 普通用户需要审核
            product.setStatus(2);
            product.setIsCertified(0);
        }

        // 保存商品
        boolean result = save(product);
        if (!result) {
            throw new BusinessException("发布商品失败");
        }

        // 保存商品详情
        detail.setProductId(product.getId());
        productDetailMapper.insert(detail);

        // 保存商品图片
        if (images != null && !images.isEmpty()) {
            for (ProductImage image : images) {
                image.setProductId(product.getId());
                productImageMapper.insert(image);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Product product, ProductDetail detail, List<ProductImage> images) {
        // 校验商品是否存在
        Product existProduct = getById(product.getId());
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验是否有权限修改
        if (!existProduct.getUserId().equals(product.getUserId())) {
            throw new BusinessException("无权修改该商品");
        }

        // 校验分类是否存在
        ProductCategory category = productCategoryService.getById(product.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 更新商品
        boolean result = updateById(product);
        if (!result) {
            throw new BusinessException("更新商品失败");
        }

        // 更新商品详情
        LambdaQueryWrapper<ProductDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(ProductDetail::getProductId, product.getId());
        ProductDetail existDetail = productDetailMapper.selectOne(detailWrapper);
        if (existDetail != null) {
            detail.setId(existDetail.getId());
            productDetailMapper.updateById(detail);
        } else {
            detail.setProductId(product.getId());
            productDetailMapper.insert(detail);
        }

        // 删除原有商品图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getId());
        productImageMapper.delete(imageWrapper);

        // 保存新的商品图片
        if (images != null && !images.isEmpty()) {
            for (ProductImage image : images) {
                image.setProductId(product.getId());
                productImageMapper.insert(image);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer status) {
        // 校验商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品状态
        if (product.getStatus() != 2) {
            throw new BusinessException("该商品不在审核中");
        }

        // 更新商品状态
        product.setStatus(status);
        return updateById(product);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        // 校验商品是否存在
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验是否有权限修改
        User user = userService.getById(product.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新商品状态
        product.setStatus(status);
        return updateById(product);
    }

    @Override
    public Page<ProductVO> page(Integer page, Integer size, Long categoryId, Long ipId, String keyword, Integer minPrice, Integer maxPrice, Long userId, Integer status, String orderBy, Boolean isAsc) {
        Page<ProductVO> pageParam = new Page<>(page, size);

        // 如果有分类ID，获取所有子分类ID
        List<Long> categoryIds = null;
        if (categoryId != null && categoryId > 0) {
            categoryIds = productCategoryService.getChildrenIds(categoryId);
        }

        return baseMapper.selectProductPage(pageParam, categoryId, ipId, keyword, minPrice, maxPrice, userId, status, orderBy, isAsc);
    }

    @Override
    public ProductVO getDetail(Long id) {
        // 获取商品基本信息和详情
        ProductVO productVO = baseMapper.selectProductDetail(id);

        if (productVO != null) {
            // 查询商品图片
            LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductImage::getProductId, id);
            wrapper.orderByAsc(ProductImage::getSort);
            List<ProductImage> images = productImageMapper.selectList(wrapper);

            // 设置商品图片
            productVO.setImages(images);
        }

        return productVO;
    }

    @Override
    public Page<Product> getUserProducts(Long userId, Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        wrapper.orderByDesc(Product::getCreatedAt);
        return page(pageParam, wrapper);
    }

    @Override
    public boolean updateCollectCount(Long id, int count) {
        // 由于数据库中没有collect_count字段，此方法暂时不做任何操作
        return true;
    }
}
