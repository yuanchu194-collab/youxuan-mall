package com.youxuan.search.config;

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
 * 搜索服务 RabbitMQ 配置，声明商品同步 ES 的交换机、队列和绑定关系。
 */
@Configuration
public class SearchRabbitMqConfig {

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange(RabbitMqConstants.PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    public Queue productSyncEsQueue() {
        return new Queue(RabbitMqConstants.PRODUCT_SYNC_ES_QUEUE, true);
    }

    @Bean
    public Binding productSyncEsBinding(Queue productSyncEsQueue, DirectExchange productExchange) {
        return BindingBuilder.bind(productSyncEsQueue)
                .to(productExchange)
                .with(RabbitMqConstants.PRODUCT_SYNC_ES_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
