package com.youxuan.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 修改首页 Banner 请求。
 */
public class BannerUpdateRequest {

    @NotBlank(message = "Banner标题不能为空")
    @Size(max = 128, message = "Banner标题不能超过128个字符")
    private String title;

    @NotBlank(message = "Banner图片URL不能为空")
    @Size(max = 255, message = "Banner图片URL不能超过255个字符")
    private String imageUrl;

    @Size(max = 32, message = "跳转类型不能超过32个字符")
    private String linkType;

    @Size(max = 255, message = "跳转目标不能超过255个字符")
    private String linkValue;

    @Min(value = 0, message = "排序不能小于0")
    private Integer sort = 0;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status = 1;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getLinkValue() {
        return linkValue;
    }

    public void setLinkValue(String linkValue) {
        this.linkValue = linkValue;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
