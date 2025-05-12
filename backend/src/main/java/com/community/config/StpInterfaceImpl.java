package com.community.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.community.entity.User;
import com.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 暂时不需要权限码
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();

        // 直接从数据库中获取最新的角色信息
        try {
            Long userId = Long.parseLong(loginId.toString());
            User user = userService.getById(userId);
            if (user != null) {
                switch (user.getRole()) {
                    case 0:
                        roles.add("user");
                        break;
                    case 1:
                        roles.add("merchant");
                        break;
                    case 2:
                        roles.add("admin");
                        break;
                    default:
                        break;
                }

                // 更新Session中的角色信息
                if (!roles.isEmpty()) {
                    StpUtil.getSession().set("role", roles.get(0));
                }
            }
        } catch (Exception e) {
            // 如果发生异常，尝试从Session中获取角色
            String sessionRole = (String) StpUtil.getSession().get("role");
            if (sessionRole != null) {
                roles.add(sessionRole);
            }
        }

        return roles;
    }
}
