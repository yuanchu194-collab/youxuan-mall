package com.youxuan.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

/**
 * 优惠券服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.youxuan")
@EnableRabbit
public class YouxuanCouponServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxuanCouponServiceApplication.class, args);
    }
}
