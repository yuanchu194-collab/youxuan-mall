package com.youxuan.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 首页运营服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanHomeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanHomeServiceApplication.class, args);
    }
}
