package com.lz.fixflow.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.validation.constraints.Null;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        return errorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException e) {
        return errorResponse(ErrorCode.NOT_LOGIN, ErrorCode.NOT_LOGIN.getMessage());
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<Void>> handleNotRoleException(NotRoleException e) {
        return errorResponse(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(Exception e) {
        log.error("未处理的请求异常", e);
        return errorResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
    }

    @Override 
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ErrorCode errorCode = ErrorCode.fromHttpStatus(statusCode.value());
        String message = errorCode.getMessage();
                
        if (statusCode.is5xxServerError()) {
            log.error("请求处理失败", e);
        } else if (e instanceof MethodArgumentNotValidException validation) {
            var error = validation.getBindingResult().getAllErrors().stream().findFirst();
            message = error.map(item -> item.getDefaultMessage()).orElse(message);
        } else if (e instanceof ResponseStatusException responseStatus
                && responseStatus.getReason() != null && !responseStatus.getReason().isBlank()) {

            message = responseStatus.getReason();
        }

        return super.handleExceptionInternal(
                e,
                Result.error(statusCode.value(), errorCode.getCode(), message),
                headers, statusCode, request);
    }

    private ResponseEntity<Result<Void>> errorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(Result.error(errorCode.getHttpStatus().value(), errorCode.getCode(), message));
    }
}
