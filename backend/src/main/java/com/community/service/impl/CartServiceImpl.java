package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.Cart;
import com.community.entity.Product;
import com.community.mapper.CartMapper;
import com.community.service.CartService;
import com.community.service.ProductService;
import com.community.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductService productService;

    @Override
    public boolean add(Long userId, Long productId, Integer quantity) {
        // 校验商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验商品是否上架
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        // 校验库存
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        // 查询购物车中是否已存在该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart cart = getOne(wrapper);

        if (cart != null) {
            // 已存在，更新数量
            cart.setQuantity(cart.getQuantity() + quantity);
            // 校验库存
            if (cart.getQuantity() > product.getStock()) {
                throw new BusinessException("商品库存不足");
            }
            return updateById(cart);
        } else {
            // 不存在，新增
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1); // 默认选中
            return save(cart);
        }
    }

    @Override
    public boolean updateQuantity(Long id, Integer quantity) {
        // 校验购物车是否存在
        Cart cart = getById(id);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        // 校验商品是否存在
        Product product = productService.getById(cart.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验库存
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        // 更新数量
        cart.setQuantity(quantity);
        return updateById(cart);
    }

    @Override
    public boolean updateSelected(Long id, Integer selected) {
        // 校验购物车是否存在
        Cart cart = getById(id);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        // 更新选中状态
        cart.setSelected(selected);
        return updateById(cart);
    }

    @Override
    public boolean selectAll(Long userId, Integer selected) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.set(Cart::getSelected, selected);
        return update(wrapper);
    }

    @Override
    public boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public List<CartVO> list(Long userId) {
        return baseMapper.selectCartList(userId);
    }

    @Override
    public int getCartProductCount(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        return (int) count(wrapper);
    }
}
