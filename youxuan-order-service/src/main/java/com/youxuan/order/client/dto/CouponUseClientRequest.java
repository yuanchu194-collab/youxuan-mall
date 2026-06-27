package com.youxuan.order.client.dto;

import java.math.BigDecimal;

/**
 * 调用优惠券服务核销优惠券的请求。
 */
public class CouponUseClientRequest {

    private Long couponId;
    private Long orderId;
    private BigDecimal orderAmount;

    public CouponUseClientRequest() {
    }

    public CouponUseClientRequest(Long couponId, Long orderId, BigDecimal orderAmount) {
        this.couponId = couponId;
        this.orderId = orderId;
        this.orderAmount = orderAmount;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }
}
