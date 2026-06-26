package com.youxuan.order.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页响应。
 */
public class OrderConfirmVO {

    private OrderAddressVO address;
    private List<OrderConfirmItemVO> items;
    private List<OrderCouponVO> availableCoupons;
    private OrderCouponVO selectedCoupon;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;

    public OrderAddressVO getAddress() { return address; }
    public void setAddress(OrderAddressVO address) { this.address = address; }
    public List<OrderConfirmItemVO> getItems() { return items; }
    public void setItems(List<OrderConfirmItemVO> items) { this.items = items; }
    public List<OrderCouponVO> getAvailableCoupons() { return availableCoupons; }
    public void setAvailableCoupons(List<OrderCouponVO> availableCoupons) { this.availableCoupons = availableCoupons; }
    public OrderCouponVO getSelectedCoupon() { return selectedCoupon; }
    public void setSelectedCoupon(OrderCouponVO selectedCoupon) { this.selectedCoupon = selectedCoupon; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
}
