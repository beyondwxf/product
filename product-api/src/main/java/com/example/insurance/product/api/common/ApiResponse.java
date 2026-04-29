package com.example.insurance.product.api.common;

/**
 * 管理端和开放端统一接口响应结构。
 *
 * @param code 响应码
 * @param message 中文响应消息
 * @param traceId 链路追踪 ID
 * @param data 响应数据
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(String code, String message, String traceId, T data) {
    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "成功", null, data);
    }
}
