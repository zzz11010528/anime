package com.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 多元社区交互系统启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@MapperScan("com.community.mapper")
public class AnimeCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeCommunityApplication.class, args);
    }
}
