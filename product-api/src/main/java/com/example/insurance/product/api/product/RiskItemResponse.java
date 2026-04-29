package com.example.insurance.product.api.product;

/**
 * 险种配置响应。
 *
 * @param id 险种 ID
 * @param versionId 产品版本 ID
 * @param riskCode 险种编码
 * @param riskName 险种名称
 * @param riskType 险种类型
 * @param coverageAmountType 保额类型
 * @param premiumCalcType 保费计算方式
 * @param sortNo 排序号
 */
public record RiskItemResponse(
        long id,
        long versionId,
        String riskCode,
        String riskName,
        String riskType,
        String coverageAmountType,
        String premiumCalcType,
        int sortNo
) {
}
