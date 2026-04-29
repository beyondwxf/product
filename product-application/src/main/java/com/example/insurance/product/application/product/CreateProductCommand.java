package com.example.insurance.product.application.product;

import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductType;

/**
 * 创建产品草稿命令，承载后台录入的产品主数据。
 *
 * @param productCode 产品编码
 * @param productName 产品全称
 * @param shortName 产品简称
 * @param productType 产品类型
 * @param productNature 产品属性
 * @param insuranceCompany 保险公司
 * @param salesChannels 销售渠道，多个渠道使用逗号分隔
 * @param operator 操作人
 */
public record CreateProductCommand(
        String productCode,
        String productName,
        String shortName,
        ProductType productType,
        ProductNature productNature,
        String insuranceCompany,
        String salesChannels,
        String operator
) {
}
