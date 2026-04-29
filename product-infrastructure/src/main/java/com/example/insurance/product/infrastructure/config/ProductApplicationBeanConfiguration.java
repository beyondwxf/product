package com.example.insurance.product.infrastructure.config;

import com.example.insurance.product.application.open.OpenProductQueryService;
import com.example.insurance.product.application.open.PublishSnapshotQueryRepository;
import com.example.insurance.product.application.product.ProductCatalogRepository;
import com.example.insurance.product.application.product.ProductManagementApplicationService;
import com.example.insurance.product.application.publish.ProductCachePort;
import com.example.insurance.product.application.publish.ProductEventPublisher;
import com.example.insurance.product.application.publish.ProductPublishApplicationService;
import com.example.insurance.product.application.publish.ProductVersionRepository;
import com.example.insurance.product.application.publish.PublishSnapshotRepository;
import com.example.insurance.product.domain.publish.PublishValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 产品中心应用层 Bean 配置。
 */
@Configuration
public class ProductApplicationBeanConfiguration {
    /**
     * 创建发布完整性校验领域服务。
     *
     * @return 发布校验服务
     */
    @Bean
    public PublishValidationService publishValidationService() {
        return new PublishValidationService();
    }

    /**
     * 创建产品管理应用服务。
     *
     * @param productCatalogRepository 产品目录仓储
     * @param productVersionRepository 产品版本仓储
     * @param publishValidationService 发布校验服务
     * @return 产品管理应用服务
     */
    @Bean
    public ProductManagementApplicationService productManagementApplicationService(
            ProductCatalogRepository productCatalogRepository,
            ProductVersionRepository productVersionRepository,
            PublishValidationService publishValidationService) {
        return new ProductManagementApplicationService(
                productCatalogRepository,
                productVersionRepository,
                publishValidationService
        );
    }

    /**
     * 创建产品发布应用服务。
     *
     * @param productVersionRepository 产品版本仓储
     * @param publishSnapshotRepository 发布快照仓储
     * @param productCachePort 产品缓存端口
     * @param productEventPublisher 产品事件端口
     * @param publishValidationService 发布校验服务
     * @return 产品发布应用服务
     */
    @Bean
    public ProductPublishApplicationService productPublishApplicationService(
            ProductVersionRepository productVersionRepository,
            PublishSnapshotRepository publishSnapshotRepository,
            ProductCachePort productCachePort,
            ProductEventPublisher productEventPublisher,
            PublishValidationService publishValidationService) {
        return new ProductPublishApplicationService(
                productVersionRepository,
                publishSnapshotRepository,
                productCachePort,
                productEventPublisher,
                publishValidationService
        );
    }

    /**
     * 创建开放产品查询服务。
     *
     * @param publishSnapshotQueryRepository 发布快照查询仓储
     * @return 开放产品查询服务
     */
    @Bean
    public OpenProductQueryService openProductQueryService(
            PublishSnapshotQueryRepository publishSnapshotQueryRepository) {
        return new OpenProductQueryService(publishSnapshotQueryRepository);
    }
}
