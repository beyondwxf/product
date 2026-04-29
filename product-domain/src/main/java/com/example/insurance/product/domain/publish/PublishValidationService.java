package com.example.insurance.product.domain.publish;

import com.example.insurance.product.domain.product.ProductNature;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品发布完整性校验领域服务。
 */
public class PublishValidationService {
    /**
     * 校验产品版本是否满足发布条件。
     *
     * @param candidate 产品发布候选对象
     * @return 发布校验结果
     */
    public PublishValidationResult validate(ProductPublishCandidate candidate) {
        List<PublishValidationIssue> issues = new ArrayList<>();

        require(candidate.productCode() != null && !candidate.productCode().isBlank(), issues,
                "PRODUCT", "PRODUCT_CODE_REQUIRED", "产品编码不能为空");
        require(candidate.versionNo() != null && !candidate.versionNo().isBlank(), issues,
                "VERSION", "VERSION_NO_REQUIRED", "产品版本号不能为空");
        require(candidate.hasMainRisk(), issues, "RISK", "MAIN_RISK_REQUIRED", "至少配置一个主险");
        require(candidate.hasCoverage(), issues, "COVERAGE", "COVERAGE_REQUIRED", "至少配置一个有效责任");
        require(candidate.hasPlan(), issues, "PLAN", "PLAN_REQUIRED", "至少配置一个有效投保方案");
        require(candidate.hasPublishedRate(), issues, "RATE", "PUBLISHED_RATE_REQUIRED", "必须绑定已发布费率表");
        require(candidate.hasRequiredClauses(), issues, "CLAUSE", "REQUIRED_CLAUSE_MISSING", "必须关联必需条款文件");
        require(candidate.hasFieldTemplate(), issues, "FIELD", "FIELD_TEMPLATE_REQUIRED", "必须绑定投保要素模板");
        require(candidate.hasValidQuestionnaire(), issues, "QUESTIONNAIRE", "QUESTIONNAIRE_REQUIRED", "必须绑定有效健康告知问卷");
        require(candidate.hasValidRuleSet(), issues, "RULE_SET", "RULE_SET_INVALID", "前端联动规则必须校验通过");
        require(candidate.hasValidSupplierRelations(), issues, "SUPPLIER", "SUPPLIER_INVALID", "供应商关联必须有效且合同未过期");
        require(!candidate.hasActiveVersionConflict(), issues, "VERSION", "ACTIVE_VERSION_CONFLICT", "同一产品不能存在多个生效版本");

        if (candidate.productNature() == ProductNature.DIVIDEND_LIFE) {
            require(candidate.hasDividendConfig(), issues,
                    "DIVIDEND", "DIVIDEND_CONFIG_REQUIRED", "分红险必须配置分红方式和红利演示参数");
        }

        return new PublishValidationResult(issues);
    }

    /**
     * 当条件不满足时追加发布校验问题。
     *
     * @param condition 校验条件
     * @param issues 问题收集列表
     * @param module 失败模块
     * @param code 稳定错误码
     * @param message 中文错误描述
     */
    private static void require(boolean condition, List<PublishValidationIssue> issues,
                                String module, String code, String message) {
        if (!condition) {
            issues.add(new PublishValidationIssue(module, code, message));
        }
    }
}
