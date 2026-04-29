package com.example.insurance.product.api.product;

/**
 * 产品列表项响应模型。
 *
 * @param productCode 产品编码
 * @param productName 产品名称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param versionNo 当前版本号
 * @param status 产品状态
 */
public record ProductListItemResponse(
        String productCode,
        String productName,
        String productType,
        String productNature,
        String versionNo,
        String status
) {
}
