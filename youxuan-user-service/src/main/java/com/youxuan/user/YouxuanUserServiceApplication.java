package com.youxuan.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanUserServiceApplication.class, args);
    }
}
