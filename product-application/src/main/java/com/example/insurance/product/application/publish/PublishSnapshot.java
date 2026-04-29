package com.example.insurance.product.application.publish;

/**
 * 发布快照摘要。
 *
 * @param id 发布快照 ID
 * @param productCode 产品编码
 * @param versionNo 产品版本号
 */
public record PublishSnapshot(long id, String productCode, String versionNo) {
}
