package com.youxuan.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 搜索服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanSearchServiceApplication.class, args);
    }
}
