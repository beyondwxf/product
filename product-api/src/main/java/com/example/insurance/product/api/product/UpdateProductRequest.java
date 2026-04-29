package com.example.insurance.product.api.product;

import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 编辑产品草稿请求。
 *
 * @param productName 产品全称
 * @param shortName 产品简称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param insuranceCompany 保险公司
 * @param salesChannels 销售渠道
 */
public record UpdateProductRequest(
        @NotBlank String productName,
        @NotBlank String shortName,
        @NotNull ProductType productType,
        @NotNull ProductNature productNature,
        @NotBlank String insuranceCompany,
        @NotBlank String salesChannels
) {
}
