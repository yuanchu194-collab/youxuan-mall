package com.youxuan.product.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.ProductSyncMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 商品变更消息发送器，在数据库事务提交后通知搜索服务同步 ES。
 */
@Component
public class ProductSyncProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public ProductSyncProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAfterCommit(Long productId, String operation) {
        ProductSyncMessage message = new ProductSyncMessage(productId, operation);
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

    private void send(ProductSyncMessage message) {
        try {
            log.info("准备发送商品同步 ES 消息 productId={}, operation={}",
                    message.getProductId(), message.getOperation());
            rabbitTemplate.convertAndSend(RabbitMqConstants.PRODUCT_EXCHANGE,
                    RabbitMqConstants.PRODUCT_SYNC_ES_ROUTING_KEY, message);
            log.info("商品同步 ES 消息发送成功 productId={}, operation={}",
                    message.getProductId(), message.getOperation());
        } catch (Exception exception) {
            log.error("商品同步 ES 消息发送失败 productId={}, operation={}",
                    message.getProductId(), message.getOperation(), exception);
        }
    }
}
