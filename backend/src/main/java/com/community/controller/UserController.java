package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.entity.User;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        
        // 获取用户信息
        User user = userService.getByUsername(username);
        user.setPassword(null); // 不返回密码
        
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", user);
        
        return R.ok(map);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody User user) {
        userService.register(user);
        return R.ok();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public R<User> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        user.setPassword(null); // 不返回密码
        return R.ok(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public R<Void> updateUserInfo(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setId(userId);
        user.setPassword(null); 
        userService.updateById(user);
        return R.ok();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        
        // 校验旧密码
        if (!cn.dev33.satoken.secure.BCrypt.checkpw(oldPassword, user.getPassword())) {
            return R.failed("旧密码错误");
        }
        
        // 更新密码
        user.setPassword(cn.dev33.satoken.secure.BCrypt.hashpw(newPassword));
        userService.updateById(user);
        
        return R.ok();
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }
}
