package com.youxuan.file.controller;

import com.youxuan.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件服务测试接口，仅用于验证服务启动和网关转发。
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
}
