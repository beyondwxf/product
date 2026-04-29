package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * 产品主档生命周期状态枚举。
 */
public enum ProductStatus implements CodedEnum {
    /**
     * 草稿状态，可编辑、可删除草稿、可提交审核。
     */
    DRAFT("DRAFT", "草稿"),
    /**
     * 审核中状态，不允许普通编辑。
     */
    IN_REVIEW("IN_REVIEW", "审核中"),
    /**
     * 审核驳回状态，可修改后重新提交。
     */
    REJECTED("REJECTED", "已驳回"),
    /**
     * 审核通过状态，可执行发布或定时发布。
     */
    APPROVED("APPROVED", "已通过"),
    /**
     * 已上架状态，下游可查询和销售。
     */
    PUBLISHED("PUBLISHED", "已上架"),
    /**
     * 已下架状态，不可销售但历史可追溯。
     */
    SUSPENDED("SUSPENDED", "已下架"),
    /**
     * 已失效状态，仅允许查看和复制新草稿。
     */
    EXPIRED("EXPIRED", "已失效");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建产品状态枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    ProductStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取产品状态稳定编码。
     *
     * @return 产品状态编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取产品状态中文描述。
     *
     * @return 产品状态描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
