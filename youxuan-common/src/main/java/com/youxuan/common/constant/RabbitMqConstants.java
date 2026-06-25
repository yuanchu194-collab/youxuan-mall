package com.youxuan.common.constant;

/**
 * RabbitMQ 常量，统一维护交换机、队列和路由键名称。
 */
public final class RabbitMqConstants {

    public static final String PRODUCT_EXCHANGE = "youxuan.product.exchange";
    public static final String COUPON_EXCHANGE = "youxuan.coupon.exchange";
    public static final String ORDER_EXCHANGE = "youxuan.order.exchange";

    public static final String PRODUCT_SYNC_ES_QUEUE = "youxuan.product.sync-es.queue";
    public static final String COUPON_RECEIVE_QUEUE = "youxuan.coupon.receive.queue";
    public static final String ORDER_TIMEOUT_QUEUE = "youxuan.order.timeout.queue";

    public static final String PRODUCT_SYNC_ES_ROUTING_KEY = "product.sync.es";
    public static final String COUPON_RECEIVE_ROUTING_KEY = "coupon.receive";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    private RabbitMqConstants() {
    }
}
