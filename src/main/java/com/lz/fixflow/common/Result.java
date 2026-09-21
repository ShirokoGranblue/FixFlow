package com.lz.fixflow.common;

import lombok.Getter;

@Getter
public class Result<T> {

    private final int code;
    private final String message;
    private final T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    public static <T> Result<T> error(int code , String message){
        return new Result<>(code,message,null);
    }
}