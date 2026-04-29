package com.example.insurance.product.application.product;

import java.util.List;
import java.util.Optional;

/**
 * 产品目录仓储端口，表达后台产品主数据用例所需的业务语义。
 */
public interface ProductCatalogRepository {
    /**
     * 创建产品主档和初始草稿版本。
     *
     * @param command 创建产品命令
     * @return 创建后的产品详情
     */
    ProductDetail create(CreateProductCommand command);

    /**
     * 编辑草稿产品基础信息。
     *
     * @param productId 产品 ID
     * @param command 编辑产品命令
     * @return 产品详情
     */
    ProductDetail updateDraft(long productId, UpdateProductCommand command);

    /**
     * 基于当前产品版本创建新草稿版本。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    ProductDetail createNewVersion(long productId, String operator);

    /**
     * 删除草稿产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     */
    void deleteDraft(long productId, String operator);

    /**
     * 分页查询产品列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ProductListItem> list(ProductListQuery query);

    /**
     * 按产品 ID 查询详情。
     *
     * @param productId 产品 ID
     * @return 产品详情
     */
    Optional<ProductDetail> findDetail(long productId);

    /**
     * 提交产品版本审核。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    ProductDetail submitReview(long versionId, String operator);

    /**
     * 审核通过产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    ProductDetail approveReview(long versionId, String operator);

    /**
     * 驳回产品版本。
     *
     * @param versionId 产品版本 ID
     * @param reason 驳回原因
     * @param operator 操作人
     * @return 产品详情
     */
    ProductDetail rejectReview(long versionId, String reason, String operator);

    /**
     * 下架产品。
     *
     * @param productId 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    ProductDetail suspend(long productId, String operator);

    /**
     * 保存险种配置。
     *
     * @param versionId 产品版本 ID
     * @param command 险种命令
     * @return 险种详情
     */
    RiskItemDetail createRisk(long versionId, SaveRiskCommand command);

    /**
     * 查询版本险种列表。
     *
     * @param versionId 产品版本 ID
     * @return 险种列表
     */
    List<RiskItemDetail> listRisks(long versionId);

    /**
     * 更新险种配置。
     *
     * @param riskId 险种 ID
     * @param command 险种命令
     * @return 险种详情
     */
    RiskItemDetail updateRisk(long riskId, SaveRiskCommand command);

    /**
     * 删除草稿版本险种。
     *
     * @param riskId 险种 ID
     * @param operator 操作人
     */
    void deleteRisk(long riskId, String operator);

    /**
     * 保存责任配置。
     *
     * @param riskId 险种 ID
     * @param command 责任命令
     * @return 责任详情
     */
    LiabilityDetail createLiability(long riskId, SaveLiabilityCommand command);

    /**
     * 查询险种责任列表。
     *
     * @param riskId 险种 ID
     * @return 责任列表
     */
    List<LiabilityDetail> listLiabilities(long riskId);

    /**
     * 更新责任配置。
     *
     * @param liabilityId 责任 ID
     * @param command 责任命令
     * @return 责任详情
     */
    LiabilityDetail updateLiability(long liabilityId, SaveLiabilityCommand command);

    /**
     * 删除草稿版本责任。
     *
     * @param liabilityId 责任 ID
     * @param operator 操作人
     */
    void deleteLiability(long liabilityId, String operator);

    /**
     * 保存条款引用。
     *
     * @param versionId 产品版本 ID
     * @param command 条款引用命令
     * @return 条款引用详情
     */
    ClauseDocumentRefDetail createClause(long versionId, SaveClauseCommand command);

    /**
     * 查询版本条款引用。
     *
     * @param versionId 产品版本 ID
     * @return 条款引用列表
     */
    List<ClauseDocumentRefDetail> listClauses(long versionId);

    /**
     * 删除条款引用。
     *
     * @param clauseId 条款引用 ID
     * @param operator 操作人
     */
    void deleteClause(long clauseId, String operator);
}
