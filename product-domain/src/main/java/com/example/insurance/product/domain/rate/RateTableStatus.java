package com.example.insurance.product.domain.rate;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * 费率表生命周期状态枚举。
 */
public enum RateTableStatus implements CodedEnum {
    /**
     * 编辑中，可导入、修改和校验。
     */
    EDITING("EDITING", "编辑中"),
    /**
     * 待精算审核，等待精算师确认。
     */
    ACTUARY_REVIEW("ACTUARY_REVIEW", "待精算审核"),
    /**
     * 已发布，可绑定产品版本。
     */
    PUBLISHED("PUBLISHED", "已发布"),
    /**
     * 已废弃，不允许新绑定。
     */
    DISCARDED("DISCARDED", "已废弃");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建费率表状态枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    RateTableStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取费率表状态稳定编码。
     *
     * @return 费率表状态编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取费率表状态中文描述。
     *
     * @return 费率表状态描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
