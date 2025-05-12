package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.service.CartService;
import com.community.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public R<List<CartVO>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(cartService.list(userId));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public R<Void> add(@RequestParam Long productId, @RequestParam Integer quantity) {
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.add(userId, productId, quantity);
        return R.ok();
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/update/quantity")
    public R<Void> updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return R.ok();
    }

    /**
     * 更新购物车商品选中状态
     */
    @PostMapping("/update/selected")
    public R<Void> updateSelected(@RequestParam Long id, @RequestParam Integer selected) {
        cartService.updateSelected(id, selected);
        return R.ok();
    }

    /**
     * 全选/取消全选
     */
    @PostMapping("/select/all")
    public R<Void> selectAll(@RequestParam Integer selected) {
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.selectAll(userId, selected);
        return R.ok();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return R.ok();
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public R<Integer> count() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(cartService.getCartProductCount(userId));
    }
}
