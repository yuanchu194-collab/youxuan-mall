package com.youxuan.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 订单发货请求。
 */
public class OrderShipRequest {

    @NotBlank(message = "物流公司不能为空")
    @Size(max = 64, message = "物流公司长度不能超过64个字符")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    @Size(max = 64, message = "物流单号长度不能超过64个字符")
    private String trackingNo;

    public String getDeliveryCompany() {
        return deliveryCompany;
    }

    public void setDeliveryCompany(String deliveryCompany) {
        this.deliveryCompany = deliveryCompany;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }
}
