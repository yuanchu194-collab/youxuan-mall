package com.youxuan.cart.vo;

import java.math.BigDecimal;

/**
 * 购物车列表响应，商品展示字段来自商品服务实时查询。
 */
public class CartItemVO {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer checked;
    private Integer stock;
    private Integer status;
    private BigDecimal totalAmount;
    private Boolean offShelf;
    private Boolean stockInsufficient;
    private String invalidReason;

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getOffShelf() {
        return offShelf;
    }

    public void setOffShelf(Boolean offShelf) {
        this.offShelf = offShelf;
    }

    public Boolean getStockInsufficient() {
        return stockInsufficient;
    }

    public void setStockInsufficient(Boolean stockInsufficient) {
        this.stockInsufficient = stockInsufficient;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
