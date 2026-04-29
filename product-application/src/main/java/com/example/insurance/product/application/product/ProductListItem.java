package com.example.insurance.product.application.product;

import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductStatus;
import com.example.insurance.product.domain.product.ProductType;

import java.time.LocalDateTime;

/**
 * 产品列表项。
 *
 * @param id 产品 ID
 * @param productCode 产品编码
 * @param productName 产品全称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param versionId 当前版本 ID
 * @param versionNo 当前版本号
 * @param status 产品状态
 * @param updatedAt 最近更新时间
 * @param updatedBy 最近操作人
 */
public record ProductListItem(
        long id,
        String productCode,
        String productName,
        ProductType productType,
        ProductNature productNature,
        long versionId,
        String versionNo,
        ProductStatus status,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
