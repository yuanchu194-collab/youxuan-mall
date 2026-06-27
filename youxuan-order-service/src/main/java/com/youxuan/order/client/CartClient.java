package com.youxuan.order.client;

import com.youxuan.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 购物车服务客户端，订单创建成功后清理当前用户已勾选商品。
 */
@FeignClient(name = "youxuan-cart-service")
public interface CartClient {

    @DeleteMapping("/checked")
    Result<Void> deleteChecked(@RequestHeader("X-User-Id") Long userId,
                               @RequestHeader("X-User-Role") String role);
}
