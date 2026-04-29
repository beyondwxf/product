package com.example.insurance.product.api.product;

import com.example.insurance.product.application.product.ProductDetail;
import com.example.insurance.product.application.product.ProductListItem;

/**
 * 产品接口模型转换器。
 */
final class ProductMapper {
    /**
     * 工具类禁止实例化。
     */
    private ProductMapper() {
    }

    /**
     * 转换列表响应。
     *
     * @param item 产品列表项
     * @return 产品列表响应
     */
    static ProductListItemResponse toListResponse(ProductListItem item) {
        return new ProductListItemResponse(
                item.id(),
                item.productCode(),
                item.productName(),
                item.productType().code(),
                item.productNature().code(),
                item.versionId(),
                item.versionNo(),
                item.status().code(),
                item.updatedAt(),
                item.updatedBy()
        );
    }

    /**
     * 转换详情响应。
     *
     * @param detail 产品详情
     * @return 产品详情响应
     */
    static ProductDetailResponse toDetailResponse(ProductDetail detail) {
        return new ProductDetailResponse(
                detail.id(),
                detail.productCode(),
                detail.productName(),
                detail.shortName(),
                detail.productType().code(),
                detail.productNature().code(),
                detail.insuranceCompany(),
                detail.salesChannels(),
                detail.status().code(),
                detail.versionId(),
                detail.versionNo(),
                detail.versionStatus().code(),
                detail.updatedAt(),
                detail.updatedBy()
        );
    }
}
