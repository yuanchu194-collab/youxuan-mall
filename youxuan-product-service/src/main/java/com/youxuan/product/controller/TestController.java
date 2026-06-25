package com.youxuan.product.controller;

import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品服务测试接口，仅用于验证服务启动、网关转发和公共异常处理。
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 验证服务基础连通性。
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }

    /**
     * 主动抛出业务异常，用于验证 GlobalExceptionHandler 是否统一返回 Result。
     */
    @GetMapping("/error")
    public Result<String> error() {
        throw new BusinessException(ErrorCode.BUSINESS_ERROR, "测试业务异常");
    }
}
