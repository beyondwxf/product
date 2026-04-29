package com.example.insurance.product.application.product;

/**
 * 保险责任详情。
 *
 * @param id 责任 ID
 * @param versionId 产品版本 ID
 * @param riskId 险种 ID
 * @param liabilityCode 责任编码
 * @param liabilityName 责任名称
 * @param liabilityType 责任类型
 * @param claimType 理赔类型
 * @param amountRule 保额规则 JSON
 * @param waitingDays 等待期天数
 * @param exemptionDesc 免责说明
 * @param paymentCondition 给付条件
 */
public record LiabilityDetail(
        long id,
        long versionId,
        long riskId,
        String liabilityCode,
        String liabilityName,
        String liabilityType,
        String claimType,
        String amountRule,
        Integer waitingDays,
        String exemptionDesc,
        String paymentCondition
) {
}
