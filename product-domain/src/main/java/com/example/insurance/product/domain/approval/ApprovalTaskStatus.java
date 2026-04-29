package com.example.insurance.product.domain.approval;

import com.example.insurance.product.domain.common.CodedEnum;

/**
 * 审核任务状态枚举。
 */
public enum ApprovalTaskStatus implements CodedEnum {
    /**
     * 待处理，当前审核人需要操作。
     */
    PENDING("PENDING", "待处理"),
    /**
     * 已通过，节点处理完成。
     */
    APPROVED("APPROVED", "已通过"),
    /**
     * 已驳回，流程终止并退回草稿。
     */
    REJECTED("REJECTED", "已驳回"),
    /**
     * 已撤回，提交人取消本次审核。
     */
    CANCELLED("CANCELLED", "已撤回");

    /**
     * 稳定编码，数据库保存该值。
     */
    private final String code;
    /**
     * 中文描述，后台展示使用。
     */
    private final String desc;

    /**
     * 创建审核任务状态枚举项。
     *
     * @param code 稳定编码
     * @param desc 中文描述
     */
    ApprovalTaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取审核任务状态稳定编码。
     *
     * @return 审核任务状态编码
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * 获取审核任务状态中文描述。
     *
     * @return 审核任务状态描述
     */
    @Override
    public String desc() {
        return desc;
    }
}
