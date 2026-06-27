package com.youxuan.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youxuan.common.constant.RabbitMqConstants;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单 RabbitMQ 配置，使用死信队列实现订单超时取消。
 */
@Configuration
public class OrderRabbitMqConfig {

    private static final int ORDER_TIMEOUT_MILLIS = 60_000;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(RabbitMqConstants.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutDelayQueue() {
        return new Queue(RabbitMqConstants.ORDER_TIMEOUT_DELAY_QUEUE, true, false, false,
                Map.of("x-message-ttl", ORDER_TIMEOUT_MILLIS,
                        "x-dead-letter-exchange", RabbitMqConstants.ORDER_EXCHANGE,
                        "x-dead-letter-routing-key", RabbitMqConstants.ORDER_TIMEOUT_ROUTING_KEY));
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(RabbitMqConstants.ORDER_TIMEOUT_QUEUE, true);
    }

    @Bean
    public Binding orderTimeoutDelayBinding(@Qualifier("orderTimeoutDelayQueue") Queue orderTimeoutDelayQueue,
                                            DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutDelayQueue)
                .to(orderExchange)
                .with(RabbitMqConstants.ORDER_TIMEOUT_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutBinding(@Qualifier("orderTimeoutQueue") Queue orderTimeoutQueue,
                                       DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderExchange)
                .with(RabbitMqConstants.ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
