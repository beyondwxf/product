package com.example.insurance.product.api.product;

import com.example.insurance.product.application.product.ProductManagementApplicationService;
import com.example.insurance.product.application.publish.ProductPublishApplicationService;
import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductType;
import com.example.insurance.product.domain.publish.PublishValidationService;
import com.example.insurance.product.infrastructure.repository.InMemoryProductRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 后台产品管理接口测试。
 */
class ProductAdminControllerTest {
    /**
     * 验证空产品列表可正常分页返回。
     */
    @Test
    void shouldListEmptyProducts() {
        ProductAdminController controller = createController();

        assertEquals(0, controller.list(null, null, null, null, null, 1, 20).data().total());
    }

    /**
     * 验证配置险种、责任和条款后可完成发布校验。
     */
    @Test
    void shouldValidatePublishAfterRequiredConfigurations() {
        ProductAdminController controller = createController();
        ProductDetailResponse product = controller.create(new CreateProductRequest(
                "TL001",
                "定期寿险测试产品",
                "定寿测试",
                ProductType.TERM_LIFE,
                ProductNature.TRADITIONAL_LIFE,
                "示例保险公司",
                "APP"
        ), "tester").data();

        RiskItemResponse risk = controller.createRisk(product.versionId(), new SaveRiskRequest(
                "MAIN_RISK",
                "主险",
                "MAIN",
                "FIXED",
                "STANDARD",
                1
        ), "tester").data();
        controller.createLiability(risk.id(), new SaveLiabilityRequest(
                "DEATH",
                "身故责任",
                "BASE",
                "DEATH",
                "{\"amount\":\"100000\"}",
                0,
                "无",
                "一次性给付"
        ), "tester");
        controller.createClause(product.versionId(), new SaveClauseRequest(
                "POLICY_CLAUSE",
                "DOC001",
                "V1",
                "https://example.com/doc/DOC001"
        ), "tester");

        assertTrue(controller.validate(product.versionId()).data().passed());
    }

    /**
     * 创建测试控制器。
     *
     * @return 后台产品管理控制器
     */
    private static ProductAdminController createController() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        PublishValidationService validationService = new PublishValidationService();
        return new ProductAdminController(
                new ProductManagementApplicationService(repository, repository, validationService),
                new ProductPublishApplicationService(repository, repository, repository, repository, validationService)
        );
    }
}
