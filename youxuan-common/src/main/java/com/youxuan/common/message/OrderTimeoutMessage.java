package com.youxuan.common.message;

/**
 * 订单超时取消消息。
 */
public class OrderTimeoutMessage {

    private Long orderId;
    private String orderNo;

    public OrderTimeoutMessage() {
    }

    public OrderTimeoutMessage(Long orderId, String orderNo) {
        this.orderId = orderId;
        this.orderNo = orderNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
