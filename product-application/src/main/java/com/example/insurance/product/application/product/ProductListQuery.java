package com.example.insurance.product.application.product;

import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductStatus;
import com.example.insurance.product.domain.product.ProductType;

/**
 * 产品列表查询条件。
 *
 * @param productCode 产品编码
 * @param productName 产品名称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param status 产品状态
 * @param pageNo 页码
 * @param pageSize 每页条数
 */
public record ProductListQuery(
        String productCode,
        String productName,
        ProductType productType,
        ProductNature productNature,
        ProductStatus status,
        int pageNo,
        int pageSize
) {
}
