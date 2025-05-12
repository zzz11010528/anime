package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回token，失败返回null
     */
    String login(String username, String password);

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功返回true，失败返回false
     */
    boolean register(User user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
}
