package com.project.hotelmanagementsystem.common;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 统一处理应用中抛出的各类异常，返回标准化的响应格式。
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理实体未找到异常
     *
     * @param ex 异常信息
     * @return 统一响应结果
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResult<Void> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.warn("Entity not found: {}", ex.getMessage());
        return ResponseResult.error(404, "资源不存在");
    }

    /**
     * 处理参数校验失败异常
     *
     * @param ex 异常信息
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        logger.warn("Validation failed: {}", errorMessage);
        return ResponseResult.error(400, errorMessage);
    }

    /**
     * 处理数据完整性冲突异常
     *
     * @param ex 异常信息
     * @return 统一响应结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseResult.error(400, "数据完整性冲突，操作失败");
    }

    /**
     * 处理请求体解析失败异常
     *
     * @param ex 异常信息
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("Request body parse error: {}", ex.getMessage());
        return ResponseResult.error(400, "请求体解析失败，请检查请求格式");
    }

    /**
     * 处理其他未捕获的异常（兜底处理）
     *
     * @param ex 异常信息
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Void> handleException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        return ResponseResult.error(500, "服务器内部错误，请稍后重试");
    }
}