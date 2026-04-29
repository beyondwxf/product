package com.example.insurance.product.application.product;

import com.example.insurance.product.application.publish.ProductVersionRepository;
import com.example.insurance.product.domain.common.BusinessException;
import com.example.insurance.product.domain.publish.PublishValidationResult;
import com.example.insurance.product.domain.publish.PublishValidationService;

/**
 * 产品管理应用服务，负责后台产品主数据用例编排。
 */
public class ProductManagementApplicationService {
    /**
     * 产品目录仓储端口。
     */
    private final ProductCatalogRepository productCatalogRepository;
    /**
     * 产品版本仓储端口。
     */
    private final ProductVersionRepository productVersionRepository;
    /**
     * 发布完整性校验领域服务。
     */
    private final PublishValidationService publishValidationService;

    /**
     * 创建产品管理应用服务。
     *
     * @param productCatalogRepository 产品目录仓储
     * @param productVersionRepository 产品版本仓储
     * @param publishValidationService 发布校验服务
     */
    public ProductManagementApplicationService(ProductCatalogRepository productCatalogRepository,
                                               ProductVersionRepository productVersionRepository,
                                               PublishValidationService publishValidationService) {
        this.productCatalogRepository = productCatalogRepository;
        this.productVersionRepository = productVersionRepository;
        this.publishValidationService = publishValidationService;
    }

    /**
     * 创建产品草稿。
     *
     * @param command 创建产品命令
     * @return 产品详情
     */
    public ProductDetail create(CreateProductCommand command) {
        return productCatalogRepository.create(command);
    }

    /**
     * 分页查询产品列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public PageResult<ProductListItem> list(ProductListQuery query) {
        return productCatalogRepository.list(query);
    }

    /**
     * 查询产品详情。
     *
     * @param productId 产品 ID
     * @return 产品详情
     */
    public ProductDetail detail(long productId) {
        return productCatalogRepository.findDetail(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "产品不存在"));
    }

    /**
     * 提交产品版本审核。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    public ProductDetail submitReview(long versionId, String operator) {
        return productCatalogRepository.submitReview(versionId, operator);
    }

    /**
     * 审核通过产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    public ProductDetail approveReview(long versionId, String operator) {
        return productCatalogRepository.approveReview(versionId, operator);
    }

    /**
     * 执行发布前完整性校验。
     *
     * @param versionId 产品版本 ID
     * @return 发布校验结果
     */
    public PublishValidationResult validatePublish(long versionId) {
        return publishValidationService.validate(productVersionRepository.buildPublishCandidate(versionId));
    }
}
