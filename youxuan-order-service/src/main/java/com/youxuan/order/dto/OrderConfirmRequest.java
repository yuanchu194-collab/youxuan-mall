package com.youxuan.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 订单确认请求，只计算价格，不创建订单。
 */
public class OrderConfirmRequest {

    @NotBlank(message = "订单来源不能为空")
    private String source;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private Long couponId;

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderConfirmItemRequest> items;

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

    public List<OrderConfirmItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderConfirmItemRequest> items) {
        this.items = items;
    }
}
