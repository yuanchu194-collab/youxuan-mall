package com.youxuan.common.result;

/**
 * 统一错误码定义，公共错误先放在这里，业务错误后续按模块扩展。
 */
public enum ErrorCode {

    SUCCESS(200, "success"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "用户未登录"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(5000, "业务处理失败"),
    SYSTEM_ERROR(500, "系统异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
