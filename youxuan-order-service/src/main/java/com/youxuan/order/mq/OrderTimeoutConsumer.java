package com.youxuan.order.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.OrderTimeoutMessage;
import com.youxuan.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时取消消费者，只处理仍处于待支付状态的订单。
 */
@Component
public class OrderTimeoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutConsumer.class);

    private final OrderService orderService;

    public OrderTimeoutConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMqConstants.ORDER_TIMEOUT_QUEUE)
    public void consume(OrderTimeoutMessage message) {
        try {
            log.info("收到订单超时取消消息 orderId={}, orderNo={}",
                    message == null ? null : message.getOrderId(),
                    message == null ? null : message.getOrderNo());
            orderService.timeoutCancel(message);
        } catch (Exception exception) {
            log.error("消费订单超时取消消息失败 message={}", message, exception);
        }
    }
}
