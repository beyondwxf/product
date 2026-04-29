package com.example.insurance.product.infrastructure.repository;

import com.example.insurance.product.application.open.PublishSnapshotQueryRepository;
import com.example.insurance.product.application.product.ClauseDocumentRefDetail;
import com.example.insurance.product.application.product.CreateProductCommand;
import com.example.insurance.product.application.product.LiabilityDetail;
import com.example.insurance.product.application.product.PageResult;
import com.example.insurance.product.application.product.ProductCatalogRepository;
import com.example.insurance.product.application.product.ProductDetail;
import com.example.insurance.product.application.product.ProductListItem;
import com.example.insurance.product.application.product.ProductListQuery;
import com.example.insurance.product.application.product.RiskItemDetail;
import com.example.insurance.product.application.product.SaveClauseCommand;
import com.example.insurance.product.application.product.SaveLiabilityCommand;
import com.example.insurance.product.application.product.SaveRiskCommand;
import com.example.insurance.product.application.product.UpdateProductCommand;
import com.example.insurance.product.application.publish.ProductCachePort;
import com.example.insurance.product.application.publish.ProductEventPublisher;
import com.example.insurance.product.application.publish.ProductVersionDraft;
import com.example.insurance.product.application.publish.ProductVersionRepository;
import com.example.insurance.product.application.publish.PublishSnapshot;
import com.example.insurance.product.application.publish.PublishSnapshotRepository;
import com.example.insurance.product.domain.common.BusinessException;
import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductStateMachine;
import com.example.insurance.product.domain.product.ProductStatus;
import com.example.insurance.product.domain.product.ProductType;
import com.example.insurance.product.domain.product.ProductVersionStateMachine;
import com.example.insurance.product.domain.product.ProductVersionStatus;
import com.example.insurance.product.domain.publish.ProductPublishCandidate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存产品仓储实现，供首批需求闭环和本地联调使用。
 */
