package com.lz.fixflow.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BUSINESS_ERROR(4000, HttpStatus.BAD_REQUEST, "请求处理失败"),
    INVALID_ARGUMENT(4001, HttpStatus.BAD_REQUEST, "请求参数不合法"),
    NOT_LOGIN(4010, HttpStatus.UNAUTHORIZED, "请先登录"),
    INVALID_CREDENTIALS(4011, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    FORBIDDEN(4030, HttpStatus.FORBIDDEN, "没有权限执行此操作"),
    NOT_FOUND(4040, HttpStatus.NOT_FOUND, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(4050, HttpStatus.METHOD_NOT_ALLOWED, "请求方法不支持"),
    NOT_ACCEPTABLE(4060, HttpStatus.NOT_ACCEPTABLE, "响应格式不支持"),
    CONFLICT(4090, HttpStatus.CONFLICT, "资源当前状态不允许此操作"),
    USERNAME_EXISTS(4091, HttpStatus.CONFLICT, "用户名已存在"),
    UNSUPPORTED_MEDIA_TYPE(4150, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "请求内容类型不支持"),
    INTERNAL_ERROR(5000, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    public static ErrorCode fromHttpStatus(int status) {
        return switch (status) {
            case 400 -> INVALID_ARGUMENT;
            case 401 -> NOT_LOGIN;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 409 -> CONFLICT;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> status >= 500 ? INTERNAL_ERROR : BUSINESS_ERROR;
        };
    }
}
