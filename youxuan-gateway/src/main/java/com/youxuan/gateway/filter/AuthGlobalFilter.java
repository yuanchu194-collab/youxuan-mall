package com.youxuan.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import com.youxuan.common.util.JwtUtils;
import com.youxuan.gateway.config.AuthProperties;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一鉴权过滤器，负责校验 Bearer Token 并向下游透传用户身份。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> whiteList;

    public AuthGlobalFilter(
            @Value("${youxuan.jwt.secret}") String secret,
            @Value("${youxuan.jwt.expire-seconds:86400}") Long expireSeconds,
            AuthProperties authProperties,
            ObjectMapper objectMapper) {
        this.jwtUtils = new JwtUtils(secret, expireSeconds);
        this.whiteList = authProperties.getWhiteList();
        this.objectMapper = objectMapper;
    }

    /**
     * 放行白名单接口；其余接口必须携带合法 Authorization: Bearer token。
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isWhiteListed(request)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "缺少登录 token");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtUtils.parseToken(token);
            Object userIdClaim = claims.get("userId");
            Long userId = userIdClaim instanceof Number number ? number.longValue() : null;
            String role = claims.get("role", String.class);
            String username = claims.get("username", String.class);
            if (userId == null || !StringUtils.hasText(role) || !StringUtils.hasText(username)) {
                return unauthorized(exchange, "token 信息不完整");
            }

            // 向下游服务透传用户 ID 和角色，用户名仅在网关完成 token 完整性校验。
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER_ID, String.valueOf(userId))
                    .header(HEADER_USER_ROLE, role)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception exception) {
            return unauthorized(exchange, "token 无效或已过期");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();
        for (String item : whiteList) {
            String[] parts = item.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            if (parts[0].equalsIgnoreCase(method.name()) && pathMatcher.match(parts[1], path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = toJsonBytes(Result.fail(ErrorCode.UNAUTHORIZED, message));
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private byte[] toJsonBytes(Result<Void> result) {
        try {
            return objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException exception) {
            return "{\"code\":401,\"message\":\"未登录\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
