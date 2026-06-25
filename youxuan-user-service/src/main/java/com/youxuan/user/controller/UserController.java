package com.youxuan.user.controller;

import com.youxuan.common.context.UserContext;
import com.youxuan.common.result.Result;
import com.youxuan.user.dto.UserLoginDTO;
import com.youxuan.user.dto.UserRegisterDTO;
import com.youxuan.user.service.UserService;
import com.youxuan.user.vo.LoginVO;
import com.youxuan.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器，提供注册、登录和查询当前用户信息接口。
 */
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册普通用户，默认角色为 USER。
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    /**
     * 用户登录，成功后返回包含 userId、username、role 的 JWT。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 查询当前用户，用户 ID 来自下游拦截器写入的 UserContext。
     */
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser(UserContext.getUserId()));
    }
}
