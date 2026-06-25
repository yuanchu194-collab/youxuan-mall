package com.youxuan.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * JWT 工具类，统一生成和解析包含 userId、username、role 的登录令牌。
 */
@Component
@ConditionalOnProperty(prefix = "youxuan.jwt", name = "secret")
public class JwtUtils {

    private final SecretKey secretKey;
    private final Long expireSeconds;

    public JwtUtils(
            @Value("${youxuan.jwt.secret}") String secret,
            @Value("${youxuan.jwt.expire-seconds:86400}") Long expireSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    /**
     * 生成登录令牌，payload 中保留网关后续透传所需的三项身份信息。
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验令牌签名，后续网关鉴权阶段会复用该方法。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
