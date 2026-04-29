package com.example.insurance.product.domain.publish;

import com.example.insurance.product.domain.product.ProductNature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 产品发布完整性校验单元测试。
 */
class PublishValidationServiceTest {
    /**
     * 发布校验服务被测对象。
     */
    private final PublishValidationService service = new PublishValidationService();

    /**
     * 验证分红寿险发布前必须配置分红参数。
     */
    @Test
    void shouldRequireDividendConfigForDividendLife() {
        ProductPublishCandidate candidate = new ProductPublishCandidate(
                "P001", "V1.0", ProductNature.DIVIDEND_LIFE,
                true, true, true, true, true, true, true, true,
                false, true, false
        );

        PublishValidationResult result = service.validate(candidate);

        assertFalse(result.passed());
        assertTrue(result.issues().stream().anyMatch(issue -> "DIVIDEND_CONFIG_REQUIRED".equals(issue.code())));
    }

    /**
     * 验证传统寿险在配置完整时可通过发布校验。
     */
    @Test
    void shouldPassWhenTraditionalLifeIsComplete() {
        ProductPublishCandidate candidate = new ProductPublishCandidate(
                "P001", "V1.0", ProductNature.TRADITIONAL_LIFE,
                true, true, true, true, true, true, true, true,
                false, true, false
        );

        PublishValidationResult result = service.validate(candidate);

        assertTrue(result.passed());
    }
}
