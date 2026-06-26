package com.youxuan.user.service;

import com.youxuan.user.dto.address.AddressAddRequest;
import com.youxuan.user.dto.address.AddressUpdateRequest;
import com.youxuan.user.vo.address.AddressVO;
import java.util.List;

/**
 * 用户收货地址服务。
 */
public interface UserAddressService {

    AddressVO add(AddressAddRequest request);

    AddressVO update(Long id, AddressUpdateRequest request);

    void delete(Long id);

    List<AddressVO> list();

    AddressVO detail(Long id);

    AddressVO getDefault();

    AddressVO setDefault(Long id);
}
