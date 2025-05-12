package com.community.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.entity.User;
import com.community.mapper.UserMapper;
import com.community.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String login(String username, String password) {
        // 根据用户名查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());

        // // 设置角色
        // switch (user.getRole()) {
        //     case 0:
        //         StpUtil.getSession().set("role", "user");
        //         break;
        //     case 1:
        //         StpUtil.getSession().set("role", "merchant");
        //         break;
        //     case 2:
        //         StpUtil.getSession().set("role", "admin");
        //         break;
        //     default:
        //         break;
        // }

        // 返回token
        return StpUtil.getTokenValue();
    }

    @Override
    public boolean register(User user) {
        // 校验用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 校验邮箱是否已存在
        if (lambdaQuery().eq(User::getEmail, user.getEmail()).one() != null) {
            throw new BusinessException("邮箱已存在");
        }

        // 校验手机号是否已存在
        if (user.getPhone() != null && lambdaQuery().eq(User::getPhone, user.getPhone()).one() != null) {
            throw new BusinessException("手机号已存在");
        }

        // 设置默认值
        user.setRole(0); // 默认普通用户
        user.setStatus(1); // 默认正常状态
        if (user.getNickname() == null) {
            user.setNickname(user.getUsername());
        }

        // 密码加密
        user.setPassword(BCrypt.hashpw(user.getPassword()));

        // 保存用户
        return save(user);
    }

    @Override
    public User getByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }
}
