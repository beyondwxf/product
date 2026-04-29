package com.example.insurance.product.api.product;

import com.example.insurance.product.application.product.ClauseDocumentRefDetail;
import com.example.insurance.product.application.product.LiabilityDetail;
import com.example.insurance.product.application.product.ProductDetail;
import com.example.insurance.product.application.product.ProductListItem;
import com.example.insurance.product.application.product.RiskItemDetail;

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

    /**
     * 转换险种响应。
     *
     * @param detail 险种详情
     * @return 险种响应
     */
    static RiskItemResponse toRiskResponse(RiskItemDetail detail) {
        return new RiskItemResponse(
                detail.id(),
                detail.versionId(),
                detail.riskCode(),
                detail.riskName(),
                detail.riskType(),
                detail.coverageAmountType(),
                detail.premiumCalcType(),
                detail.sortNo()
        );
    }

    /**
     * 转换责任响应。
     *
     * @param detail 责任详情
     * @return 责任响应
     */
    static LiabilityResponse toLiabilityResponse(LiabilityDetail detail) {
        return new LiabilityResponse(
                detail.id(),
                detail.versionId(),
                detail.riskId(),
                detail.liabilityCode(),
                detail.liabilityName(),
                detail.liabilityType(),
                detail.claimType(),
                detail.amountRule(),
                detail.waitingDays(),
                detail.exemptionDesc(),
                detail.paymentCondition()
        );
    }

    /**
     * 转换条款引用响应。
     *
     * @param detail 条款引用详情
     * @return 条款引用响应
     */
    static ClauseDocumentRefResponse toClauseResponse(ClauseDocumentRefDetail detail) {
        return new ClauseDocumentRefResponse(
                detail.id(),
                detail.versionId(),
                detail.documentType(),
                detail.documentId(),
                detail.documentVersion(),
                detail.previewUrl()
        );
    }
}
