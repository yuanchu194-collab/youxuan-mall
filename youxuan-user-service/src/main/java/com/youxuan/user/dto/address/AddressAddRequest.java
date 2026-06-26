package com.youxuan.user.dto.address;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增收货地址请求。
 */
public class AddressAddRequest {

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 64, message = "收货人姓名不能超过64个字符")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Size(max = 20, message = "收货人手机号不能超过20个字符")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 64, message = "省份不能超过64个字符")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 64, message = "城市不能超过64个字符")
    private String city;

    @NotBlank(message = "区县不能为空")
    @Size(max = 64, message = "区县不能超过64个字符")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String detailAddress;

    @Min(value = 0, message = "默认地址标记只能是0或1")
    @Max(value = 1, message = "默认地址标记只能是0或1")
    private Integer defaultFlag = 0;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public Integer getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(Integer defaultFlag) {
        this.defaultFlag = defaultFlag;
    }
}
