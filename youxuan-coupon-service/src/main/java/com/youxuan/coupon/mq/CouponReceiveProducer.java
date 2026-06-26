package com.youxuan.coupon.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.CouponReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 优惠券领取消息发送器。
 */
@Component
public class CouponReceiveProducer {

    private static final Logger log = LoggerFactory.getLogger(CouponReceiveProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public CouponReceiveProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(CouponReceiveMessage message) {
        try {
            log.info("准备发送优惠券领取消息 userId={}, couponId={}", message.getUserId(), message.getCouponId());
            rabbitTemplate.convertAndSend(RabbitMqConstants.COUPON_EXCHANGE,
                    RabbitMqConstants.COUPON_RECEIVE_ROUTING_KEY, message);
            log.info("优惠券领取消息发送成功 userId={}, couponId={}", message.getUserId(), message.getCouponId());
        } catch (Exception exception) {
            log.error("优惠券领取消息发送失败 userId={}, couponId={}",
                    message.getUserId(), message.getCouponId(), exception);
            throw exception;
        }
    }
}
