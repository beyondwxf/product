package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * V1.5 支持的寿险产品类型枚举。
 */
public enum ProductType implements CodedEnum {
    /**
     * 定期寿险。
     */
    TERM_LIFE("TERM_LIFE", "定期寿险"),
    /**
     * 终身寿险。
     */
    WHOLE_LIFE("WHOLE_LIFE", "终身寿险"),
    /**
     * 两全保险。
     */
    ENDOWMENT("ENDOWMENT", "两全保险");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建产品类型枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    ProductType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取产品类型稳定编码。
     *
     * @return 产品类型编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取产品类型中文描述。
     *
     * @return 产品类型描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
