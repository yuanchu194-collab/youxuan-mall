package com.youxuan.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 购物车服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
@EnableFeignClients(basePackages = "com.youxuan.cart.client")
public class YouxuanCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanCartServiceApplication.class, args);
    }
}
