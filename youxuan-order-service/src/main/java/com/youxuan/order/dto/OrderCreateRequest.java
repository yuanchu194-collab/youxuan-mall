package com.youxuan.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建订单请求，下单时会重新校验商品、地址和优惠券。
 */
public class OrderCreateRequest {

    @NotBlank(message = "订单来源不能为空")
    private String source;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private Long couponId;

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderCreateItemRequest> items;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public List<OrderCreateItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderCreateItemRequest> items) {
        this.items = items;
    }
}
