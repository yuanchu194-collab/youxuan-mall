package com.youxuan.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 库存扣减和恢复请求。
 */
public class StockChangeDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer quantity;

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
