package com.youxuan.order.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.OrderTimeoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 订单超时取消消息发送器，订单创建事务提交后发送。
 */
@Component
public class OrderTimeoutProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderTimeoutProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAfterCommit(Long orderId, String orderNo) {
        OrderTimeoutMessage message = new OrderTimeoutMessage(orderId, orderNo);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(message);
                }
            });
            return;
        }
        send(message);
    }

    private void send(OrderTimeoutMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.ORDER_EXCHANGE,
                    RabbitMqConstants.ORDER_TIMEOUT_DELAY_ROUTING_KEY, message);
            log.info("订单超时取消消息发送成功 orderId={}, orderNo={}",
                    message.getOrderId(), message.getOrderNo());
        } catch (Exception exception) {
            log.error("订单超时取消消息发送失败 orderId={}, orderNo={}",
                    message.getOrderId(), message.getOrderNo(), exception);
        }
    }
}
