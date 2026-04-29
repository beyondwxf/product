package com.example.insurance.product.infrastructure.repository;

import com.example.insurance.product.application.open.PublishSnapshotQueryRepository;
import com.example.insurance.product.application.product.CreateProductCommand;
import com.example.insurance.product.application.product.PageResult;
import com.example.insurance.product.application.product.ProductCatalogRepository;
import com.example.insurance.product.application.product.ProductDetail;
import com.example.insurance.product.application.product.ProductListItem;
import com.example.insurance.product.application.product.ProductListQuery;
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
import org.springframework.stereotype.Repository;

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
@Repository
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
                true,
                true,
                true,
                true,
                command.productNature() == ProductNature.TRADITIONAL_LIFE,
                true,
                now,
                command.operator()
        );
        products.put(productId, product);
        versions.put(versionId, version);
        return toDetail(product, version);
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
        if (product == null) {
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
        boolean activeConflict = versions.values().stream()
                .anyMatch(item -> item.id() != versionId
                        && item.productCode().equals(version.productCode())
                        && item.status() == ProductVersionStatus.ACTIVE);
        return new ProductPublishCandidate(
                version.productCode(),
                version.versionNo(),
                product.productNature(),
                version.hasMainRisk(),
                version.hasCoverage(),
                version.hasPlan(),
                version.hasPublishedRate(),
                version.hasRequiredClauses(),
                version.hasFieldTemplate(),
                version.hasValidQuestionnaire(),
                version.hasValidRuleSet(),
                version.hasDividendConfig(),
                version.hasValidSupplierRelations(),
                activeConflict
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
     * 校验创建产品命令。
     *
     * @param command 创建命令
     */
    private static void validateCreateCommand(CreateProductCommand command) {
        if (isBlank(command.productCode()) || isBlank(command.productName()) || isBlank(command.shortName())) {
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
        if (product == null) {
            throw new BusinessException("PRODUCT_NOT_FOUND", "产品不存在");
        }
        return product;
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
                    insuranceCompany, salesChannels, targetStatus, currentVersionId, now, operator);
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
            return new VersionRow(id, productId, productCode, versionNo, targetStatus, hasMainRisk,
                    hasCoverage, hasPlan, hasPublishedRate, hasRequiredClauses, hasFieldTemplate,
                    hasValidQuestionnaire, hasValidRuleSet, hasDividendConfig, hasValidSupplierRelations,
                    now, operator);
        }
    }

    /**
     * 发布快照行。
     */
    private record SnapshotRow(String productCode, String versionNo, String snapshotJson, boolean current) {
    }
}
