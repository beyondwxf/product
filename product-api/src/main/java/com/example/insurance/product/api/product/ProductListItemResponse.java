package com.example.insurance.product.api.product;

import java.time.LocalDateTime;

/**
 * 产品列表项响应模型。
 *
 * @param id 产品 ID
 * @param productCode 产品编码
 * @param productName 产品名称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param versionId 当前版本 ID
 * @param versionNo 当前版本号
 * @param status 产品状态
 * @param updatedAt 最近更新时间
 * @param updatedBy 最近操作人
 */
public record ProductListItemResponse(
        long id,
        String productCode,
        String productName,
        String productType,
        String productNature,
        long versionId,
        String versionNo,
        String status,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
