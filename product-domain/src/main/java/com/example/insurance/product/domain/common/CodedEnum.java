package com.example.insurance.product.domain.common;

/**
 * 带稳定编码和中文描述的枚举契约。
 */
public interface CodedEnum {
    /**
     * 获取落库和接口传输使用的稳定编码。
     *
     * @return 枚举编码
     */
    String code();

    /**
     * 获取面向后台用户展示的中文描述。
     *
     * @return 中文描述
     */
    String desc();
}
