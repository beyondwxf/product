package com.example.insurance.product.application.publish;

import com.example.insurance.product.domain.common.BusinessException;
import com.example.insurance.product.domain.product.ProductVersionStateMachine;
import com.example.insurance.product.domain.product.ProductVersionStatus;
import com.example.insurance.product.domain.publish.ProductPublishCandidate;
import com.example.insurance.product.domain.publish.PublishValidationResult;
import com.example.insurance.product.domain.publish.PublishValidationService;

/**
 * 产品发布应用服务，负责发布用例编排和事务边界承载。
 */
public class ProductPublishApplicationService {
    /**
     * 产品版本仓储端口，用于读取发布候选信息和切换版本状态。
     */
    private final ProductVersionRepository productVersionRepository;
    /**
     * 发布快照仓储端口，用于生成不可变发布快照。
     */
    private final PublishSnapshotRepository publishSnapshotRepository;
    /**
     * 产品缓存端口，用于刷新或删除 Redis 产品缓存。
     */
    private final ProductCachePort productCachePort;
    /**
     * 产品事件发布端口，用于向下游广播发布结果。
     */
    private final ProductEventPublisher productEventPublisher;
    /**
     * 发布完整性校验领域服务。
     */
    private final PublishValidationService publishValidationService;

    /**
     * 创建产品发布应用服务。
     *
     * @param productVersionRepository 产品版本仓储端口
     * @param publishSnapshotRepository 发布快照仓储端口
     * @param productCachePort 产品缓存端口
     * @param productEventPublisher 产品事件发布端口
     * @param publishValidationService 发布完整性校验领域服务
     */
    public ProductPublishApplicationService(ProductVersionRepository productVersionRepository,
                                            PublishSnapshotRepository publishSnapshotRepository,
                                            ProductCachePort productCachePort,
                                            ProductEventPublisher productEventPublisher,
                                            PublishValidationService publishValidationService) {
        this.productVersionRepository = productVersionRepository;
        this.publishSnapshotRepository = publishSnapshotRepository;
        this.productCachePort = productCachePort;
        this.productEventPublisher = productEventPublisher;
        this.publishValidationService = publishValidationService;
    }

    /**
     * 发布指定产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 发布结果
     */
    public PublishResult publish(long versionId, String operator) {
        ProductVersionDraft draft = productVersionRepository.getPublishDraft(versionId);
        ProductVersionStateMachine.assertCanTransit(draft.status(), ProductVersionStatus.ACTIVE);

        ProductPublishCandidate candidate = productVersionRepository.buildPublishCandidate(versionId);
        PublishValidationResult validationResult = publishValidationService.validate(candidate);
        if (!validationResult.passed()) {
            throw new BusinessException("PRODUCT_PUBLISH_VALIDATION_FAILED", "产品发布校验失败");
        }

        PublishSnapshot snapshot = publishSnapshotRepository.createSnapshot(versionId, operator);
        productVersionRepository.activateVersion(versionId, operator);
        productCachePort.refreshCurrentProduct(snapshot.productCode(), snapshot.versionNo());
        productEventPublisher.publishProductActivated(snapshot.productCode(), snapshot.versionNo());
        return new PublishResult(snapshot.productCode(), snapshot.versionNo());
    }
}
