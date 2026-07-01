package com.youxuan.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
@EnableFeignClients(basePackages = "com.youxuan.product.client")
public class YouxuanProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanProductServiceApplication.class, args);
    }
}
