package com.example.insurance.product.api.product;

import com.example.insurance.product.application.product.ProductManagementApplicationService;
import com.example.insurance.product.application.publish.ProductPublishApplicationService;
import com.example.insurance.product.domain.publish.PublishValidationService;
import com.example.insurance.product.infrastructure.repository.InMemoryProductRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 后台产品管理接口测试。
 */
class ProductAdminControllerTest {
    /**
     * 验证空产品列表可正常分页返回。
     */
    @Test
    void shouldListEmptyProducts() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        PublishValidationService validationService = new PublishValidationService();
        ProductAdminController controller = new ProductAdminController(
                new ProductManagementApplicationService(repository, repository, validationService),
                new ProductPublishApplicationService(repository, repository, repository, repository, validationService)
        );

        assertEquals(0, controller.list(null, null, null, null, null, 1, 20).data().total());
    }
}
