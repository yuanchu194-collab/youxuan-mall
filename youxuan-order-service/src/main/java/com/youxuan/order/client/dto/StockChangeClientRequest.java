package com.youxuan.order.client.dto;

/**
 * 调用商品服务扣减或恢复库存的请求。
 */
public class StockChangeClientRequest {

    private Long productId;
    private Integer quantity;

    public StockChangeClientRequest() {
    }

    public StockChangeClientRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
