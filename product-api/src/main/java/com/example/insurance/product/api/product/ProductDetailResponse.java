package com.example.insurance.product.api.product;

import java.time.LocalDateTime;

/**
 * 产品详情响应。
 *
 * @param id 产品 ID
 * @param productCode 产品编码
 * @param productName 产品全称
 * @param shortName 产品简称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param insuranceCompany 保险公司
 * @param salesChannels 销售渠道
 * @param status 产品状态
 * @param versionId 当前版本 ID
 * @param versionNo 当前版本号
 * @param versionStatus 当前版本状态
 * @param updatedAt 最近更新时间
 * @param updatedBy 最近操作人
 */
public record ProductDetailResponse(
        long id,
        String productCode,
        String productName,
        String shortName,
        String productType,
        String productNature,
        String insuranceCompany,
        String salesChannels,
        String status,
        long versionId,
        String versionNo,
        String versionStatus,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
