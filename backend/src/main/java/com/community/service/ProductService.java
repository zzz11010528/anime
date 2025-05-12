package com.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Product;
import com.community.entity.ProductDetail;
import com.community.entity.ProductImage;
import com.community.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 发布商品
     * @param product 商品信息
     * @param detail 商品详情
     * @param images 商品图片
     * @return 是否成功
     */
    boolean publish(Product product, ProductDetail detail, List<ProductImage> images);

    /**
     * 更新商品
     * @param product 商品信息
     * @param detail 商品详情
     * @param images 商品图片
     * @return 是否成功
     */
    boolean update(Product product, ProductDetail detail, List<ProductImage> images);

    /**
     * 审核商品
     * @param id 商品ID
     * @param status 审核状态：1-通过，0-拒绝
     * @return 是否成功
     */
    boolean audit(Long id, Integer status);

    /**
     * 上下架商品
     * @param id 商品ID
     * @param status 状态：0-下架，1-上架
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 分页查询商品列表
     * @param page 页码
     * @param size 每页大小
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
    Page<ProductVO> page(Integer page, Integer size, Long categoryId, Long ipId, String keyword, Integer minPrice, Integer maxPrice, Long userId, Integer status, String orderBy, Boolean isAsc);

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getDetail(Long id);

    /**
     * 获取用户发布的商品列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 商品列表
     */
    Page<Product> getUserProducts(Long userId, Integer page, Integer size);

    /**
     * 更新收藏数
     * @param id 商品ID
     * @param count 增加的数量，可以为负数
     * @return 是否成功
     */
    boolean updateCollectCount(Long id, int count);
}
