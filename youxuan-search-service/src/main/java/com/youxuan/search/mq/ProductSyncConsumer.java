package com.youxuan.search.mq;

import com.youxuan.common.constant.RabbitMqConstants;
import com.youxuan.common.message.ProductSyncMessage;
import com.youxuan.search.service.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 商品同步 ES 消费者，根据商品变更操作更新或删除 ES 文档。
 */
@Component
public class ProductSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncConsumer.class);

    private final ProductSearchService productSearchService;

    public ProductSyncConsumer(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @RabbitListener(queues = RabbitMqConstants.PRODUCT_SYNC_ES_QUEUE)
    public void consume(ProductSyncMessage message) {
        if (message == null || message.getProductId() == null) {
            log.warn("收到无效商品同步 ES 消息 message={}", message);
            return;
        }
        Long productId = message.getProductId();
        String operation = message.getOperation();
        try {
            log.info("收到商品同步 ES 消息 productId={}, operation={}", productId, operation);
            if (ProductSyncMessage.SAVE_OR_UPDATE.equals(operation) || ProductSyncMessage.UP.equals(operation)) {
                productSearchService.syncProduct(productId);
                return;
            }
            if (ProductSyncMessage.DELETE.equals(operation) || ProductSyncMessage.DOWN.equals(operation)) {
                productSearchService.deleteProductDocument(productId);
                return;
            }
            log.warn("未知商品同步 ES 操作 productId={}, operation={}", productId, operation);
        } catch (Exception exception) {
            log.error("消费商品同步 ES 消息失败 productId={}, operation={}", productId, operation, exception);
        }
    }
}
