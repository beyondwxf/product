package com.example.insurance.product.application.product;

/**
 * 保存保险责任命令。
 *
 * @param liabilityCode 责任编码
 * @param liabilityName 责任名称
 * @param liabilityType 责任类型
 * @param claimType 理赔类型
 * @param amountRule 保额规则 JSON
 * @param waitingDays 等待期天数
 * @param exemptionDesc 免责说明
 * @param paymentCondition 给付条件
 * @param operator 操作人
 */
public record SaveLiabilityCommand(
        String liabilityCode,
        String liabilityName,
        String liabilityType,
        String claimType,
        String amountRule,
        Integer waitingDays,
        String exemptionDesc,
        String paymentCondition,
        String operator
) {
}
