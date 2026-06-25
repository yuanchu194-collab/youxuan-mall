package com.youxuan.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 购物车服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanCartServiceApplication.class, args);
    }
}
