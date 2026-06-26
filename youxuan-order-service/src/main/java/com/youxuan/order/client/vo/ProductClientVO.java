package com.youxuan.order.client.vo;

import java.math.BigDecimal;

/**
 * 接收商品服务详情接口返回的数据。
 */
public class ProductClientVO {

    private Long id;
    private String name;
    private String mainImage;
    private BigDecimal price;
    private Integer status;
    private Integer stock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
