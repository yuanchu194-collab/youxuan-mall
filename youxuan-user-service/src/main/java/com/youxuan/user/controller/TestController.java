package com.youxuan.user.controller;

import com.youxuan.common.context.UserContext;
import com.youxuan.common.result.Result;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务测试接口，仅用于验证服务启动和网关转发。
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
     * 验证网关透传的用户 ID 和角色是否已写入 UserContext。
     */
    @GetMapping("/context")
    public Result<Map<String, Object>> context() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", UserContext.getUserId());
        data.put("role", UserContext.getRole());
        return Result.success(data);
    }
}
