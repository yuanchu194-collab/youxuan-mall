package com.youxuan.common.message;

/**
 * 商品同步 ES 消息体。
 */
public class ProductSyncMessage {

    public static final String SAVE_OR_UPDATE = "SAVE_OR_UPDATE";
    public static final String DELETE = "DELETE";
    public static final String DOWN = "DOWN";
    public static final String UP = "UP";

    private Long productId;
    private String operation;

    public ProductSyncMessage() {
    }

    public ProductSyncMessage(Long productId, String operation) {
        this.productId = productId;
        this.operation = operation;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
