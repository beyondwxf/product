package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * 产品属性枚举，限定 V1.5 支持传统寿险和分红寿险。
 */
public enum ProductNature implements CodedEnum {
    /**
     * 传统寿险，不包含分红参数配置。
     */
    TRADITIONAL_LIFE("TRADITIONAL_LIFE", "传统寿险"),
    /**
     * 分红寿险，发布前必须配置分红方式和红利演示参数。
     */
    DIVIDEND_LIFE("DIVIDEND_LIFE", "分红寿险");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建产品属性枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    ProductNature(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取产品属性稳定编码。
     *
     * @return 产品属性编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取产品属性中文描述。
     *
     * @return 产品属性描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
