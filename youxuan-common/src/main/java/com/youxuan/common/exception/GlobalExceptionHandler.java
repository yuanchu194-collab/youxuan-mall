package com.youxuan.common.exception;

import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，将服务中的异常统一转换为 Result 返回。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常，保持业务错误码和错误提示可控。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数校验异常，优先返回第一个字段错误。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = ErrorCode.PARAM_ERROR.getMessage();
        if (exception.getBindingResult().hasFieldErrors()) {
            message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理 JSON 格式错误等请求体解析异常。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return Result.fail(ErrorCode.PARAM_ERROR, "请求体格式错误");
    }

    /**
     * 兜底处理未知异常，避免把堆栈细节直接暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("Unhandled system exception", exception);
        return Result.fail(ErrorCode.SYSTEM_ERROR);
    }
}
