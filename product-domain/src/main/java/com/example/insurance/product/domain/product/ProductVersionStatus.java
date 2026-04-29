package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * 产品版本生命周期状态枚举。
 */
public enum ProductVersionStatus implements CodedEnum {
    /**
     * 草稿版本，可编辑。
     */
    DRAFT("DRAFT", "草稿"),
    /**
     * 审核中版本，不允许普通编辑。
     */
    IN_REVIEW("IN_REVIEW", "审核中"),
    /**
     * 审核通过版本，允许进入发布。
     */
    APPROVED("APPROVED", "已通过"),
    /**
     * 生效中版本，开放接口可读取。
     */
    ACTIVE("ACTIVE", "生效中"),
    /**
     * 已失效版本，仅用于历史追溯。
     */
    INACTIVE("INACTIVE", "已失效");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建产品版本状态枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    ProductVersionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取产品版本状态稳定编码。
     *
     * @return 产品版本状态编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取产品版本状态中文描述。
     *
     * @return 产品版本状态描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
