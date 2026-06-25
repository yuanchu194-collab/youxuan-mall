package com.youxuan.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanOrderServiceApplication.class, args);
    }
}
