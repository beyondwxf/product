package com.example.insurance.product.application.product;

/**
 * 保存险种配置命令。
 *
 * @param riskCode 险种编码
 * @param riskName 险种名称
 * @param riskType 险种类型：MAIN 主险，ATTACHMENT 附加险
 * @param coverageAmountType 保额类型
 * @param premiumCalcType 保费计算方式
 * @param sortNo 排序号
 * @param operator 操作人
 */
public record SaveRiskCommand(
        String riskCode,
        String riskName,
        String riskType,
        String coverageAmountType,
        String premiumCalcType,
        int sortNo,
        String operator
) {
}
