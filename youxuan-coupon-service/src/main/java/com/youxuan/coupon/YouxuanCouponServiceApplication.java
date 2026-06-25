package com.youxuan.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 优惠券服务启动类，当前阶段只提供服务注册和测试接口。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
public class YouxuanCouponServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanCouponServiceApplication.class, args);
    }
}
