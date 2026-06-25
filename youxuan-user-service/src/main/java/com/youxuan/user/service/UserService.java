package com.youxuan.user.service;

import com.youxuan.user.dto.UserLoginDTO;
import com.youxuan.user.dto.UserRegisterDTO;
import com.youxuan.user.vo.LoginVO;
import com.youxuan.user.vo.UserVO;

/**
 * 用户服务接口，定义注册、登录和查询当前用户能力。
 */
public interface UserService {

    UserVO register(UserRegisterDTO registerDTO);

    LoginVO login(UserLoginDTO loginDTO);

    UserVO getCurrentUser(Long userId);
}
