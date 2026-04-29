package com.example.insurance.product.application.publish;

/**
 * 产品发布结果。
 *
 * @param productCode 产品编码
 * @param versionNo 产品版本号
 */
public record PublishResult(String productCode, String versionNo) {
}
