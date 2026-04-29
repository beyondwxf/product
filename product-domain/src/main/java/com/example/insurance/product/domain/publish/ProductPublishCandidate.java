package com.example.insurance.product.domain.publish;

import com.example.insurance.product.domain.product.ProductNature;

/**
 * 产品发布候选对象，承载发布校验所需的聚合完整性标记。
 *
 * @param productCode 产品编码
 * @param versionNo 产品版本号
 * @param productNature 产品属性，区分传统寿险和分红寿险
 * @param hasMainRisk 是否已配置主险
 * @param hasCoverage 是否已配置有效责任
 * @param hasPlan 是否已配置有效投保方案
 * @param hasPublishedRate 是否已绑定已发布费率
 * @param hasRequiredClauses 是否已关联必需条款
 * @param hasFieldTemplate 是否已绑定投保要素模板
 * @param hasValidQuestionnaire 是否已绑定有效健康告知问卷
 * @param hasValidRuleSet 前端联动规则是否校验通过
 * @param hasDividendConfig 是否已配置分红方式和红利演示参数
 * @param hasValidSupplierRelations 供应商关联是否有效
 * @param hasActiveVersionConflict 是否存在当前生效版本冲突
 */
public record ProductPublishCandidate(
        String productCode,
        String versionNo,
        ProductNature productNature,
        boolean hasMainRisk,
        boolean hasCoverage,
        boolean hasPlan,
        boolean hasPublishedRate,
        boolean hasRequiredClauses,
        boolean hasFieldTemplate,
        boolean hasValidQuestionnaire,
        boolean hasValidRuleSet,
        boolean hasDividendConfig,
        boolean hasValidSupplierRelations,
        boolean hasActiveVersionConflict
) {
}
