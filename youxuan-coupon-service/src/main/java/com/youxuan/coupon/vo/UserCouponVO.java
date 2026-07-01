package com.youxuan.coupon.vo;

import com.youxuan.coupon.entity.Coupon;
import com.youxuan.coupon.entity.UserCoupon;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的优惠券响应，包含用户领取状态和优惠券展示字段。
 */
public class UserCouponVO {

    private Long userCouponId;
    private Long couponId;
    private String couponName;
    private Integer couponStatus;
    private BigDecimal amount;
    private BigDecimal minAmount;
    private Integer status;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String scopeType;
    private Long categoryId;

    public static UserCouponVO from(UserCoupon userCoupon, Coupon coupon) {
        UserCouponVO vo = new UserCouponVO();
        vo.setUserCouponId(userCoupon.getId());
        vo.setCouponId(userCoupon.getCouponId());
        vo.setStatus(userCoupon.getStatus());
        vo.setReceiveTime(userCoupon.getReceiveTime());
        vo.setUseTime(userCoupon.getUseTime());
        if (coupon != null) {
            vo.setCouponName(coupon.getName());
            vo.setCouponStatus(coupon.getStatus());
            vo.setAmount(coupon.getAmount());
            vo.setMinAmount(coupon.getMinAmount());
            vo.setStartTime(coupon.getStartTime());
            vo.setEndTime(coupon.getEndTime());
            vo.setScopeType(coupon.getScopeType());
            vo.setCategoryId(coupon.getCategoryId());
        } else {
            vo.setCouponName("已失效优惠券");
            vo.setCouponStatus(0);
        }
        return vo;
    }

    public Long getUserCouponId() { return userCouponId; }
    public void setUserCouponId(Long userCouponId) { this.userCouponId = userCouponId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public Integer getCouponStatus() { return couponStatus; }
    public void setCouponStatus(Integer couponStatus) { this.couponStatus = couponStatus; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    public LocalDateTime getUseTime() { return useTime; }
    public void setUseTime(LocalDateTime useTime) { this.useTime = useTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
