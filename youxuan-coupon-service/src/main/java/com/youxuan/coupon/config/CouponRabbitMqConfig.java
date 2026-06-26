package com.youxuan.coupon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youxuan.common.constant.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 优惠券 RabbitMQ 配置，声明领取落库队列。
 */
@Configuration
public class CouponRabbitMqConfig {

    @Bean
    public DirectExchange couponExchange() {
        return new DirectExchange(RabbitMqConstants.COUPON_EXCHANGE, true, false);
    }

    @Bean
    public Queue couponReceiveQueue() {
        return new Queue(RabbitMqConstants.COUPON_RECEIVE_QUEUE, true);
    }

    @Bean
    public Binding couponReceiveBinding(Queue couponReceiveQueue, DirectExchange couponExchange) {
        return BindingBuilder.bind(couponReceiveQueue)
                .to(couponExchange)
                .with(RabbitMqConstants.COUPON_RECEIVE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
