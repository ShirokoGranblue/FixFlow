package com.lz.fixflow.common;

public record Result<T>(int httpStatus, int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(200, 200, "success", data);
    }

    public static <T> Result<T> error(int httpStatus, int code, String message) {
        return new Result<>(httpStatus, code, message, null);
    }
}
