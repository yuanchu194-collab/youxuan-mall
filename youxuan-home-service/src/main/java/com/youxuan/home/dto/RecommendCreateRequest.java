package com.youxuan.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增首页推荐位请求。
 */
public class RecommendCreateRequest {

    @NotBlank(message = "推荐标题不能为空")
    @Size(max = 128, message = "推荐标题不能超过128个字符")
    private String title;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 0, message = "排序不能小于0")
    private Integer sort = 0;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status = 1;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
