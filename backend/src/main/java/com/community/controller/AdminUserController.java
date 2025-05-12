package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.result.R;
import com.community.entity.User;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员-用户管理控制器
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public R<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {

        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 创建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }

        if (nickname != null && !nickname.isEmpty()) {
            wrapper.like(User::getNickname, nickname);
        }

        if (email != null && !email.isEmpty()) {
            wrapper.like(User::getEmail, email);
        }

        if (phone != null && !phone.isEmpty()) {
            wrapper.like(User::getPhone, phone);
        }

        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        // 分页查询
        Page<User> userPage = userService.page(new Page<>(page, size), wrapper);

        // 处理敏感信息
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return R.ok(userPage);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<User> getDetail(@PathVariable Long id) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 处理敏感信息
        user.setPassword(null);

        return R.ok(user);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody Map<String, Object> params) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 不能修改管理员状态
        if (user.getRole() == 2 && StpUtil.getLoginIdAsLong() != id) {
            return R.failed("不能修改其他管理员的状态");
        }

        // 更新状态
        user.setStatus(status);
        userService.updateById(user);

        return R.ok();
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/role")
    public R<Void> updateRole(@RequestBody Map<String, Object> params) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        Long id = Long.parseLong(params.get("id").toString());
        Integer role = Integer.parseInt(params.get("role").toString());

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 不能修改管理员角色
        if (user.getRole() == 2 && StpUtil.getLoginIdAsLong() != id) {
            return R.failed("不能修改其他管理员的角色");
        }

        // 更新角色
        user.setRole(role);
        userService.updateById(user);

        return R.ok();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public R<Void> update(@RequestBody User user) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 查询用户信息
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return R.failed("用户不存在");
        }

        // 不能修改管理员信息
        if (existUser.getRole() == 2 && StpUtil.getLoginIdAsLong() != user.getId()) {
            return R.failed("不能修改其他管理员的信息");
        }

        // 不修改密码
        user.setPassword(null);

        // 更新用户信息
        userService.updateById(user);

        return R.ok();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset-password")
    public R<Void> resetPassword(@RequestBody Map<String, Object> params) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        Long id = Long.parseLong(params.get("id").toString());
        String password = params.get("password").toString();

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 不能修改管理员密码
        if (user.getRole() == 2 && StpUtil.getLoginIdAsLong() != id) {
            return R.failed("不能修改其他管理员的密码");
        }

        // 更新密码
        user.setPassword(cn.dev33.satoken.secure.BCrypt.hashpw(password));
        userService.updateById(user);

        return R.ok();
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody User user) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userService.count(wrapper) > 0) {
            return R.failed("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, user.getEmail());
            if (userService.count(wrapper) > 0) {
                return R.failed("邮箱已存在");
            }
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, user.getPhone());
            if (userService.count(wrapper) > 0) {
                return R.failed("手机号已存在");
            }
        }

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(0); // 默认普通用户
        }

        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }

        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(cn.dev33.satoken.secure.BCrypt.hashpw(user.getPassword()));
        } else {
            return R.failed("密码不能为空");
        }

        // 保存用户
        userService.save(user);

        return R.ok();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 校验权限
        if (!StpUtil.isLogin()) {
            return R.failed("请先登录");
        }

        if (!StpUtil.hasRole("admin")) {
            return R.failed("无权操作，需要管理员权限");
        }

        // 查询用户信息
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 不能删除管理员
        if (user.getRole() == 2) {
            return R.failed("不能删除管理员账号");
        }

        // 不能删除自己
        if (StpUtil.getLoginIdAsLong() == id) {
            return R.failed("不能删除当前登录账号");
        }

        // 删除用户
        userService.removeById(id);

        return R.ok();
    }
}
