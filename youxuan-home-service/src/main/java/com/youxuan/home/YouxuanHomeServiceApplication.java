package com.youxuan.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 首页运营服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
@EnableFeignClients(basePackages = "com.youxuan.home.client")
public class YouxuanHomeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanHomeServiceApplication.class, args);
    }
}
