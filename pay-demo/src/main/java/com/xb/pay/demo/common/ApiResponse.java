package com.xb.pay.demo.common;

/**
 * ApiResponse - 统一 API 响应封装
 *
 * REST 接口统一返回格式，让前端可用固定结构解析响应。
 * 使用 Java 21 record 实现不可变数据载体。
 *
 * @author ibqy
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
