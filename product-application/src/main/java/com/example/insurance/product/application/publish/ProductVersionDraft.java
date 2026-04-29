package com.example.insurance.product.application.publish;

import com.example.insurance.product.domain.product.ProductVersionStatus;

/**
 * 产品版本发布草稿摘要。
 *
 * @param id 产品版本 ID
 * @param productCode 产品编码
 * @param versionNo 产品版本号
 * @param status 产品版本当前状态
 */
public record ProductVersionDraft(long id, String productCode, String versionNo, ProductVersionStatus status) {
}
