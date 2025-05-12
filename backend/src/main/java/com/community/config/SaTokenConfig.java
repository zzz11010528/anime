package com.community.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验 - 排除登录接口和文件上传接口
            SaRouter.match("/**")

                    .notMatch("/user/login", "/user/register","/pay/return")
                    .notMatch("/file/**", "/upload/**", "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
                    .check(r -> StpUtil.checkLogin());

            // 角色校验 - 管理员
            SaRouter.match("/admin/**", r -> StpUtil.checkRole("admin"));

            // 角色校验 - 商家
            SaRouter.match("/merchant/**")
                   .notMatch("/merchant/certification/list", "/merchant/certification/audit")
                   .check(r -> StpUtil.checkRole("merchant"));

            // 角色校验 - 管理员专用接口
            SaRouter.match("/merchant/certification/list", "/merchant/certification/audit", r -> StpUtil.checkRole("admin"));

            // 角色校验 - 统计接口
            SaRouter.match("/stat/overview", r -> StpUtil.checkRole("admin"));
            SaRouter.match("/stat/merchant/overview", r -> StpUtil.checkRole("merchant"));
        })).addPathPatterns("/**");
    }
}
