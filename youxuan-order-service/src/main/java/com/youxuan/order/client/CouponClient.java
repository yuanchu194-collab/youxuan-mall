package com.youxuan.order.client;

import com.youxuan.common.result.Result;
import com.youxuan.order.client.dto.CouponRestoreClientRequest;
import com.youxuan.order.client.dto.CouponUseClientRequest;
import com.youxuan.order.client.vo.CouponClientVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 优惠券服务 Feign 客户端，查询当前用户在指定金额下可用的优惠券。
 */
@FeignClient(name = "youxuan-coupon-service")
public interface CouponClient {

    @GetMapping("/available")
    Result<List<CouponClientVO>> available(@RequestParam("amount") BigDecimal amount,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestHeader("X-User-Role") String role);

    @PostMapping("/use")
    Result<Void> use(@RequestBody CouponUseClientRequest request,
                     @RequestHeader("X-User-Id") Long userId,
                     @RequestHeader("X-User-Role") String role);

    @PostMapping("/restore")
    Result<Void> restore(@RequestBody CouponRestoreClientRequest request,
                         @RequestHeader("X-User-Id") Long userId,
                         @RequestHeader("X-User-Role") String role);
}
