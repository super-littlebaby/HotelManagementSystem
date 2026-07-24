package com.project.hotelmanagementsystem.common;

/**
 * 统一响应体封装类
 * <p>
 * 用于封装所有接口的响应数据，统一返回格式。
 * </p>
 *
 * @param <T> 响应数据泛型类型
 */
public class ResponseResult<T> {

    /**
     * 响应码（200成功，其他为错误码）
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 无参构造函数
     */
    public ResponseResult() {
    }

    /**
     * 全参构造函数
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     */
    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回数据
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "success", data);
    }

    /**
     * 成功返回消息和数据
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }

    /**
     * 错误返回
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    /**
     * 错误返回（默认code=500）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(500, message, null);
    }

    /**
     * 获取响应码
     *
     * @return 响应码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置响应码
     *
     * @param code 响应码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     *
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }
}