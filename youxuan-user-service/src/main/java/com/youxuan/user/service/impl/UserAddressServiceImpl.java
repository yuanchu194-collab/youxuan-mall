package com.youxuan.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.youxuan.common.context.UserContext;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.user.dto.address.AddressAddRequest;
import com.youxuan.user.dto.address.AddressUpdateRequest;
import com.youxuan.user.entity.UserAddress;
import com.youxuan.user.mapper.UserAddressMapper;
import com.youxuan.user.service.UserAddressService;
import com.youxuan.user.vo.address.AddressVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户收货地址服务实现。
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    private static final int DEFAULT_FLAG = 1;
    private static final int NOT_DEFAULT_FLAG = 0;

    private final UserAddressMapper userAddressMapper;

    public UserAddressServiceImpl(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO add(AddressAddRequest request) {
        Long userId = currentUserId();
        boolean firstAddress = countByUserId(userId) == 0;
        int defaultFlag = firstAddress || Integer.valueOf(DEFAULT_FLAG).equals(request.getDefaultFlag())
                ? DEFAULT_FLAG
                : NOT_DEFAULT_FLAG;
        if (defaultFlag == DEFAULT_FLAG) {
            clearDefault(userId, null);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        fillAddress(address, request.getReceiverName(), request.getReceiverPhone(), request.getProvince(),
                request.getCity(), request.getDistrict(), request.getDetailAddress(), defaultFlag);
        address.setDeleted(0);
        userAddressMapper.insert(address);
        return AddressVO.from(userAddressMapper.selectById(address.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO update(Long id, AddressUpdateRequest request) {
        UserAddress address = getCurrentUserAddress(id);
        int defaultFlag = Integer.valueOf(DEFAULT_FLAG).equals(request.getDefaultFlag())
                ? DEFAULT_FLAG
                : NOT_DEFAULT_FLAG;
        if (defaultFlag == DEFAULT_FLAG) {
            clearDefault(address.getUserId(), address.getId());
        }
        fillAddress(address, request.getReceiverName(), request.getReceiverPhone(), request.getProvince(),
                request.getCity(), request.getDistrict(), request.getDetailAddress(), defaultFlag);
        userAddressMapper.updateById(address);
        return AddressVO.from(userAddressMapper.selectById(address.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        UserAddress address = getCurrentUserAddress(id);
        userAddressMapper.deleteById(address.getId());
    }

    @Override
    public List<AddressVO> list() {
        Long userId = currentUserId();
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getDefaultFlag)
                        .orderByDesc(UserAddress::getUpdateTime)
                        .orderByDesc(UserAddress::getId))
                .stream()
                .map(AddressVO::from)
                .toList();
    }

    @Override
    public AddressVO detail(Long id) {
        return AddressVO.from(getCurrentUserAddress(id));
    }

    @Override
    public AddressVO getDefault() {
        Long userId = currentUserId();
        UserAddress address = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getDefaultFlag, DEFAULT_FLAG)
                .last("LIMIT 1"));
        return AddressVO.from(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO setDefault(Long id) {
        UserAddress address = getCurrentUserAddress(id);
        clearDefault(address.getUserId(), address.getId());
        address.setDefaultFlag(DEFAULT_FLAG);
        userAddressMapper.updateById(address);
        return AddressVO.from(userAddressMapper.selectById(address.getId()));
    }

    private UserAddress getCurrentUserAddress(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "地址ID不能为空");
        }
        UserAddress address = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, currentUserId())
                .last("LIMIT 1"));
        if (address == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        }
        return address;
    }

    private long countByUserId(Long userId) {
        return userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId));
    }

    private void clearDefault(Long userId, Long excludeId) {
        userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .ne(excludeId != null, UserAddress::getId, excludeId)
                .set(UserAddress::getDefaultFlag, NOT_DEFAULT_FLAG));
    }

    private void fillAddress(UserAddress address, String receiverName, String receiverPhone, String province,
                             String city, String district, String detailAddress, Integer defaultFlag) {
        address.setReceiverName(receiverName.trim());
        address.setReceiverPhone(receiverPhone.trim());
        address.setProvince(province.trim());
        address.setCity(city.trim());
        address.setDistrict(district.trim());
        address.setDetailAddress(detailAddress.trim());
        address.setDefaultFlag(defaultFlag);
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }
}
