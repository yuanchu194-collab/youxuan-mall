package com.youxuan.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 优选商城网关服务启动类，负责统一入口和路由转发。
 */
@SpringBootApplication
public class YouxuanGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanGatewayApplication.class, args);
    }
}
