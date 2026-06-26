package com.youxuan.user.controller;

import com.youxuan.common.result.Result;
import com.youxuan.user.dto.address.AddressAddRequest;
import com.youxuan.user.dto.address.AddressUpdateRequest;
import com.youxuan.user.service.UserAddressService;
import com.youxuan.user.vo.address.AddressVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收货地址接口。
 */
@RestController
@RequestMapping(value = "/address", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<AddressVO> add(@Valid @RequestBody AddressAddRequest request) {
        return Result.success(userAddressService.add(request));
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<AddressVO> update(@PathVariable("id") Long id, @Valid @RequestBody AddressUpdateRequest request) {
        return Result.success(userAddressService.update(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userAddressService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.success(userAddressService.list());
    }

    @GetMapping("/{id:\\d+}")
    public Result<AddressVO> detail(@PathVariable("id") Long id) {
        return Result.success(userAddressService.detail(id));
    }

    @GetMapping("/default")
    public Result<AddressVO> getDefault() {
        return Result.success(userAddressService.getDefault());
    }

    @PutMapping("/{id:\\d+}/default")
    public Result<AddressVO> setDefault(@PathVariable("id") Long id) {
        return Result.success(userAddressService.setDefault(id));
    }
}
