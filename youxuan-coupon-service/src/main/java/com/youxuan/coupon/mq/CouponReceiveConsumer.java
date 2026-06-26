package com.youxuan.coupon.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.CouponReceiveMessage;
import com.youxuan.coupon.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取消费者，异步写入用户优惠券并扣减 MySQL 库存。
 */
@Component
public class CouponReceiveConsumer {

    private static final Logger log = LoggerFactory.getLogger(CouponReceiveConsumer.class);

    private final CouponService couponService;

    public CouponReceiveConsumer(CouponService couponService) {
        this.couponService = couponService;
    }

    @RabbitListener(queues = RabbitMqConstants.COUPON_RECEIVE_QUEUE)
    public void consume(CouponReceiveMessage message) {
        try {
            log.info("收到优惠券领取消息 userId={}, couponId={}",
                    message == null ? null : message.getUserId(),
                    message == null ? null : message.getCouponId());
            couponService.consumeReceiveMessage(message);
        } catch (Exception exception) {
            log.error("消费优惠券领取消息失败 message={}", message, exception);
        }
    }
}
