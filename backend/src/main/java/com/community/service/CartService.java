package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.Cart;
import com.community.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean add(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     * @param id 购物车ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean updateQuantity(Long id, Integer quantity);

    /**
     * 更新购物车商品选中状态
     * @param id 购物车ID
     * @param selected 是否选中：0-未选中，1-已选中
     * @return 是否成功
     */
    boolean updateSelected(Long id, Integer selected);

    /**
     * 全选/取消全选
     * @param userId 用户ID
     * @param selected 是否选中：0-未选中，1-已选中
     * @return 是否成功
     */
    boolean selectAll(Long userId, Integer selected);

    /**
     * 删除购物车商品
     * @param id 购物车ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 获取用户购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> list(Long userId);

    /**
     * 获取用户购物车商品数量
     * @param userId 用户ID
     * @return 商品数量
     */
    int getCartProductCount(Long userId);
}
