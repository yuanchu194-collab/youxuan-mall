package com.youxuan.order.vo;

import com.youxuan.order.client.vo.ProductClientVO;
import java.math.BigDecimal;

/**
 * 订单确认页商品项展示。
 */
public class OrderConfirmItemVO {

    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer stock;
    private Integer status;

    public static OrderConfirmItemVO from(ProductClientVO product, Integer quantity) {
        OrderConfirmItemVO vo = new OrderConfirmItemVO();
        vo.setProductId(product.getId());
        vo.setProductName(product.getName());
        vo.setMainImage(product.getMainImage());
        vo.setPrice(product.getPrice());
        vo.setQuantity(quantity);
        vo.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        return vo;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
