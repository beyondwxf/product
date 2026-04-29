package com.example.insurance.product.application.product;

import com.example.insurance.product.application.publish.ProductVersionRepository;
import com.example.insurance.product.domain.common.BusinessException;
import com.example.insurance.product.domain.publish.PublishValidationResult;
import com.example.insurance.product.domain.publish.PublishValidationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 产品管理应用服务，负责后台产品主数据用例编排。
 */
@Transactional
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
     * 编辑产品草稿。
     *
     * @param productId 产品 ID
     * @param command 编辑命令
     * @return 产品详情
     */
    public ProductDetail updateDraft(long productId, UpdateProductCommand command) {
        return productCatalogRepository.updateDraft(productId, command);
    }

    /**
     * 创建新草稿版本。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    public ProductDetail createNewVersion(long productId, String operator) {
        return productCatalogRepository.createNewVersion(productId, operator);
    }

    /**
     * 删除草稿产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     */
    public void deleteDraft(long productId, String operator) {
        productCatalogRepository.deleteDraft(productId, operator);
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
     * 驳回产品版本。
     *
     * @param versionId 产品版本 ID
     * @param reason 驳回原因
     * @param operator 操作人
     * @return 产品详情
     */
    public ProductDetail rejectReview(long versionId, String reason, String operator) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("REJECT_REASON_REQUIRED", "驳回原因不能为空");
        }
        return productCatalogRepository.rejectReview(versionId, reason, operator);
    }

    /**
     * 下架产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    public ProductDetail suspend(long productId, String operator) {
        return productCatalogRepository.suspend(productId, operator);
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

    /**
     * 创建险种配置。
     *
     * @param versionId 产品版本 ID
     * @param command 险种命令
     * @return 险种详情
     */
    public RiskItemDetail createRisk(long versionId, SaveRiskCommand command) {
        return productCatalogRepository.createRisk(versionId, command);
    }

    /**
     * 查询版本险种列表。
     *
     * @param versionId 产品版本 ID
     * @return 险种列表
     */
    public List<RiskItemDetail> listRisks(long versionId) {
        return productCatalogRepository.listRisks(versionId);
    }

    /**
     * 更新险种配置。
     *
     * @param riskId 险种 ID
     * @param command 险种命令
     * @return 险种详情
     */
    public RiskItemDetail updateRisk(long riskId, SaveRiskCommand command) {
        return productCatalogRepository.updateRisk(riskId, command);
    }

    /**
     * 删除险种配置。
     *
     * @param riskId 险种 ID
     * @param operator 操作人
     */
    public void deleteRisk(long riskId, String operator) {
        productCatalogRepository.deleteRisk(riskId, operator);
    }

    /**
     * 创建责任配置。
     *
     * @param riskId 险种 ID
     * @param command 责任命令
     * @return 责任详情
     */
    public LiabilityDetail createLiability(long riskId, SaveLiabilityCommand command) {
        return productCatalogRepository.createLiability(riskId, command);
    }

    /**
     * 查询险种责任列表。
     *
     * @param riskId 险种 ID
     * @return 责任列表
     */
    public List<LiabilityDetail> listLiabilities(long riskId) {
        return productCatalogRepository.listLiabilities(riskId);
    }

    /**
     * 更新责任配置。
     *
     * @param liabilityId 责任 ID
     * @param command 责任命令
     * @return 责任详情
     */
    public LiabilityDetail updateLiability(long liabilityId, SaveLiabilityCommand command) {
        return productCatalogRepository.updateLiability(liabilityId, command);
    }

    /**
     * 删除责任配置。
     *
     * @param liabilityId 责任 ID
     * @param operator 操作人
     */
    public void deleteLiability(long liabilityId, String operator) {
        productCatalogRepository.deleteLiability(liabilityId, operator);
    }

    /**
     * 创建条款引用。
     *
     * @param versionId 产品版本 ID
     * @param command 条款引用命令
     * @return 条款引用详情
     */
    public ClauseDocumentRefDetail createClause(long versionId, SaveClauseCommand command) {
        return productCatalogRepository.createClause(versionId, command);
    }

    /**
     * 查询版本条款引用列表。
     *
     * @param versionId 产品版本 ID
     * @return 条款引用列表
     */
    public List<ClauseDocumentRefDetail> listClauses(long versionId) {
        return productCatalogRepository.listClauses(versionId);
    }

    /**
     * 删除条款引用。
     *
     * @param clauseId 条款引用 ID
     * @param operator 操作人
     */
    public void deleteClause(long clauseId, String operator) {
        productCatalogRepository.deleteClause(clauseId, operator);
    }
}
