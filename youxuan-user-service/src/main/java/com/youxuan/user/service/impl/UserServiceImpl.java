package com.youxuan.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.util.JwtUtils;
import com.youxuan.user.dto.UserLoginDTO;
import com.youxuan.user.dto.UserRegisterDTO;
import com.youxuan.user.entity.SysUser;
import com.youxuan.user.mapper.SysUserMapper;
import com.youxuan.user.service.UserService;
import com.youxuan.user.vo.LoginVO;
import com.youxuan.user.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现，处理注册、登录和当前用户查询的核心业务规则。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "USER";
    private static final int STATUS_NORMAL = 1;

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(SysUserMapper sysUserMapper, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 注册用户：校验用户名唯一性，并使用 BCrypt 保存密码摘要。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        String username = registerDTO.getUsername().trim();
        if (existsByUsername(username)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(resolveNickname(registerDTO.getNickname(), username));
        user.setPhone(registerDTO.getPhone());
        user.setRole(DEFAULT_ROLE);
        user.setStatus(STATUS_NORMAL);
        user.setDeleted(0);
        sysUserMapper.insert(user);
        return UserVO.from(user);
    }

    /**
     * 登录用户：校验账号状态和密码，通过后签发包含 userId、username、role 的 JWT。
     */
    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        SysUser user = findByUsername(loginDTO.getUsername().trim());
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户名或密码错误");
        }
        if (!Integer.valueOf(STATUS_NORMAL).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, UserVO.from(user));
    }

    /**
     * 当前阶段先根据 X-User-Id 查询用户；网关解析 token 并透传留到阶段 5。
     */
    @Override
    public UserVO getCurrentUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少请求头 X-User-Id");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return UserVO.from(user);
    }

    private boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    private SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private String resolveNickname(String nickname, String username) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return username;
    }
}
