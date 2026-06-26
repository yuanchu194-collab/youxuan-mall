package com.youxuan.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
@EnableFeignClients(basePackages = "com.youxuan.order.client")
public class YouxuanOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanOrderServiceApplication.class, args);
    }
}