public class InMemoryProductRepository implements ProductCatalogRepository,
        ProductVersionRepository,
        PublishSnapshotRepository,
        PublishSnapshotQueryRepository,
        ProductCachePort,
        ProductEventPublisher {
    /**
     * 产品 ID 序列。
     */
    private final AtomicLong productIdSequence = new AtomicLong(1000);
    /**
     * 产品版本 ID 序列。
     */
    private final AtomicLong versionIdSequence = new AtomicLong(2000);
    /**
     * 险种 ID 序列。
     */
    private final AtomicLong riskIdSequence = new AtomicLong(4000);
    /**
     * 责任 ID 序列。
     */
    private final AtomicLong liabilityIdSequence = new AtomicLong(5000);
    /**
     * 条款引用 ID 序列。
     */
    private final AtomicLong clauseIdSequence = new AtomicLong(6000);
    /**
     * 发布快照 ID 序列。
     */
    private final AtomicLong snapshotIdSequence = new AtomicLong(3000);
    /**
     * 产品主档内存表。
     */
    private final Map<Long, ProductRow> products = new LinkedHashMap<>();
    /**
     * 产品版本内存表。
     */
    private final Map<Long, VersionRow> versions = new LinkedHashMap<>();
    /**
     * 险种内存表。
     */
    private final Map<Long, RiskRow> risks = new LinkedHashMap<>();
    /**
     * 责任内存表。
     */
    private final Map<Long, LiabilityRow> liabilities = new LinkedHashMap<>();
    /**
     * 条款引用内存表。
     */
    private final Map<Long, ClauseRow> clauses = new LinkedHashMap<>();
    /**
     * 发布快照内存表。
     */
    private final Map<String, SnapshotRow> snapshots = new LinkedHashMap<>();

    /**
     * 创建产品主档和 V1.0 草稿版本。
     *
     * @param command 创建产品命令
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail create(CreateProductCommand command) {
        validateCreateCommand(command);
        boolean exists = products.values().stream()
                .anyMatch(product -> product.productCode().equalsIgnoreCase(command.productCode()));
        if (exists) {
            throw new BusinessException("PRODUCT_CODE_DUPLICATED", "产品编码已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        long productId = productIdSequence.incrementAndGet();
        long versionId = versionIdSequence.incrementAndGet();
        ProductRow product = new ProductRow(
                productId,
                command.productCode(),
                command.productName(),
                command.shortName(),
                command.productType(),
                command.productNature(),
                command.insuranceCompany(),
                command.salesChannels(),
                ProductStatus.DRAFT,
                versionId,
                false,
                now,
                command.operator()
        );
        VersionRow version = new VersionRow(
                versionId,
                productId,
                command.productCode(),
                "V1.0",
                ProductVersionStatus.DRAFT,
                true,
                true,
                true,
                true,
                command.productNature() != ProductNature.DIVIDEND_LIFE,
                true,
                now,
                command.operator()
        );
        products.put(productId, product);
        versions.put(versionId, version);
        return toDetail(product, version);
    }

    /**
     * 编辑草稿产品基础信息。
     *
     * @param productId 产品 ID
     * @param command 编辑产品命令
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail updateDraft(long productId, UpdateProductCommand command) {
        validateUpdateCommand(command);
        ProductRow product = requireProduct(productId);
        VersionRow version = requireVersion(product.currentVersionId());
        assertDraftEditable(product, version);

        LocalDateTime now = LocalDateTime.now();
        ProductStatus targetStatus = product.status() == ProductStatus.REJECTED ? ProductStatus.DRAFT : product.status();
        if (product.status() == ProductStatus.REJECTED) {
            ProductStateMachine.assertCanTransit(ProductStatus.REJECTED, ProductStatus.DRAFT);
        }
        ProductRow updatedProduct = product.withBasicInfo(
                command.productName(),
                command.shortName(),
                command.productType(),
                command.productNature(),
                command.insuranceCompany(),
                command.salesChannels(),
                targetStatus,
                now,
                command.operator()
        );
        VersionRow updatedVersion = version.withDividendRequired(command.productNature(), now, command.operator());
        products.put(productId, updatedProduct);
        versions.put(version.id(), updatedVersion);
        return toDetail(updatedProduct, updatedVersion);
    }

    /**
     * 基于当前产品版本创建新草稿版本。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail createNewVersion(long productId, String operator) {
        ProductRow product = requireProduct(productId);
        VersionRow currentVersion = requireVersion(product.currentVersionId());
        if (currentVersion.status() == ProductVersionStatus.DRAFT || currentVersion.status() == ProductVersionStatus.IN_REVIEW) {
            throw new BusinessException("PRODUCT_VERSION_DRAFT_EXISTS", "当前产品已存在不可复制的新版本");
        }

        LocalDateTime now = LocalDateTime.now();
        long versionId = versionIdSequence.incrementAndGet();
        VersionRow newVersion = new VersionRow(
                versionId,
                product.id(),
                product.productCode(),
                nextVersionNo(currentVersion.versionNo()),
                ProductVersionStatus.DRAFT,
                currentVersion.hasPlan(),
                currentVersion.hasPublishedRate(),
                currentVersion.hasFieldTemplate(),
                currentVersion.hasValidQuestionnaire(),
                product.productNature() != ProductNature.DIVIDEND_LIFE || currentVersion.hasDividendConfig(),
                currentVersion.hasValidSupplierRelations(),
                now,
                operator
        );
        ProductRow updatedProduct = product.withCurrentVersion(versionId, ProductStatus.DRAFT, now, operator);
        versions.put(versionId, newVersion);
        copyVersionConfiguration(currentVersion.id(), versionId);
        products.put(product.id(), updatedProduct);
        return toDetail(updatedProduct, newVersion);
    }

    /**
     * 删除草稿产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     */
    @Override
    public synchronized void deleteDraft(long productId, String operator) {
        ProductRow product = requireProduct(productId);
        VersionRow version = requireVersion(product.currentVersionId());
        assertDraftEditable(product, version);
        ProductStateMachine.assertCanTransit(product.status(), ProductStatus.EXPIRED);
        products.put(productId, product.withStatus(ProductStatus.EXPIRED, LocalDateTime.now(), operator));
        versions.put(version.id(), version.withStatus(ProductVersionStatus.INACTIVE, LocalDateTime.now(), operator));
    }

    /**
     * 分页查询产品列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public synchronized PageResult<ProductListItem> list(ProductListQuery query) {
        int pageNo = Math.max(query.pageNo(), 1);
        int pageSize = Math.min(Math.max(query.pageSize(), 1), 100);
        List<ProductListItem> filtered = products.values().stream()
                .filter(product -> contains(product.productCode(), query.productCode()))
                .filter(product -> contains(product.productName(), query.productName()))
                .filter(product -> query.productType() == null || product.productType() == query.productType())
                .filter(product -> query.productNature() == null || product.productNature() == query.productNature())
                .filter(product -> query.status() == null || product.status() == query.status())
                .filter(product -> !product.deleted())
                .sorted(Comparator.comparing(ProductRow::updatedAt).reversed())
                .map(product -> toListItem(product, versions.get(product.currentVersionId())))
                .toList();
        int fromIndex = Math.min((pageNo - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        return new PageResult<>(pageNo, pageSize, filtered.size(), filtered.subList(fromIndex, toIndex));
    }

    /**
     * 按产品 ID 查询产品详情。
     *
     * @param productId 产品 ID
     * @return 产品详情
     */
    @Override
    public synchronized Optional<ProductDetail> findDetail(long productId) {
        ProductRow product = products.get(productId);
        if (product == null || product.deleted()) {
            return Optional.empty();
        }
        return Optional.of(toDetail(product, versions.get(product.currentVersionId())));
    }

    /**
     * 提交产品版本审核。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail submitReview(long versionId, String operator) {
        VersionRow version = requireVersion(versionId);
        ProductVersionStateMachine.assertCanTransit(version.status(), ProductVersionStatus.IN_REVIEW);
        ProductRow product = requireProduct(version.productId());
        ProductStateMachine.assertCanTransit(product.status(), ProductStatus.IN_REVIEW);
        LocalDateTime now = LocalDateTime.now();
        VersionRow updatedVersion = version.withStatus(ProductVersionStatus.IN_REVIEW, now, operator);
        ProductRow updatedProduct = product.withStatus(ProductStatus.IN_REVIEW, now, operator);
        versions.put(versionId, updatedVersion);
        products.put(product.id(), updatedProduct);
        return toDetail(updatedProduct, updatedVersion);
    }

    /**
     * 审核通过产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail approveReview(long versionId, String operator) {
        VersionRow version = requireVersion(versionId);
        ProductVersionStateMachine.assertCanTransit(version.status(), ProductVersionStatus.APPROVED);
        ProductRow product = requireProduct(version.productId());
        ProductStateMachine.assertCanTransit(product.status(), ProductStatus.APPROVED);
        LocalDateTime now = LocalDateTime.now();
        VersionRow updatedVersion = version.withStatus(ProductVersionStatus.APPROVED, now, operator);
        ProductRow updatedProduct = product.withStatus(ProductStatus.APPROVED, now, operator);
        versions.put(versionId, updatedVersion);
        products.put(product.id(), updatedProduct);
        return toDetail(updatedProduct, updatedVersion);
    }

    /**
     * 驳回产品版本。
     *
     * @param versionId 产品版本 ID
     * @param reason 驳回原因
     * @param operator 操作人
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail rejectReview(long versionId, String reason, String operator) {
        VersionRow version = requireVersion(versionId);
        ProductVersionStateMachine.assertCanTransit(version.status(), ProductVersionStatus.DRAFT);
        ProductRow product = requireProduct(version.productId());
        ProductStateMachine.assertCanTransit(product.status(), ProductStatus.REJECTED);
        LocalDateTime now = LocalDateTime.now();
        VersionRow updatedVersion = version.withStatus(ProductVersionStatus.DRAFT, now, operator);
        ProductRow updatedProduct = product.withStatus(ProductStatus.REJECTED, now, operator);
        versions.put(versionId, updatedVersion);
        products.put(product.id(), updatedProduct);
        return toDetail(updatedProduct, updatedVersion);
    }

    /**
     * 下架产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @Override
    public synchronized ProductDetail suspend(long productId, String operator) {
        ProductRow product = requireProduct(productId);
        ProductStateMachine.assertCanTransit(product.status(), ProductStatus.SUSPENDED);
        VersionRow version = requireVersion(product.currentVersionId());
        if (version.status() == ProductVersionStatus.ACTIVE) {
            ProductVersionStateMachine.assertCanTransit(version.status(), ProductVersionStatus.INACTIVE);
        }
        LocalDateTime now = LocalDateTime.now();
        ProductRow updatedProduct = product.withStatus(ProductStatus.SUSPENDED, now, operator);
        VersionRow updatedVersion = version.status() == ProductVersionStatus.ACTIVE
                ? version.withStatus(ProductVersionStatus.INACTIVE, now, operator)
                : version.withUpdated(now, operator);
        products.put(product.id(), updatedProduct);
        versions.put(version.id(), updatedVersion);
        evictCurrentProduct(product.productCode());
        publishProductSuspended(product.productCode(), version.versionNo());
        return toDetail(updatedProduct, updatedVersion);
    }

    /**
     * 创建险种配置。
     *
     * @param versionId 产品版本 ID
     * @param command 险种命令
     * @return 险种详情
     */
    @Override
    public synchronized RiskItemDetail createRisk(long versionId, SaveRiskCommand command) {
        VersionRow version = requireVersion(versionId);
        assertVersionDraft(version);
        validateRiskCommand(command);
        boolean duplicated = risks.values().stream()
                .anyMatch(risk -> risk.versionId() == versionId
                        && risk.riskCode().equalsIgnoreCase(command.riskCode()));
        if (duplicated) {
            throw new BusinessException("RISK_CODE_DUPLICATED", "险种编码在当前版本下已存在");
        }
        long riskId = riskIdSequence.incrementAndGet();
        RiskRow risk = new RiskRow(riskId, versionId, command.riskCode(), command.riskName(), command.riskType(),
                command.coverageAmountType(), command.premiumCalcType(), command.sortNo());
        risks.put(riskId, risk);
        touchVersion(versionId, command.operator());
        return toRiskDetail(risk);
    }

    /**
     * 查询版本险种列表。
     *
     * @param versionId 产品版本 ID
     * @return 险种列表
     */
    @Override
    public synchronized List<RiskItemDetail> listRisks(long versionId) {
        requireVersion(versionId);
        return risks.values().stream()
                .filter(risk -> risk.versionId() == versionId)
                .sorted(Comparator.comparingInt(RiskRow::sortNo).thenComparing(RiskRow::id))
                .map(InMemoryProductRepository::toRiskDetail)
                .toList();
    }

    /**
     * 更新险种配置。
     *
     * @param riskId 险种 ID
     * @param command 险种命令
     * @return 险种详情
     */
    @Override
    public synchronized RiskItemDetail updateRisk(long riskId, SaveRiskCommand command) {
        RiskRow risk = requireRisk(riskId);
        assertVersionDraft(requireVersion(risk.versionId()));
        validateRiskCommand(command);
        boolean duplicated = risks.values().stream()
                .anyMatch(item -> item.id() != riskId
                        && item.versionId() == risk.versionId()
                        && item.riskCode().equalsIgnoreCase(command.riskCode()));
        if (duplicated) {
            throw new BusinessException("RISK_CODE_DUPLICATED", "险种编码在当前版本下已存在");
        }
        RiskRow updated = new RiskRow(riskId, risk.versionId(), command.riskCode(), command.riskName(),
                command.riskType(), command.coverageAmountType(), command.premiumCalcType(), command.sortNo());
        risks.put(riskId, updated);
        touchVersion(risk.versionId(), command.operator());
        return toRiskDetail(updated);
    }

    /**
     * 删除草稿版本险种。
     *
     * @param riskId 险种 ID
     * @param operator 操作人
     */
    @Override
    public synchronized void deleteRisk(long riskId, String operator) {
        RiskRow risk = requireRisk(riskId);
        assertVersionDraft(requireVersion(risk.versionId()));
        liabilities.entrySet().removeIf(entry -> entry.getValue().riskId() == riskId);
        risks.remove(riskId);
        touchVersion(risk.versionId(), operator);
    }

    /**
     * 创建责任配置。
     *
     * @param riskId 险种 ID
     * @param command 责任命令
     * @return 责任详情
     */
    @Override
    public synchronized LiabilityDetail createLiability(long riskId, SaveLiabilityCommand command) {
        RiskRow risk = requireRisk(riskId);
        assertVersionDraft(requireVersion(risk.versionId()));
        validateLiabilityCommand(command);
        boolean duplicated = liabilities.values().stream()
                .anyMatch(liability -> liability.riskId() == riskId
                        && liability.liabilityCode().equalsIgnoreCase(command.liabilityCode()));
        if (duplicated) {
            throw new BusinessException("LIABILITY_CODE_DUPLICATED", "责任编码在当前险种下已存在");
        }
        long liabilityId = liabilityIdSequence.incrementAndGet();
        LiabilityRow liability = new LiabilityRow(liabilityId, risk.versionId(), riskId, command.liabilityCode(),
                command.liabilityName(), command.liabilityType(), command.claimType(), command.amountRule(),
                command.waitingDays(), command.exemptionDesc(), command.paymentCondition());
        liabilities.put(liabilityId, liability);
        touchVersion(risk.versionId(), command.operator());
        return toLiabilityDetail(liability);
    }

    /**
     * 查询险种责任列表。
     *
     * @param riskId 险种 ID
     * @return 责任列表
     */
    @Override
    public synchronized List<LiabilityDetail> listLiabilities(long riskId) {
        requireRisk(riskId);
        return liabilities.values().stream()
                .filter(liability -> liability.riskId() == riskId)
                .sorted(Comparator.comparing(LiabilityRow::liabilityCode))
                .map(InMemoryProductRepository::toLiabilityDetail)
                .toList();
    }

    /**
     * 更新责任配置。
     *
     * @param liabilityId 责任 ID
     * @param command 责任命令
     * @return 责任详情
     */
    @Override
    public synchronized LiabilityDetail updateLiability(long liabilityId, SaveLiabilityCommand command) {
        LiabilityRow liability = requireLiability(liabilityId);
        assertVersionDraft(requireVersion(liability.versionId()));
        validateLiabilityCommand(command);
        boolean duplicated = liabilities.values().stream()
                .anyMatch(item -> item.id() != liabilityId
                        && item.riskId() == liability.riskId()
                        && item.liabilityCode().equalsIgnoreCase(command.liabilityCode()));
        if (duplicated) {
            throw new BusinessException("LIABILITY_CODE_DUPLICATED", "责任编码在当前险种下已存在");
        }
        LiabilityRow updated = new LiabilityRow(liabilityId, liability.versionId(), liability.riskId(),
                command.liabilityCode(), command.liabilityName(), command.liabilityType(), command.claimType(),
                command.amountRule(), command.waitingDays(), command.exemptionDesc(), command.paymentCondition());
        liabilities.put(liabilityId, updated);
        touchVersion(liability.versionId(), command.operator());
        return toLiabilityDetail(updated);
    }

    /**
     * 删除草稿版本责任。
     *
     * @param liabilityId 责任 ID
     * @param operator 操作人
     */
    @Override
    public synchronized void deleteLiability(long liabilityId, String operator) {
        LiabilityRow liability = requireLiability(liabilityId);
        assertVersionDraft(requireVersion(liability.versionId()));
        liabilities.remove(liabilityId);
        touchVersion(liability.versionId(), operator);
    }

    /**
     * 创建条款引用。
     *
     * @param versionId 产品版本 ID
     * @param command 条款引用命令
     * @return 条款引用详情
     */
    @Override
    public synchronized ClauseDocumentRefDetail createClause(long versionId, SaveClauseCommand command) {
        VersionRow version = requireVersion(versionId);
        assertVersionDraft(version);
        validateClauseCommand(command);
        boolean duplicated = clauses.values().stream()
                .anyMatch(clause -> clause.versionId() == versionId
                        && clause.documentType().equalsIgnoreCase(command.documentType())
                        && clause.documentId().equalsIgnoreCase(command.documentId()));
        if (duplicated) {
            throw new BusinessException("CLAUSE_DOCUMENT_DUPLICATED", "条款文件在当前版本下已存在");
        }
        long clauseId = clauseIdSequence.incrementAndGet();
        ClauseRow clause = new ClauseRow(clauseId, versionId, command.documentType(), command.documentId(),
                command.documentVersion(), command.previewUrl());
        clauses.put(clauseId, clause);
        touchVersion(versionId, command.operator());
        return toClauseDetail(clause);
    }

    /**
     * 查询版本条款引用列表。
     *
     * @param versionId 产品版本 ID
     * @return 条款引用列表
     */
    @Override
    public synchronized List<ClauseDocumentRefDetail> listClauses(long versionId) {
        requireVersion(versionId);
        return clauses.values().stream()
                .filter(clause -> clause.versionId() == versionId)
                .sorted(Comparator.comparing(ClauseRow::documentType).thenComparing(ClauseRow::id))
                .map(InMemoryProductRepository::toClauseDetail)
                .toList();
    }

    /**
     * 删除条款引用。
     *
     * @param clauseId 条款引用 ID
     * @param operator 操作人
     */
    @Override
    public synchronized void deleteClause(long clauseId, String operator) {
        ClauseRow clause = requireClause(clauseId);
        assertVersionDraft(requireVersion(clause.versionId()));
        clauses.remove(clauseId);
        touchVersion(clause.versionId(), operator);
    }

    /**
     * 获取待发布版本摘要。
     *
     * @param versionId 产品版本 ID
     * @return 产品版本摘要
     */
    @Override
    public synchronized ProductVersionDraft getPublishDraft(long versionId) {
        VersionRow version = requireVersion(versionId);
        return new ProductVersionDraft(version.id(), version.productCode(), version.versionNo(), version.status());
    }

    /**
     * 组装发布校验候选对象。
     *
     * @param versionId 产品版本 ID
     * @return 发布候选对象
     */
    @Override
    public synchronized ProductPublishCandidate buildPublishCandidate(long versionId) {
        VersionRow version = requireVersion(versionId);
        ProductRow product = requireProduct(version.productId());
        boolean hasMainRisk = risks.values().stream()
                .anyMatch(risk -> risk.versionId() == versionId && "MAIN".equalsIgnoreCase(risk.riskType()));
        boolean hasCoverage = liabilities.values().stream()
                .anyMatch(liability -> liability.versionId() == versionId);
        boolean hasRequiredClauses = clauses.values().stream()
                .anyMatch(clause -> clause.versionId() == versionId);
        return new ProductPublishCandidate(
                version.productCode(),
                version.versionNo(),
                product.productNature(),
                hasMainRisk,
                hasCoverage,
                version.hasPlan(),
                version.hasPublishedRate(),
                hasRequiredClauses,
                version.hasFieldTemplate(),
                version.hasValidQuestionnaire(),
                true,
                version.hasDividendConfig(),
                version.hasValidSupplierRelations(),
                false
        );
    }

    /**
     * 激活产品版本并下架旧版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     */
    @Override
    public synchronized void activateVersion(long versionId, String operator) {
        VersionRow target = requireVersion(versionId);
        ProductRow product = requireProduct(target.productId());
        LocalDateTime now = LocalDateTime.now();
        versions.replaceAll((id, version) -> version.productCode().equals(target.productCode())
                && version.status() == ProductVersionStatus.ACTIVE
                ? version.withStatus(ProductVersionStatus.INACTIVE, now, operator)
                : version);
        versions.put(versionId, target.withStatus(ProductVersionStatus.ACTIVE, now, operator));
        products.put(product.id(), product.withStatus(ProductStatus.PUBLISHED, now, operator));
    }

    /**
     * 创建不可变发布快照。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 发布快照摘要
     */
    @Override
    public synchronized PublishSnapshot createSnapshot(long versionId, String operator) {
        VersionRow version = requireVersion(versionId);
        ProductRow product = requireProduct(version.productId());
        String snapshotJson = """
                {"productCode":"%s","productName":"%s","versionNo":"%s","productType":"%s","productNature":"%s","status":"PUBLISHED"}
                """.formatted(
                escape(product.productCode()),
                escape(product.productName()),
                escape(version.versionNo()),
                product.productType().code(),
                product.productNature().code()
        ).trim();
        snapshots.replaceAll((key, snapshot) -> snapshot.productCode().equals(product.productCode())
                ? snapshot.withCurrent(false)
                : snapshot);
        snapshots.put(snapshotKey(product.productCode(), version.versionNo()),
                new SnapshotRow(product.productCode(), version.versionNo(), snapshotJson, true));
        return new PublishSnapshot(snapshotIdSequence.incrementAndGet(), product.productCode(), version.versionNo());
    }

    /**
     * 查询当前生效快照 JSON。
     *
     * @param productCode 产品编码
     * @return 快照 JSON
     */
    @Override
    public synchronized Optional<String> findCurrentSnapshotJson(String productCode) {
        return snapshots.values().stream()
                .filter(snapshot -> snapshot.current() && snapshot.productCode().equalsIgnoreCase(productCode))
                .map(SnapshotRow::snapshotJson)
                .findFirst();
    }

    /**
     * 查询指定版本快照 JSON。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     * @return 快照 JSON
     */
    @Override
    public synchronized Optional<String> findSnapshotJson(String productCode, String versionNo) {
        return Optional.ofNullable(snapshots.get(snapshotKey(productCode, versionNo)))
                .map(SnapshotRow::snapshotJson);
    }

    /**
     * 刷新当前产品缓存。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     */
    @Override
    public void refreshCurrentProduct(String productCode, String versionNo) {
        // 内存实现无需刷新外部缓存。
    }

    /**
     * 删除当前产品缓存。
     *
     * @param productCode 产品编码
     */
    @Override
    public void evictCurrentProduct(String productCode) {
        // 内存实现无需删除外部缓存。
    }

    /**
     * 发布产品激活事件。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     */
    @Override
    public void publishProductActivated(String productCode, String versionNo) {
        // 内存实现无需发送外部事件。
    }

    /**
     * 发布产品下架事件。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     */
    @Override
    public void publishProductSuspended(String productCode, String versionNo) {
        // 内存实现无需发送外部事件。
    }

    /**
     * 复制版本关联配置。
     *
     * @param sourceVersionId 来源版本 ID
     * @param targetVersionId 目标版本 ID
     */
    private void copyVersionConfiguration(long sourceVersionId, long targetVersionId) {
        Map<Long, Long> riskIdMapping = new LinkedHashMap<>();
        List<RiskRow> sourceRisks = risks.values().stream()
                .filter(risk -> risk.versionId() == sourceVersionId)
                .toList();
        sourceRisks.forEach(risk -> {
            long newRiskId = riskIdSequence.incrementAndGet();
            riskIdMapping.put(risk.id(), newRiskId);
            risks.put(newRiskId, risk.copyTo(newRiskId, targetVersionId));
        });

        List<LiabilityRow> sourceLiabilities = liabilities.values().stream()
                .filter(liability -> liability.versionId() == sourceVersionId)
                .toList();
        sourceLiabilities.forEach(liability -> {
            Long newRiskId = riskIdMapping.get(liability.riskId());
            if (newRiskId != null) {
                long newLiabilityId = liabilityIdSequence.incrementAndGet();
                liabilities.put(newLiabilityId, liability.copyTo(newLiabilityId, targetVersionId, newRiskId));
            }
        });

        List<ClauseRow> sourceClauses = clauses.values().stream()
                .filter(clause -> clause.versionId() == sourceVersionId)
                .toList();
        sourceClauses.forEach(clause -> {
            long newClauseId = clauseIdSequence.incrementAndGet();
            clauses.put(newClauseId, clause.copyTo(newClauseId, targetVersionId));
        });
    }

    /**
     * 校验创建产品命令。
     *
     * @param command 创建命令
     */
    private static void validateCreateCommand(CreateProductCommand command) {
        if (command == null || isBlank(command.productCode()) || isBlank(command.productName()) || isBlank(command.shortName())) {
            throw new BusinessException("PRODUCT_REQUIRED_FIELD_MISSING", "产品编码、名称和简称不能为空");
        }
        if (command.productType() == null || command.productNature() == null) {
            throw new BusinessException("PRODUCT_TYPE_REQUIRED", "产品类型和产品属性不能为空");
        }
        if (isBlank(command.insuranceCompany()) || isBlank(command.salesChannels())) {
            throw new BusinessException("PRODUCT_CHANNEL_REQUIRED", "保险公司和销售渠道不能为空");
        }
    }

    /**
     * 校验编辑产品命令。
     *
     * @param command 编辑命令
     */
    private static void validateUpdateCommand(UpdateProductCommand command) {
        if (command == null || isBlank(command.productName()) || isBlank(command.shortName())) {
            throw new BusinessException("PRODUCT_REQUIRED_FIELD_MISSING", "产品名称和简称不能为空");
        }
        if (command.productType() == null || command.productNature() == null) {
            throw new BusinessException("PRODUCT_TYPE_REQUIRED", "产品类型和产品属性不能为空");
        }
        if (isBlank(command.insuranceCompany()) || isBlank(command.salesChannels())) {
            throw new BusinessException("PRODUCT_CHANNEL_REQUIRED", "保险公司和销售渠道不能为空");
        }
    }

    /**
     * 校验险种保存命令。
     *
     * @param command 险种命令
     */
    private static void validateRiskCommand(SaveRiskCommand command) {
        if (command == null || isBlank(command.riskCode()) || isBlank(command.riskName()) || isBlank(command.riskType())) {
            throw new BusinessException("RISK_REQUIRED_FIELD_MISSING", "险种编码、名称和类型不能为空");
        }
    }

    /**
     * 校验责任保存命令。
     *
     * @param command 责任命令
     */
    private static void validateLiabilityCommand(SaveLiabilityCommand command) {
        if (command == null || isBlank(command.liabilityCode()) || isBlank(command.liabilityName())) {
            throw new BusinessException("LIABILITY_REQUIRED_FIELD_MISSING", "责任编码和名称不能为空");
        }
    }

    /**
     * 校验条款引用保存命令。
     *
     * @param command 条款引用命令
     */
    private static void validateClauseCommand(SaveClauseCommand command) {
        if (command == null || isBlank(command.documentType()) || isBlank(command.documentId())
                || isBlank(command.documentVersion())) {
            throw new BusinessException("CLAUSE_REQUIRED_FIELD_MISSING", "条款类型、文件 ID 和版本不能为空");
        }
    }

    /**
     * 校验当前产品和版本是否允许草稿编辑。
     *
     * @param product 产品行
     * @param version 版本行
     */
    private static void assertDraftEditable(ProductRow product, VersionRow version) {
        boolean editableProduct = product.status() == ProductStatus.DRAFT || product.status() == ProductStatus.REJECTED;
        if (!editableProduct || version.status() != ProductVersionStatus.DRAFT) {
            throw new BusinessException("PRODUCT_DRAFT_EDIT_DENIED", "仅草稿或驳回产品允许编辑");
        }
    }

    /**
     * 校验版本是否为草稿。
     *
     * @param version 版本行
     */
    private static void assertVersionDraft(VersionRow version) {
        if (version.status() != ProductVersionStatus.DRAFT) {
            throw new BusinessException("PRODUCT_VERSION_EDIT_DENIED", "仅草稿版本允许维护配置");
        }
    }

    /**
     * 标记版本最近更新时间。
     *
     * @param versionId 版本 ID
     * @param operator 操作人
     */
    private void touchVersion(long versionId, String operator) {
        VersionRow version = requireVersion(versionId);
        ProductRow product = requireProduct(version.productId());
        LocalDateTime now = LocalDateTime.now();
        versions.put(versionId, version.withUpdated(now, operator));
        products.put(product.id(), product.withStatus(product.status(), now, operator));
    }

    /**
     * 查询并校验版本存在。
     *
     * @param versionId 版本 ID
     * @return 版本行
     */
    private VersionRow requireVersion(long versionId) {
        VersionRow version = versions.get(versionId);
        if (version == null) {
            throw new BusinessException("PRODUCT_VERSION_NOT_FOUND", "产品版本不存在");
        }
        return version;
    }

    /**
     * 查询并校验产品存在。
     *
     * @param productId 产品 ID
     * @return 产品行
     */
    private ProductRow requireProduct(long productId) {
        ProductRow product = products.get(productId);
        if (product == null || product.deleted()) {
            throw new BusinessException("PRODUCT_NOT_FOUND", "产品不存在");
        }
        return product;
    }

    /**
     * 查询并校验险种存在。
     *
     * @param riskId 险种 ID
     * @return 险种行
     */
    private RiskRow requireRisk(long riskId) {
        RiskRow risk = risks.get(riskId);
        if (risk == null) {
            throw new BusinessException("RISK_NOT_FOUND", "险种不存在");
        }
        return risk;
    }

    /**
     * 查询并校验责任存在。
     *
     * @param liabilityId 责任 ID
     * @return 责任行
     */
    private LiabilityRow requireLiability(long liabilityId) {
        LiabilityRow liability = liabilities.get(liabilityId);
        if (liability == null) {
            throw new BusinessException("LIABILITY_NOT_FOUND", "责任不存在");
        }
        return liability;
    }

    /**
     * 查询并校验条款引用存在。
     *
     * @param clauseId 条款引用 ID
     * @return 条款引用行
     */
    private ClauseRow requireClause(long clauseId) {
        ClauseRow clause = clauses.get(clauseId);
        if (clause == null) {
            throw new BusinessException("CLAUSE_NOT_FOUND", "条款引用不存在");
        }
        return clause;
    }

    /**
     * 转换产品列表项。
     *
     * @param product 产品行
     * @param version 版本行
     * @return 列表项
     */
    private static ProductListItem toListItem(ProductRow product, VersionRow version) {
        return new ProductListItem(
                product.id(),
                product.productCode(),
                product.productName(),
                product.productType(),
                product.productNature(),
                version.id(),
                version.versionNo(),
                product.status(),
                product.updatedAt(),
                product.updatedBy()
        );
    }

    /**
     * 转换产品详情。
     *
     * @param product 产品行
     * @param version 版本行
     * @return 产品详情
     */
    private static ProductDetail toDetail(ProductRow product, VersionRow version) {
        return new ProductDetail(
                product.id(),
                product.productCode(),
                product.productName(),
                product.shortName(),
                product.productType(),
                product.productNature(),
                product.insuranceCompany(),
                product.salesChannels(),
                product.status(),
                version.id(),
                version.versionNo(),
                version.status(),
                product.updatedAt(),
                product.updatedBy()
        );
    }

    /**
     * 转换险种详情。
     *
     * @param risk 险种行
     * @return 险种详情
     */
    private static RiskItemDetail toRiskDetail(RiskRow risk) {
        return new RiskItemDetail(risk.id(), risk.versionId(), risk.riskCode(), risk.riskName(), risk.riskType(),
                risk.coverageAmountType(), risk.premiumCalcType(), risk.sortNo());
    }

    /**
     * 转换责任详情。
     *
     * @param liability 责任行
     * @return 责任详情
     */
    private static LiabilityDetail toLiabilityDetail(LiabilityRow liability) {
        return new LiabilityDetail(liability.id(), liability.versionId(), liability.riskId(), liability.liabilityCode(),
                liability.liabilityName(), liability.liabilityType(), liability.claimType(), liability.amountRule(),
                liability.waitingDays(), liability.exemptionDesc(), liability.paymentCondition());
    }

    /**
     * 转换条款引用详情。
     *
     * @param clause 条款引用行
     * @return 条款引用详情
     */
    private static ClauseDocumentRefDetail toClauseDetail(ClauseRow clause) {
        return new ClauseDocumentRefDetail(clause.id(), clause.versionId(), clause.documentType(),
                clause.documentId(), clause.documentVersion(), clause.previewUrl());
    }

    /**
     * 生成下一版本号。
     *
     * @param versionNo 当前版本号
     * @return 下一版本号
     */
    private static String nextVersionNo(String versionNo) {
        if (versionNo != null && versionNo.startsWith("V")) {
            try {
                int major = Integer.parseInt(versionNo.substring(1).split("\\.")[0]);
                return "V%s.0".formatted(major + 1);
            } catch (NumberFormatException ignored) {
                return "V2.0";
            }
        }
        return "V2.0";
    }

    /**
     * 判断文本是否包含查询关键字。
     *
     * @param value 目标值
     * @param keyword 查询关键字
     * @return 是否匹配
     */
    private static boolean contains(String value, String keyword) {
        return isBlank(keyword) || value.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * 判断字符串是否为空。
     *
     * @param value 字符串
     * @return 是否为空
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 生成快照键。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     * @return 快照键
     */
    private static String snapshotKey(String productCode, String versionNo) {
        return productCode + ":" + versionNo;
    }

    /**
     * 转义 JSON 字符串值。
     *
     * @param value 原始值
     * @return 转义后值
     */
    private static String escape(String value) {
        return Objects.toString(value, "").replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 产品主档行。
     */
    private record ProductRow(
            long id,
            String productCode,
            String productName,
            String shortName,
            ProductType productType,
            ProductNature productNature,
            String insuranceCompany,
            String salesChannels,
            ProductStatus status,
            long currentVersionId,
            boolean deleted,
            LocalDateTime updatedAt,
            String updatedBy
    ) {
        /**
         * 返回更新状态后的产品行。
         *
         * @param targetStatus 目标状态
         * @param now 更新时间
         * @param operator 操作人
         * @return 产品行
         */
        ProductRow withStatus(ProductStatus targetStatus, LocalDateTime now, String operator) {
            return new ProductRow(id, productCode, productName, shortName, productType, productNature,
                    insuranceCompany, salesChannels, targetStatus, currentVersionId, deleted, now, operator);
        }

        /**
         * 返回更新基础信息后的产品行。
         *
         * @param targetProductName 产品名称
         * @param targetShortName 产品简称
         * @param targetProductType 产品类型
         * @param targetProductNature 产品属性
         * @param targetInsuranceCompany 保险公司
         * @param targetSalesChannels 销售渠道
         * @param targetStatus 目标状态
         * @param now 更新时间
         * @param operator 操作人
         * @return 产品行
         */
        ProductRow withBasicInfo(String targetProductName, String targetShortName, ProductType targetProductType,
                                 ProductNature targetProductNature, String targetInsuranceCompany,
                                 String targetSalesChannels, ProductStatus targetStatus,
                                 LocalDateTime now, String operator) {
            return new ProductRow(id, productCode, targetProductName, targetShortName, targetProductType,
                    targetProductNature, targetInsuranceCompany, targetSalesChannels, targetStatus,
                    currentVersionId, deleted, now, operator);
        }

        /**
         * 返回切换当前版本后的产品行。
         *
         * @param versionId 当前版本 ID
         * @param targetStatus 目标状态
         * @param now 更新时间
         * @param operator 操作人
         * @return 产品行
         */
        ProductRow withCurrentVersion(long versionId, ProductStatus targetStatus, LocalDateTime now, String operator) {
            return new ProductRow(id, productCode, productName, shortName, productType, productNature,
                    insuranceCompany, salesChannels, targetStatus, versionId, deleted, now, operator);
        }
    }

    /**
     * 产品版本行。
     */
    private record VersionRow(
            long id,
            long productId,
            String productCode,
            String versionNo,
            ProductVersionStatus status,
            boolean hasPlan,
            boolean hasPublishedRate,
            boolean hasFieldTemplate,
            boolean hasValidQuestionnaire,
            boolean hasDividendConfig,
            boolean hasValidSupplierRelations,
            LocalDateTime updatedAt,
            String updatedBy
    ) {
        /**
         * 返回更新状态后的版本行。
         *
         * @param targetStatus 目标状态
         * @param now 更新时间
         * @param operator 操作人
         * @return 版本行
         */
        VersionRow withStatus(ProductVersionStatus targetStatus, LocalDateTime now, String operator) {
            return new VersionRow(id, productId, productCode, versionNo, targetStatus, hasPlan,
                    hasPublishedRate, hasFieldTemplate, hasValidQuestionnaire, hasDividendConfig,
                    hasValidSupplierRelations, now, operator);
        }

        /**
         * 返回更新时间后的版本行。
         *
         * @param now 更新时间
         * @param operator 操作人
         * @return 版本行
         */
        VersionRow withUpdated(LocalDateTime now, String operator) {
            return new VersionRow(id, productId, productCode, versionNo, status, hasPlan,
                    hasPublishedRate, hasFieldTemplate, hasValidQuestionnaire, hasDividendConfig,
                    hasValidSupplierRelations, now, operator);
        }

        /**
         * 返回更新分红配置要求后的版本行。
         *
         * @param productNature 产品属性
         * @param now 更新时间
         * @param operator 操作人
         * @return 版本行
         */
        VersionRow withDividendRequired(ProductNature productNature, LocalDateTime now, String operator) {
            return new VersionRow(id, productId, productCode, versionNo, status, hasPlan,
                    hasPublishedRate, hasFieldTemplate, hasValidQuestionnaire,
                    productNature != ProductNature.DIVIDEND_LIFE || hasDividendConfig,
                    hasValidSupplierRelations, now, operator);
        }
    }

    /**
     * 险种行。
     */
    private record RiskRow(long id, long versionId, String riskCode, String riskName, String riskType,
                           String coverageAmountType, String premiumCalcType, int sortNo) {
        /**
         * 复制到目标版本。
         *
         * @param targetId 目标险种 ID
         * @param targetVersionId 目标版本 ID
         * @return 险种行
         */
        RiskRow copyTo(long targetId, long targetVersionId) {
            return new RiskRow(targetId, targetVersionId, riskCode, riskName, riskType,
                    coverageAmountType, premiumCalcType, sortNo);
        }
    }

    /**
     * 责任行。
     */
    private record LiabilityRow(long id, long versionId, long riskId, String liabilityCode, String liabilityName,
                                String liabilityType, String claimType, String amountRule, Integer waitingDays,
                                String exemptionDesc, String paymentCondition) {
        /**
         * 复制到目标版本和险种。
         *
         * @param targetId 目标责任 ID
         * @param targetVersionId 目标版本 ID
         * @param targetRiskId 目标险种 ID
         * @return 责任行
         */
        LiabilityRow copyTo(long targetId, long targetVersionId, long targetRiskId) {
            return new LiabilityRow(targetId, targetVersionId, targetRiskId, liabilityCode, liabilityName,
                    liabilityType, claimType, amountRule, waitingDays, exemptionDesc, paymentCondition);
        }
    }

    /**
     * 条款引用行。
     */
    private record ClauseRow(long id, long versionId, String documentType, String documentId,
                             String documentVersion, String previewUrl) {
        /**
         * 复制到目标版本。
         *
         * @param targetId 目标条款引用 ID
         * @param targetVersionId 目标版本 ID
         * @return 条款引用行
         */
        ClauseRow copyTo(long targetId, long targetVersionId) {
            return new ClauseRow(targetId, targetVersionId, documentType, documentId, documentVersion, previewUrl);
        }
    }

    /**
     * 发布快照行。
     */
    private record SnapshotRow(String productCode, String versionNo, String snapshotJson, boolean current) {
        /**
         * 返回更新当前标识后的快照行。
         *
         * @param targetCurrent 是否当前快照
         * @return 快照行
         */
        SnapshotRow withCurrent(boolean targetCurrent) {
            return new SnapshotRow(productCode, versionNo, snapshotJson, targetCurrent);
        }
    }
}
