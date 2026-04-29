package com.example.insurance.product.api.product;

import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建产品请求。
 *
 * @param productCode 产品编码
 * @param productName 产品全称
 * @param shortName 产品简称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param insuranceCompany 保险公司
 * @param salesChannels 销售渠道
 */
public record CreateProductRequest(
        @NotBlank(message = "不能为空") String productCode,
        @NotBlank(message = "不能为空") String productName,
        @NotBlank(message = "不能为空") String shortName,
        @NotNull(message = "不能为空") ProductType productType,
        @NotNull(message = "不能为空") ProductNature productNature,
        @NotBlank(message = "不能为空") String insuranceCompany,
        @NotBlank(message = "不能为空") String salesChannels
) {
}
