package com.youxuan.common.message;

import java.time.LocalDateTime;

/**
 * 优惠券领取消息体。
 */
public class CouponReceiveMessage {

    private Long userId;
    private Long couponId;
    private LocalDateTime receiveTime;

    public CouponReceiveMessage() {
    }

    public CouponReceiveMessage(Long userId, Long couponId, LocalDateTime receiveTime) {
        this.userId = userId;
        this.couponId = couponId;
        this.receiveTime = receiveTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
}
