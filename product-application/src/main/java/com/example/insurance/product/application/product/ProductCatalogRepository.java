package com.example.insurance.product.application.product;

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
}
