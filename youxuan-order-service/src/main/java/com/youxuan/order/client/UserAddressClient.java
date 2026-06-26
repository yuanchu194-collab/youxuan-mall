package com.youxuan.order.client;

import com.youxuan.common.result.Result;
import com.youxuan.order.client.vo.UserAddressClientVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户地址 Feign 客户端，通过请求头透传当前用户，复用 user-service 的归属校验。
 */
@FeignClient(name = "youxuan-user-service")
public interface UserAddressClient {

    @GetMapping("/address/{id}")
    Result<UserAddressClientVO> detail(@PathVariable("id") Long id,
                                       @RequestHeader("X-User-Id") Long userId,
                                       @RequestHeader("X-User-Role") String role);
}
